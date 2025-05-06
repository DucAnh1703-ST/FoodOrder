package com.example.foodorder.model

import com.example.foodorder.constant.Constant
import java.io.Serializable

class Order : Serializable {
    var id: Long = 0
    var name: String? = null
    var email: String? = null
    var phone: String? = null
    var address: String? = null
    var amount = 0
    var foods: String? = null
    var payment = 0
    var note: String? = null
    var status: Int = Constant.CODE_NEW_ORDER

    var cancelBy: String? = null
    var cancelReason: String? = null
    var deliveryFee = 0
    var totalPrice = 0
    var transaction: String? = null

    constructor() {}

    constructor(
        id: Long, name: String?, email: String?, phone: String?,
        address: String?, amount: Int, foods: String?, payment: Int, note: String?, status: Int,  deliveryFee: Int,  totalPrice: Int
    ) {
        this.id = id
        this.name = name
        this.email = email
        this.phone = phone
        this.address = address
        this.amount = amount
        this.foods = foods
        this.payment = payment
        this.note = note
        this.status = status
        this.deliveryFee = deliveryFee
        this.totalPrice = totalPrice
    }
}