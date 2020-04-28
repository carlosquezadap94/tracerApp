package io.bluetrace.opentrace.models


import com.google.gson.annotations.SerializedName

data class EventData(
    @SerializedName("detectionMethod")
    val detectionMethod: String,
    @SerializedName("deviceInContact")
    val deviceInContact: DeviceInContact,
    @SerializedName("eventDuration")
    val eventDuration: Int,
    @SerializedName("eventLocation")
    val eventLocation: EventLocation
)