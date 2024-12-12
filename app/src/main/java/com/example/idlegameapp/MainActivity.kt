package com.example.idlegameapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.example.idlegameapp.GameView
import com.example.idlegameapp.DotCollisionListener

class MainActivity : AppCompatActivity(), DotCollisionListener {
    private var currency: Double = 100000.0 // Total currency
    private var multiplierUpgradeCost: Double = 50.0
    private var exponentUpgradeCost: Double = 200.0

    private var flatUpgradeCost: Double = 10.0

    private lateinit var currencyTextView: TextView
    private lateinit var currencyPerSecondTextView: TextView
    private lateinit var flatUpgradeButton: Button
    private lateinit var multiplierUpgradeButton: Button
    private lateinit var exponentUpgradeButton: Button
    private lateinit var gameView: GameView

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currencyTextView = findViewById(R.id.currencyTextView)
        flatUpgradeButton = findViewById(R.id.flatUpgradeButton)
        multiplierUpgradeButton = findViewById(R.id.multiplierUpgradeButton)
        exponentUpgradeButton = findViewById(R.id.exponentUpgradeButton)
        gameView = findViewById(R.id.gameView)
        gameView.setDotCollisionListener(this)

        updateUI()
        startIncrementing()

        flatUpgradeButton.setOnClickListener { buyFlatUpgrade() }
        multiplierUpgradeButton.setOnClickListener { buyMultiplierUpgrade() }
        exponentUpgradeButton.setOnClickListener { buyExponentUpgrade() }
    }

    private fun startIncrementing() {
        handler.post(object : Runnable {
            override fun run() {
                gameView.updateDots(0.016f) // Just update dot positions
                handler.postDelayed(this, 16) // Keep the 60 FPS animation
            }
        })
    }

    private fun buyFlatUpgrade() {
        if (currency >= flatUpgradeCost) {
            currency -= flatUpgradeCost
            gameView.addDotToLine() // Add a dot as a visual representation of the upgrade
            flatUpgradeCost *= 1.5 // Cost increases
            updateUI()
        }
    }

    private fun buyMultiplierUpgrade() {
        if (currency >= multiplierUpgradeCost) {
            currency -= multiplierUpgradeCost
            gameView.increaseSpeed()  // Increase speed level
            multiplierUpgradeCost *= 1.5 // Cost increases
            updateUI()
        }
    }

    private fun buyExponentUpgrade() {
        if (currency >= exponentUpgradeCost) {
            currency -= exponentUpgradeCost
            gameView.addLine() // Add a new line for this upgrade
            exponentUpgradeCost *= 3.0 // Cost increases
            updateUI()
        }
    }

    private fun updateUI() {
        currencyTextView.text = "Currency: ${"%.2f".format(currency)}"
        
        flatUpgradeButton.text = "Add Dot (${"%.2f".format(flatUpgradeCost)})"
        multiplierUpgradeButton.text = "Speed x1.1 (${"%.2f".format(multiplierUpgradeCost)})"
        exponentUpgradeButton.text = "New Line (${"%.2f".format(exponentUpgradeCost)})"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop the loop when the activity is destroyed
    }

    override fun onDotHitEnd() {
        currency += 1.0
        updateUI()
    }
}
