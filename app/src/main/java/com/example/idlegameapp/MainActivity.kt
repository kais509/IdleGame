package com.example.idlegame

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.example.idlegameapp.R

class MainActivity : AppCompatActivity() {
    private var currency: Double = 1000.0 // Total currency
    private var baseCurrencyPerSecond: Double = 1.0 // Base currency per second
    private var multiplier: Double = 1.0 // Multiplier for currency per second
    private var exponent: Double = 1.0 // Exponent for currency per second

    private var flatUpgradeCost: Double = 10.0
    private var multiplierUpgradeCost: Double = 50.0
    private var exponentUpgradeCost: Double = 200.0

    private lateinit var currencyTextView: TextView
    private lateinit var flatUpgradeButton: Button
    private lateinit var multiplierUpgradeButton: Button
    private lateinit var exponentUpgradeButton: Button
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currencyTextView = findViewById(R.id.currencyTextView)
        flatUpgradeButton = findViewById(R.id.flatUpgradeButton)
        multiplierUpgradeButton = findViewById(R.id.multiplierUpgradeButton)
        exponentUpgradeButton = findViewById(R.id.exponentUpgradeButton)

        updateUI()
        startIncrementing()

        flatUpgradeButton.setOnClickListener { buyFlatUpgrade() }
        multiplierUpgradeButton.setOnClickListener { buyMultiplierUpgrade() }
        exponentUpgradeButton.setOnClickListener { buyExponentUpgrade() }
    }

    private fun startIncrementing() {
        handler.post(object : Runnable {
            override fun run() {
                val currencyPerSecond = Math.pow(baseCurrencyPerSecond * multiplier, exponent)
                currency += currencyPerSecond
                updateUI()
                handler.postDelayed(this, 1000) // Repeat every second
            }
        })
    }

    private fun buyFlatUpgrade() {
        if (currency >= flatUpgradeCost) {
            currency -= flatUpgradeCost
            baseCurrencyPerSecond += 1.0
            flatUpgradeCost *= 1.5 // Cost increases
            updateUI()
        }
    }

    private fun buyMultiplierUpgrade() {
        if (currency >= multiplierUpgradeCost) {
            currency -= multiplierUpgradeCost
            multiplier += 0.1
            multiplierUpgradeCost *= 2.0 // Cost increases
            updateUI()
        }
    }

    private fun buyExponentUpgrade() {
        if (currency >= exponentUpgradeCost) {
            currency -= exponentUpgradeCost
            exponent += 0.1
            exponentUpgradeCost *= 3.0 // Cost increases
            updateUI()
        }
    }

    private fun updateUI() {
        currencyTextView.text = "Currency: ${"%.2f".format(currency)}"

        flatUpgradeButton.text = "Flat Upgrade (+1/sec): ${"%.2f".format(flatUpgradeCost)}"
        multiplierUpgradeButton.text = "Multiplier Upgrade (x1.1): ${"%.2f".format(multiplierUpgradeCost)}"
        exponentUpgradeButton.text = "Exponent Upgrade (^1.1): ${"%.2f".format(exponentUpgradeCost)}"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop the loop when the activity is destroyed
    }
}
