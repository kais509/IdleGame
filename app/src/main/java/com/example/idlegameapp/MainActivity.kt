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

    private var totalCurrencyEarned = 0f

    private var lastCurrencyLogTime = 0L

    private var flatUpgradeCount = 0
    private var speedUpgradeCount = 0
    private var lineUpgradeCount = 0
    private var currencyMultiplierCount = 0

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
            startActivity(Intent(this, StatsActivity::class.java))
        }

        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Start tracking currency per second
        currencyTracker.post(currencyTrackerRunnable)

        totalCurrencyEarned = getSharedPreferences("GameData", MODE_PRIVATE)
            .getFloat("totalCurrencyEarned", 0f)
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
            flatUpgradeCount++
            updateUI()
        }
    }

    private fun buyMultiplierUpgrade() {
        if (currency >= multiplierUpgradeCost) {
            currency = (currency - multiplierUpgradeCost).roundToInt().toDouble()
            gameView.increaseSpeed()
            multiplierUpgradeCost = (multiplierUpgradeCost * 2.0).roundToInt().toDouble()
            speedUpgradeCount++
            updateUI()
        }
    }

    private fun buyExponentUpgrade() {
        if (currency >= exponentUpgradeCost) {
            currency = (currency - exponentUpgradeCost).roundToInt().toDouble()
            gameView.addLine()
            exponentUpgradeCost = (exponentUpgradeCost * 2.0).roundToInt().toDouble()
            lineUpgradeCount++
            updateUI()
        }
    }

    private fun buyCurrencyMultiplierUpgrade() {
        if (currency >= currencyMultiplierCost) {
            currency = (currency - currencyMultiplierCost).roundToInt().toDouble()
            currencyMultiplierLevel++
            currencyPerHit++
            currencyMultiplierCost = (currencyMultiplierCost * 1.8).roundToInt().toDouble()
            currencyMultiplierCount++
            updateUI()
        }
    }

    private fun calculateCurrencyPerSecond(): Float {
        val dotsCount = gameView.getDotsCount()
        val baseSpeed = gameView.getBaseSpeed()
        val speedLevel = gameView.speedLevel
        val polygonMultiplier = gameView.getCurrentSides() / 3f
        val deltaTime = 0.016f  // Matching GameView's update interval
        val updatesPerSecond = 60f  // Based on the 16ms update interval
        
        // Calculate movement the same way GameView does
        val movementPerUpdate = baseSpeed * Math.pow(1.1, speedLevel.toDouble()).toFloat() * 
            polygonMultiplier * deltaTime
        val linesPerSecond = movementPerUpdate * updatesPerSecond  // This is our actualDistancePerSecond
        
        // Only log every 2 seconds
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCurrencyLogTime >= 2000) {
            Log.d("CurrencyCalc", """
                Currency Calculation:
                - Dots: $dotsCount
                - Movement per update: ${movementPerUpdate * 100}% of line
                - Lines completed per second: $linesPerSecond
                - Currency per hit: $currencyPerHit
                """.trimIndent())
        }
        
        // Each dot completes 'linesPerSecond' lines per second
        // Each line has currentSides vertices
        // Each vertex hit gives currencyPerHit currency
        val hitsPerSecond = linesPerSecond * dotsCount
        val result = hitsPerSecond * currencyPerHit
        
        if (currentTime - lastCurrencyLogTime >= 2000) {
            Log.d("CurrencyCalc", """
                Final calculation:
                - Hits per second: $hitsPerSecond
                - Currency per second: $result
                """.trimIndent())
            lastCurrencyLogTime = currentTime
        }
        
        return result
    }

    private fun updateUI() {
        val wholeNumberFormatter = DecimalFormat("#,###")
        val decimalFormatter = DecimalFormat("#,##0.00")
        
        currencyTextView.text = "Currency: ${wholeNumberFormatter.format(currency.roundToInt())}"
        
        val currencyPerSecond = calculateCurrencyPerSecond()
        val displayValue = if (currencyPerSecond.isFinite()) currencyPerSecond else 0.0
        currencyPerSecondView.text = "(${decimalFormatter.format(displayValue)}/sec)"
        
        findViewById<Button>(R.id.flatUpgradeButton).text = 
            "Add Dot ($flatUpgradeCount)\n(${wholeNumberFormatter.format(flatUpgradeCost.roundToInt())})"
        findViewById<Button>(R.id.multiplierUpgradeButton).text = 
            "Speed x1.1 ($speedUpgradeCount)\n(${wholeNumberFormatter.format(multiplierUpgradeCost.roundToInt())})"
        findViewById<Button>(R.id.exponentUpgradeButton).text = 
            "New Line ($lineUpgradeCount)\n(${wholeNumberFormatter.format(exponentUpgradeCost.roundToInt())})"
        findViewById<Button>(R.id.autoClickerUpgradeButton).text = 
            "+${currencyPerHit}/hit ($currencyMultiplierCount)\n(${wholeNumberFormatter.format(currencyMultiplierCost.roundToInt())})"
    }

    override fun onDotHitVertex() {
        val earned = currencyPerHit.toDouble()
        currency = (currency + earned).roundToInt().toDouble()
        totalCurrencyEarned += earned.toFloat()
        
        // Save total currency earned
        getSharedPreferences("GameData", MODE_PRIVATE)
            .edit()
            .putFloat("totalCurrencyEarned", totalCurrencyEarned)
            .apply()
            
        updateUI()
    }

    override fun onDotHitEnd() {
        val earned = currencyPerHit.toDouble()
        currency = (currency + earned).roundToInt().toDouble()
        totalCurrencyEarned += earned.toFloat()
        
        // Save total currency earned
        getSharedPreferences("GameData", MODE_PRIVATE)
            .edit()
            .putFloat("totalCurrencyEarned", totalCurrencyEarned)
            .apply()
            
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
            flatUpgradeCount = 0
            speedUpgradeCount = 0
            lineUpgradeCount = 0
            currencyMultiplierCount = 0
            currencyPerHit = 1
            gameView.resetGame()
            
            // Add initial dot
            gameView.addDotToLine()
            
            updateUI()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        currencyTracker.removeCallbacks(currencyTrackerRunnable)
    }

    private fun updateCurrency(deltaTime: Float) {
        val earned = calculateCurrencyPerSecond() * deltaTime
        currency += earned
        totalCurrencyEarned += earned.toFloat()
        
        // Save total currency earned every time currency updates
        getSharedPreferences("GameData", MODE_PRIVATE)
            .edit()
            .putFloat("totalCurrencyEarned", totalCurrencyEarned)
            .apply()
            
        updateUI()
    }

    // Add this function to save currency when the app is closed
    override fun onPause() {
        super.onPause()
        getSharedPreferences("GameData", MODE_PRIVATE)
            .edit()
            .putFloat("totalCurrencyEarned", totalCurrencyEarned)
            .apply()
    }
}
