package com.example.idlegameapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.idlegameapp.MainActivity

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
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
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
    private val dotSpeed get() = baseSpeed * 
        Math.pow(1.1, speedLevel.toDouble()).toFloat() * 
        polygonSpeedMultiplier
    private val centerX = 500f
    private val centerY = 400f
    private val radius = 300f
    private var numDots = 0 // Track number of dots
    private var currentSides = 2 // Start with triangle

    init {
        // Initialize with triangle but no dots
        addLine()
    }

    fun addDotToLine() {
        if (lines.isNotEmpty() && numDots == 0) {
            // Add first dot to the first line
            lines[0].dots.add(Dot(position = 0.5f, direction = 1))
            numDots++
        } else if (lines.isNotEmpty()) {
            // Add new dot to next line that doesn't have a dot
            for (i in 0 until lines.size) {
                if (lines[i].dots.isEmpty()) {
                    lines[i].dots.add(Dot(position = 0.5f, direction = 1))
                    numDots++
                    break
                }
            }
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
        lines.forEachIndexed { lineIndex, line ->
            val dotsToMove = ArrayList(line.dots) // Create copy to avoid concurrent modification
            
            line.dots.clear() // Clear existing dots
            
            dotsToMove.forEach { dot ->
                dot.position += dot.direction * dotSpeed * deltaTime
                
                if (dot.position >= 1f) {
                    // Move to next line
                    val nextLineIndex = (lineIndex + 1) % lines.size
                    dot.position = 0f // Reset position for new line
                    lines[nextLineIndex].dots.add(dot)
                    dotCollisionListener?.onDotHitVertex()
                } else if (dot.position < 0f) {
                    // Move to previous line
                    val prevLineIndex = if (lineIndex == 0) lines.size - 1 else lineIndex - 1
                    dot.position = 1f // Reset position for new line
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
}
