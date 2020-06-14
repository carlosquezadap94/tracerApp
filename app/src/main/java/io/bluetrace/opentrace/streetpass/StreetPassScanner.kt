package io.bluetrace.opentrace.streetpass

import android.content.Context
import android.os.Handler
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.bluetooth.BLEScanner
import io.bluetrace.opentrace.listeners.BleScanListener
import io.bluetrace.opentrace.listeners.ContextListener
import io.bluetrace.opentrace.persistence.status.Status
import io.bluetrace.opentrace.services.Constants.Companion.infiniteScanning
import io.bluetrace.opentrace.streetpass.callback.BleScanCallback
import kotlin.properties.Delegates

class StreetPassScanner constructor(
    context: Context,
    serviceUUIDString: String,
    private val scanDurationInMillis: Long
) : BleScanListener, ContextListener {

    private var scanner: BLEScanner by Delegates.notNull()

    private var context: Context by Delegates.notNull()
    private val TAG = "StreetPassScanner"

    private var handler: Handler = Handler()

    var scannerCount = 0

    val scanCallback = BleScanCallback(this, this)

//    var discoverer: BLEDiscoverer

    init {
        scanner = BLEScanner(context, serviceUUIDString, 0)
        this.context = context
//        discoverer = BLEDiscoverer(context, serviceUUIDString)
    }

    fun startScan() {

        var statusRecord =
            Status("Scanning Started")
        Utils.broadcastStatusReceived(context, statusRecord)

        scanner.startScan(scanCallback)
        scannerCount++

        if (!infiniteScanning) {
            handler.postDelayed(
                { stopScan() }
                , scanDurationInMillis)
        }


//        discoverer.startDiscovery()
    }

    fun stopScan() {
        //only stop if scanning was successful - kinda.
        if (scannerCount > 0) {
            var statusRecord =
                Status("Scanning Stopped")
            Utils.broadcastStatusReceived(context, statusRecord)
            scannerCount--
            scanner.stopScan()
//        discoverer.cancelDiscovery()
        }
    }

    fun isScanning(): Boolean {
        return scannerCount > 0
    }

    override fun scannerCount() = scannerCount

    override fun discountScanCount(): Int {
        return scannerCount--
    }

    override fun getContext_() = context


}

