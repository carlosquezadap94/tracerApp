package io.bluetrace.opentrace.streetpass.bluetoothDeviceModels

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ConnectablePeripheral(
    var manuData: String,
    var transmissionPower: Int?,
    var rssi: Int
) : Parcelable