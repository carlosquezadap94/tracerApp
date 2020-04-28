package io.bluetrace.opentrace.models


import com.google.gson.annotations.SerializedName

data class OtherData(
    @SerializedName("deviceModel")
    val deviceModel: String,
    @SerializedName("deviceName")
    val deviceName: String,
    @SerializedName("deviceOs")
    val deviceOs: String
)