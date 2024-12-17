package com.example.idlegameapp

class SkillTreeManager {
    private val skillTree = mutableListOf<SkillNode>()
    private val unlockedNodes = mutableSetOf<String>()

    init {
        // Initial node
        skillTree.add(SkillNode(
            id = "start_dots",
            name = "Starting Dots",
            description = "Increase starting number of dots",
            maxLevel = 5
        ))
        
        // First tier upgrades
        skillTree.add(SkillNode(
            id = "base_speed",
            name = "Base Speed",
            description = "Increase base movement speed",
            maxLevel = 5,
            prerequisites = listOf("start_dots")
        ))
        
        skillTree.add(SkillNode(
            id = "dot_value",
            name = "Dot Value",
            description = "Increase currency from each dot",
            maxLevel = 5,
            prerequisites = listOf("start_dots")
        ))

        // Speed branch
        skillTree.add(SkillNode(
            id = "momentum",
            name = "Momentum",
            description = "Dots move faster over time",
            maxLevel = 3,
            prerequisites = listOf("base_speed")
        ))

        skillTree.add(SkillNode(
            id = "auto_dots",
            name = "Auto Dots",
            description = "Automatically add dots over time",
            maxLevel = 3,
            prerequisites = listOf("base_speed")
        ))

        // Value branch
        skillTree.add(SkillNode(
            id = "lucky_dots",
            name = "Lucky Dots",
            description = "Chance for dots to give bonus currency",
            maxLevel = 3,
            prerequisites = listOf("dot_value")
        ))

        skillTree.add(SkillNode(
            id = "multi_lines",
            name = "Multi Lines",
            description = "Start with additional lines",
            maxLevel = 3,
            prerequisites = listOf("dot_value")
        ))
    }

    fun getSkillTree(): List<SkillNode> = skillTree

    fun getUnlockedNodes(): Set<String> = unlockedNodes

    fun purchaseNode(nodeId: String, currentEssence: Int): Int {
        val node = skillTree.find { it.id == nodeId } ?: return currentEssence
        if (!node.canBePurchased(currentEssence, unlockedNodes)) return currentEssence
        
        val newEssence = currentEssence - node.getCurrentCost()
        node.level++
        unlockedNodes.add(nodeId)
        return newEssence
    }
}