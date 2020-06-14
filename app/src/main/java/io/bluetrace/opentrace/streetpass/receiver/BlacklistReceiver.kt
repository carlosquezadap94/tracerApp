package io.bluetrace.opentrace.streetpass.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.bluetrace.opentrace.bluetooth.gatt.ACTION_DEVICE_PROCESSED
import io.bluetrace.opentrace.bluetooth.gatt.DEVICE_ADDRESS
import io.bluetrace.opentrace.listeners.BlacklistListener
import io.bluetrace.opentrace.streetpass.BlacklistEntry

class BlacklistReceiver(private val listener: BlacklistListener) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_DEVICE_PROCESSED == intent.action) {
            val deviceAddress = intent.getStringExtra(DEVICE_ADDRESS)
            val entry = BlacklistEntry(deviceAddress, System.currentTimeMillis())
            listener.addEntry(entry)
        }
    }
}