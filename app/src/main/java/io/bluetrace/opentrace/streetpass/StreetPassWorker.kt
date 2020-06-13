package io.bluetrace.opentrace.streetpass

import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.bluetrace.opentrace.BuildConfig
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.bluetooth.gatt.ACTION_DEVICE_PROCESSED
import io.bluetrace.opentrace.bluetooth.gatt.CONNECTION_DATA
import io.bluetrace.opentrace.bluetooth.gatt.DEVICE_ADDRESS
import io.bluetrace.opentrace.protocol.BlueTrace
import io.bluetrace.opentrace.services.BluetoothMonitoringService
import io.bluetrace.opentrace.services.BluetoothMonitoringService.Companion.blacklistDuration
import io.bluetrace.opentrace.services.BluetoothMonitoringService.Companion.maxQueueTime
import io.bluetrace.opentrace.services.BluetoothMonitoringService.Companion.useBlacklist
import java.util.*
import java.util.concurrent.PriorityBlockingQueue

class StreetPassWorker(val context: Context) {

    private val workQueue: PriorityBlockingQueue<Work> = PriorityBlockingQueue(5, Collections.reverseOrder<Work>())
    private val blacklist: MutableList<BlacklistEntry> = Collections.synchronizedList(ArrayList())

    private val scannedDeviceReceiver = ScannedDeviceReceiver()
    private val blacklistReceiver = BlacklistReceiver()
    private val serviceUUID: UUID = UUID.fromString(BuildConfig.BLE_SSID)
    private val characteristicV2: UUID = UUID.fromString(BuildConfig.V2_CHARACTERISTIC_ID)

    private val TAG = "StreetPassWorker"

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private lateinit var timeoutHandler: Handler
    private lateinit var queueHandler: Handler
    private lateinit var blacklistHandler: Handler

    private var currentWork: Work? = null
    private var localBroadcastManager: LocalBroadcastManager =
        LocalBroadcastManager.getInstance(context)

    val onWorkTimeoutListener = object : Work.OnWorkTimeoutListener {
        override fun onWorkTimeout(work: Work) {

            if (!isCurrentlyWorkedOn(work.device.address)) {

            }



            //connection never formed - don't need to disconnect
            if (!work.checklist.connected.status) {

                if (work.device.address == currentWork?.device?.address) {
                    currentWork = null
                }

                try {
                    work.gatt?.close()
                } catch (e: Exception) {

                }

                finishWork(work)
            }
            //the connection is still there - might be stuck / work in progress
            else if (work.checklist.connected.status && !work.checklist.disconnected.status) {

                if (work.checklist.readCharacteristic.status || work.checklist.writeCharacteristic.status || work.checklist.skipped.status) {


                    try {
                        work.gatt?.disconnect()
                        //disconnect callback won't get invoked
                        if (work.gatt == null) {
                            currentWork = null
                            finishWork(work)
                        }
                    } catch (e: Throwable) {

                    }

                } else {


                    try {
                        work.gatt?.disconnect()
                        //disconnect callback won't get invoked
                        if (work.gatt == null) {
                            currentWork = null
                            finishWork(work)
                        }
                    } catch (e: Throwable) {

                    }
                }
            }

            //all other edge cases? - disconnected
            else {

            }
        }
    }

    init {
        prepare()
    }

    private fun prepare() {
        val deviceAvailableFilter = IntentFilter(ACTION_DEVICE_SCANNED)
        localBroadcastManager.registerReceiver(scannedDeviceReceiver, deviceAvailableFilter)

        val deviceProcessedFilter = IntentFilter(ACTION_DEVICE_PROCESSED)
        localBroadcastManager.registerReceiver(blacklistReceiver, deviceProcessedFilter)

        timeoutHandler = Handler()
        queueHandler = Handler()
        blacklistHandler = Handler()
    }

    fun isCurrentlyWorkedOn(address: String?): Boolean {
        return currentWork?.let {
            it.device.address == address
        } ?: false
    }

    fun addWork(work: Work): Boolean {
        //if it's our current work. ignore
        if (isCurrentlyWorkedOn(work.device.address)) {
            return false
        }

        //if its in blacklist - check for both mac address and manu data?
        //devices seem to cache manuData. needs further testing. temporarily disabling.
        if (useBlacklist) {
            if (
                blacklist.filter { it.uniqueIdentifier == work.device.address }.isNotEmpty()
//                || blacklist.filter { it.uniqueIdentifier == work.connectable.manuData }.isNotEmpty()
            ) {

                return false
            }
        }

        //if we haven't seen this device yet
        if (workQueue.filter { it.device.address == work.device.address }.isEmpty()) {
            workQueue.offer(work)
            queueHandler.postDelayed({

            }, maxQueueTime)

            return true
        }
        //this gadget is already in the queue, we can use the latest rssi and txpower? replace the entry
        else {



            var prevWork = workQueue.find { it.device.address == work.device.address }
            var removed = workQueue.remove(prevWork)
            var added = workQueue.offer(work)



            return false
        }
    }

    fun doWork() {
        //check the status of the current work item
        if (currentWork != null) {


            //if the job was finished or timed out but was not removed
            var timedout = System.currentTimeMillis() > currentWork?.timeout ?: 0
            if (currentWork?.finished == true || timedout) {


                //check if there is, for some reason, an existing connection
                if (currentWork != null) {
                    if (bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).contains(
                            currentWork?.device
                        )
                    ) {

                        currentWork?.gatt?.disconnect()
                    }
                } else {
                    doWork()
                }
            }

            return
        }

        if (workQueue.isEmpty()) {

            return
        }



        var workToDo: Work? = null
        val now = System.currentTimeMillis()

        while (workToDo == null && workQueue.isNotEmpty()) {
            workToDo = workQueue.poll()
            workToDo?.let { work ->
                if (now - work.timeStamp > maxQueueTime) {

                    workToDo = null
                }
            }
        }

        workToDo?.let { currentWorkOrder ->

            val device = currentWorkOrder.device

            if (useBlacklist) {
                if (blacklist.filter { it.uniqueIdentifier == device.address }.isNotEmpty()) {

                    doWork()
                    return
                }
            }

            val alreadyConnected = getConnectionStatus(device)

            if (alreadyConnected) {
                //this might mean that the other device is currently connected to this device's local gatt server
                //skip. we'll rely on the other party to do a write
                currentWorkOrder.checklist.skipped.status = true
                currentWorkOrder.checklist.skipped.timePerformed = System.currentTimeMillis()
                finishWork(currentWorkOrder)
            } else {

                currentWorkOrder.let {

                    val gattCallback = CentralGattCallback(it)

                    currentWork = it

                    try {
                        it.checklist.started.status = true
                        it.checklist.started.timePerformed = System.currentTimeMillis()

                        it.startWork(context, gattCallback)

                        var connecting = it.gatt?.connect() ?: false

                        if (!connecting) {

                            currentWork = null
                            doWork()
                            return

                        } else {

                        }

                        timeoutHandler.postDelayed(
                            it.timeoutRunnable,
                            BluetoothMonitoringService.connectionTimeout
                        )
                        it.timeout =
                            System.currentTimeMillis() + BluetoothMonitoringService.connectionTimeout


                    } catch (e: Throwable) {
                        currentWork = null
                        doWork()
                        return
                    }
                }
            }
        }

        if (workToDo == null) {

        }

    }

    private fun getConnectionStatus(device: BluetoothDevice): Boolean {

        val connectedDevices = bluetoothManager.getDevicesMatchingConnectionStates(
            BluetoothProfile.GATT,
            intArrayOf(BluetoothProfile.STATE_CONNECTED)
        )
        return connectedDevices.contains(device)
    }

    fun finishWork(work: Work) {

        if (work.finished) {

            return
        }

        if (work.isCriticalsCompleted()) {
            Utils.broadcastDeviceProcessed(context, work.device.address)
//            Utils.broadcastDeviceProcessed(context, work.connectable.manuData)
        }



        timeoutHandler.removeCallbacks(work.timeoutRunnable)


        work.finished = true
        doWork()
    }

    inner class CentralGattCallback(val work: Work) : BluetoothGattCallback() {

        fun endWorkConnection(gatt: BluetoothGatt) {
            gatt.disconnect()
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

            gatt?.let {

                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {


                        //get a fast connection?
//                        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED)
                        gatt.requestMtu(512)

                        work.checklist.connected.status = true
                        work.checklist.connected.timePerformed = System.currentTimeMillis()
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        work.checklist.disconnected.status = true
                        work.checklist.disconnected.timePerformed = System.currentTimeMillis()

                        //remove timeout runnable if its still there
                        timeoutHandler.removeCallbacks(work.timeoutRunnable)


                        //remove job from list of current work - if it is the current work
                        if (work.device.address == currentWork?.device?.address) {
                            currentWork = null
                        }
                        gatt.close()
                        finishWork(work)
                    }

                    else -> {

                        endWorkConnection(gatt)
                    }
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {

            if (!work.checklist.mtuChanged.status) {

                work.checklist.mtuChanged.status = true
                work.checklist.mtuChanged.timePerformed = System.currentTimeMillis()



                gatt?.let {
                    val discoveryOn = gatt.discoverServices()

                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {


                    var service = gatt.getService(serviceUUID)

                    service?.let {

                        //select characteristicUUID to read from
                        val characteristic = service.getCharacteristic(characteristicV2)

                        if (characteristic != null) {
                            val readSuccess = gatt.readCharacteristic(characteristic)

                        } else {

                            endWorkConnection(gatt)
                        }
                    }

                    if (service == null) {

                        endWorkConnection(gatt)
                    }
                }
                else -> {

                    endWorkConnection(gatt)
                }
            }
        }

        // data read from a perhipheral
        //I am a central
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {




                    if (BlueTrace.supportsCharUUID(characteristic.uuid)) {

                        try {
                            val bluetraceImplementation =
                                BlueTrace.getImplementation(characteristic.uuid)
                            val dataBytes = characteristic.value

                            val connectionRecord =
                                bluetraceImplementation
                                    .central
                                    .processReadRequestDataReceived(
                                        dataRead = dataBytes,
                                        peripheralAddress = work.device.address,
                                        rssi = work.connectable.rssi,
                                        txPower = work.connectable.transmissionPower
                                    )

                            //if the deserializing was a success, connectionRecord will not be null, save it
                            connectionRecord?.let {
                                Utils.broadcastStreetPassReceived(
                                    context,
                                    connectionRecord
                                )
                            }
                        } catch (e: Throwable) {
                        }

                    }
                    work.checklist.readCharacteristic.status = true
                    work.checklist.readCharacteristic.timePerformed = System.currentTimeMillis()
                }

                else -> {

                }
            }

            //attempt to do a write
            if (BlueTrace.supportsCharUUID(characteristic.uuid)) {



                val bluetraceImplementation = BlueTrace.getImplementation(characteristic.uuid)

                var writedata = bluetraceImplementation.central.prepareWriteRequestData(
                    bluetraceImplementation.versionInt,
                    work.connectable.rssi,
                    work.connectable.transmissionPower
                )
                characteristic.value = writedata
                val writeSuccess = gatt.writeCharacteristic(characteristic)


                endWorkConnection(gatt)


            } else {

                endWorkConnection(gatt)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    work.checklist.writeCharacteristic.status = true
                    work.checklist.writeCharacteristic.timePerformed =
                        System.currentTimeMillis()
                }
                else -> {
                }
            }

            endWorkConnection(gatt)
        }

    }

    fun terminateConnections() {


        currentWork?.gatt?.disconnect()
        currentWork = null

        timeoutHandler.removeCallbacksAndMessages(null)
        queueHandler.removeCallbacksAndMessages(null)
        blacklistHandler.removeCallbacksAndMessages(null)

        workQueue.clear()
        blacklist.clear()
    }

    fun unregisterReceivers() {
        try {
            localBroadcastManager.unregisterReceiver(blacklistReceiver)
        } catch (e: Throwable) {
        }

        try {
            localBroadcastManager.unregisterReceiver(scannedDeviceReceiver)
        } catch (e: Throwable) {

        }
    }

    inner class BlacklistReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_DEVICE_PROCESSED == intent.action) {
                val deviceAddress = intent.getStringExtra(DEVICE_ADDRESS)

                val entry = BlacklistEntry(deviceAddress, System.currentTimeMillis())
                blacklist.add(entry)
                blacklistHandler.postDelayed({

                }, blacklistDuration)
            }
        }
    }

    inner class ScannedDeviceReceiver : BroadcastReceiver() {

        private val TAG = "ScannedDeviceReceiver"

        override fun onReceive(context: Context?, intent: Intent?) {

            intent?.let {
                if (ACTION_DEVICE_SCANNED == intent.action) {
                    //get data from extras
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val connectable: ConnectablePeripheral? =
                        intent.getParcelableExtra(CONNECTION_DATA)

                    val devicePresent = device != null
                    val connectablePresent = connectable != null



                    device?.let {
                        connectable?.let {
                            val work = Work(device, connectable, onWorkTimeoutListener)
                            if (addWork(work)) {
                                doWork()
                            }
                        }
                    }
                }
            }
        }
    }
}
