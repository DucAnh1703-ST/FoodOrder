package com.example.foodorder.database

import com.example.foodorder.model.request.OrderRequest
import com.example.foodorder.model.response.OrderResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AppApi {

    // Cái này là ví dụ có trả về. còn thực sự bài toán này không cần trả về gì cả
    @POST("/api/order_new")
    fun postNewOrderHasResponse(@Body orderRequest: OrderRequest): Call<OrderResponse>

    @GET("/success")
    fun getSuccessServer(): Call<Unit>

    @POST("/api/order_new") // for user
    fun postNewOrder(@Body orderRequest: OrderRequest): Call<Unit>

    @POST("/api/order_cancel") // for user
    fun postCancelOrder(@Body orderRequest: OrderRequest): Call<Unit>

    @POST("/api/order_accept") // for admin
    fun postAcceptOrder(@Body orderRequest: OrderRequest): Call<Unit>

    // không còn dùng theo yêu cầu của thầy (đã ẩn nút từ chối) => do bỏ phương thức thanh toán, chỉ dùng thanh toán online.
    @POST("/api/order_refuse") // for admin
    fun postRefuseOrder(@Body orderRequest: OrderRequest): Call<Unit>

    @POST("/api/order_send") // for admin
    fun postSendDeliveryOrder(@Body orderRequest: OrderRequest): Call<Unit>

    @POST("/api/order_complete") // expect driver, but now admin is using
    fun postCompleteOrder(@Body orderRequest: OrderRequest): Call<Unit>
}

