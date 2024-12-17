package com.example.idlegameapp

data class SkillNode(
    val id: String,
    val name: String,
    val description: String,
    var level: Int = 0,
    val maxLevel: Int = 1,
    val prerequisites: List<String> = emptyList(),
    val cost: Int = 0,
    val baseCost: Int = 0
) {
    fun getCurrentCost(): Int = 0
    
    fun canBePurchased(essence: Int, unlockedNodes: Set<String>): Boolean {
        return level < maxLevel && 
               prerequisites.all { it in unlockedNodes }
    }
} 