package com.example.ui.business.wizard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.VariantColorUtils
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.LotCostCalculator

private fun parseSuccessVariantWithCount(raw: String): Pair<String, Int?> {
    val timesMatch = Regex("""^(.*?)\s*[×xX]\s*(\d+)$""").find(raw.trim())
    val okMatch = Regex("""^(.*?)\s*\((\d+)\s*ok\)$""", RegexOption.IGNORE_CASE).find(raw.trim())
    return when {
        timesMatch != null -> timesMatch.groupValues[1].trim() to timesMatch.groupValues[2].toIntOrNull()
        okMatch != null -> okMatch.groupValues[1].trim() to okMatch.groupValues[2].toIntOrNull()
        else -> raw.trim() to null
    }
}

@Composable
fun LogisticSuccessView(
    order: BusinessOrderEntity,
    cmupRate: Long,
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    onDismiss: () -> Unit,
    onViewStock: () -> Unit,
    onCreateSale: () -> Unit,
    onReorder: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val selectedTransitaire = remember(transitaires, order.carrier) {
        if (order.carrier.isBlank()) null
        else transitaires.firstOrNull {
            it.name.equals(order.carrier, ignoreCase = true) ||
                order.carrier.contains(it.name, ignoreCase = true)
        }
    }
    val resolvedAirRate = if (order.freightMode.equals("EXPRESS", ignoreCase = true) && (selectedTransitaire?.airFastRateAr ?: 0L) > 0L) {
        selectedTransitaire?.airFastRateAr
    } else {
        selectedTransitaire?.airRateAr
    }

    val breakdown = remember(order, cmupRate, selectedTransitaire) {
        LotCostCalculator.calculate(
            order = order,
            cmupRate = cmupRate,
            transitaireAirRateAr = resolvedAirRate,
            transitaireSeaRateAr = selectedTransitaire?.seaRateAr
        )
    }
    val totalQty = breakdown.quantity
    val totalVendable = if (order.stockCount > 0) order.stockCount else totalQty
    val freightCostAr = breakdown.freightCostAr
    val refundAr = breakdown.refundAr
    val totalNetExpensesAr = breakdown.netCostAr
    val unitFifoCostAr = if (order.unitCostAr > 0L) {
        order.unitCostAr
    } else if (totalVendable > 0) {
        totalNetExpensesAr / totalVendable
    } else {
        breakdown.unitCostAr
    }
    val recommendedPriceAr = if (order.resalePriceAr > 0L) order.resalePriceAr else 0L

    val variantsBreakdown = remember(order.variants, totalVendable) {
        val rawList = VariantColorUtils.parseVariants(order.variants)
        val entries = (if (rawList.isNotEmpty()) rawList else listOf("Standard"))
            .map { parseSuccessVariantWithCount(it) }
            .map { (name, count) -> name.ifBlank { "Standard" } to count }

        val allHaveExplicit = entries.all { it.second != null }
        val explicitSum = entries.sumOf { (it.second ?: 0).coerceAtLeast(0) }

        when {
            entries.size == 1 -> listOf(entries.first().first to totalVendable)
            allHaveExplicit && explicitSum == totalVendable ->
                entries.map { it.first to (it.second ?: 0).coerceAtLeast(0) }
            allHaveExplicit && explicitSum < totalVendable && explicitSum > 0 -> {
                val remainder = totalVendable - explicitSum
                entries.mapIndexed { idx, (name, count) ->
                    val base = (count ?: 0).coerceAtLeast(0)
                    name to if (idx == 0) base + remainder else base
                }
            }
            allHaveExplicit && explicitSum > 0 -> {
                var assigned = 0
                entries.mapIndexed { idx, (name, count) ->
                    if (idx == entries.lastIndex) {
                        name to (totalVendable - assigned).coerceAtLeast(0)
                    } else {
                        val share = ((count ?: 0).toDouble() / explicitSum.toDouble() * totalVendable).toInt()
                            .coerceIn(0, (totalVendable - assigned).coerceAtLeast(0))
                        assigned += share
                        name to share
                    }
                }
            }
            else -> {
                val n = entries.size.coerceAtLeast(1)
                val base = totalVendable / n
                val rem = totalVendable % n
                entries.mapIndexed { idx, (name, _) ->
                    name to (base + if (idx < rem) 1 else 0)
                }
            }
        }
    }

    val hasValidMargin = recommendedPriceAr > 0L && unitFifoCostAr > 0L
    val marginPerUnitAr = if (hasValidMargin) recommendedPriceAr - unitFifoCostAr else 0L
    val marginPercent = if (hasValidMargin) {
        ((marginPerUnitAr.toDouble() / unitFifoCostAr.toDouble()) * 100).toInt()
    } else 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colors.cyanSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Importation Réussie",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.badgeNeutralBg)
                    .testTag("btn_close_success_wizard")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fermer le wizard",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        SuccessHeroCelebrationCard(order = order)

        SuccessMilestoneTimelineCard(order = order)

        SuccessStockIntegrationCard(
            order = order,
            totalVendable = totalVendable,
            variantsBreakdown = variantsBreakdown,
            refundAr = refundAr
        )

        SuccessFinancialSummaryCard(
            order = order,
            unitFifoCostAr = unitFifoCostAr,
            recommendedPriceAr = recommendedPriceAr,
            hasValidMargin = hasValidMargin,
            marginPercent = marginPercent,
            marginPerUnitAr = marginPerUnitAr,
            totalNetExpensesAr = totalNetExpensesAr,
            freightCostAr = freightCostAr,
            refundAr = refundAr
        )

        SuccessProductPreviewCard(order = order)

        SuccessActionButtonsColumn(
            onViewStock = onViewStock,
            onCreateSale = onCreateSale,
            onReorder = onReorder
        )
    }
}

@Composable
private fun SuccessFinancialSummaryCard(
    order: BusinessOrderEntity,
    unitFifoCostAr: Long,
    recommendedPriceAr: Long,
    hasValidMargin: Boolean,
    marginPercent: Int,
    marginPerUnitAr: Long,
    totalNetExpensesAr: Long,
    freightCostAr: Long,
    refundAr: Long
) {
    val colors = rememberTransitaireColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        shadowElevation = 2.dp
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Bilan Financier Définitif",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Calcul FIFO post-logistique",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.violetSoftBg
                ) {
                    Text(
                        text = "CMUP FIXÉ",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.violetAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.innerCardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Coût d'achat unitaire net",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(unitFifoCostAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = colors.textPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Prix conseillé",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = if (recommendedPriceAr > 0L) CurrencyFormatter.formatAriary(recommendedPriceAr) else "Non défini",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (recommendedPriceAr > 0L) colors.cyanAccent else colors.textSecondary
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (hasValidMargin && marginPerUnitAr < 0L) colors.dangerSoftBg else colors.cyanSoftBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = if (hasValidMargin && marginPerUnitAr < 0L) colors.dangerAccent else colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Marge brute prévisionnelle",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }

                    if (hasValidMargin) {
                        val signPrefix = if (marginPerUnitAr >= 0L) "+" else ""
                        val marginTint = if (marginPerUnitAr < 0L) colors.dangerAccent else colors.cyanAccent
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$signPrefix$marginPercent%",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = marginTint
                            )
                            Text(
                                text = "($signPrefix${CurrencyFormatter.formatAriary(marginPerUnitAr).replace(" Ar", "")} Ar / pce)",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    } else {
                        Text(
                            text = "Prix non défini",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dépense globale nette du lot", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                    Text(CurrencyFormatter.formatAriary(totalNetExpensesAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fret ${order.carrier.ifBlank { "transitaire" }} ${if (order.isFreightPaid) "réglé" else "prévu"}",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = if (freightCostAr > 0L) CurrencyFormatter.formatAriary(freightCostAr) else "Non défini",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (freightCostAr > 0L) colors.textPrimary else colors.textSecondary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Formule appliquée", fontFamily = Inter, fontSize = 13.sp, color = colors.textSecondary)
                    val formulaParts = buildList {
                        add("Marchandise")
                        add("Port")
                        add("Fret")
                        if (order.customsTaxAr > 0L) add("Douane")
                    }.joinToString(" + ") + (if (refundAr > 0L) " − Remb." else "")
                    Surface(shape = RoundedCornerShape(9999.dp), color = colors.badgeNeutralBg) {
                        Text(
                            text = formulaParts,
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessProductPreviewCard(order: BusinessOrderEntity) {
    val colors = rememberTransitaireColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(colors.cyanSoftBg, colors.violetSoftBg))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = colors.cyanAccent,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = order.product.ifBlank { "Article #${order.id}" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = order.supplier.ifBlank { "Fournisseur Chine" },
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = colors.badgeNeutralBg) {
                        Text(
                            text = "SKU: ${order.referenceCode.ifBlank { "SKU-${order.id}" }}",
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = colors.badgeNeutralBg) {
                        Text(
                            text = "Transitaire: ${order.carrier.ifBlank { "Transitaire" }}",
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessActionButtonsColumn(
    onViewStock: () -> Unit,
    onCreateSale: () -> Unit,
    onReorder: () -> Unit
) {
    val colors = rememberTransitaireColors()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onViewStock,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_view_updated_stock"),
            colors = ButtonDefaults.buttonColors(containerColor = colors.cyanSolidBg),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Inventory, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(
                    text = "📦 Voir le stock mis à jour",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(onClick = onCreateSale)
                .testTag("btn_create_first_sale"),
            shape = RoundedCornerShape(14.dp),
            color = colors.cardBg,
            border = BorderStroke(1.dp, colors.violetAccent.copy(alpha = 0.35f)),
            shadowElevation = 1.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, tint = colors.violetAccent, modifier = Modifier.size(20.dp))
                    Text(
                        text = "💰 Créer une première vente",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.violetAccent
                    )
                }
            }
        }

        TextButton(
            onClick = onReorder,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("btn_reorder_item"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Cached, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                Text(
                    text = "🔄 Racheter cet article (Nouveau lot 1688)",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}
