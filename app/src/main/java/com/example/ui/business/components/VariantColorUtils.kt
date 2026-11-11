package com.example.ui.business.components

import androidx.compose.ui.graphics.Color

object VariantColorUtils {

    /**
     * Splits a variant string safely by commas, dots (·), or semicolons.
     * Prevents empty or whitespace-only elements.
     */
    fun parseVariants(variantsStr: String): List<String> {
        if (variantsStr.isBlank()) return emptyList()
        return variantsStr
            .split(",", "·", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    /**
     * Dynamically maps a variant name to an appropriate Compose Color.
     * If white/light, a border should be rendered around the color dot.
     */
    fun getVariantColor(variantText: String, index: Int = 0): Color {
        val lower = variantText.lowercase()
        return when {
            lower.contains("noir") || lower.contains("black") || lower.contains("sombre") || lower.contains("dark") -> Color(0xFF1E1E1E)
            lower.contains("blanc") || lower.contains("white") || lower.contains("claire") || lower.contains("light") -> Color(0xFFF8F9FA)
            lower.contains("rouge") || lower.contains("red") -> Color(0xFFE53935)
            lower.contains("bleu") || lower.contains("blue") || lower.contains("marine") || lower.contains("nuit") -> Color(0xFF1E88E5)
            lower.contains("vert") || lower.contains("green") || lower.contains("émeraude") -> Color(0xFF4CAF50)
            lower.contains("rose") || lower.contains("pink") -> Color(0xFFEC407A)
            lower.contains("jaune") || lower.contains("yellow") -> Color(0xFFFBC02D)
            lower.contains("orange") -> Color(0xFFFB8C00)
            lower.contains("violet") || lower.contains("purple") -> Color(0xFF8E24AA)
            lower.contains("gris") || lower.contains("grey") || lower.contains("gray") -> Color(0xFF78909C)
            lower.contains("argent") || lower.contains("silver") -> Color(0xFFB0BEC5)
            lower.contains("or") || lower.contains("gold") -> Color(0xFFFFD700)
            lower.contains("titane") || lower.contains("stellaire") -> Color(0xFF546E7A)
            lower.contains("marron") || lower.contains("brun") || lower.contains("brown") -> Color(0xFF795548)
            else -> {
                val fallbackPalette = listOf(
                    Color(0xFF00687A), Color(0xFF712AE2), Color(0xFFEA580C),
                    Color(0xFF0284C7), Color(0xFF059669), Color(0xFFD97706)
                )
                val absHash = kotlin.math.abs(variantText.hashCode() + index)
                fallbackPalette[absHash % fallbackPalette.size]
            }
        }
    }

    /**
     * True if the color is light (e.g., White) and needs a visible outline.
     */
    fun isLightColor(color: Color): Boolean {
        return color.red > 0.85f && color.green > 0.85f && color.blue > 0.85f
    }
}
