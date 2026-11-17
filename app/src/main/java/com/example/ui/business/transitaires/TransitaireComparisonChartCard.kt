package com.example.ui.business.transitaires

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun TransitaireComparisonChartCard(
    selectedFretMode: String,
    selectedPeriod: String,
    activeTransitaires: List<CatalogueTransitaireSummaryUi>,
    sortedByRate: List<CatalogueTransitaireSummaryUi>,
    activeTooltipText: String,
    showTile0: Boolean,
    showTile1: Boolean,
    showTile2: Boolean,
    onToggleTile0: (Boolean, String?) -> Unit,
    onToggleTile1: (Boolean, String?) -> Unit,
    onToggleTile2: (Boolean, String?) -> Unit,
    colors: TransitaireColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
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
                Column {
                    val periodLabel = when (selectedPeriod) {
                        "3m" -> "3 mois"
                        "6m" -> "6 mois"
                        "1y" -> "1 an"
                        else -> "Tout"
                    }
                    Text(
                        text = "Comparatif tarifs ($periodLabel)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Tarifs actifs enregistrés en base",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.cyanSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.cyanSoftBg)
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = activeTooltipText,
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textPrimary,
                        lineHeight = 16.sp
                    )
                }
            }

            val chartColors = listOf(
                Color(0xFF06B6D4),
                Color(0xFF7C3AED),
                Color(0xFFF97316),
                Color(0xFF10B981),
                Color(0xFFEC4899),
                Color(0xFF3B82F6)
            )
            val displayedTransitaires = sortedByRate.take(6)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.innerCardBg)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                if (displayedTransitaires.isEmpty()) {
                    Text(
                        text = if (activeTransitaires.isEmpty()) {
                            "Ajoutez des transitaires pour afficher le graphique comparatif."
                        } else {
                            "Aucun tarif ${if (selectedFretMode == "AERIEN") "aérien" else "maritime"} défini pour vos transitaires actifs."
                        },
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val leftPadding = 12f
                        val rightPadding = 12f
                        val bottomPadding = 24f
                        val topPadding = 12f
                        val chartW = (w - leftPadding - rightPadding).coerceAtLeast(1f)
                        val chartH = (h - topPadding - bottomPadding).coerceAtLeast(1f)

                        val rates = displayedTransitaires.map {
                            (if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr).toFloat()
                        }
                        val maxRate = (rates.maxOrNull() ?: 1f).coerceAtLeast(1f)
                        val minRate = (rates.minOrNull() ?: 0f)
                        val range = (maxRate - minRate * 0.6f).coerceAtLeast(1f)

                        val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        listOf(0f, 0.5f, 1f).forEach { frac ->
                            val y = topPadding + chartH * frac
                            drawLine(
                                color = Color(0xFFBCC9CD).copy(alpha = 0.35f),
                                start = Offset(leftPadding, y),
                                end = Offset(w - rightPadding, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dashedEffect
                            )
                        }

                        val count = displayedTransitaires.size
                        val slotW = chartW / count
                        val barW = (slotW * 0.45f).coerceAtMost(48.dp.toPx())

                        displayedTransitaires.forEachIndexed { idx, t ->
                            val rate = if (selectedFretMode == "AERIEN") t.airRateAr.toFloat() else t.seaRateAr.toFloat()
                            val normalized = ((rate - minRate * 0.6f) / range).coerceIn(0.12f, 1f)
                            val barH = chartH * normalized
                            val barX = leftPadding + slotW * idx + (slotW - barW) / 2f
                            val barY = topPadding + chartH - barH
                            val barColor = chartColors[idx % chartColors.size]

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(barX, barY),
                                size = Size(barW, barH),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        displayedTransitaires.forEachIndexed { idx, t ->
                            Text(
                                text = t.initials.ifBlank { t.name.take(4) },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = chartColors[idx % chartColors.size],
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            val unratedCount = activeTransitaires.count {
                (if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr) <= 0L
            }
            if (unratedCount > 0) {
                Text(
                    text = "$unratedCount transitaire(s) actif(s) sans tarif ${if (selectedFretMode == "AERIEN") "aérien" else "maritime"} défini sont exclus du classement.",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }

            if (activeTransitaires.isNotEmpty()) {
                val today = LocalDate.now()
                val dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val maritimeSorted = remember(activeTransitaires) {
                    activeTransitaires.mapNotNull { t ->
                        val parsed = TariffGridParser.parseMinMaxDaysOrNull(t.seaLeadDays) ?: return@mapNotNull null
                        Triple(t, parsed.first, parsed.second)
                    }.sortedBy { it.second }
                }
                val fastestMinDays = maritimeSorted.firstOrNull()?.second

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Comparatif transport maritime (Bateau)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.textPrimary
                    )
                    if (maritimeSorted.isEmpty()) {
                        Text(
                            text = "Aucun délai maritime défini sur vos transitaires actifs.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    } else {
                        Text(
                            text = "Départ estimé : ${today.format(dateFmt)}",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        maritimeSorted.forEach { (t, minD, maxD) ->
                            val arrivalMin = today.plusDays(minD.toLong()).format(dateFmt)
                            val arrivalMax = today.plusDays(maxD.toLong()).format(dateFmt)
                            val arrivalText = if (minD == maxD) arrivalMin else "$arrivalMin - $arrivalMax"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = t.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Arrivée : $arrivalText",
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (fastestMinDays != null && minD == fastestMinDays) colors.cyanSoftBg
                                            else colors.badgeNeutralBg
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (minD == maxD) "${minD}j" else "${minD}-${maxD}j",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (fastestMinDays != null && minD == fastestMinDays) colors.cyanAccent else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val t0 = sortedByRate.getOrNull(0)
                val t1 = sortedByRate.getOrNull(1)
                val t2 = sortedByRate.getOrNull(2)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (showTile1) colors.innerCardBg else colors.badgeNeutralBg)
                        .clickable {
                            val next = !showTile1
                            val msg = if (next && t1 != null) {
                                val rate = if (selectedFretMode == "AERIEN") t1.airRateAr else t1.seaRateAr
                                "${t1.name} : ${CurrencyFormatter.formatAriary(rate)}."
                            } else null
                            onToggleTile1(next, msg)
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.warningAccent)
                            )
                            Text(
                                text = t1?.name ?: "—",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = t1?.let { CurrencyFormatter.formatAriary(if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr) } ?: "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (showTile0) colors.cyanSoftBg else colors.badgeNeutralBg)
                        .clickable {
                            val next = !showTile0
                            val msg = if (next && t0 != null) {
                                val rate = if (selectedFretMode == "AERIEN") t0.airRateAr else t0.seaRateAr
                                "${t0.name} : ${CurrencyFormatter.formatAriary(rate)} — Le plus économique."
                            } else null
                            onToggleTile0(next, msg)
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.cyanAccent)
                            )
                            Text(
                                text = t0?.name ?: "—",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = colors.cyanAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = t0?.let { CurrencyFormatter.formatAriary(if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr) } ?: "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.cyanAccent
                        )
                        Text(
                            text = "LE + BAS",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            color = colors.cyanAccent
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (showTile2) colors.innerCardBg else colors.badgeNeutralBg)
                        .clickable {
                            val next = !showTile2
                            val msg = if (next && t2 != null) {
                                val rate = if (selectedFretMode == "AERIEN") t2.airRateAr else t2.seaRateAr
                                "${t2.name} : ${CurrencyFormatter.formatAriary(rate)}."
                            } else null
                            onToggleTile2(next, msg)
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.violetAccent)
                            )
                            Text(
                                text = t2?.name ?: "—",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = t2?.let { CurrencyFormatter.formatAriary(if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr) } ?: "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
