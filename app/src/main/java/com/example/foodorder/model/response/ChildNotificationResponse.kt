package com.example.foodorder.model.response

import com.google.gson.annotations.SerializedName

data class ChildNotificationResponse(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("body")
    val body: String? = null
)