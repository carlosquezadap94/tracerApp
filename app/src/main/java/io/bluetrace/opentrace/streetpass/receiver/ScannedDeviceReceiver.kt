package io.bluetrace.opentrace.streetpass.receiver

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.bluetrace.opentrace.bluetooth.gatt.CONNECTION_DATA
import io.bluetrace.opentrace.listeners.StreetPassListener
import io.bluetrace.opentrace.listeners.WorkListener
import io.bluetrace.opentrace.streetpass.ACTION_DEVICE_SCANNED
import io.bluetrace.opentrace.streetpass.OnWorkTimeoutListener
import io.bluetrace.opentrace.streetpass.Work
import io.bluetrace.opentrace.streetpass.bluetoothDeviceModels.ConnectablePeripheral

class ScannedDeviceReceiver(private val streePassListener: StreetPassListener,
                            private val workListener: WorkListener)
    : BroadcastReceiver() {

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
                        if (streePassListener.addWork(work)) {
                            streePassListener.doWork()
                        }
                    }
                }
            }
        }
    }


    val onWorkTimeoutListener = object : OnWorkTimeoutListener {
        override fun onWorkTimeout(work: Work) {

            if (!streePassListener.isCurrentlyWorkedOn(work.device.address)) {

            }
            //connection never formed - don't need to disconnect
            if (!work.checklist.connected.status) {

                if (work.device.address == workListener.getCurrrentWork().device?.address) {
                    workListener.currentWorkToNull()
                }

                try {
                    work.gatt?.close()
                } catch (e: Exception) {

                }

                workListener.finishWork(work)
            }
            //the connection is still there - might be stuck / work in progress
            else if (work.checklist.connected.status && !work.checklist.disconnected.status) {

                if (work.checklist.readCharacteristic.status || work.checklist.writeCharacteristic.status || work.checklist.skipped.status) {


                    try {
                        work.gatt?.disconnect()
                        //disconnect callback won't get invoked
                        if (work.gatt == null) {
                            workListener.currentWorkToNull()
                            workListener.finishWork(work)
                        }
                    } catch (e: Throwable) {

                    }

                } else {


                    try {
                        work.gatt?.disconnect()
                        //disconnect callback won't get invoked
                        if (work.gatt == null) {
                            workListener.currentWorkToNull()
                            workListener.finishWork(work)
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

}