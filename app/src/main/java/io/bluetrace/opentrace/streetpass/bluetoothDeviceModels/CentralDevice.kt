package io.bluetrace.opentrace.streetpass.bluetoothDeviceModels

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CentralDevice(
    val modelC: String,
    val address: String?
) : Parcelable