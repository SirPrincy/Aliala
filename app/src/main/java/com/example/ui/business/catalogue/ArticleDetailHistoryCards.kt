package com.example.ui.business.catalogue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

@Composable
internal fun ArticleFreightSpecsCard(
    primaryOrder: BusinessOrderEntity,
    weightGrams: Int,
    volumeM3Str: String,
    unitFreightRealAr: Long,
    estFreightUnitAr: Long,
    freightPerKgAr: Long,
    activeCarrierName: String?,
    isIndicativeFreight: Boolean,
    colors: TransitaireColors
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
                        .background(colors.expressSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalShipping, null, tint = colors.expressAccent, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "Spécifications & Fret",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Mode : ${primaryOrder.freightMode.ifBlank { "AÉRIEN" }}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Poids unitaire", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = if (weightGrams > 0) "$weightGrams g" else "Non renseigné",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Volume estimé", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = if (volumeM3Str != "—") "$volumeM3Str m³" else "Non renseigné",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.expressSoftBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Flight, null, tint = colors.expressAccent, modifier = Modifier.size(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (unitFreightRealAr > 0L) "Fret réel sur le dernier lot" else "Estimation fret transitaire",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.expressAccent
                        )
                        val desc = when {
                            unitFreightRealAr > 0L ->
                                "${CurrencyFormatter.formatAriary(unitFreightRealAr)} / unité (lot : ${CurrencyFormatter.formatAriary(primaryOrder.freightCostAr)})"
                            estFreightUnitAr > 0L -> {
                                val carrierInfo = activeCarrierName?.let { "$it · " } ?: ""
                                val indicativeTag = if (isIndicativeFreight) " · tarif indicatif" else ""
                                "≈ ${CurrencyFormatter.formatAriary(estFreightUnitAr)} / unité ($carrierInfo${CurrencyFormatter.formatAriary(freightPerKgAr)}/kg$indicativeTag)"
                            }
                            else -> "Renseignez le poids unitaire dans Modifier pour estimer le fret."
                        }
                        Text(
                            text = desc,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ArticleLotsHistoryCard(
    article: CatalogueArticleSummaryUi,
    cmupRate: Long,
    dateFormatter: SimpleDateFormat,
    colors: TransitaireColors,
    onLotClick: (BusinessOrderEntity) -> Unit,
    onNewLotClick: () -> Unit
) {
    val sortedLots = remember(article.lots) {
        article.lots.sortedByDescending { it.orderDate }
    }
    val effectiveRate = if (cmupRate > 0L) cmupRate.toDouble() else 650.0

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
                        Icon(Icons.AutoMirrored.Filled.TrendingDown, null, tint = colors.cyanAccent, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "Historique des Lots (${sortedLots.size})",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${article.totalBoughtCount} pcs achetées au total",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.cyanSoftBg,
                    modifier = Modifier.clickable(onClick = onNewLotClick)
                ) {
                    Text(
                        text = "+ Lot",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.cyanAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (sortedLots.isEmpty() || (sortedLots.size == 1 && sortedLots.first().quantityBought == 0 && sortedLots.first().status == "CATALOGUE")) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Fiche référentiel sans lot acheté. Utilisez « Acheter » pour lancer votre premier approvisionnement.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sortedLots.forEach { lot ->
                        val lotUnitCostAr = when {
                            lot.unitCostAr > 0L -> lot.unitCostAr
                            lot.priceYuan > 0.0 -> {
                                val rate = if (lot.exchangeRate > 0.0) lot.exchangeRate else effectiveRate
                                val unitFreight = if (lot.freightCostAr > 0L && lot.quantityBought > 0) lot.freightCostAr / lot.quantityBought else 0L
                                round(lot.priceYuan * rate).toLong() + unitFreight
                            }
                            else -> 0L
                        }
                        val statusLabel = translateLotStatus(lot.status)
                        val yuanStr = if (lot.priceYuan > 0.0) {
                            if (lot.priceYuan % 1.0 == 0.0) "¥ ${lot.priceYuan.toInt()}" else String.format(Locale.US, "¥ %.2f", lot.priceYuan)
                        } else "—"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.innerCardBg)
                                .clickable { onLotClick(lot) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.ReceiptLong, null, tint = colors.cyanAccent, modifier = Modifier.size(18.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = lot.lotNumber.ifBlank { "Lot #${lot.id}" },
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.textPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = colors.badgeNeutralBg
                                        ) {
                                            Text(
                                                text = statusLabel,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp,
                                                color = colors.textSecondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${dateFormatter.format(Date(lot.orderDate))} • ${lot.quantityBought} ach. / ${lot.stockCount} dispo",
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                    if (lot.carrier.isNotBlank() || lot.platform.isNotBlank()) {
                                        Text(
                                            text = listOf(lot.platform, lot.carrier).filter { it.isNotBlank() }.joinToString(" · "),
                                            fontFamily = Inter,
                                            fontSize = 10.sp,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (lotUnitCostAr > 0L) CurrencyFormatter.formatAriary(lotUnitCostAr) else "—",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "$yuanStr / u",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Ouvrir le suivi du lot",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ArticleSalesPerformanceCard(
    article: CatalogueArticleSummaryUi,
    dateFormatter: SimpleDateFormat,
    colors: TransitaireColors
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
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QueryStats, null, tint = colors.violetAccent, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "Performances & Ventes",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${article.salesHistory.size} transaction(s) enregistrée(s)",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                if (article.resalePriceAr > 0L && article.latestUnitCostAr > 0L) {
                    val mColor = if (article.marginPercent >= 20) colors.successAccent else colors.violetAccent
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = mColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "Marge ${article.marginPercent}%",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = mColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Vendues", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = "${article.totalSoldCount} pcs",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("CA Réalisé", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = CurrencyFormatter.formatAriary(article.totalRevenueAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = colors.cyanAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Profit net", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        val profitColor = if (article.realProfitAr >= 0L) colors.successAccent else colors.dangerAccent
                        Text(
                            text = CurrencyFormatter.formatAriary(article.realProfitAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = profitColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (article.salesHistory.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "DERNIÈRES VENTES",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    article.salesHistory.take(5).forEach { sale ->
                        SaleHistoryRow(sale = sale, dateFormatter = dateFormatter, colors = colors)
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
                        text = "Aucune vente enregistrée pour cet article pour le moment.",
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
private fun SaleHistoryRow(
    sale: BusinessSaleEntity,
    dateFormatter: SimpleDateFormat,
    colors: TransitaireColors
) {
    val totalAr = if (sale.totalPriceAr > 0L) sale.totalPriceAr else sale.unitResalePriceAr * sale.quantity
    val isPaid = sale.paidAmountAr >= totalAr && totalAr > 0L
    val isPartial = sale.paidAmountAr in 1L until totalAr

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.innerCardBg)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = sale.clientName.ifBlank { "Client comptoir" },
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${dateFormatter.format(Date(sale.saleDate))} • ${sale.quantity} pc(s)",
                fontFamily = Inter,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = CurrencyFormatter.formatAriary(totalAr),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.textPrimary
            )
            val (badgeBg, badgeFg, badgeText) = when {
                isPaid -> Triple(colors.successSoftBg, colors.successAccent, "Réglée")
                isPartial -> Triple(colors.expressSoftBg, colors.expressAccent, "Partielle")
                else -> Triple(colors.dangerSoftBg, colors.dangerAccent, "Impayée")
            }
            Surface(shape = RoundedCornerShape(6.dp), color = badgeBg) {
                Text(
                    text = badgeText,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = badgeFg,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

private fun translateLotStatus(status: String): String {
    return when (status) {
        "DRAFT" -> "Brouillon"
        "ORDERED" -> "Commandé"
        "WAREHOUSE" -> "Entrepôt"
        "GUANGZHOU" -> "Guangzhou"
        "FLIGHT" -> "En transit"
        "CUSTOMS" -> "Douane"
        "QUALITY_CONTROL" -> "Contrôle"
        "DELIVERY_READY" -> "En stock"
        "SOLD" -> "Soldé"
        "CATALOGUE" -> "Référentiel"
        else -> status
    }
}
