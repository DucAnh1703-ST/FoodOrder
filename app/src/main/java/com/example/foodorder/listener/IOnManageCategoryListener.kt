package com.example.foodorder.listener

import com.example.foodorder.model.Category

interface IOnManageCategoryListener {
    fun onClickUpdateCategory(category: Category?)
    fun onClickDeleteCategory(category: Category?)
}