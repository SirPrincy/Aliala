package com.example.ui.business.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.VioletSecondary
import com.example.ui.theme.WarningAmber

internal data class TransitaireColors(
    val isDark: Boolean,
    val pageBg: Color,
    val cardBg: Color,
    val elevatedCardBg: Color,
    val innerCardBg: Color,
    val subtleBg: Color,
    val inputBg: Color,
    val toggleTrackBg: Color,
    val badgeNeutralBg: Color,
    val border: Color,
    val hairline: Color,
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
    val warningSoftBg: Color,
    val roseAccent: Color,
    val roseSoftBg: Color
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
            elevatedCardBg = if (isDark) Color(0xFF233046) else Color(0xFFFFFFFF),
            innerCardBg = if (isDark) Color(0xFF263349) else Color(0xFFF3F6FA),
            subtleBg = if (isDark) Color(0xFF141E30) else Color(0xFFF8FAFC),
            inputBg = if (isDark) Color(0xFF172235) else Color(0xFFF1F5F9),
            toggleTrackBg = if (isDark) Color(0xFF162032) else Color(0xFFEFF3F8),
            badgeNeutralBg = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
            border = if (isDark) Color(0xFF2E3C54) else Color(0xFFE5EAF2),
            hairline = if (isDark) Color(0xFF2B384E) else Color(0xFFEEF2F7),
            textPrimary = colorScheme.onSurface,
            textSecondary = colorScheme.onSurfaceVariant,
            cyanAccent = if (isDark) Color(0xFF22D3EE) else Color(0xFF0891B2),
            cyanSolidBg = CyanPrimary,
            cyanSoftBg = if (isDark) Color(0xFF0B3646) else Color(0xFFE6FAFD),
            violetAccent = if (isDark) Color(0xFFA78BFA) else VioletSecondary,
            violetSolidBg = VioletSecondary,
            violetSoftBg = if (isDark) Color(0xFF2E165C) else Color(0xFFF3EEFF),
            expressAccent = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
            expressSoftBg = if (isDark) Color(0xFF0E3E5E) else Color(0xFFE5F4FD),
            dangerAccent = if (isDark) Color(0xFFF87171) else DangerRed,
            dangerSoftBg = if (isDark) Color(0xFF47151A) else Color(0xFFFEECEE),
            successAccent = if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A),
            successSoftBg = if (isDark) Color(0xFF0B3520) else SuccessGreen.copy(alpha = 0.13f),
            warningAccent = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
            warningSoftBg = if (isDark) Color(0xFF432308) else WarningAmber.copy(alpha = 0.15f),
            roseAccent = if (isDark) Color(0xFFF472B6) else Color(0xFFDB2777),
            roseSoftBg = if (isDark) Color(0xFF47132E) else Color(0xFFFDF0F6)
        )
    }
}

internal fun pastelToneForKey(key: String, colors: TransitaireColors): Pair<Color, Color> {
    val palette = listOf(
        colors.cyanSoftBg to colors.cyanAccent,
        colors.violetSoftBg to colors.violetAccent,
        colors.expressSoftBg to colors.expressAccent,
        colors.successSoftBg to colors.successAccent,
        colors.roseSoftBg to colors.roseAccent
    )
    val idx = kotlin.math.abs(key.hashCode()) % palette.size
    return palette[idx]
}
