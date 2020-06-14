package io.bluetrace.opentrace.services.receiver

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.listeners.BluetoothStatusListener

class BluetoothStatusReceiver (private var listener: BluetoothStatusListener) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                var state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)

                when (state) {
                    BluetoothAdapter.STATE_TURNING_OFF -> {

                        listener.onNotifyLackingThings()
                        listener.onTeardown()
                    }
                    BluetoothAdapter.STATE_OFF -> {

                    }
                    BluetoothAdapter.STATE_TURNING_ON -> {

                    }
                    BluetoothAdapter.STATE_ON -> {

                       Utils.startBluetoothMonitoringService(context)
                    }
                }
            }
        }
    }
}