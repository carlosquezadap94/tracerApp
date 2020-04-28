package io.bluetrace.opentrace.models


import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("identityDocumentType")
    val identityDocumentType: String,
    @SerializedName("identityDocumentValue")
    val identityDocumentValue: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String
)