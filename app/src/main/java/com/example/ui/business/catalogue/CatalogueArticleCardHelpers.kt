package com.example.ui.business.catalogue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.shared.BusinessImageStorage
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import java.util.Locale

@Composable
internal fun ArticleVisualThumbnail(
    imageUrl: String,
    fallbackEmoji: String,
    familyColor: Color,
    productName: String,
    modifier: Modifier = Modifier,
    emojiFontSize: Int = 30
) {
    val isPhoto = BusinessImageStorage.isPhotoUriOrPath(imageUrl)
    val displayEmoji = if (imageUrl.isNotBlank() && !imageUrl.contains("/")) imageUrl else fallbackEmoji

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(
                    familyColor.copy(alpha = 0.16f),
                    MaterialTheme.colorScheme.surfaceVariant
                )
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        if (isPhoto) {
            AsyncImage(
                model = imageUrl,
                contentDescription = productName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = displayEmoji,
                fontSize = emojiFontSize.sp
            )
        }
    }
}

internal fun parseColorSafe(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

internal fun formatYuanPrecise(value: Double): String {
    if (value <= 0.0) return "—"
    return if (value % 1.0 == 0.0) "¥ ${value.toInt()}"
    else String.format(Locale.US, "¥ %.2f", value)
}

@Composable
internal fun ArticleStatusPill(
    statusTag: String,
    stockCount: Int,
    transitCount: Int,
    colors: TransitaireColors
) {
    val (bg, fg, label) = when (statusTag) {
        "En stock" -> Triple(colors.successSoftBg, colors.successAccent, "$stockCount en stock")
        "Stock faible" -> Triple(colors.expressSoftBg, colors.expressAccent, "$stockCount stock bas")
        "En transit" -> Triple(colors.violetSoftBg, colors.violetAccent, "$transitCount en transit")
        "Référentiel" -> Triple(colors.cyanSoftBg, colors.cyanAccent, "Référentiel")
        else -> Triple(colors.dangerSoftBg, colors.dangerAccent, "Rupture")
    }
    Surface(
        shape = RoundedCornerShape(9999.dp),
        color = bg
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
internal fun ArticleActionDropdown(
    expanded: Boolean,
    isHidden: Boolean,
    onDismiss: () -> Unit,
    onOpenDetail: () -> Unit,
    onEdit: () -> Unit,
    onRestock: () -> Unit,
    onToggleHide: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = rememberTransitaireColors()
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Fiche complète", fontFamily = PlusJakartaSans) },
            leadingIcon = { Icon(Icons.Default.Info, null, tint = colors.cyanAccent) },
            onClick = onOpenDetail
        )
        DropdownMenuItem(
            text = { Text("Modifier", fontFamily = PlusJakartaSans) },
            leadingIcon = { Icon(Icons.Default.Edit, null, tint = colors.textSecondary) },
            onClick = onEdit
        )
        DropdownMenuItem(
            text = { Text("Nouvel achat", fontFamily = PlusJakartaSans) },
            leadingIcon = { Icon(Icons.Default.ShoppingCart, null, tint = colors.violetAccent) },
            onClick = onRestock
        )
        DropdownMenuItem(
            text = { Text(if (isHidden) "Réafficher" else "Masquer", fontFamily = PlusJakartaSans) },
            leadingIcon = { Icon(if (isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = colors.textSecondary) },
            onClick = onToggleHide
        )
        DropdownMenuItem(
            text = { Text("Supprimer", fontFamily = PlusJakartaSans, color = colors.dangerAccent) },
            leadingIcon = { Icon(Icons.Default.Delete, null, tint = colors.dangerAccent) },
            onClick = onDelete
        )
    }
}

@Composable
internal fun HiddenCatalogueCard(
    article: CatalogueArticleSummaryUi,
    onRestoreClick: () -> Unit,
    onClick: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val order = article.primaryOrder
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = colors.innerCardBg,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.badgeNeutralBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VisibilityOff, null, tint = colors.textSecondary, modifier = Modifier.size(24.dp))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Masqué • ${order.family.ifBlank { "Sans famille" }}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = order.product,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = order.referenceCode.ifBlank { "REF-${order.id}" },
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${article.totalStockCount} pcs • ${article.lotsCount} lot(s)",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )

                Button(
                    onClick = onRestoreClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.cyanSoftBg,
                        contentColor = colors.cyanAccent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Réafficher", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }
    }
}
