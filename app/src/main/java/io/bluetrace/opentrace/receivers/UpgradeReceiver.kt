package io.bluetrace.opentrace.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.bluetrace.opentrace.Utils

class UpgradeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        try {
            if (Intent.ACTION_MY_PACKAGE_REPLACED != intent!!.action) return
            // Start your service here.
            context?.let {
                Utils.startBluetoothMonitoringService(context)
            }
        } catch (e: Exception) {
        }
    }
}
