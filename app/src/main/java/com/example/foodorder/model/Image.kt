package com.example.foodorder.model

import java.io.Serializable

class Image : Serializable {

    var url: String? = null

    constructor() {}

    constructor(url: String?) {
        this.url = url
    }
}