package com.example.ui.business.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

internal data class TransitaireColors(
    val isDark: Boolean,
    val pageBg: Color,
    val cardBg: Color,
    val innerCardBg: Color,
    val subtleBg: Color,
    val inputBg: Color,
    val toggleTrackBg: Color,
    val badgeNeutralBg: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val cyanAccent: Color,
    val cyanSolidBg: Color,
    val cyanSoftBg: Color,
    val violetAccent: Color,
    val violetSolidBg: Color,
    val violetSoftBg: Color,
    val expressAccent: Color,
    val expressSoftBg: Color,
    val dangerAccent: Color,
    val dangerSoftBg: Color,
    val successAccent: Color,
    val successSoftBg: Color,
    val warningAccent: Color,
    val warningSoftBg: Color
)

@Composable
internal fun rememberTransitaireColors(): TransitaireColors {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f
    return remember(isDark, colorScheme) {
        TransitaireColors(
            isDark = isDark,
            pageBg = colorScheme.background,
            cardBg = colorScheme.surface,
            innerCardBg = if (isDark) Color(0xFF263349) else Color(0xFFF2F3FF),
            subtleBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
            inputBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF2F3FF),
            toggleTrackBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
            badgeNeutralBg = if (isDark) Color(0xFF334155) else Color(0xFFEAEDFF),
            border = colorScheme.outline,
            textPrimary = colorScheme.onSurface,
            textSecondary = colorScheme.onSurfaceVariant,
            cyanAccent = if (isDark) Color(0xFF22D3EE) else Color(0xFF00687A),
            cyanSolidBg = Color(0xFF06B6D4),
            cyanSoftBg = if (isDark) Color(0xFF083344) else Color(0xFFE0F7FA),
            violetAccent = if (isDark) Color(0xFFA78BFA) else Color(0xFF712AE2),
            violetSolidBg = if (isDark) Color(0xFF7C3AED) else Color(0xFF712AE2),
            violetSoftBg = if (isDark) Color(0xFF3B0764) else Color(0xFFF3E8FF),
            expressAccent = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
            expressSoftBg = if (isDark) Color(0xFF0C4A6E) else Color(0xFFE0F2FE),
            dangerAccent = if (isDark) Color(0xFFF87171) else Color(0xFFBA1A1A),
            dangerSoftBg = if (isDark) Color(0xFF450A0A) else Color(0xFFFFDAD6),
            successAccent = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D),
            successSoftBg = if (isDark) Color(0xFF052E16) else Color(0xFFDCFCE7),
            warningAccent = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
            warningSoftBg = if (isDark) Color(0xFF451A03) else Color(0xFFFEF3C7)
        )
    }
}
