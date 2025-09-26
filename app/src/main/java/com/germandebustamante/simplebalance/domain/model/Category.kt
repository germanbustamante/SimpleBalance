package com.germandebustamante.simplebalance.domain.model

/**
 * Predefined expense categories for MVP version.
 * 
 * Each category has:
 * - [code]: Stable identifier for database storage and future compatibility
 * - [nameResId]: String resource ID for localization (EN/ES)
 * - [iconName]: Material icon name for UI display
 * 
 * Future versions will support custom categories while maintaining backward compatibility
 * through the stable [code] field.
 */
enum class Category(
    val code: String,
    val nameResId: Int,
    val iconName: String
) {
    FOOD(
        code = "food",
        nameResId = android.R.string.ok, // TODO: Replace with actual string resources
        iconName = "restaurant"
    ),
    
    TRANSPORT(
        code = "transport", 
        nameResId = android.R.string.ok, // TODO: Replace with actual string resources
        iconName = "directions_car"
    ),
    
    SHOPPING(
        code = "shopping",
        nameResId = android.R.string.ok, // TODO: Replace with actual string resources  
        iconName = "shopping_bag"
    ),
    
    HEALTH(
        code = "health",
        nameResId = android.R.string.ok, // TODO: Replace with actual string resources
        iconName = "health_and_safety"
    ),
    
    LEISURE(
        code = "leisure",
        nameResId = android.R.string.ok, // TODO: Replace with actual string resources
        iconName = "sports_esports"
    ),
    
    HOME(
        code = "home",
        nameResId = android.R.string.ok, // TODO: Replace with actual string resources
        iconName = "home"
    ),
    
    OTHER(
        code = "other",
        nameResId = android.R.string.ok, // TODO: Replace with actual string resources
        iconName = "more_horiz"
    );
    
    companion object {
        /**
         * Find category by stable code. Used when loading from database.
         * @param code The category code to find
         * @return The matching Category, or [OTHER] if not found (for forward compatibility)
         */
        fun fromCode(code: String): Category {
            return values().find { it.code == code } ?: OTHER
        }
        
        /**
         * Get default category for new expenses.
         * @return [OTHER] as the safe default choice
         */
        fun getDefault(): Category = OTHER
        
        /**
         * Get all categories in display order (most common first).
         * @return List of categories ordered for UI display
         */
        fun getDisplayOrder(): List<Category> = listOf(
            FOOD, TRANSPORT, SHOPPING, HEALTH, LEISURE, HOME, OTHER
        )
    }
}