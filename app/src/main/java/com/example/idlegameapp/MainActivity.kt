package com.example.idlegameapp


import android.icu.text.DecimalFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.content.Intent
import android.content.Context
import kotlin.math.roundToInt
import kotlin.math.tan
import android.util.Log

class MainActivity : AppCompatActivity(), DotCollisionListener {
    private var currency: Double = 100000.0 // Total currency
    private var multiplierUpgradeCost: Double = 50.0
    private var exponentUpgradeCost: Double = 200.0

    private var flatUpgradeCost: Double = 10.0

    private lateinit var currencyTextView: TextView
    private lateinit var currencyPerSecondView: TextView
    private lateinit var flatUpgradeButton: Button
    private lateinit var multiplierUpgradeButton: Button
    private lateinit var exponentUpgradeButton: Button
    private lateinit var gameView: GameView

    private val PRESTIGE_REQUEST_CODE = 1

    private var currencyPerHit = 1 // Base currency per vertex hit
    private var currencyMultiplierCost = 150.0
    private var currencyMultiplierLevel = 0

    private val handler = Handler(Looper.getMainLooper())

    private var lastCurrency = 0.0
    private var lastCurrencyPerSecond = 0.0
    private val currencyTracker = Handler(Looper.getMainLooper())
    private val currencyTrackerRunnable = object : Runnable {
        override fun run() {
            val currentCurrency = currency
            val earned = currentCurrency - lastCurrency
            
            // Only update if we earned something (prevents negative values when spending)
            if (earned > 0) {
                lastCurrencyPerSecond = earned
            }
            
            lastCurrency = currentCurrency
            updateUI()
            currencyTracker.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currencyTextView = findViewById(R.id.currencyTextView)
        currencyPerSecondView = findViewById(R.id.currencyPerSecondView)
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

        findViewById<Button>(R.id.autoClickerUpgradeButton).setOnClickListener {
            buyCurrencyMultiplierUpgrade()
        }

        // Add navigation button click listeners
        findViewById<Button>(R.id.prestigeButton).setOnClickListener {
            val intent = Intent(this, PrestigeActivity::class.java)
            intent.putExtra("currentCurrency", currency)
            startActivityForResult(intent, PRESTIGE_REQUEST_CODE)
        }

        findViewById<Button>(R.id.statsButton).setOnClickListener {
            // Stats screen navigation (to be implemented)
        }

        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            // Settings screen navigation (to be implemented)
        }

        // Start tracking currency per second
        currencyTracker.post(currencyTrackerRunnable)
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

    private fun buyCurrencyMultiplierUpgrade() {
        if (currency >= currencyMultiplierCost) {
            currency = (currency - currencyMultiplierCost).roundToInt().toDouble()
            currencyMultiplierLevel++
            currencyPerHit++
            currencyMultiplierCost = (currencyMultiplierCost * 1.8).roundToInt().toDouble()
            updateUI()
        }
    }

    private fun calculateCurrencyPerSecond(): Float {
        val dotsCount = gameView.getDotsCount()
        val speed = gameView.dotSpeed
        val sidesCount = gameView.getCurrentSides()
        
        // Each dot hits a vertex when it completes a line segment
        // Speed is in units per second (how many line segments completed per second)
        val hitsPerSecondPerDot = speed
        
        // Total currency per second = hits per second * number of dots * currency per hit
        val result = hitsPerSecondPerDot * dotsCount * currencyPerHit
        
        Log.d("CurrencyCalc", """
            Dots: $dotsCount
            Speed: $speed
            Sides: $sidesCount
            Hits/sec/dot: $hitsPerSecondPerDot
            Final result: $result
        """.trimIndent())
        
        return result
    }

    private fun updateUI() {
        val wholeNumberFormatter = DecimalFormat("#,###")
        val decimalFormatter = DecimalFormat("#,##0.00")
        
        currencyTextView.text = "Currency: ${wholeNumberFormatter.format(currency.roundToInt())}"
        
        val currencyPerSecond = calculateCurrencyPerSecond()
        // Add safety check for display
        val displayValue = if (currencyPerSecond.isFinite()) currencyPerSecond else 0.0
        currencyPerSecondView.text = "(${decimalFormatter.format(displayValue)}/sec)"
        
        findViewById<Button>(R.id.flatUpgradeButton).text = 
            "Add Dot\n(${wholeNumberFormatter.format(flatUpgradeCost.roundToInt())})"
        findViewById<Button>(R.id.multiplierUpgradeButton).text = 
            "Speed x1.1\n(${wholeNumberFormatter.format(multiplierUpgradeCost.roundToInt())})"
        findViewById<Button>(R.id.exponentUpgradeButton).text = 
            "New Line\n(${wholeNumberFormatter.format(exponentUpgradeCost.roundToInt())})"
        findViewById<Button>(R.id.autoClickerUpgradeButton).text = 
            "+${currencyPerHit}/hit\n(${wholeNumberFormatter.format(currencyMultiplierCost.roundToInt())})"
    }

    override fun onDotHitVertex() {
        currency = (currency + currencyPerHit).roundToInt().toDouble()
        updateUI()
    }

    override fun onDotHitEnd() {
        currency = (currency + currencyPerHit).roundToInt().toDouble()
        updateUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PRESTIGE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Reset game progress
            currency = 0.0
            flatUpgradeCost = 10.0
            multiplierUpgradeCost = 50.0
            exponentUpgradeCost = 200.0
            gameView.resetGame()
            
            // Apply meta upgrades
            val prefs = getSharedPreferences("PrestigeData", Context.MODE_PRIVATE)
            val startingDots = prefs.getInt("startingDots", 0)
            val baseSpeedBonus = prefs.getInt("baseSpeed", 0)
            val currencyMultiplier = prefs.getInt("currencyMultiplier", 0)
            
            // Apply starting dots
            repeat(startingDots) {
                gameView.addDotToLine()
            }
            
            // Apply speed bonus
            gameView.setBaseSpeedBonus(baseSpeedBonus * 0.1f)
            
            // Set currency multiplier
            gameView.setCurrencyMultiplier(1.0f + (currencyMultiplier * 0.2f))
            
            updateUI()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        currencyTracker.removeCallbacks(currencyTrackerRunnable)
    }
}
