package io.bluetrace.opentrace.models


import com.google.gson.annotations.SerializedName

data class EventLocation(
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lng")
    val lng: String
)