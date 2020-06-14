package io.bluetrace.opentrace.streetpass

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.IntentFilter
import android.os.Handler
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.bluetooth.gatt.ACTION_DEVICE_PROCESSED
import io.bluetrace.opentrace.listeners.BlacklistListener
import io.bluetrace.opentrace.listeners.ContextListener
import io.bluetrace.opentrace.listeners.StreetPassListener
import io.bluetrace.opentrace.listeners.WorkListener
import io.bluetrace.opentrace.services.Constants
import io.bluetrace.opentrace.services.Constants.Companion.connectionTimeout
import io.bluetrace.opentrace.services.Constants.Companion.maxQueueTime
import io.bluetrace.opentrace.services.Constants.Companion.useBlacklist
import io.bluetrace.opentrace.streetpass.callback.CentralGattCallback
import io.bluetrace.opentrace.streetpass.receiver.BlacklistReceiver
import io.bluetrace.opentrace.streetpass.receiver.ScannedDeviceReceiver
import java.util.*
import java.util.concurrent.PriorityBlockingQueue

class StreetPassWorker(val context: Context) : StreetPassListener, BlacklistListener,
    ContextListener, WorkListener {

    private val workQueue: PriorityBlockingQueue<Work> =
        PriorityBlockingQueue(5, Collections.reverseOrder<Work>())
    private val blacklist: MutableList<BlacklistEntry> = Collections.synchronizedList(ArrayList())

    private val scannedDeviceReceiver = ScannedDeviceReceiver(this, this)
    private val blacklistReceiver = BlacklistReceiver(this)

    private val TAG = "StreetPassWorker"

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private lateinit var timeoutHandler: Handler
    private lateinit var queueHandler: Handler
    private lateinit var blacklistHandler: Handler

    private var currentWork: Work? = null
    private var localBroadcastManager: LocalBroadcastManager =
        LocalBroadcastManager.getInstance(context)


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

    override fun isCurrentlyWorkedOn(address: String): Boolean {
        return currentWork?.let {
            it.device.address == address
        } ?: false
    }

    override fun currentWorkToNull() {
        currentWork = null
    }

    override fun timeoutHandler(runnable: Runnable) {
        timeoutHandler.removeCallbacks(runnable)
    }

    override fun getCurrrentWork() = currentWork!!

    override fun addWork(work: Work): Boolean {
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

    override fun doWork() {
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

                    val gattCallback = CentralGattCallback(it, this, this)

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
                            connectionTimeout
                        )
                        it.timeout =
                            System.currentTimeMillis() + connectionTimeout


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

    override fun finishWork(work: Work) {

        if (work.finished) {

            return
        }

        if (work.isCriticalsCompleted()) {
            Utils.broadcastDeviceProcessed(context, work.device.address)
            Utils.broadcastDeviceProcessed(context, work.connectable.manuData)
        }



        timeoutHandler.removeCallbacks(work.timeoutRunnable)


        work.finished = true
        doWork()
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

    override fun addEntry(blackEntry: BlacklistEntry) {
        blacklist.add(blackEntry)
    }

    override fun handlerBlackList() {
        blacklistHandler.postDelayed({
        }, Constants.blacklistDuration)
    }

    override fun getContext_() = context


}
