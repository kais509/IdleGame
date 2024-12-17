package com.example.idlegameapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

interface DotCollisionListener {
    fun onDotHitEnd()
    fun onDotHitVertex()
}

data class Line(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val dots: MutableList<Dot> = mutableListOf()
)

data class Dot(
    var position: Float, // Between 0.0 and 1.0
    var direction: Int // 1 for forward, -1 for backward
)

class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var dotCollisionListener: DotCollisionListener? = null
    private val lines = mutableListOf<Line>()
    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
    }
    private val baseSpeed = 0.2f
    private var speedLevel = 0
    private val baseRevolutionTime = 5f // Time in seconds for one revolution
    
    // Calculate the speed multiplier based on number of sides
    private val polygonSpeedMultiplier get() = currentSides / 3f // Normalize to triangle
    
    // Adjust speed for both upgrades and polygon size
    val dotSpeed get() = (baseSpeed * 
        Math.pow(1.1, speedLevel.toDouble()).toFloat() * 
        polygonSpeedMultiplier * 
        (1f + baseSpeedBonus))
    private var centerX = 500f
    private var centerY = 400f
    private var radius = 300f
    private var numDots = 0 // Track number of dots
    private var currentSides = 2 // Start with triangle
    private var baseSpeedBonus = 0f
    private var currencyMultiplier = 1f
    private var sideLength: Float = 0f

    init {
        // Initialize with triangle but no dots
        addLine()
    }

    fun addDotToLine() {
        // Add dot to the first line
        if (lines.isNotEmpty()) {
            val dot = Dot(position = 0f, direction = 1)
            lines[0].dots.add(dot)
            numDots++
        }
    }

    fun addCurrency(amount: Double) {
        // Logic to modify animations based on currency (if needed)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        lines.forEach { line ->
            canvas.drawLine(line.startX, line.startY, line.endX, line.endY, paint)
            line.dots.forEach { dot ->
                val (x, y) = calculateDotPosition(line, dot)
                canvas.drawCircle(x, y, 10f, paint)
            }
        }
    }

    fun updateDots(deltaTime: Float) {
        updateDotPositions(deltaTime)
    }

    private fun updateDotPositions(deltaTime: Float) {
        for (line in lines) {
            val dotsToMove = ArrayList(line.dots)
            line.dots.clear()
            
            for (dot in dotsToMove) {
                dot.position += dotSpeed * deltaTime * dot.direction
                
                if (dot.position >= 1f) {
                    // Move to next line
                    val currentLineIndex = lines.indexOf(line)
                    val nextLineIndex = (currentLineIndex + 1) % lines.size
                    dot.position = 0f
                    lines[nextLineIndex].dots.add(dot)
                    dotCollisionListener?.onDotHitVertex()
                } else if (dot.position < 0f) {
                    // Move to previous line
                    val currentLineIndex = lines.indexOf(line)
                    val prevLineIndex = if (currentLineIndex == 0) lines.size - 1 else currentLineIndex - 1
                    dot.position = 1f
                    lines[prevLineIndex].dots.add(dot)
                    dotCollisionListener?.onDotHitVertex()
                } else {
                    // Keep dot on current line
                    line.dots.add(dot)
                }
            }
        }
        invalidate()
    }

    fun addLine() {
        // Store current dots before clearing lines
        val currentDots = lines.flatMap { it.dots }.toList()
        
        // Clear existing lines
        lines.clear()
        
        // Increment sides for new polygon
        currentSides++
        
        // Create new polygon with more sides
        for (i in 0 until currentSides) {
            val startAngle = 2.0 * Math.PI * i / currentSides
            val endAngle = 2.0 * Math.PI * ((i + 1) % currentSides) / currentSides
            
            val startX = centerX + radius * Math.cos(startAngle).toFloat()
            val startY = centerY + radius * Math.sin(startAngle).toFloat()
            val endX = centerX + radius * Math.cos(endAngle).toFloat()
            val endY = centerY + radius * Math.sin(endAngle).toFloat()
            
            lines.add(Line(startX, startY, endX, endY))
        }
        
        // Redistribute existing dots evenly across the new shape
        if (currentDots.isNotEmpty()) {
            val dotsPerLine = currentDots.size / currentSides
            val remainingDots = currentDots.size % currentSides
            
            for (i in 0 until currentSides) {
                val numDotsForThisLine = dotsPerLine + (if (i < remainingDots) 1 else 0)
                for (j in 0 until numDotsForThisLine) {
                    lines[i].dots.add(Dot(position = 0.5f, direction = 1))
                }
            }
        }
    }

    private fun calculateDotPosition(line: Line, dot: Dot): Pair<Float, Float> {
        val x = line.startX + (line.endX - line.startX) * dot.position
        val y = line.startY + (line.endY - line.startY) * dot.position
        return Pair(x, y)
    }

    fun setDotCollisionListener(listener: DotCollisionListener) {
        dotCollisionListener = listener
    }

    // Update function to increase speed level
    fun increaseSpeed() {
        speedLevel++
    }

    fun resetGame() {
        lines.clear()
        currentSides = 3
        speedLevel = 0
        numDots = 0
        addLine() // Recreate initial triangle
        invalidate()
    }

    fun setBaseSpeedBonus(bonus: Float) {
        baseSpeedBonus = bonus
    }

    fun setCurrencyMultiplier(multiplier: Float) {
        currencyMultiplier = multiplier
    }

    fun getDotsCount(): Int = numDots
    
    fun getCurrentSides(): Int = currentSides
    
    fun getBaseSpeed(): Float = baseSpeed

    fun getRadius(): Float = radius

    fun getSideLength(): Float = sideLength

    private fun createPolygon() {
        val points = mutableListOf<PointF>()
        val angleStep = (2 * Math.PI / currentSides)
        
        for (i in 0 until currentSides) {
            val angle = i * angleStep
            val x = radius * cos(angle).toFloat() + centerX
            val y = radius * sin(angle).toFloat() + centerY
            points.add(PointF(x, y))
        }

        // Calculate side length between consecutive points
        if (points.size >= 2) {
            val p1 = points[0]
            val p2 = points[1]
            val dx = p2.x - p1.x
            val dy = p2.y - p1.y
            sideLength = sqrt(dx * dx + dy * dy)
            Log.d("GameView", "Calculated side length: $sideLength")
            Log.d("GameView", "Points: (${p1.x}, ${p1.y}) to (${p2.x}, ${p2.y})")
        }

        lines.clear()
        for (i in points.indices) {
            val start = points[i]
            val end = points[(i + 1) % points.size]
            lines.add(Line(
                startX = start.x,
                startY = start.y,
                endX = end.x,
                endY = end.y
            ))
        }
    }

    // Make sure this is called after the view is laid out
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = min(w, h) / 3f  // Use 1/3 of the smallest dimension
        createPolygon()  // Recreate polygon with new dimensions
    }
}
