package me.busta.barksaccountant.model

enum class IngredientUnit(val symbol: String) {
    GRAMS("g"),
    KILOGRAMS("kg"),
    MILLILITERS("ml"),
    LITERS("l"),
    UNITS("u");

    companion object {
        fun fromName(name: String?): IngredientUnit =
            entries.firstOrNull { it.name == name } ?: GRAMS
    }
}
