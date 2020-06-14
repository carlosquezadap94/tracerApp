package io.bluetrace.opentrace.services.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.bluetrace.opentrace.bluetooth.gatt.ACTION_RECEIVED_STREETPASS
import io.bluetrace.opentrace.bluetooth.gatt.STREET_PASS
import io.bluetrace.opentrace.listeners.StorageListener
import io.bluetrace.opentrace.persistence.streetpass.StreetPassRecord
import io.bluetrace.opentrace.streetpass.ConnectionRecord
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StreetPassReceiver(private val listener: StorageListener) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (ACTION_RECEIVED_STREETPASS == intent.action) {
            var connRecord: ConnectionRecord = intent.getParcelableExtra(STREET_PASS)


            if (connRecord.msg.isNotEmpty()) {
                val record =
                    StreetPassRecord(
                        v = connRecord.version,
                        msg = connRecord.msg,
                        org = connRecord.org,
                        modelP = connRecord.peripheral.modelP,
                        modelC = connRecord.central.modelC,
                        rssi = connRecord.rssi,
                        txPower = connRecord.txPower
                    )

                GlobalScope.launch {

                    listener.onStreetPassRecordStorage(record)
                }
            }
        }
    }
}