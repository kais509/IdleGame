package com.example.idlegameapp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import android.graphics.Color
import com.google.android.material.button.MaterialButton
import android.view.ViewGroup
import android.content.res.ColorStateList


class SkillNodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {

    private var skillNode: SkillNode? = null

    init {
        layoutParams = ViewGroup.LayoutParams(300, 300)
        
        // Improve text appearance
        textSize = 18f
        textAlignment = TEXT_ALIGNMENT_CENTER
        setPadding(40, 40, 40, 40)
        
        // Add rounded corners and elevation
        cornerRadius = 40
        elevation = 8f
        
        // Style improvements
        setTextColor(Color.WHITE)
        rippleColor = ColorStateList.valueOf(Color.WHITE)  // White ripple effect
        
        // Default state (locked)
        setBackgroundColor(Color.parseColor("#7B1FA2"))  // Darker purple
        stateListAnimator = null  // Remove default button animation
        
        // Add stroke border
        strokeWidth = 4
        strokeColor = ColorStateList.valueOf(Color.parseColor("#9C27B0"))
    }

    fun setSkillNode(node: SkillNode) {
        skillNode = node
        // Improve text formatting
        text = buildString {
            append(node.name)
            append("\n\n")  // Add more space between name and level
            append("${node.level}/${node.maxLevel}")
        }
        
        if (node.level > 0) {
            // Unlocked state
            setBackgroundColor(Color.parseColor("#388E3C"))  // Darker green
            strokeColor = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            // Locked state
            setBackgroundColor(Color.parseColor("#7B1FA2"))  // Darker purple
            strokeColor = ColorStateList.valueOf(Color.parseColor("#9C27B0"))
        }
    }

    fun getSkillNode(): SkillNode? = skillNode
} 