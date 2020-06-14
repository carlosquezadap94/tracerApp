package io.bluetrace.opentrace.streetpass.callback

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.listeners.BleScanListener
import io.bluetrace.opentrace.listeners.ContextListener
import io.bluetrace.opentrace.streetpass.bluetoothDeviceModels.ConnectablePeripheral

class BleScanCallback(
    private val contextListener: ContextListener,
    private val bleScanListener: BleScanListener
) : ScanCallback() {

    private val TAG = "BleScanCallback"

    private fun processScanResult(scanResult: ScanResult?) {

        scanResult?.let { result ->
            val device = result.device
            var rssi = result.rssi // get RSSI value

            var txPower: Int? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                txPower = result.txPower
                if (txPower == 127) {
                    txPower = null
                }
            }

            var manuData: ByteArray =
                scanResult.scanRecord?.getManufacturerSpecificData(1023) ?: "N.A".toByteArray()
            var manuString = String(manuData, Charsets.UTF_8)

            var connectable = ConnectablePeripheral(manuString, txPower, rssi)


            Utils.broadcastDeviceScanned(contextListener.getContext_(), device, connectable)
        }
    }

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        processScanResult(result)
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)

        val reason = when (errorCode) {
            SCAN_FAILED_ALREADY_STARTED -> "$errorCode - SCAN_FAILED_ALREADY_STARTED"
            SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "$errorCode - SCAN_FAILED_APPLICATION_REGISTRATION_FAILED"
            SCAN_FAILED_FEATURE_UNSUPPORTED -> "$errorCode - SCAN_FAILED_FEATURE_UNSUPPORTED"
            SCAN_FAILED_INTERNAL_ERROR -> "$errorCode - SCAN_FAILED_INTERNAL_ERROR"
            else -> {
                "$errorCode - UNDOCUMENTED"
            }
        }

        //cannerCount--
        if (bleScanListener.scannerCount() > 0) {
            bleScanListener.discountScanCount()
        }
    }
}