package io.bluetrace.opentrace.models


import com.google.gson.annotations.SerializedName

data class DeviceInContact(
    @SerializedName("deviceIdForVendor")
    val deviceIdForVendor: String,
    @SerializedName("macAddress")
    val macAddress: String,
    @SerializedName("rssi")
    val rssi: Int,
    @SerializedName("transmitPower")
    val transmitPower: Int
)