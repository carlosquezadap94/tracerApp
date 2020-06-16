package io.bluetrace.opentrace.services.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.bluetrace.opentrace.bluetooth.gatt.ACTION_RECEIVED_STREETPASS
import io.bluetrace.opentrace.bluetooth.gatt.STREET_PASS
import io.bluetrace.opentrace.listeners.StorageRecordListener
import io.bluetrace.opentrace.infraestructura.db.entidades.StreetPassRecordEntity
import io.bluetrace.opentrace.streetpass.bluetoothDeviceModels.ConnectionRecord
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StreetPassReceiver(private val statusListener: StorageRecordListener) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (ACTION_RECEIVED_STREETPASS == intent.action) {
            var connRecord: ConnectionRecord = intent.getParcelableExtra(STREET_PASS)


            if (connRecord.msg.isNotEmpty()) {
                val record =
                    StreetPassRecordEntity(
                        v = connRecord.version,
                        msg = connRecord.msg,
                        org = connRecord.org,
                        modelP = connRecord.peripheral.modelP,
                        modelC = connRecord.central.modelC,
                        rssi = connRecord.rssi,
                        txPower = connRecord.txPower
                    )

                GlobalScope.launch {

                    statusListener.onStreetPassRecordStorage(record)
                }
            }
        }
    }
}