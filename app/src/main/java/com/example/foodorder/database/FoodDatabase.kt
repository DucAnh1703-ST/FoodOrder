package com.example.foodorder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.foodorder.model.Food

@Database(entities = [Food::class], version = 2)
abstract class FoodDatabase : RoomDatabase() {

    abstract fun foodDAO(): FoodDAO?

//    singleton pattern
    companion object {
        private const val DATABASE_NAME = "food.db"
        private var instance: FoodDatabase? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): FoodDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext, FoodDatabase::class.java, DATABASE_NAME)
                        .allowMainThreadQueries()
                        .build()
            }
            return instance
        }
    }
}