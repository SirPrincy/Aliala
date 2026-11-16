package com.example.ui.business.finances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

internal data class CapitalPatrimonyMetrics(
    val totalApportsCapital: Long,
    val totalRetraitsPerso: Long,
    val netCapital: Long,
    val capitalMovements: List<BusinessTransactionEntity>,
    val totalTreasuryAr: Long,
    val yuanValueAr: Long,
    val stockValueAr: Long,
    val inRouteValueAr: Long,
    val receivablesAr: Long,
    val totalInvestedCostAr: Long,
    val activeInvestmentsValueAr: Long,
    val activeInvestmentsCount: Int,
    val investmentsReturnAr: Long,
    val totalAssetsAr: Long,
    val totalLiabilitiesAr: Long,
    val netPatrimonyAr: Long,
    val totalProfitAr: Long,
    val roiPercent: Int?,
    val evolutionSeries: List<Long>
)

internal data class InvestmentCategoryOption(
    val code: String,
    val label: String,
    val emoji: String
)

internal val INVESTMENT_CATEGORIES = listOf(
    InvestmentCategoryOption("PLACEMENT", "Argent / Placement", "💰"),
    InvestmentCategoryOption("MATERIEL", "Matériel", "📷"),
    InvestmentCategoryOption("LOGISTIQUE", "Logistique", "🛵"),
    InvestmentCategoryOption("STOCK_STRATEGIQUE", "Stock clé", "📦"),
    InvestmentCategoryOption("MARKETING", "Marketing", "📣"),
    InvestmentCategoryOption("INFRASTRUCTURE", "Local / Bureau", "🏪"),
    InvestmentCategoryOption("AUTRE", "Autre", "💼")
)

internal fun categoryInfo(code: String): InvestmentCategoryOption {
    return INVESTMENT_CATEGORIES.firstOrNull { it.code.equals(code, ignoreCase = true) }
        ?: INVESTMENT_CATEGORIES.last()
}

internal fun investmentStatusLabel(status: String): String {
    return when (status) {
        "AMORTI" -> "Amorti"
        "CEDE" -> "Cédé"
        else -> "Actif"
    }
}

internal fun isCapitalContributionTx(tx: BusinessTransactionEntity): Boolean {
    val isIn = tx.type == "IN" || tx.type == "INCOME" || tx.type == "CAPITAL_IN"
    if (!isIn) return false
    return tx.referenceTag.contains("Capital", ignoreCase = true) ||
        tx.title.contains("Apport", ignoreCase = true)
}

internal fun isCapitalWithdrawalTx(tx: BusinessTransactionEntity): Boolean {
    val isOut = tx.type == "OUT" || tx.type == "EXPENSE" || tx.type == "CAPITAL_OUT"
    if (!isOut) return false
    return tx.referenceTag.contains("Retrait", ignoreCase = true) ||
        tx.referenceTag.contains("Perso", ignoreCase = true) ||
        tx.title.contains("Retrait", ignoreCase = true)
}

private fun localMillisToUtcPickerMillis(localMillis: Long): Long {
    val localCal = Calendar.getInstance().apply { timeInMillis = localMillis }
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(
            localCal.get(Calendar.YEAR),
            localCal.get(Calendar.MONTH),
            localCal.get(Calendar.DAY_OF_MONTH),
            0, 0, 0
        )
    }
    return utcCal.timeInMillis
}

private fun utcPickerMillisToLocalNoon(utcMillis: Long): Long {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    val localCal = Calendar.getInstance().apply {
        clear()
        set(
            utcCal.get(Calendar.YEAR),
            utcCal.get(Calendar.MONTH),
            utcCal.get(Calendar.DAY_OF_MONTH),
            12, 0, 0
        )
    }
    return localCal.timeInMillis
}

@Composable
internal fun rememberCapitalPatrimonyMetrics(
    uiState: BusinessUiState,
    ariaryAccounts: List<BusinessAccountEntity>,
    transactions: List<BusinessTransactionEntity>,
    investments: List<BusinessInvestmentEntity>,
    empruntsDueAr: Long,
    associeDueAr: Long
): CapitalPatrimonyMetrics {
    return remember(uiState, ariaryAccounts, transactions, investments, empruntsDueAr, associeDueAr) {
        val apportsTxs = transactions.filter { isCapitalContributionTx(it) }
        val retraitsTxs = transactions.filter { isCapitalWithdrawalTx(it) }
        val totalApports = apportsTxs.sumOf { it.amountAr }
        val totalRetraits = retraitsTxs.sumOf { it.amountAr }
        val netCapital = totalApports - totalRetraits
        val capitalMovements = (apportsTxs + retraitsTxs)
            .distinctBy { it.id }
            .sortedByDescending { it.dateMillis }

        val totalTreasury = ariaryAccounts.sumOf { it.balanceAr }
        val yuanValue = uiState.estimatedYuanValueAr
        val stockValue = uiState.totalStockValueAr
        val inRouteValue = uiState.inRouteAmountAr
        val receivables = uiState.clientReceivablesAmount

        val totalInvestedCost = investments.sumOf { it.amountAr }
        val activeInvestments = investments.filter { it.status == "ACTIF" }
        val activeInvestmentsValue = activeInvestments.sumOf { it.estimatedValueAr }
        val investmentsReturn = investments.sumOf { it.generatedReturnAr }

        val totalAssets = totalTreasury + yuanValue + stockValue + inRouteValue + activeInvestmentsValue + receivables
        val totalLiabilities = empruntsDueAr + uiState.freightDueAmount + associeDueAr.coerceAtLeast(0L)
        val netPatrimony = totalAssets - totalLiabilities

        val totalProfit = uiState.cumulativeBusinessProfitAr + investmentsReturn
        val roiBase = when {
            netCapital > 0L -> netCapital
            totalInvestedCost > 0L -> totalInvestedCost
            else -> 0L
        }
        val roiPercent = if (roiBase > 0L) {
            ((totalProfit.toDouble() / roiBase.toDouble()) * 100.0).roundToInt()
        } else null

        val timelineEvents = buildList<Pair<Long, Long>> {
            apportsTxs.forEach { add(it.dateMillis to it.amountAr) }
            retraitsTxs.forEach { add(it.dateMillis to -it.amountAr) }
            investments.forEach {
                if (it.generatedReturnAr > 0L) add(it.dateMillis to it.generatedReturnAr)
                if (it.linkedTransactionId == null && it.status == "ACTIF") {
                    add(it.dateMillis to it.estimatedValueAr)
                }
            }
            uiState.sales.forEach { sale ->
                val grossProfit = (sale.totalPriceAr - (sale.unitCostAr * sale.quantity))
                if (grossProfit != 0L) add(sale.saleDate to grossProfit)
            }
        }.sortedBy { it.first }

        val series = mutableListOf<Long>()
        var running = 0L
        timelineEvents.forEach { (_, delta) ->
            running += delta
            series.add(running)
        }

        CapitalPatrimonyMetrics(
            totalApportsCapital = totalApports,
            totalRetraitsPerso = totalRetraits,
            netCapital = netCapital,
            capitalMovements = capitalMovements,
            totalTreasuryAr = totalTreasury,
            yuanValueAr = yuanValue,
            stockValueAr = stockValue,
            inRouteValueAr = inRouteValue,
            receivablesAr = receivables,
            totalInvestedCostAr = totalInvestedCost,
            activeInvestmentsValueAr = activeInvestmentsValue,
            activeInvestmentsCount = activeInvestments.size,
            investmentsReturnAr = investmentsReturn,
            totalAssetsAr = totalAssets,
            totalLiabilitiesAr = totalLiabilities,
            netPatrimonyAr = netPatrimony,
            totalProfitAr = totalProfit,
            roiPercent = roiPercent,
            evolutionSeries = series
        )
    }
}

@Composable
internal fun CapitalKpiTile(
    label: String,
    value: String,
    subtitle: String,
    accent: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontFamily = Inter,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun PatrimonyBreakdownBar(
    metrics: CapitalPatrimonyMetrics,
    colors: TransitaireColors
) {
    val stockAndTransit = metrics.stockValueAr + metrics.inRouteValueAr
    val segments = buildList<Triple<String, Long, Color>> {
        if (metrics.totalTreasuryAr > 0L) add(Triple("Cash Ar", metrics.totalTreasuryAr, colors.cyanAccent))
        if (metrics.yuanValueAr > 0L) add(Triple("Yuans ¥", metrics.yuanValueAr, colors.warningAccent))
        if (stockAndTransit > 0L) add(Triple("Stock & Transit", stockAndTransit, colors.violetAccent))
        if (metrics.activeInvestmentsValueAr > 0L) add(Triple("Actifs investis", metrics.activeInvestmentsValueAr, colors.successAccent))
        if (metrics.receivablesAr > 0L) add(Triple("Créances", metrics.receivablesAr, colors.expressAccent))
    }

    if (segments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(colors.badgeNeutralBg)
        )
        Text(
            text = "Aucun actif enregistré pour l'instant.",
            fontFamily = Inter,
            fontSize = 11.sp,
            color = colors.textSecondary
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(colors.badgeNeutralBg)
        ) {
            segments.forEach { (_, amount, color) ->
                Box(
                    modifier = Modifier
                        .weight(amount.toFloat())
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            segments.forEach { (label, amount, color) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Text(
                        text = "$label · ${CurrencyFormatter.formatAriary(amount)}",
                        fontFamily = Inter,
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
internal fun RealEvolutionSparkline(
    values: List<Long>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val minVal = values.minOrNull()?.toFloat() ?: 0f
        val maxVal = values.maxOrNull()?.toFloat() ?: 0f
        val span = (maxVal - minVal).takeIf { it > 0f } ?: 1f
        val padY = 4.dp.toPx()
        val usableH = (size.height - padY * 2f).coerceAtLeast(1f)
        val stepX = size.width / (values.size - 1).coerceAtLeast(1)

        val points = values.mapIndexed { idx, v ->
            val x = idx * stepX
            val normalizedY = (v.toFloat() - minVal) / span
            val y = size.height - padY - (normalizedY * usableH)
            Offset(x, y)
        }

        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawCircle(
            color = lineColor,
            radius = 3.2.dp.toPx(),
            center = points.last()
        )
    }
}

@Composable
internal fun InvestmentRowCard(
    investment: BusinessInvestmentEntity,
    dateFormatter: SimpleDateFormat,
    colors: TransitaireColors,
    onClick: () -> Unit
) {
    val cat = categoryInfo(investment.category)
    val (statusBg, statusFg) = when (investment.status) {
        "AMORTI" -> colors.cyanSoftBg to colors.cyanAccent
        "CEDE" -> colors.badgeNeutralBg to colors.textSecondary
        else -> colors.successSoftBg to colors.successAccent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.innerCardBg)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.cardBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = cat.emoji, fontSize = 18.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = investment.title,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(shape = RoundedCornerShape(6.dp), color = statusBg) {
                        Text(
                            text = investmentStatusLabel(investment.status),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = statusFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${cat.label} • ${dateFormatter.format(Date(investment.dateMillis))} • ${investment.accountName}",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = CurrencyFormatter.formatAriary(investment.amountAr),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.textPrimary
            )
            if (investment.generatedReturnAr > 0L) {
                Text(
                    text = "+${CurrencyFormatter.formatAriary(investment.generatedReturnAr)} retour",
                    fontFamily = Inter,
                    fontSize = 10.sp,
                    color = colors.successAccent
                )
            } else {
                Text(
                    text = "Val. ${CurrencyFormatter.formatAriary(investment.estimatedValueAr)}",
                    fontFamily = Inter,
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CapitalDateField(
    label: String,
    dateMillis: Long,
    colors: TransitaireColors,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }
    var showPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { showPicker = true },
        shape = RoundedCornerShape(14.dp),
        color = colors.innerCardBg,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    fontFamily = Inter,
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = dateFormatter.format(Date(dateMillis)),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
            }
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Choisir une date",
                tint = colors.cyanAccent,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = localMillisToUtcPickerMillis(dateMillis)
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { utcMillis ->
                            onDateSelected(utcPickerMillisToLocalNoon(utcMillis))
                        }
                        showPicker = false
                    }
                ) {
                    Text("Valider", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
internal fun EmptyCapitalBox(
    message: String,
    actionLabel: String,
    colors: TransitaireColors,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Inventory2, null, tint = colors.cyanAccent, modifier = Modifier.size(28.dp))
            Text(
                text = message,
                fontFamily = Inter,
                fontSize = 13.sp,
                color = colors.textSecondary
            )
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionLabel, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
internal fun CapitalKeyValueRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontSize = 12.sp,
            color = labelColor,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = valueColor
        )
    }
}
