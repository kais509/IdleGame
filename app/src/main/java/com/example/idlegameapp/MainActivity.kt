package com.example.idlegameapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.example.idlegameapp.GameView
import com.example.idlegameapp.DotCollisionListener
import java.text.DecimalFormat
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), DotCollisionListener {
    private var currency: Double = 10000000.0 // Total currency
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
            currency = (currency - flatUpgradeCost).roundToInt().toDouble()
            gameView.addDotToLine()
            flatUpgradeCost = (flatUpgradeCost * 1.5).roundToInt().toDouble()
            updateUI()
        }
    }

    private fun buyMultiplierUpgrade() {
        if (currency >= multiplierUpgradeCost) {
            currency = (currency - multiplierUpgradeCost).roundToInt().toDouble()
            gameView.increaseSpeed()
            multiplierUpgradeCost = (multiplierUpgradeCost * 2.0).roundToInt().toDouble()
            updateUI()
        }
    }

    private fun buyExponentUpgrade() {
        if (currency >= exponentUpgradeCost) {
            currency = (currency - exponentUpgradeCost).roundToInt().toDouble()
            gameView.addLine()
            exponentUpgradeCost = (exponentUpgradeCost * 2.0).roundToInt().toDouble()
            updateUI()
        }
    }

    private fun updateUI() {
        val formatter = DecimalFormat("#,###") // Changed to show no decimals
        currencyTextView.text = "Currency: ${formatter.format(currency.roundToInt())}"
        findViewById<Button>(R.id.flatUpgradeButton).text = 
            "Add Dot (${formatter.format(flatUpgradeCost.roundToInt())})"
        findViewById<Button>(R.id.multiplierUpgradeButton).text = 
            "Speed x1.1 (${formatter.format(multiplierUpgradeCost.roundToInt())})"
        findViewById<Button>(R.id.exponentUpgradeButton).text = 
            "New Line (${formatter.format(exponentUpgradeCost.roundToInt())})"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop the loop when the activity is destroyed
    }

    override fun onDotHitEnd() {
        currency = (currency + 1).roundToInt().toDouble()
        updateUI()
    }

    override fun onDotHitVertex() {
        currency = (currency + 1).roundToInt().toDouble()
        updateUI()
    }
}
