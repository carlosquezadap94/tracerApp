package io.bluetrace.opentrace.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import io.bluetrace.opentrace.BuildConfig
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.bluetooth.BLEAdvertiser
import io.bluetrace.opentrace.bluetooth.gatt.ACTION_RECEIVED_STATUS
import io.bluetrace.opentrace.bluetooth.gatt.ACTION_RECEIVED_STREETPASS
import io.bluetrace.opentrace.listeners.BluetoothStatusListener
import io.bluetrace.opentrace.notifications.NotificationTemplates
import io.bluetrace.opentrace.persistence.status.StatusRecordStorage
import io.bluetrace.opentrace.streetpass.StreetPassScanner
import io.bluetrace.opentrace.streetpass.StreetPassServer
import io.bluetrace.opentrace.streetpass.StreetPassWorker
import io.bluetrace.opentrace.persistence.streetpass.StreetPassRecordStorage
import io.bluetrace.opentrace.services.broadcast.BluetoothStatusReceiver
import io.bluetrace.opentrace.services.broadcast.StatusReceiver
import io.bluetrace.opentrace.services.broadcast.StreetPassReceiver
import io.bluetrace.opentrace.services.enums.Command
import io.bluetrace.opentrace.services.enums.NotificationState
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

class BluetoothMonitoringService : Service(), CoroutineScope, BluetoothStatusListener {

    private var mNotificationManager: NotificationManager? = null

    private lateinit var serviceUUID: String

    private var streetPassServer: StreetPassServer? = null
    private var streetPassScanner: StreetPassScanner? = null
    private var advertiser: BLEAdvertiser? = null

    var worker: StreetPassWorker? = null

    private val streetPassReceiver = StreetPassReceiver()
    private val statusReceiver = StatusReceiver()
    private val bluetoothStatusReceiver = BluetoothStatusReceiver(this)

    private lateinit var streetPassRecordStorage: StreetPassRecordStorage
    private lateinit var statusRecordStorage: StatusRecordStorage

    private var job: Job = Job()

    private lateinit var functions: FirebaseFunctions

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var commandHandler: CommandHandler

    private lateinit var localBroadcastManager: LocalBroadcastManager

    private var notificationShown: NotificationState? = null

    override fun onCreate() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        setup()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        //check for permissions
        if (!hasLocationPermissions() || !isBluetoothEnabled()) {

            notifyLackingThings()
            return START_STICKY
        }

        //check for write permissions  - not required for now. SDLog maybe?
        //only required for debug builds - for now
        if (BuildConfig.DEBUG) {
            if (!hasWritePermissions()) {


                stopSelf()
                return START_STICKY
            }
        }

        intent?.let {
            val cmd = intent.getIntExtra(COMMAND_KEY, Command.INVALID.index)
            runService(Command.findByValue(cmd))

            return START_STICKY
        }

        if (intent == null) {

//            Utils.startBluetoothMonitoringService(applicationContext)
            commandHandler.startBluetoothMonitoringService()
        }

        // Tells the system to not try to recreate the service after it has been killed.
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun setup() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager



        commandHandler = CommandHandler(WeakReference(this))


        serviceUUID = BuildConfig.BLE_SSID

        worker = StreetPassWorker(this.applicationContext)

        unregisterReceivers()
        registerReceivers()

        streetPassRecordStorage =
            StreetPassRecordStorage(
                this.applicationContext
            )
        statusRecordStorage =
            StatusRecordStorage(this.applicationContext)

        setupNotifications()
        functions = FirebaseFunctions.getInstance(BuildConfig.FIREBASE_REGION)
    }

    fun teardown() {
        streetPassServer?.tearDown()
        streetPassServer = null

        streetPassScanner?.stopScan()
        streetPassScanner = null

        commandHandler.removeCallbacksAndMessages(null)

        Utils.cancelBMUpdateCheck(this.applicationContext)
        Utils.cancelNextScan(this.applicationContext)
        Utils.cancelNextAdvertise(this.applicationContext)
    }

    private fun setupNotifications() {

        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_SERVICE
            // Create the channel for the notification
            val mChannel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
            mChannel.enableLights(false)
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(0L)
            mChannel.setSound(null, null)
            mChannel.setShowBadge(false)

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager!!.createNotificationChannel(mChannel)
        }
    }

    private fun notifyLackingThings(override: Boolean = false) {
        if (notificationShown != NotificationState.LACKING_THINGS || override) {
            var notif =
                NotificationTemplates.lackingThingsNotification(this.applicationContext, CHANNEL_ID)
            startForeground(NOTIFICATION_ID, notif)
            notificationShown = NotificationState.LACKING_THINGS
        }
    }

    private fun notifyRunning(override: Boolean = false) {
        if (notificationShown != NotificationState.RUNNING || override) {
            var notif =
                NotificationTemplates.getRunningNotification(this.applicationContext, CHANNEL_ID)
            startForeground(NOTIFICATION_ID, notif)
            notificationShown = NotificationState.RUNNING
        }
    }

    private fun hasLocationPermissions(): Boolean {
        val perms = Utils.getRequiredPermissions()
        return EasyPermissions.hasPermissions(this.applicationContext, *perms)
    }

    private fun hasWritePermissions(): Boolean {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return EasyPermissions.hasPermissions(this.applicationContext, *perms)
    }



    private fun isBluetoothEnabled(): Boolean {
        var btOn = false
        val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        }

        bluetoothAdapter?.let {
            btOn = it.isEnabled
        }
        return btOn
    }


    fun runService(cmd: Command?) {

        var doWork = true

        //check for permissions
        if (!hasLocationPermissions() || !isBluetoothEnabled()) {

            notifyLackingThings()
            return
        }

        //check for write permissions  - not required for now. SDLog maybe?
        //only required for debug builds - for now
        if (BuildConfig.DEBUG) {
            if (!hasWritePermissions()) {


                stopSelf()
                return
            }
        }

        //show running foreground notification if its not showing that
        notifyRunning()

        when (cmd) {
            Command.ACTION_START -> {
                setupService()
                Utils.scheduleNextHealthCheck(this.applicationContext, healthCheckInterval)
                Utils.scheduleRepeatingPurge(this.applicationContext, purgeInterval)
                Utils.scheduleBMUpdateCheck(this.applicationContext, bmCheckInterval)
                actionStart()
            }

            Command.ACTION_SCAN -> {
                scheduleScan()

                if (doWork) {
                    actionScan()
                }
            }

            Command.ACTION_ADVERTISE -> {
                scheduleAdvertisement()
                if (doWork) {
                    actionAdvertise()
                }
            }

            Command.ACTION_UPDATE_BM -> {
                Utils.scheduleBMUpdateCheck(this.applicationContext, bmCheckInterval)
            }

            Command.ACTION_STOP -> {
                actionStop()
            }

            Command.ACTION_SELF_CHECK -> {
                Utils.scheduleNextHealthCheck(this.applicationContext, healthCheckInterval)
                if (doWork) {
                    actionHealthCheck()
                }
            }

            Command.ACTION_PURGE -> {
                actionPurge()
            }

            else ->{}
        }
    }

    private fun actionStop() {
        stopForeground(true)
        stopSelf()
    }

    private fun actionHealthCheck() {
        performHealthCheck()
        Utils.scheduleRepeatingPurge(this.applicationContext, purgeInterval)
    }

    private fun actionPurge() {
        performPurge()
    }

    private fun actionStart() {
        setupCycles()
    }

    fun calcPhaseShift(min: Long, max: Long): Long {
        return (min + (Math.random() * (max - min))).toLong()
    }

    private fun actionScan() {
        performScan()
    }

    private fun actionAdvertise() {
        setupAdvertiser()
        if (isBluetoothEnabled()) {
            advertiser?.startAdvertising(advertisingDuration)
        } else {
        }
    }

    private fun setupService() {
        streetPassServer =
            streetPassServer ?: StreetPassServer(this.applicationContext, serviceUUID)
        setupScanner()
        setupAdvertiser()
    }

    private fun setupScanner() {
        streetPassScanner = streetPassScanner ?: StreetPassScanner(
            this,
            serviceUUID,
            scanDuration
        )
    }

    private fun setupAdvertiser() {
        advertiser = advertiser ?: BLEAdvertiser(serviceUUID)
    }

    private fun setupCycles() {
        setupScanCycles()
        setupAdvertisingCycles()
    }

    private fun setupScanCycles() {
        commandHandler.scheduleNextScan(0)
    }

    private fun setupAdvertisingCycles() {
        commandHandler.scheduleNextAdvertise(0)
    }

    private fun performScan() {
        setupScanner()
        startScan()
    }

    private fun scheduleScan() {
        if (!infiniteScanning) {
            commandHandler.scheduleNextScan(
                scanDuration + calcPhaseShift(
                    minScanInterval,
                    maxScanInterval
                )
            )
        }
    }

    private fun scheduleAdvertisement() {
        if (!infiniteAdvertising) {
            commandHandler.scheduleNextAdvertise(advertisingDuration + advertisingGap)
        }
    }

    private fun startScan() {

        if (isBluetoothEnabled()) {

            streetPassScanner?.let { scanner ->
                if (!scanner.isScanning()) {
                    scanner.startScan()
                } else {
                }
            }
        } else {
        }
    }

    private fun performHealthCheck() {

        if (!hasLocationPermissions() || !isBluetoothEnabled()) {
            notifyLackingThings(true)
            return
        }

        notifyRunning(true)

        //ensure our service is there
        setupService()

        if (!infiniteScanning) {
            if (!commandHandler.hasScanScheduled()) {
                commandHandler.scheduleNextScan(100)
            } else {
            }
        } else {
        }

        if (!infiniteAdvertising) {
            if (!commandHandler.hasAdvertiseScheduled()) {

                //setupAdvertisingCycles()
                commandHandler.scheduleNextAdvertise(100)
            } else {

            }
        } else {

        }
    }

    private fun performPurge() {
        val context = this
        launch {
            val before = System.currentTimeMillis() - purgeTTL


            streetPassRecordStorage.purgeOldRecords(before)
            statusRecordStorage.purgeOldRecords(before)
        }
    }

    private fun stopService() {
        teardown()
        unregisterReceivers()

        worker?.terminateConnections()
        worker?.unregisterReceivers()

        job.cancel()
    }

    private fun registerReceivers() {
        val recordAvailableFilter = IntentFilter(ACTION_RECEIVED_STREETPASS)
        localBroadcastManager.registerReceiver(streetPassReceiver, recordAvailableFilter)

        val statusReceivedFilter = IntentFilter(ACTION_RECEIVED_STATUS)
        localBroadcastManager.registerReceiver(statusReceiver, statusReceivedFilter)

        val bluetoothStatusReceivedFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStatusReceiver, bluetoothStatusReceivedFilter)

    }

    private fun unregisterReceivers() {
        try {
            localBroadcastManager.unregisterReceiver(streetPassReceiver)
        } catch (e: Throwable) {
        }

        try {
            localBroadcastManager.unregisterReceiver(statusReceiver)
        } catch (e: Throwable) {

        }

        try {
            unregisterReceiver(bluetoothStatusReceiver)
        } catch (e: Throwable) {

        }
    }

    companion object {

        private val TAG = "BTMService"

        private val NOTIFICATION_ID = BuildConfig.SERVICE_FOREGROUND_NOTIFICATION_ID
        private val CHANNEL_ID = BuildConfig.SERVICE_FOREGROUND_CHANNEL_ID
        val CHANNEL_SERVICE = BuildConfig.SERVICE_FOREGROUND_CHANNEL_NAME

        val PUSH_NOTIFICATION_ID = BuildConfig.PUSH_NOTIFICATION_ID

        val COMMAND_KEY = "${BuildConfig.APPLICATION_ID}_CMD"

        val PENDING_ACTIVITY = 5
        val PENDING_START = 6
        val PENDING_SCAN_REQ_CODE = 7
        val PENDING_ADVERTISE_REQ_CODE = 8
        val PENDING_HEALTH_CHECK_CODE = 9
        val PENDING_WIZARD_REQ_CODE = 10
        val PENDING_BM_UPDATE = 11
        val PENDING_PURGE_CODE = 12


        //should be more than advertising gap?
        val scanDuration: Long = BuildConfig.SCAN_DURATION
        val minScanInterval: Long = BuildConfig.MIN_SCAN_INTERVAL
        val maxScanInterval: Long = BuildConfig.MAX_SCAN_INTERVAL

        val advertisingDuration: Long = BuildConfig.ADVERTISING_DURATION
        val advertisingGap: Long = BuildConfig.ADVERTISING_INTERVAL
        val maxQueueTime: Long = BuildConfig.MAX_QUEUE_TIME
        val bmCheckInterval: Long = BuildConfig.BM_CHECK_INTERVAL
        val healthCheckInterval: Long = BuildConfig.HEALTH_CHECK_INTERVAL
        val purgeInterval: Long = BuildConfig.PURGE_INTERVAL
        val purgeTTL: Long = BuildConfig.PURGE_TTL
        val connectionTimeout: Long = BuildConfig.CONNECTION_TIMEOUT
        val blacklistDuration: Long = BuildConfig.BLACKLIST_DURATION
        val infiniteScanning = false
        val infiniteAdvertising = false
        val useBlacklist = true
    }

    override fun onTeardown() {
        teardown()
    }

    override fun onNotifyLackingThings() {
        notifyLackingThings()
    }
}
