package com.example.foodorder.model

class Contact(var id: Int, var image: Int) {

    companion object {
        const val FACEBOOK = 0
        const val HOTLINE = 1
        const val YOUTUBE = 2
        const val ZALO = 3
    }
}