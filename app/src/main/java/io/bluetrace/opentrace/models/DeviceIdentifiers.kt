package io.bluetrace.opentrace.models


import com.google.gson.annotations.SerializedName

data class DeviceIdentifiers(
    @SerializedName("deviceIdForVendor")
    val deviceIdForVendor: String,
    @SerializedName("macAddress")
    val macAddress: String
)