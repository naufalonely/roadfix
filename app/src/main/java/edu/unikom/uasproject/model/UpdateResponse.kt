package edu.unikom.uasproject.model

import com.google.gson.annotations.SerializedName

data class UpdateResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)