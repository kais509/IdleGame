package com.example.idlegameapp

class SkillTreeManager {
    private val nodes = mutableMapOf<String, SkillNode>()
    private val unlockedNodes = mutableSetOf<String>()

    init {
        // Root node (no prerequisites)
        nodes["start_dots"] = SkillNode(
            id = "start_dots",
            name = "Starting Dots",
            description = "Start with additional dots",
            baseCost = 1,
            prerequisites = listOf()  // No prerequisites
        )

        // Tier 2 nodes (require start_dots)
        nodes["base_speed"] = SkillNode(
            id = "base_speed",
            name = "Base Speed",
            description = "Increase base movement speed by 10%",
            baseCost = 2,
            prerequisites = listOf("start_dots")
        )

        nodes["currency_gain"] = SkillNode(
            id = "currency_gain",
            name = "Currency Gain",
            description = "Increase currency per hit by 1",
            baseCost = 3,
            prerequisites = listOf("start_dots")
        )
    }

    fun getNode(id: String): SkillNode? = nodes[id]

    fun purchaseNode(id: String, availablePoints: Int): Int {
        val node = nodes[id] ?: return availablePoints
        if (node.canBePurchased(availablePoints, unlockedNodes)) {
            val cost = node.getCurrentCost()
            node.level++
            unlockedNodes.add(node.id)
            return availablePoints - cost
        }
        return availablePoints
    }

    fun loadState(state: Map<String, Int>) {
        state.forEach { (id, level) ->
            nodes[id]?.let { node ->
                node.level = level
                if (level > 0) unlockedNodes.add(id)
            }
        }
    }

    fun getState(): Map<String, Int> {
        return nodes.mapValues { it.value.level }
    }

    fun getUnlockedNodes(): Set<String> = unlockedNodes.toSet()
} 