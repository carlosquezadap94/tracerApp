package io.bluetrace.opentrace.services.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.bluetrace.opentrace.bluetooth.gatt.ACTION_RECEIVED_STATUS
import io.bluetrace.opentrace.bluetooth.gatt.STATUS
import io.bluetrace.opentrace.persistence.status.Status
import io.bluetrace.opentrace.persistence.status.StatusRecord
import kotlinx.coroutines.launch

class StatusReceiver : BroadcastReceiver() {
    private val TAG = "StatusReceiver"

    override fun onReceive(context: Context, intent: Intent) {

        if (ACTION_RECEIVED_STATUS == intent.action) {
            var statusRecord: Status = intent.getParcelableExtra(STATUS)

            if (statusRecord.msg.isNotEmpty()) {
                val statusRecord =
                    StatusRecord(
                        statusRecord.msg
                    )
                launch {
                    statusRecordStorage.saveRecord(statusRecord)
                }
            }
        }
    }
}