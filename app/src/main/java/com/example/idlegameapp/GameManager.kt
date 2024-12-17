package com.example.idlegameapp

import android.content.Context

class GameManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("GameData", Context.MODE_PRIVATE)
    
    fun getCurrency(): Double {
        return sharedPreferences.getLong("currency", 0).toDouble()
    }

    fun setCurrency(amount: Double) {
        sharedPreferences.edit().putLong("currency", amount.toLong()).apply()
    }
} 