package io.bluetrace.opentrace.models


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("deviceData")
    val deviceData: DeviceData,
    @SerializedName("events")
    val events: List<Event>,
    @SerializedName("userData")
    val userData: UserData
)