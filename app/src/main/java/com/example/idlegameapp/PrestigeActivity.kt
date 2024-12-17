package com.example.idlegameapp

import com.example.idlegameapp.SkillTreeView
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.idlegameapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.idlegameapp.GameManager
import android.widget.Toast
import kotlin.math.sqrt

class PrestigeActivity : AppCompatActivity() {
    private lateinit var skillTreeView: SkillTreeView
    private lateinit var vertexEssenceText: TextView
    private lateinit var prestigeButton: MaterialButton
    private lateinit var homeButton: MaterialButton
    private lateinit var prestigeInfoText: TextView
    
    private var vertexEssence = 0
    private lateinit var skillTreeManager: SkillTreeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prestige)

        // Initialize managers
        skillTreeManager = SkillTreeManager()

        // Initialize views
        skillTreeView = findViewById(R.id.skillTreeView)
        vertexEssenceText = findViewById(R.id.vertexEssenceCount)
        prestigeButton = findViewById(R.id.prestigeButton)
        homeButton = findViewById(R.id.homeButton)
        prestigeInfoText = findViewById(R.id.prestigeInfo)

        // Load saved data and setup skill tree
        loadPrestigeData()
        setupSkillTree()

        // Setup prestige button
        prestigeButton.setOnClickListener {
            val gameManager = GameManager(this)
            val currentCurrency = gameManager.getCurrency()
            performPrestige(calculatePrestigeGain(currentCurrency))
        }

        // Setup home button
        homeButton.setOnClickListener {
            finish()  // This will return to the previous activity
        }
    }

    private fun loadPrestigeData() {
        val sharedPreferences = getSharedPreferences("PrestigeData", Context.MODE_PRIVATE)
        
        // Check if this is the first time (no saved data)
        if (!sharedPreferences.contains("vertexEssence")) {
            // Initialize with 100 vertex essence
            sharedPreferences.edit()
                .putInt("vertexEssence", 100)
                .apply()
        }
        
        vertexEssence = sharedPreferences.getInt("vertexEssence", 100)
        updateVertexEssenceDisplay()
    }

    private fun updateVertexEssenceDisplay() {
        vertexEssenceText.text = "Vertex Essence: $vertexEssence"
    }

    private fun setupSkillTree() {
        skillTreeView.removeAllViews()
        
        // Calculate center of screen
        val centerX = resources.displayMetrics.widthPixels / 2f - 150f  // Half button width
        
        val nodePositions = mapOf(
            // Center starting node
            "start_dots" to Pair(centerX, 100f),
            
            // First tier upgrades below start_dots
            "base_speed" to Pair(centerX - 200f, 450f),  // Left branch
            "dot_value" to Pair(centerX + 200f, 450f),   // Right branch
            
            // Speed branch (left side)
            "momentum" to Pair(centerX - 400f, 800f),    // Far left
            "auto_dots" to Pair(centerX - 0f, 800f),     // Center left
            
            // Value branch (right side)
            "lucky_dots" to Pair(centerX + 0f, 800f),    // Center right
            "multi_lines" to Pair(centerX + 400f, 800f)  // Far right
        )
        
        skillTreeManager.getSkillTree().forEach { node ->
            nodePositions[node.id]?.let { (x, y) ->
                skillTreeView.addSkillNode(node, x, y)
            }
        }
        
        skillTreeView.setOnNodeClickListener { node ->
            showSkillDetails(node)
        }
    }

    private fun showSkillDetails(node: SkillNode) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(node.name)
            .setMessage("""
                ${node.description}
                Level: ${node.level}/${node.maxLevel}
                Cost: ${node.getCurrentCost()} Vertex Essence
            """.trimIndent())
            .setPositiveButton("Upgrade") { _, _ ->
                if (node.canBePurchased(vertexEssence, skillTreeManager.getUnlockedNodes())) {
                    vertexEssence = skillTreeManager.purchaseNode(node.id, vertexEssence)
                    updateVertexEssenceDisplay()
                    skillTreeView.updateNode(node.id)
                } else {
                    Toast.makeText(this, "Cannot upgrade this skill yet", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Close", null)
            .create()
        dialog.show()
    }

    private fun calculatePrestigeGain(currentCurrency: Double): Int {
        return (sqrt(currentCurrency / 1e6)).toInt()
    }

    private fun performPrestige(essenceGain: Int) {
        val gameManager = GameManager(this)
        gameManager.setCurrency(0.0)  // Reset currency
        vertexEssence += essenceGain
        
        // Save prestige data
        getSharedPreferences("PrestigeData", Context.MODE_PRIVATE)
            .edit()
            .putInt("vertexEssence", vertexEssence)
            .apply()
            
        updateVertexEssenceDisplay()
        Toast.makeText(this, "Gained $essenceGain Vertex Essence!", Toast.LENGTH_LONG).show()
    }
} 