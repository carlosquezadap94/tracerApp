package io.bluetrace.opentrace.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.bluetrace.opentrace.Utils

class StartOnBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {

            try {
                Utils.scheduleStartMonitoringService(context, 500)
            } catch (e: Throwable) {
                e.printStackTrace()
            }

        }
    }
}
