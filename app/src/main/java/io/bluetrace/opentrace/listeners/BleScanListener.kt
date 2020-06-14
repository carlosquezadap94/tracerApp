package io.bluetrace.opentrace.listeners

interface BleScanListener {
    fun scannerCount():Int
    fun discountScanCount():Int
}