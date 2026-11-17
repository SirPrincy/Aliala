package com.example.ui.business.transitaires

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
internal fun TransitaireMarketKpisGrid(
    selectedFretMode: String,
    activeTransitaires: List<CatalogueTransitaireSummaryUi>,
    sortedByRate: List<CatalogueTransitaireSummaryUi>,
    colors: TransitaireColors
) {
    val cheapestTrans = sortedByRate.firstOrNull()
    val priciestTrans = sortedByRate.lastOrNull()
    val bestRate = cheapestTrans?.let { if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr } ?: 0L
    val maxRate = priciestTrans?.let { if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr } ?: 0L
    val rateSpread = (maxRate - bestRate).coerceAtLeast(0L)
    val spreadPct = if (bestRate > 0L && rateSpread > 0L) {
        ((rateSpread.toDouble() / bestRate.toDouble()) * 100.0).toInt()
    } else 0
    val avgMarketRate = if (sortedByRate.isNotEmpty()) {
        sortedByRate.map { if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr }.average().toLong()
    } else 0L
    val fastestRealEntry = remember(activeTransitaires, selectedFretMode) {
        activeTransitaires.mapNotNull { t ->
            val avg = if (selectedFretMode == "AERIEN") t.avgAirTransitDays else t.avgSeaTransitDays
            if (avg != null) Triple(t, "${com.example.util.TransitStatsCalculator.formatDaysAvg(avg)} réel", avg) else null
        }.minByOrNull { it.third }
    }
    val fastestLeadEntry = remember(activeTransitaires, selectedFretMode, fastestRealEntry) {
        if (fastestRealEntry != null) {
            fastestRealEntry
        } else {
            activeTransitaires.mapNotNull { t ->
                val rawLead = if (selectedFretMode == "AERIEN") t.airLeadDays else t.seaLeadDays
                val minDays = Regex("\\d+").find(rawLead)?.value?.toIntOrNull()
                if (rawLead.isBlank() || minDays == null || minDays <= 0) null
                else Triple(t, rawLead, minDays.toDouble())
            }.minByOrNull { it.third }
        }
    }
    val currentUnit = if (selectedFretMode == "AERIEN") "Ar/kg" else "Ar/m³"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = colors.cardBg,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MOINS CHER ACTUEL",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Column {
                    Text(
                        text = cheapestTrans?.name ?: "Aucun",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = colors.cyanAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (bestRate > 0L) CurrencyFormatter.formatAriary(bestRate).replace(" Ar", "") else "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = currentUnit,
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = colors.cardBg,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TARIF MAXIMAL",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.dangerSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = colors.dangerAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Column {
                    Text(
                        text = priciestTrans?.name ?: "—",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (spreadPct > 0) "+$spreadPct%" else "0%",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.dangerAccent
                        )
                        Text(
                            text = if (rateSpread > 0L) "(+${CurrencyFormatter.formatAriary(rateSpread)})" else "(Base)",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = colors.cardBg,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (fastestRealEntry != null) "LE + RAPIDE (RÉEL)" else "DÉLAI LE + COURT",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Balance,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Column {
                    Text(
                        text = fastestLeadEntry?.first?.name ?: "—",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = fastestLeadEntry?.second ?: "Non défini",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (fastestLeadEntry != null) colors.cyanAccent else colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = colors.cardBg,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MOYENNE MARCHÉ",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.badgeNeutralBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Column {
                    Text(
                        text = if (avgMarketRate > 0L) CurrencyFormatter.formatAriary(avgMarketRate).replace(" Ar", "") else "—",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = currentUnit,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "(${sortedByRate.size} tarifés)",
                            fontFamily = Inter,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TransitairePotentialSavingCard(
    selectedFretMode: String,
    orders: List<BusinessOrderEntity>,
    sortedByRate: List<CatalogueTransitaireSummaryUi>,
    cheapestTrans: CatalogueTransitaireSummaryUi?,
    priciestTrans: CatalogueTransitaireSummaryUi?,
    bestRate: Long,
    rateSpread: Long,
    colors: TransitaireColors,
    onOpenSimulator: () -> Unit
) {
    val matchingModeOrders = remember(orders, selectedFretMode) {
        orders.filter { o ->
            val isSeaOrder = o.freightMode.equals("MARITIME", ignoreCase = true) || o.freightMode.equals("SEA", ignoreCase = true)
            if (selectedFretMode == "AERIEN") !isSeaOrder && (o.weightKg ?: 0.0) > 0.0 && o.freightCostAr > 0L
            else isSeaOrder && (o.volumeM3 ?: 0.0) > 0.0 && o.freightCostAr > 0L
        }.sortedByDescending { it.orderDate }.take(10)
    }
    val bestName = cheapestTrans?.name ?: "le moins cher"
    val (potentialSavingAr, savingSummaryText) = remember(matchingModeOrders, bestRate, rateSpread, bestName, selectedFretMode) {
        if (matchingModeOrders.isNotEmpty() && bestRate > 0L) {
            val totalActualFreight = matchingModeOrders.sumOf { it.freightCostAr }
            val totalMeasure = if (selectedFretMode == "AERIEN") {
                matchingModeOrders.sumOf { it.weightKg ?: 0.0 }
            } else {
                matchingModeOrders.sumOf { it.volumeM3 ?: 0.0 }
            }
            val optimalCost = kotlin.math.round(totalMeasure * bestRate).toLong()
            val saving = (totalActualFreight - optimalCost).coerceAtLeast(0L)
            val measureStr = if (selectedFretMode == "AERIEN") "${String.format(Locale.FRANCE, "%.1f", totalMeasure)} kg"
            else "${String.format(Locale.FRANCE, "%.5f", totalMeasure)} m³"
            saving to "Sur tes ${matchingModeOrders.size} dernière(s) expédition(s) ($measureStr), le tarif $bestName aurait permis d'économiser ${CurrencyFormatter.formatAriary(saving)}."
        } else if (sortedByRate.size >= 2) {
            val sampleUnits = if (selectedFretMode == "AERIEN") 10 else 1
            val saving = rateSpread * sampleUnits
            val unitTxt = if (selectedFretMode == "AERIEN") "10 kg" else "1 m³"
            saving to "Pour un envoi type de $unitTxt, choisir $bestName économise ${CurrencyFormatter.formatAriary(saving)} face à ${priciestTrans?.name ?: "l'offre la plus haute"}."
        } else {
            0L to "Ajoutez au moins 2 transitaires ou des colis avec poids/volume pour calculer vos économies potentielles."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.innerCardBg)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.cyanSolidBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Économie potentielle",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(colors.cyanSoftBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (potentialSavingAr > 0L) "+${CurrencyFormatter.formatAriary(potentialSavingAr)}" else "Optimal",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = colors.cyanAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = savingSummaryText,
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Button(
                onClick = onOpenSimulator,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.cyanSolidBg,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_simuler_envoi")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Simuler un envoi personnalisé",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
