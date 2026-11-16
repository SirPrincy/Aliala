package com.example.ui.business.finances

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinancesForecastCard(
    forecastHorizon: String,
    totalTreasuryAr: Long,
    forecastData: CashflowForecastSummary,
    onSelectHorizon: (String) -> Unit,
    onOpenDetail: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val cyanColor = colors.cyanAccent
    val emeraldColor = colors.successAccent
    val redColor = colors.dangerAccent

    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.expressSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = null,
                            tint = colors.expressAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Prévision Trésorerie",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Projection sur ${forecastData.horizonDays} jours",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(2.dp)
                ) {
                    listOf("30 j", "60 j", "90 j").forEach { horizon ->
                        val isSel = forecastHorizon == horizon
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { onSelectHorizon(horizon) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = horizon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) cyanColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(emeraldColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "À recevoir : ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = CurrencyFormatter.formatAriary(forecastData.totalInflowAr),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = emeraldColor
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(redColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "À payer : ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = CurrencyFormatter.formatAriary(forecastData.totalOutflowAr),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = redColor
                            )
                        }
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val barWidth = 14.dp.toPx()
                        val cornerRad = CornerRadius(4.dp.toPx())
                        val topPad = 8.dp.toPx()
                        val bottomPad = 6.dp.toPx()
                        val usableH = (h - topPad - bottomPad).coerceAtLeast(10f)
                        val baseBottom = h - bottomPad

                        val maxBarFlow = forecastData.buckets
                            .maxOfOrNull { maxOf(it.inflowAr, it.outflowAr) }
                            ?.coerceAtLeast(1L) ?: 1L

                        val xCenters = listOf(0.14f, 0.38f, 0.62f, 0.86f).map { w * it }

                        forecastData.buckets.forEachIndexed { idx, bucket ->
                            val cx = xCenters[idx]
                            val outRatio = if (bucket.outflowAr > 0L) {
                                (bucket.outflowAr.toFloat() / maxBarFlow.toFloat()).coerceIn(0.08f, 1f)
                            } else 0f
                            val inRatio = if (bucket.inflowAr > 0L) {
                                (bucket.inflowAr.toFloat() / maxBarFlow.toFloat()).coerceIn(0.08f, 1f)
                            } else 0f

                            if (outRatio > 0f) {
                                val barH = usableH * outRatio
                                val barColor = if (forecastData.hasTension && forecastData.minBucketIndex == idx) {
                                    redColor
                                } else {
                                    redColor.copy(alpha = 0.8f)
                                }
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(cx - barWidth - 2.dp.toPx(), baseBottom - barH),
                                    size = Size(barWidth, barH),
                                    cornerRadius = cornerRad
                                )
                            }
                            if (inRatio > 0f) {
                                val barH = usableH * inRatio
                                drawRoundRect(
                                    color = emeraldColor.copy(alpha = 0.85f),
                                    topLeft = Offset(cx + 2.dp.toPx(), baseBottom - barH),
                                    size = Size(barWidth, barH),
                                    cornerRadius = cornerRad
                                )
                            }
                        }

                        val balancePoints = listOf(totalTreasuryAr) + forecastData.buckets.map { it.projectedBalanceAr }
                        val minBal = balancePoints.minOrNull() ?: 0L
                        val maxBal = balancePoints.maxOrNull() ?: 0L
                        val range = (maxBal - minBal).coerceAtLeast(1L)

                        fun yForBalance(bal: Long): Float {
                            return if (maxBal == minBal) {
                                topPad + usableH * 0.45f
                            } else {
                                val norm = (bal - minBal).toFloat() / range.toFloat()
                                baseBottom - (norm * usableH * 0.85f) - usableH * 0.08f
                            }
                        }

                        val curveXs = listOf(w * 0.03f) + xCenters
                        val curveOffsets = curveXs.mapIndexed { idx, x ->
                            Offset(x, yForBalance(balancePoints[idx]))
                        }

                        val path = Path().apply {
                            moveTo(curveOffsets.first().x, curveOffsets.first().y)
                            for (i in 0 until curveOffsets.lastIndex) {
                                val p0 = curveOffsets[i]
                                val p1 = curveOffsets[i + 1]
                                val midX = (p0.x + p1.x) / 2f
                                cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = cyanColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        val minPtIdx = forecastData.minBucketIndex + 1
                        if (minPtIdx in curveOffsets.indices && forecastData.hasTension) {
                            drawCircle(
                                color = redColor,
                                radius = 5.dp.toPx(),
                                center = curveOffsets[minPtIdx]
                            )
                        }
                        drawCircle(
                            color = cyanColor,
                            radius = 5.dp.toPx(),
                            center = curveOffsets.last()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        forecastData.buckets.take(3).forEachIndexed { idx, b ->
                            val isStress = forecastData.hasTension && forecastData.minBucketIndex == idx
                            Text(
                                text = if (isStress) "${b.label} (Tension)" else b.label,
                                fontSize = 10.sp,
                                fontWeight = if (isStress) FontWeight.Bold else FontWeight.Normal,
                                color = if (isStress) redColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${forecastData.horizonDays} j : ${CurrencyFormatter.formatAriary(forecastData.projectedEndBalanceAr)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (forecastData.projectedEndBalanceAr < 0L) redColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            val minBucket = forecastData.buckets.getOrNull(forecastData.minBucketIndex)
            val (alertBg, alertTint, alertTextColor, alertMsg) = when {
                forecastData.itemsInHorizon.isEmpty() -> listOf(
                    colors.cyanSoftBg,
                    colors.cyanAccent,
                    colors.textPrimary,
                    "Aucune échéance prévue sur les ${forecastData.horizonDays} prochains jours. Trésorerie actuelle : ${CurrencyFormatter.formatAriary(totalTreasuryAr)}."
                )
                minBucket != null && minBucket.projectedBalanceAr < 0L -> listOf(
                    colors.dangerSoftBg,
                    colors.dangerAccent,
                    colors.dangerAccent,
                    "Alerte déficit : solde estimé négatif (${CurrencyFormatter.formatAriary(minBucket.projectedBalanceAr)}) autour du ${minBucket.label} (${CurrencyFormatter.formatAriary(minBucket.outflowAr)} à payer)."
                )
                forecastData.hasTension && minBucket != null -> listOf(
                    colors.warningSoftBg,
                    colors.warningAccent,
                    colors.warningAccent,
                    "Point de tension le ${minBucket.label} : solde estimé au plus bas à ${CurrencyFormatter.formatAriary(minBucket.projectedBalanceAr)} avec ${CurrencyFormatter.formatAriary(minBucket.outflowAr)} à décaisser."
                )
                else -> listOf(
                    colors.successSoftBg,
                    colors.successAccent,
                    colors.successAccent,
                    "Trésorerie équilibrée sur ${forecastData.horizonDays} jours : solde projeté de ${CurrencyFormatter.formatAriary(forecastData.projectedEndBalanceAr)}."
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(alertBg as Color)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = alertTint as Color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alertMsg as String,
                        fontSize = 12.sp,
                        color = alertTextColor as Color,
                        lineHeight = 16.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenDetail)
                    .padding(vertical = 4.dp)
                    .testTag("btn_forecast_detail"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Voir le détail prévisionnel (${forecastData.itemsInHorizon.size + forecastData.unscheduledDebts.size})",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = cyanColor
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Voir le détail prévisionnel",
                    tint = cyanColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ForecastDetailDialog(
    startingTreasuryAr: Long,
    forecast: CashflowForecastSummary,
    onDismiss: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val fullDateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Column {
                Text(
                    text = "Détail prévisionnel (${forecast.horizonDays} j)",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.textPrimary
                )
                Text(
                    text = "Trésorerie de départ : ${CurrencyFormatter.formatAriary(startingTreasuryAr)}",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("À recevoir", fontSize = 10.sp, color = colors.textSecondary)
                        Text(
                            text = "+${CurrencyFormatter.formatAriary(forecast.totalInflowAr)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.successAccent
                        )
                    }
                    Column {
                        Text("À payer", fontSize = 10.sp, color = colors.textSecondary)
                        Text(
                            text = "-${CurrencyFormatter.formatAriary(forecast.totalOutflowAr)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.dangerAccent
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Solde final", fontSize = 10.sp, color = colors.textSecondary)
                        Text(
                            text = CurrencyFormatter.formatAriary(forecast.projectedEndBalanceAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (forecast.projectedEndBalanceAr < 0L) colors.dangerAccent else colors.cyanAccent
                        )
                    }
                }

                if (forecast.itemsInHorizon.isEmpty()) {
                    Text(
                        text = "Aucune créance client, facture transitaire ou dette échue sur les ${forecast.horizonDays} prochains jours.",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                } else {
                    Text(
                        text = "Échéances prises en compte",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.textPrimary
                    )
                    forecast.itemsInHorizon.forEach { item ->
                        val dateLabel = item.dateMillis?.let { fullDateFmt.format(Date(it)) } ?: "Sans échéance"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.innerCardBg.copy(alpha = 0.7f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = buildString {
                                        if (item.subtitle.isNotBlank()) append("${item.subtitle} • ")
                                        append(if (item.isOverdue) "En retard ($dateLabel)" else dateLabel)
                                    },
                                    fontSize = 10.sp,
                                    color = if (item.isOverdue) colors.dangerAccent else colors.textSecondary
                                )
                            }
                            Text(
                                text = (if (item.isInflow) "+" else "-") + CurrencyFormatter.formatAriary(item.amountAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (item.isInflow) colors.successAccent else colors.dangerAccent
                            )
                        }
                    }
                }

                if (forecast.unscheduledDebts.isNotEmpty()) {
                    Text(
                        text = "Dettes / Prêts sans date d'échéance (hors courbe)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    forecast.unscheduledDebts.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.innerCardBg.copy(alpha = 0.45f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                if (item.subtitle.isNotBlank()) {
                                    Text(item.subtitle, fontSize = 10.sp, color = colors.textSecondary)
                                }
                            }
                            Text(
                                text = (if (item.isInflow) "+" else "-") + CurrencyFormatter.formatAriary(item.amountAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (item.isInflow) colors.successAccent else colors.dangerAccent
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        }
    )
}
