package com.example.idlegameapp

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout

class SkillTreeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val nodes = mutableMapOf<String, SkillNodeView>()
    private var onNodeClickListener: ((SkillNode) -> Unit)? = null

    init {
        // Change to black background
        setBackgroundColor(Color.BLACK)
        minimumWidth = resources.displayMetrics.widthPixels
        minimumHeight = 1000
    }

    fun addSkillNode(node: SkillNode, x: Float, y: Float) {
        val nodeView = SkillNodeView(context)
        nodeView.setSkillNode(node)
        
        // Use FrameLayout.LayoutParams to position absolutely
        val params = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = x.toInt()
        params.topMargin = y.toInt()
        nodeView.layoutParams = params
        
        nodeView.setOnClickListener {
            nodeView.getSkillNode()?.let { node ->
                onNodeClickListener?.invoke(node)
            }
        }

        nodes[node.id] = nodeView
        addView(nodeView)
    }

    fun setOnNodeClickListener(listener: (SkillNode) -> Unit) {
        onNodeClickListener = listener
    }

    fun updateNode(nodeId: String) {
        nodes[nodeId]?.getSkillNode()?.let { node ->
            nodes[nodeId]?.setSkillNode(node)
        }
    }
} 