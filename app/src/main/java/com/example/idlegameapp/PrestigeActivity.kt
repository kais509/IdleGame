package com.example.idlegameapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.idlegameapp.R

class PrestigeActivity : AppCompatActivity() {
    private var vertexEssence = 0
    private var startingDotsLevel = 0
    private var baseSpeedLevel = 0
    private var currencyMultiplierLevel = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prestige) 

        // Load saved prestige currency and upgrades
        loadPrestigeData()

        // Calculate potential prestige currency
        val currentCurrency = intent.getDoubleExtra("currentCurrency", 0.0)
        val potentialGain = calculatePrestigeGain(currentCurrency)

        // Update UI
        updatePrestigeInfo(potentialGain)
        updateMetaUpgradeButtons()

        // Prestige button
        findViewById<Button>(R.id.prestigeButton).setOnClickListener {
            performPrestige(potentialGain)
        }

        // Meta upgrade buttons
        setupMetaUpgradeButtons()
    }

    private fun loadPrestigeData() {
        val prefs = getSharedPreferences("PrestigeData", Context.MODE_PRIVATE)
        vertexEssence = prefs.getInt("vertexEssence", 0)
        startingDotsLevel = prefs.getInt("startingDots", 0)
        baseSpeedLevel = prefs.getInt("baseSpeed", 0)
        currencyMultiplierLevel = prefs.getInt("currencyMultiplier", 0)
    }

    private fun calculatePrestigeGain(currency: Double): Int {
        return (Math.sqrt(currency / 1000.0)).toInt()
    }

    private fun updatePrestigeInfo(potentialGain: Int) {
        findViewById<TextView>(R.id.vertexEssenceCount).text = "Vertex Essence: $vertexEssence"
        findViewById<TextView>(R.id.prestigeInfo).text =
            "Resetting will give you:\n$potentialGain Vertex Essence"
    }

    private fun performPrestige(gain: Int) {
        vertexEssence += gain
        savePrestigeData()
        
        // Return result to MainActivity
        setResult(RESULT_OK)
        finish()
    }

    private fun setupMetaUpgradeButtons() {
        findViewById<Button>(R.id.metaUpgrade1).setOnClickListener {
            if (vertexEssence >= (startingDotsLevel + 1)) {
                vertexEssence -= (startingDotsLevel + 1)
                startingDotsLevel++
                savePrestigeData()
                updateMetaUpgradeButtons()
            }
        }

        findViewById<Button>(R.id.metaUpgrade2).setOnClickListener {
            if (vertexEssence >= (baseSpeedLevel + 2)) {
                vertexEssence -= (baseSpeedLevel + 2)
                baseSpeedLevel++
                savePrestigeData()
                updateMetaUpgradeButtons()
            }
        }

        findViewById<Button>(R.id.metaUpgrade3).setOnClickListener {
            if (vertexEssence >= (currencyMultiplierLevel + 3)) {
                vertexEssence -= (currencyMultiplierLevel + 3)
                currencyMultiplierLevel++
                savePrestigeData()
                updateMetaUpgradeButtons()
            }
        }
    }

    private fun updateMetaUpgradeButtons() {
        findViewById<Button>(R.id.metaUpgrade1).text = 
            "Starting Dots +1 (Cost: ${startingDotsLevel + 1})"
        findViewById<Button>(R.id.metaUpgrade2).text = 
            "Base Speed +10% (Cost: ${baseSpeedLevel + 2})"
        findViewById<Button>(R.id.metaUpgrade3).text = 
            "Currency Gain +20% (Cost: ${currencyMultiplierLevel + 3})"
        
        findViewById<TextView>(R.id.vertexEssenceCount).text = 
            "Vertex Essence: $vertexEssence"
    }

    private fun savePrestigeData() {
        getSharedPreferences("PrestigeData", Context.MODE_PRIVATE).edit().apply {
            putInt("vertexEssence", vertexEssence)
            putInt("startingDots", startingDotsLevel)
            putInt("baseSpeed", baseSpeedLevel)
            putInt("currencyMultiplier", currencyMultiplierLevel)
            apply()
        }
    }
} 