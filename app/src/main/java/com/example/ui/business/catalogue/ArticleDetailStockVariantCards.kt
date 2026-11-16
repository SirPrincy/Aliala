package com.example.ui.business.catalogue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.VariantColorUtils
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans

@Composable
internal fun ArticleStockConsolidatedCard(
    article: CatalogueArticleSummaryUi,
    colors: TransitaireColors
) {
    val totalStock = article.totalStockCount
    val reserved = article.totalReservedCount
    val inTransit = article.totalInTransitCount
    val defective = article.totalDefectiveCount
    val totalInventory = totalStock + reserved + inTransit + defective

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Inventory2, null, tint = colors.cyanAccent, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "État des Stocks consolidé",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Total suivi : $totalInventory unité(s) • ${article.lotsCount} lot(s)",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StockMiniBox(
                    label = "Disponible",
                    value = totalStock,
                    subtitle = "En rayon",
                    accent = colors.successAccent,
                    bg = colors.innerCardBg,
                    modifier = Modifier.weight(1f)
                )
                StockMiniBox(
                    label = "En transit",
                    value = inTransit,
                    subtitle = "Chine → Mada",
                    accent = colors.violetAccent,
                    bg = colors.innerCardBg,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StockMiniBox(
                    label = "Réservé",
                    value = reserved,
                    subtitle = "Clients",
                    accent = colors.cyanAccent,
                    bg = colors.innerCardBg,
                    modifier = Modifier.weight(1f)
                )
                StockMiniBox(
                    label = "Défectueux",
                    value = defective,
                    subtitle = "SAV / Perte",
                    accent = colors.dangerAccent,
                    bg = colors.innerCardBg,
                    modifier = Modifier.weight(1f)
                )
            }

            if (totalInventory > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(colors.badgeNeutralBg)
                    ) {
                        if (totalStock > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(totalStock.toFloat())
                                    .fillMaxHeight()
                                    .background(colors.successAccent)
                            )
                        }
                        if (inTransit > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(inTransit.toFloat())
                                    .fillMaxHeight()
                                    .background(colors.violetAccent)
                            )
                        }
                        if (reserved > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(reserved.toFloat())
                                    .fillMaxHeight()
                                    .background(colors.cyanAccent)
                            )
                        }
                        if (defective > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(defective.toFloat())
                                    .fillMaxHeight()
                                    .background(colors.dangerAccent)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val pctDispo = (totalStock * 100) / totalInventory
                        val pctTransit = (inTransit * 100) / totalInventory
                        val pctRes = (reserved * 100) / totalInventory
                        val pctDef = (defective * 100) / totalInventory
                        Text("$pctDispo% Dispo", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                        Text("$pctTransit% Transit", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                        Text("$pctRes% Rés.", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                        Text("$pctDef% Déf.", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Aucune unité en stock ni en transit pour cet article.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun StockMiniBox(
    label: String,
    value: Int,
    subtitle: String,
    accent: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(subtitle, fontFamily = Inter, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            }
            Text(
                text = "$value",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = accent
            )
        }
    }
}

@Composable
internal fun ArticleVariantsCard(
    variants: List<String>,
    colors: TransitaireColors,
    onRemoveVariant: (String) -> Unit,
    onAddVariant: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.violetSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Palette, null, tint = colors.violetAccent, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "Déclinaisons & Couleurs",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (variants.isNotEmpty()) "${variants.size} déclinaison(s)" else "Aucune variante configurée",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            if (variants.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    variants.forEachIndexed { idx, varText ->
                        val dotColor = VariantColorUtils.getVariantColor(varText, idx)
                        val isLight = VariantColorUtils.isLightColor(dotColor)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.innerCardBg)
                                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                        .then(
                                            if (isLight) Modifier.border(1.dp, colors.border, CircleShape)
                                            else Modifier
                                        )
                                )
                                Text(
                                    text = varText,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                            }
                            IconButton(
                                onClick = { onRemoveVariant(varText) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Retirer $varText",
                                    tint = colors.dangerAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Aucune couleur ou taille spécifique ajoutée.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.cyanSoftBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddCircle, null, tint = colors.cyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ajouter une couleur / variante",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.cyanAccent
                    )
                }
            }
        }
    }
}
