package com.example.foodorder.listener

import com.example.foodorder.model.Food

interface IOnManagerFoodListener {
    fun onClickUpdateFood(food: Food?)
    fun onClickDeleteFood(food: Food?)
}