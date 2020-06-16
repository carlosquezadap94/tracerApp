package io.bluetrace.opentrace.services.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.bluetrace.opentrace.bluetooth.gatt.ACTION_RECEIVED_STATUS
import io.bluetrace.opentrace.bluetooth.gatt.STATUS
import io.bluetrace.opentrace.listeners.StorageStatusListener
import io.bluetrace.opentrace.persistence.status.Status
import io.bluetrace.opentrace.infraestructura.db.entidades.StatusRecordEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StatusReceiver(private val statusListener: StorageStatusListener) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (ACTION_RECEIVED_STATUS == intent.action) {
            var statusRecord: Status = intent.getParcelableExtra(STATUS)

            if (statusRecord.msg.isNotEmpty()) {
                val statusRecord =
                    StatusRecordEntity(
                        statusRecord.msg
                    )
                GlobalScope.launch {
                    statusListener.onStatusRecordStorage(statusRecord)
                }
            }
        }
    }
}