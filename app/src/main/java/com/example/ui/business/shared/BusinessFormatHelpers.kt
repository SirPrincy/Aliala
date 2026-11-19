package com.example.ui.business.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.PlusJakartaSans
import java.util.Locale

internal fun formatYuan(amount: Double): String {
    val safe = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
    return if (safe % 1.0 == 0.0) {
        "¥ ${safe.toLong()}"
    } else {
        "¥ ${String.format(Locale.FRANCE, "%.2f", safe)}"
    }
}

internal fun parseDoubleInput(raw: String): Double? {
    val clean = raw.trim().replace(" ", "").replace(",", ".")
    return clean.toDoubleOrNull()
}

internal fun parseLongInput(raw: String): Long? {
    val digits = raw.filter { it.isDigit() }
    return digits.toLongOrNull()
}

internal fun familyEmojiFor(family: String): String {
    val f = family.lowercase()
    return when {
        f.contains("électron") || f.contains("electron") || f.contains("audio") -> "🎧"
        f.contains("mode") || f.contains("vêt") || f.contains("vet") || f.contains("habit") -> "👕"
        f.contains("maison") || f.contains("déco") || f.contains("deco") -> "🏠"
        f.contains("beauté") || f.contains("beaute") || f.contains("cosmé") -> "✨"
        f.contains("sport") || f.contains("fit") -> "🏃"
        f.contains("auto") || f.contains("moto") -> "🚗"
        f.contains("access") || f.contains("montre") || f.contains("bijou") -> "⌚"
        f.contains("informat") || f.contains("pc") || f.contains("tel") -> "📱"
        else -> "📦"
    }
}

internal fun lotStatusLabel(status: String): String = when (status) {
    "DRAFT" -> "Brouillon"
    "ORDERED" -> "Commandé"
    "WAREHOUSE" -> "Chez le fournisseur"
    "GUANGZHOU" -> "Dépôt Chine"
    "FLIGHT" -> "En transit"
    "CUSTOMS" -> "Douane Mada"
    "QUALITY_CONTROL" -> "Contrôle qualité"
    "DELIVERY_READY", "IN_STOCK" -> "En stock"
    "COMPLETED", "SOLD" -> "Clôturé"
    "CATALOGUE" -> "Catalogue"
    else -> "En cours"
}

internal fun lotStatusTone(status: String, colors: TransitaireColors): Pair<Color, Color> = when (status) {
    "DRAFT" -> colors.badgeNeutralBg to colors.textSecondary
    "ORDERED", "WAREHOUSE" -> colors.cyanSoftBg to colors.cyanAccent
    "GUANGZHOU", "FLIGHT" -> colors.expressSoftBg to colors.expressAccent
    "CUSTOMS", "QUALITY_CONTROL" -> colors.warningSoftBg to colors.warningAccent
    "DELIVERY_READY", "IN_STOCK", "COMPLETED", "SOLD" -> colors.successSoftBg to colors.successAccent
    else -> colors.violetSoftBg to colors.violetAccent
}

internal fun isLotInTransit(status: String): Boolean =
    status in setOf("ORDERED", "WAREHOUSE", "GUANGZHOU", "FLIGHT", "CUSTOMS", "QUALITY_CONTROL")

internal fun isLotArrivedOrStocked(status: String): Boolean =
    status in setOf("DELIVERY_READY", "IN_STOCK", "COMPLETED", "SOLD")

@Composable
internal fun HumanStatusPill(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(9999.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
internal fun ProductVisualThumb(
    imageUrl: String,
    productName: String,
    family: String,
    colors: TransitaireColors,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    cornerRadius: Dp = 16.dp
) {
    val (bg, _) = pastelToneForKey(family.ifBlank { productName }, colors)
    val isPhoto = BusinessImageStorage.isPhotoUriOrPath(imageUrl)
    val emoji = when {
        imageUrl.isNotBlank() && !isPhoto && imageUrl.length <= 4 -> imageUrl
        else -> familyEmojiFor(family)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        if (isPhoto) {
            AsyncImage(
                model = imageUrl,
                contentDescription = productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = emoji,
                fontSize = (size.value * 0.44f).sp
            )
        }
    }
}
