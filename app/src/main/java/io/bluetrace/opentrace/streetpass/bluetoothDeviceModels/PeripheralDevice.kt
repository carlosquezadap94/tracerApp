package io.bluetrace.opentrace.streetpass.bluetoothDeviceModels

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PeripheralDevice(
    val modelP: String,
    val address: String?
) : Parcelable