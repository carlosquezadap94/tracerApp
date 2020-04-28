package io.bluetrace.opentrace.models


import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("eventData")
    val eventData: EventData,
    @SerializedName("eventDate")
    val eventDate: String,
    @SerializedName("eventType")
    val eventType: String
)