package com.example.idlegameapp


import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.idlegameapp.R

class PrestigeActivity : AppCompatActivity() {
    private var vertexEssence = 0
    private val skillTreeManager = SkillTreeManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prestige)

        // Load saved prestige currency and skill levels
        loadPrestigeData()

        // Calculate potential prestige currency
        val currentCurrency = intent.getDoubleExtra("currentCurrency", 0.0)
        val potentialGain = calculatePrestigeGain(currentCurrency)

        // Update UI
        updatePrestigeInfo(potentialGain)

        // Setup skill tree buttons
        setupSkillTreeButtons()

        // Prestige button
        findViewById<Button>(R.id.prestigeButton).setOnClickListener {
            performPrestige(potentialGain)
        }

        // Back button
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun setupSkillTreeButtons() {
        findViewById<Button>(R.id.startingDotsSkill).setOnClickListener {
            purchaseSkill("start_dots")
        }

        findViewById<Button>(R.id.baseSpeedSkill).setOnClickListener {
            purchaseSkill("base_speed")
        }

        findViewById<Button>(R.id.currencyGainSkill).setOnClickListener {
            purchaseSkill("currency_gain")
        }

        updateSkillButtons()
    }

    private fun purchaseSkill(skillId: String) {
        val newEssence = skillTreeManager.purchaseNode(skillId, vertexEssence)
        if (newEssence != vertexEssence) {
            vertexEssence = newEssence
            updateSkillButtons()
            savePrestigeData()
        }
    }

    private fun updateSkillButtons() {
        skillTreeManager.getNode("start_dots")?.let { node ->
            findViewById<Button>(R.id.startingDotsSkill).apply {
                text = "Starting Dots (${node.level}/${node.maxLevel})\nCost: ${node.getCurrentCost()}"
                isEnabled = node.canBePurchased(vertexEssence, skillTreeManager.getUnlockedNodes())
            }
        }

        skillTreeManager.getNode("base_speed")?.let { node ->
            findViewById<Button>(R.id.baseSpeedSkill).apply {
                text = "Base Speed (${node.level}/${node.maxLevel})\nCost: ${node.getCurrentCost()}"
                isEnabled = node.canBePurchased(vertexEssence, skillTreeManager.getUnlockedNodes())
            }
        }

        skillTreeManager.getNode("currency_gain")?.let { node ->
            findViewById<Button>(R.id.currencyGainSkill).apply {
                text = "Currency Gain (${node.level}/${node.maxLevel})\nCost: ${node.getCurrentCost()}"
                isEnabled = node.canBePurchased(vertexEssence, skillTreeManager.getUnlockedNodes())
            }
        }

        findViewById<TextView>(R.id.vertexEssenceCount).text = "Vertex Essence: $vertexEssence"
    }

    private fun loadPrestigeData() {
        val prefs = getSharedPreferences("PrestigeData", Context.MODE_PRIVATE)
        vertexEssence = prefs.getInt("vertexEssence", 0)
        
        // Load skill levels
        val skillLevels = mutableMapOf<String, Int>()
        skillLevels["start_dots"] = prefs.getInt("skill_start_dots", 0)
        skillLevels["base_speed"] = prefs.getInt("skill_base_speed", 0)
        skillLevels["currency_gain"] = prefs.getInt("skill_currency_gain", 0)
        
        skillTreeManager.loadState(skillLevels)
    }

    private fun savePrestigeData() {
        val prefs = getSharedPreferences("PrestigeData", Context.MODE_PRIVATE).edit()
        prefs.putInt("vertexEssence", vertexEssence)
        
        // Save skill levels
        val skillLevels = skillTreeManager.getState()
        skillLevels.forEach { (id, level) ->
            prefs.putInt("skill_$id", level)
        }
        
        prefs.apply()
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
        setResult(RESULT_OK)
        finish()
    }
} 