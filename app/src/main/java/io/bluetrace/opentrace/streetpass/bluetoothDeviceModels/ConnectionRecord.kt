package io.bluetrace.opentrace.streetpass.bluetoothDeviceModels

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConnectionRecord(
    val version: Int,

    val msg: String,
    val org: String,

    val peripheral: PeripheralDevice,
    val central: CentralDevice,

    var rssi: Int,
    var txPower: Int?
) : Parcelable {
    override fun toString(): String {
        return "Central ${central.modelC} - ${central.address} ---> Peripheral ${peripheral.modelP} - ${peripheral.address}"
    }
}