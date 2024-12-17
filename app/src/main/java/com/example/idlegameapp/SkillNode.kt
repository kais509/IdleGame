package com.example.idlegameapp

data class SkillNode(
    val id: String,
    val name: String,
    val description: String,
    var level: Int = 0,
    val maxLevel: Int = 5,
    val baseCost: Int = 1,
    val costMultiplier: Float = 1.5f,
    val prerequisites: List<String> = listOf()
) {
    fun getCurrentCost(): Int {
        return if (level >= maxLevel) {
            Int.MAX_VALUE
        } else {
            (baseCost * Math.pow(costMultiplier.toDouble(), level.toDouble())).toInt()
        }
    }

    fun canBePurchased(availablePoints: Int, unlockedNodes: Set<String>): Boolean {
        return level < maxLevel && 
               getCurrentCost() <= availablePoints && 
               (prerequisites.isEmpty() || prerequisites.all { it in unlockedNodes })
    }
} 