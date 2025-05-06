package com.example.foodorder.model.response

import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("token")
    val token: String? = null,
    @SerializedName("notification")
    val notification: ChildNotificationResponse? = null,
    @SerializedName("data")
    val data: ChildOrderDataResponse? = null
)