package com.example.omega.model


object Category {
    val DEFAULT_CATEGORIES = listOf(
        "Food",
        "Transportation",
        "Housing",
        "Entertainment",
        "Shopping",
        "Utilities",
        "Healthcare",
        "Education",
        "Travel",
        "Other"
    )

    // Map to store category colors for consistent coloring
    val CATEGORY_COLORS = mapOf(
        "Food" to "#4CAF50",
        "Transportation" to "#2196F3",
        "Housing" to "#FFC107",
        "Entertainment" to "#E91E63",
        "Shopping" to "#9C27B0",
        "Utilities" to "#FF5722",
        "Healthcare" to "#795548",
        "Education" to "#607D8B",
        "Travel" to "#009688",
        "Other" to "#673AB7"
    )

    // Get color for a category, defaulting to a color if not found
    fun getColorForCategory(category: String): Int {
        val colorHex = CATEGORY_COLORS[category] ?: "#607D8B"
        return android.graphics.Color.parseColor(colorHex)
    }
}
