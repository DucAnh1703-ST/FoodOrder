package com.example.foodorder.model.response

import com.google.gson.annotations.SerializedName

data class ChildOrderDataResponse(
    @SerializedName("typeFor")
    val typeFor: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("orderId")
    val orderId: String? = null
)