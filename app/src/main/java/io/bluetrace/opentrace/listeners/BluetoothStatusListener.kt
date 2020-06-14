package io.bluetrace.opentrace.listeners

interface BluetoothStatusListener {
    fun onNotifyLackingThings()
    fun onTeardown()
}