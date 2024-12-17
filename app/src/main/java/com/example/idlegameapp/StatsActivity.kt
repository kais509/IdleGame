package com.example.idlegameapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import java.text.DecimalFormat

class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val totalCurrency = getSharedPreferences("GameData", MODE_PRIVATE)
            .getFloat("totalCurrencyEarned", 0f)

        val formatter = DecimalFormat("#,###")
        findViewById<TextView>(R.id.totalCurrencyText).text = 
            "Total Currency Earned: ${formatter.format(totalCurrency.toInt())}"

        findViewById<MaterialButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
} 