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
    private val baseSpeed = 0.1f  // Base speed value
    private var speedLevel = 0  // Track the number of speed upgrades
    private val dotSpeed get() = baseSpeed * Math.pow(1.1, speedLevel.toDouble()).toFloat()

    init {
        // Initialize with one line
        addLine()
    }

    fun addLine() {
        val newLine = Line(
            startX = 100f, startY = 400f, endX = 900f, endY = 400f
        )
        lines.add(newLine)
    }

    fun addDotToLine() {
        if (lines.isNotEmpty()) {
            val line = lines.last()
            line.dots.add(Dot(position = 0.5f, direction = 1))
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
        lines.forEach { line ->
            line.dots.forEach { dot ->
                dot.position += dot.direction * dotSpeed * deltaTime
                if (dot.position <= 0f || dot.position >= 1f) {
                    dot.direction *= -1 // Reverse direction
                    dotCollisionListener?.onDotHitEnd()
                }
            }
        }
        invalidate() // Redraw the view
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
