package io.bluetrace.opentrace.models


import com.google.gson.annotations.SerializedName

data class DeviceData(
    @SerializedName("deviceIdentifiers")
    val deviceIdentifiers: DeviceIdentifiers,
    @SerializedName("otherData")
    val otherData: OtherData
)