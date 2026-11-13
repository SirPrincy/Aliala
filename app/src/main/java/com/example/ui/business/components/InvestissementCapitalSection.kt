package com.example.ui.business.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.ui.business.BusinessUiState
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

private data class CapitalPatrimonyMetrics(
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

private data class InvestmentCategoryOption(
    val code: String,
    val label: String,
    val emoji: String
)

private val INVESTMENT_CATEGORIES = listOf(
    InvestmentCategoryOption("PLACEMENT", "Argent / Placement", "💰"),
    InvestmentCategoryOption("MATERIEL", "Matériel", "📷"),
    InvestmentCategoryOption("LOGISTIQUE", "Logistique", "🛵"),
    InvestmentCategoryOption("STOCK_STRATEGIQUE", "Stock clé", "📦"),
    InvestmentCategoryOption("MARKETING", "Marketing", "📣"),
    InvestmentCategoryOption("INFRASTRUCTURE", "Local / Bureau", "🏪"),
    InvestmentCategoryOption("AUTRE", "Autre", "💼")
)

private fun categoryInfo(code: String): InvestmentCategoryOption {
    return INVESTMENT_CATEGORIES.firstOrNull { it.code.equals(code, ignoreCase = true) }
        ?: INVESTMENT_CATEGORIES.last()
}

private fun investmentStatusLabel(status: String): String {
    return when (status) {
        "AMORTI" -> "Amorti"
        "CEDE" -> "Cédé"
        else -> "Actif"
    }
}

private fun isCapitalContributionTx(tx: BusinessTransactionEntity): Boolean {
    val isIn = tx.type == "IN" || tx.type == "INCOME" || tx.type == "CAPITAL_IN"
    if (!isIn) return false
    return tx.referenceTag.contains("Capital", ignoreCase = true) ||
        tx.title.contains("Apport", ignoreCase = true)
}

private fun isCapitalWithdrawalTx(tx: BusinessTransactionEntity): Boolean {
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
private fun rememberCapitalPatrimonyMetrics(
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

        // Construction chronologique réelle à partir des apports, retraits, investissements & ventes
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
fun InvestissementCapitalCard(
    uiState: BusinessUiState,
    ariaryAccounts: List<BusinessAccountEntity>,
    transactions: List<BusinessTransactionEntity>,
    investments: List<BusinessInvestmentEntity>,
    empruntsDueAr: Long,
    associeDueAr: Long,
    onRecordCapitalMovement: (title: String, isContribution: Boolean, amountAr: Long, accountName: String, note: String, affectAccount: Boolean, dateMillis: Long) -> Unit,
    onAddInvestment: (title: String, category: String, amountAr: Long, estimatedValueAr: Long, generatedReturnAr: Long, accountName: String, notes: String, dateMillis: Long) -> Unit,
    onUpdateInvestment: (BusinessInvestmentEntity) -> Unit,
    onDeleteInvestment: (BusinessInvestmentEntity) -> Unit,
    onDeleteCapitalTransaction: (BusinessTransactionEntity) -> Unit,
    onCreateAccountClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = rememberTransitaireColors()
    val metrics = rememberCapitalPatrimonyMetrics(
        uiState = uiState,
        ariaryAccounts = ariaryAccounts,
        transactions = transactions,
        investments = investments,
        empruntsDueAr = empruntsDueAr,
        associeDueAr = associeDueAr
    )
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }

    var showDetailSheet by remember { mutableStateOf(false) }
    var capitalDialogIsContribution by remember { mutableStateOf<Boolean?>(null) }
    var showAddInvestmentDialog by remember { mutableStateOf(false) }
    var editingInvestment by remember { mutableStateOf<BusinessInvestmentEntity?>(null) }
    var inspectingInvestment by remember { mutableStateOf<BusinessInvestmentEntity?>(null) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header + ROI réel + Bouton Détails
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💼", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "Investissement & Capital",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Patrimoine, apports & actifs engagés",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val roi = metrics.roiPercent
                    val (roiBg, roiFg, roiLabel) = when {
                        roi == null -> Triple(colors.badgeNeutralBg, colors.textSecondary, "À initialiser")
                        roi >= 0 -> Triple(colors.successSoftBg, colors.successAccent, "ROI +$roi%")
                        else -> Triple(colors.dangerSoftBg, colors.dangerAccent, "ROI $roi%")
                    }
                    Surface(shape = CircleShape, color = roiBg) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (roi != null && roi < 0) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = roiFg,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = roiLabel,
                                fontFamily = PlusJakartaSans,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = roiFg
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.cyanSoftBg,
                        modifier = Modifier
                            .clickable { showDetailSheet = true }
                            .testTag("btn_open_capital_detail")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gérer",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = colors.cyanAccent
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Ouvrir Investissement & Capital",
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // 2. Grille 4 KPI responsives
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 560.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CapitalKpiTile(
                            label = "Capital net",
                            value = CurrencyFormatter.formatAriary(metrics.netCapital),
                            subtitle = "Apports ${CurrencyFormatter.formatAriary(metrics.totalApportsCapital)}",
                            accent = colors.cyanAccent,
                            bg = colors.cyanSoftBg.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        )
                        CapitalKpiTile(
                            label = "Patrimoine net",
                            value = CurrencyFormatter.formatAriary(metrics.netPatrimonyAr),
                            subtitle = "Actifs ${CurrencyFormatter.formatAriary(metrics.totalAssetsAr)}",
                            accent = colors.violetAccent,
                            bg = colors.violetSoftBg.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        )
                        CapitalKpiTile(
                            label = "Investissements",
                            value = CurrencyFormatter.formatAriary(metrics.activeInvestmentsValueAr),
                            subtitle = "${metrics.activeInvestmentsCount} actif(s)",
                            accent = colors.expressAccent,
                            bg = colors.innerCardBg,
                            modifier = Modifier.weight(1f)
                        )
                        CapitalKpiTile(
                            label = "Bénéfice cumulé",
                            value = "${if (metrics.totalProfitAr >= 0L) "+" else ""}${CurrencyFormatter.formatAriary(metrics.totalProfitAr)}",
                            subtitle = "Retraits ${CurrencyFormatter.formatAriary(metrics.totalRetraitsPerso)}",
                            accent = if (metrics.totalProfitAr >= 0L) colors.successAccent else colors.dangerAccent,
                            bg = colors.innerCardBg,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CapitalKpiTile(
                                label = "Capital net engagé",
                                value = CurrencyFormatter.formatAriary(metrics.netCapital),
                                subtitle = "Apports ${CurrencyFormatter.formatAriary(metrics.totalApportsCapital)}",
                                accent = colors.cyanAccent,
                                bg = colors.cyanSoftBg.copy(alpha = 0.65f),
                                modifier = Modifier.weight(1f)
                            )
                            CapitalKpiTile(
                                label = "Patrimoine net",
                                value = CurrencyFormatter.formatAriary(metrics.netPatrimonyAr),
                                subtitle = if (metrics.totalLiabilitiesAr > 0L) "Dettes -${CurrencyFormatter.formatAriary(metrics.totalLiabilitiesAr)}" else "Actifs nets",
                                accent = colors.violetAccent,
                                bg = colors.violetSoftBg.copy(alpha = 0.65f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CapitalKpiTile(
                                label = "Matériel & Projets",
                                value = CurrencyFormatter.formatAriary(metrics.activeInvestmentsValueAr),
                                subtitle = "${metrics.activeInvestmentsCount} investissement(s) actif(s)",
                                accent = colors.expressAccent,
                                bg = colors.innerCardBg,
                                modifier = Modifier.weight(1f)
                            )
                            CapitalKpiTile(
                                label = "Bénéfice cumulé",
                                value = "${if (metrics.totalProfitAr >= 0L) "+" else ""}${CurrencyFormatter.formatAriary(metrics.totalProfitAr)}",
                                subtitle = "Retraits ${CurrencyFormatter.formatAriary(metrics.totalRetraitsPerso)}",
                                accent = if (metrics.totalProfitAr >= 0L) colors.successAccent else colors.dangerAccent,
                                bg = colors.innerCardBg,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. Répartition réelle du patrimoine + vraie courbe d'évolution
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.innerCardBg)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Où travaille votre capital",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Actifs totaux : ${CurrencyFormatter.formatAriary(metrics.totalAssetsAr)}",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }

                        if (metrics.evolutionSeries.size >= 2) {
                            RealEvolutionSparkline(
                                values = metrics.evolutionSeries,
                                lineColor = if (metrics.evolutionSeries.last() >= metrics.evolutionSeries.first()) colors.successAccent else colors.dangerAccent,
                                modifier = Modifier.size(width = 88.dp, height = 30.dp)
                            )
                        } else {
                            Surface(shape = RoundedCornerShape(8.dp), color = colors.badgeNeutralBg) {
                                Text(
                                    text = "${metrics.capitalMovements.size + investments.size} op.",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    PatrimonyBreakdownBar(metrics = metrics, colors = colors)
                }
            }

            // 4. Liste rapide des investissements récents
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INVESTISSEMENTS & IMMOBILISATIONS (${investments.size})",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    if (investments.isNotEmpty()) {
                        Text(
                            text = "Voir tout",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.cyanAccent,
                            modifier = Modifier.clickable { showDetailSheet = true }
                        )
                    }
                }

                if (investments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.innerCardBg)
                            .clickable { showAddInvestmentDialog = true }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Aucun investissement enregistré (matériel, logistique, boutique, marketing). Touchez + Investir pour démarrer.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                } else {
                    investments.take(3).forEach { inv ->
                        InvestmentRowCard(
                            investment = inv,
                            dateFormatter = dateFormatter,
                            colors = colors,
                            onClick = { inspectingInvestment = inv }
                        )
                    }
                }
            }

            // 5. Boutons d'action rapides (tactiles >= 48dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { capitalDialogIsContribution = true }
                        .testTag("btn_capital_apport"),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cyanSoftBg
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AddCircle, null, tint = colors.cyanAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Apport",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.cyanAccent,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { capitalDialogIsContribution = false }
                        .testTag("btn_capital_retrait"),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.dangerSoftBg
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.RemoveCircle, null, tint = colors.dangerAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Retrait",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.dangerAccent,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = { showAddInvestmentDialog = true },
                    modifier = Modifier
                        .weight(1.15f)
                        .height(48.dp)
                        .testTag("btn_add_investment"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.violetSolidBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Investir",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }

    // Dialogues
    capitalDialogIsContribution?.let { isContribution ->
        AddCapitalMovementDialog(
            isContribution = isContribution,
            ariaryAccounts = ariaryAccounts,
            onDismiss = { capitalDialogIsContribution = null },
            onCreateAccountClick = {
                capitalDialogIsContribution = null
                onCreateAccountClick()
            },
            onConfirm = { title, amountAr, accountName, note, affectAccount, dateMillis ->
                onRecordCapitalMovement(title, isContribution, amountAr, accountName, note, affectAccount, dateMillis)
                capitalDialogIsContribution = null
            }
        )
    }

    if (showAddInvestmentDialog || editingInvestment != null) {
        AddOrEditInvestmentDialog(
            existing = editingInvestment,
            ariaryAccounts = ariaryAccounts,
            onDismiss = {
                showAddInvestmentDialog = false
                editingInvestment = null
            },
            onConfirmCreate = { title, cat, amountAr, estValAr, returnAr, accName, notes, dateMillis ->
                onAddInvestment(title, cat, amountAr, estValAr, returnAr, accName, notes, dateMillis)
                showAddInvestmentDialog = false
            },
            onConfirmUpdate = { updated ->
                onUpdateInvestment(updated)
                editingInvestment = null
            }
        )
    }

    inspectingInvestment?.let { inv ->
        val liveInv = investments.firstOrNull { it.id == inv.id } ?: inv
        InvestmentDetailDialog(
            investment = liveInv,
            dateFormatter = dateFormatter,
            onDismiss = { inspectingInvestment = null },
            onEdit = {
                inspectingInvestment = null
                editingInvestment = liveInv
            },
            onStatusChange = { newStatus ->
                val adjustedVal = when (newStatus) {
                    "CEDE" -> 0L
                    "AMORTI" -> liveInv.estimatedValueAr
                    else -> if (liveInv.estimatedValueAr == 0L) liveInv.amountAr else liveInv.estimatedValueAr
                }
                onUpdateInvestment(liveInv.copy(status = newStatus, estimatedValueAr = adjustedVal))
            },
            onDelete = {
                onDeleteInvestment(liveInv)
                inspectingInvestment = null
            }
        )
    }

    if (showDetailSheet) {
        CapitalPatrimonyDetailDialog(
            metrics = metrics,
            investments = investments,
            dateFormatter = dateFormatter,
            onDismiss = { showDetailSheet = false },
            onApportClick = { capitalDialogIsContribution = true },
            onRetraitClick = { capitalDialogIsContribution = false },
            onAddInvestmentClick = { showAddInvestmentDialog = true },
            onInvestmentClick = { inspectingInvestment = it },
            onDeleteCapitalTx = onDeleteCapitalTransaction
        )
    }
}

@Composable
private fun CapitalKpiTile(
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
private fun PatrimonyBreakdownBar(
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
private fun RealEvolutionSparkline(
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
private fun InvestmentRowCard(
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
private fun CapitalDateField(
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
private fun AddCapitalMovementDialog(
    isContribution: Boolean,
    ariaryAccounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onCreateAccountClick: () -> Unit = {},
    onConfirm: (
        title: String,
        amountAr: Long,
        accountName: String,
        note: String,
        affectAccount: Boolean,
        dateMillis: Long
    ) -> Unit
) {
    val colors = rememberTransitaireColors()
    var title by remember {
        mutableStateOf(if (isContribution) "Apport de capital" else "Retrait personnel")
    }
    var amountText by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var affectAccount by remember(ariaryAccounts, isContribution) {
        mutableStateOf(if (isContribution) true else ariaryAccounts.isNotEmpty())
    }
    var selectedAccount by remember(ariaryAccounts) {
        mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "")
    }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val parsedAmount = amountText.toLongOrNull() ?: 0L
    val currentAccountBalance = ariaryAccounts.firstOrNull {
        it.name.equals(selectedAccount, ignoreCase = true)
    }?.balanceAr ?: 0L
    val isContributionBlocked = isContribution && ariaryAccounts.isEmpty()
    val willOverdraw = affectAccount && !isContribution && parsedAmount > currentAccountBalance && ariaryAccounts.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Text(
                text = if (isContribution) "Apport en capital" else "Retrait personnel",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isContributionBlocked) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.dangerSoftBg,
                        border = BorderStroke(1.dp, colors.dangerAccent.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Action bloquée : aucun compte disponible.",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.dangerAccent
                            )
                            Text(
                                text = "Tout apport doit être enregistré dans un compte (ex. Espèces, Mvola). Créez d'abord un compte dans Comptes & Modes.",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                            Button(
                                onClick = onCreateAccountClick,
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Créer un compte", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = null
                        },
                        label = { Text("Libellé *") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { ch -> ch.isDigit() }
                            errorMessage = null
                        },
                        label = { Text("Montant (Ar) *") },
                        placeholder = { Text("ex. 500000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    CapitalDateField(
                        label = if (isContribution) "Date de l'apport" else "Date du retrait",
                        dateMillis = selectedDateMillis,
                        colors = colors,
                        onDateSelected = { selectedDateMillis = it }
                    )

                    if (!isContribution) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (ariaryAccounts.isNotEmpty()) {
                                        affectAccount = !affectAccount
                                        errorMessage = null
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = colors.innerCardBg,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Impacter un compte & Transactions",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (affectAccount) {
                                            "Débite le compte et ajoute une sortie dans Transactions"
                                        } else {
                                            "Hors caisse : suivi du capital uniquement (sans transaction)"
                                        },
                                        fontFamily = Inter,
                                        fontSize = 10.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Switch(
                                    checked = affectAccount,
                                    onCheckedChange = {
                                        if (ariaryAccounts.isNotEmpty() || !it) {
                                            affectAccount = it
                                            errorMessage = null
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (isContribution || affectAccount) {
                        Text(
                            text = if (isContribution) "Compte crédité (obligatoire) *" else "Compte Ariary débité",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ariaryAccounts.forEach { acc ->
                                val isSel = selectedAccount.equals(acc.name, ignoreCase = true)
                                Surface(
                                    modifier = Modifier.clickable {
                                        selectedAccount = acc.name
                                        errorMessage = null
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) CyanPrimary else colors.innerCardBg,
                                    border = if (isSel) null else BorderStroke(1.dp, colors.border)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                        Text(
                                            text = acc.name,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isSel) Color.Black else colors.textPrimary
                                        )
                                        Text(
                                            text = CurrencyFormatter.formatAriary(acc.balanceAr),
                                            fontFamily = Inter,
                                            fontSize = 10.sp,
                                            color = if (isSel) Color.Black.copy(alpha = 0.75f) else colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        if (willOverdraw) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.warningSoftBg
                            ) {
                                Text(
                                    text = "Solde actuel sur $selectedAccount : ${CurrencyFormatter.formatAriary(currentAccountBalance)}. Le solde deviendra négatif après ce retrait.",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.warningAccent,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note / Précision (optionnel)") },
                        placeholder = { Text("ex. Épargne perso, Bénéfice Q1") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.dangerAccent
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isContributionBlocked,
                onClick = {
                    when {
                        isContributionBlocked -> errorMessage = "Créez d'abord un compte."
                        title.isBlank() -> errorMessage = "Veuillez saisir un libellé."
                        parsedAmount <= 0L -> errorMessage = "Veuillez saisir un montant supérieur à 0 Ar."
                        (isContribution || affectAccount) && selectedAccount.isBlank() ->
                            errorMessage = "Veuillez choisir un compte."
                        else -> onConfirm(
                            title.trim(),
                            parsedAmount,
                            if (isContribution || affectAccount) selectedAccount.trim() else "Hors caisse",
                            note.trim(),
                            isContribution || affectAccount,
                            selectedDateMillis
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isContribution) CyanPrimary else colors.dangerAccent,
                    contentColor = if (isContribution) Color.Black else Color.White
                )
            ) {
                Text(
                    text = if (isContribution) "Enregistrer l'apport" else "Enregistrer le retrait",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", fontFamily = PlusJakartaSans)
            }
        }
    )
}

@Composable
private fun AddOrEditInvestmentDialog(
    existing: BusinessInvestmentEntity?,
    ariaryAccounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onConfirmCreate: (
        title: String,
        category: String,
        amountAr: Long,
        estimatedValueAr: Long,
        generatedReturnAr: Long,
        accountName: String,
        notes: String,
        dateMillis: Long
    ) -> Unit,
    onConfirmUpdate: (BusinessInvestmentEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    val isEdit = existing != null

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "PLACEMENT") }
    var amountText by remember { mutableStateOf(if (existing != null && existing.amountAr > 0L) existing.amountAr.toString() else "") }
    var estimatedValueText by remember {
        mutableStateOf(if (existing != null) existing.estimatedValueAr.toString() else "")
    }
    var valueManuallyEdited by remember { mutableStateOf(existing != null) }
    var generatedReturnText by remember {
        mutableStateOf(if (existing != null && existing.generatedReturnAr > 0L) existing.generatedReturnAr.toString() else "")
    }
    var status by remember { mutableStateOf(existing?.status ?: "ACTIF") }
    var debitAccount by remember(ariaryAccounts) { mutableStateOf(ariaryAccounts.isNotEmpty()) }
    var selectedAccount by remember(ariaryAccounts) {
        mutableStateOf(existing?.accountName ?: ariaryAccounts.firstOrNull()?.name ?: "Hors caisse")
    }
    var selectedDateMillis by remember { mutableLongStateOf(existing?.dateMillis ?: System.currentTimeMillis()) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val titlePlaceholder = when (category) {
        "PLACEMENT" -> "ex. Épargne bloquée, Parts boutique, Prêt rémunéré..."
        "MATERIEL" -> "ex. Studio photo, Imprimante, Étagères..."
        "LOGISTIQUE" -> "ex. Moto livraison, Balance colis..."
        "STOCK_STRATEGIQUE" -> "ex. Lot réserve haute saison..."
        "MARKETING" -> "ex. Campagne Facebook Ads, Shooting..."
        "INFRASTRUCTURE" -> "ex. Aménagement boutique, Caution local..."
        else -> "Nom du projet ou de l'investissement..."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBg)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
                // Top Bar
                Surface(color = colors.cardBg, shadowElevation = 1.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Close, "Fermer", tint = colors.textPrimary)
                            }
                            Text(
                                text = if (isEdit) "Modifier l'investissement" else "Nouvel investissement",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = colors.textPrimary
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Type d'investissement",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        INVESTMENT_CATEGORIES.forEach { cat ->
                            val isSel = category == cat.code
                            Surface(
                                modifier = Modifier.clickable { category = cat.code },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) colors.violetSoftBg else colors.cardBg,
                                border = BorderStroke(if (isSel) 1.5.dp else 1.dp, if (isSel) colors.violetAccent else colors.border)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(cat.emoji, fontSize = 15.sp)
                                    Text(
                                        text = cat.label,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSel) colors.violetAccent else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = null
                        },
                        label = { Text("Nom de l'investissement *") },
                        placeholder = { Text(titlePlaceholder) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    CapitalDateField(
                        label = "Date de l'investissement",
                        dateMillis = selectedDateMillis,
                        colors = colors,
                        onDateSelected = { selectedDateMillis = it }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                amountText = filtered
                                if (!valueManuallyEdited) {
                                    estimatedValueText = filtered
                                }
                                errorMessage = null
                            },
                            label = { Text("Montant investi (Ar) *") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = estimatedValueText,
                            onValueChange = { input ->
                                estimatedValueText = input.filter { it.isDigit() }
                                valueManuallyEdited = true
                            },
                            label = { Text("Valeur actuelle (Ar)") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = generatedReturnText,
                        onValueChange = { generatedReturnText = it.filter { ch -> ch.isDigit() } },
                        label = {
                            Text(
                                if (category == "PLACEMENT") "Intérêts / Gains générés (Ar, optionnel)"
                                else "Retours / Revenus générés (Ar, optionnel)"
                            )
                        },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isEdit) {
                        Text(
                            text = "Statut de l'actif",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("ACTIF" to "Actif", "AMORTI" to "Amorti", "CEDE" to "Cédé").forEach { (code, label) ->
                                val isSel = status == code
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { status = code },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) CyanPrimary else colors.cardBg,
                                    border = if (isSel) null else BorderStroke(1.dp, colors.border)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSel) Color.Black else colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { debitAccount = !debitAccount },
                            shape = RoundedCornerShape(14.dp),
                            color = colors.cardBg,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Débiter un compte & Transactions",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (debitAccount) {
                                            "Débite le compte choisi et enregistre la sortie dans Transactions"
                                        } else {
                                            "Hors caisse : suivi patrimonial uniquement (aucune transaction)"
                                        },
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Switch(
                                    checked = debitAccount,
                                    onCheckedChange = { debitAccount = it }
                                )
                            }
                        }

                        if (debitAccount) {
                            if (ariaryAccounts.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ariaryAccounts.forEach { acc ->
                                        val isSel = selectedAccount.equals(acc.name, ignoreCase = true)
                                        Surface(
                                            modifier = Modifier.clickable { selectedAccount = acc.name },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSel) CyanPrimary else colors.cardBg,
                                            border = if (isSel) null else BorderStroke(1.dp, colors.border)
                                        ) {
                                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                                Text(
                                                    text = acc.name,
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (isSel) Color.Black else colors.textPrimary
                                                )
                                                Text(
                                                    text = CurrencyFormatter.formatAriary(acc.balanceAr),
                                                    fontFamily = Inter,
                                                    fontSize = 10.sp,
                                                    color = if (isSel) Color.Black.copy(alpha = 0.75f) else colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = selectedAccount,
                                    onValueChange = { selectedAccount = it },
                                    label = { Text("Nom du compte à débiter") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Détails (optionnel)") },
                        placeholder = { Text("Conditions, taux, échéance, garantie...") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.dangerAccent
                        )
                    }
                }

                Surface(color = colors.cardBg, shadowElevation = 4.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Annuler", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val parsedAmount = amountText.toLongOrNull() ?: 0L
                                val parsedEst = estimatedValueText.toLongOrNull() ?: parsedAmount
                                val parsedReturn = generatedReturnText.toLongOrNull() ?: 0L
                                when {
                                    title.isBlank() -> errorMessage = "Veuillez saisir le nom de l'investissement."
                                    parsedAmount <= 0L -> errorMessage = "Veuillez saisir un montant investi valide."
                                    isEdit && existing != null -> {
                                        onConfirmUpdate(
                                            existing.copy(
                                                title = title.trim(),
                                                category = category,
                                                amountAr = parsedAmount,
                                                estimatedValueAr = parsedEst.coerceAtLeast(0L),
                                                generatedReturnAr = parsedReturn.coerceAtLeast(0L),
                                                status = status,
                                                dateMillis = selectedDateMillis,
                                                notes = notes.trim()
                                            )
                                        )
                                    }
                                    else -> {
                                        onConfirmCreate(
                                            title.trim(),
                                            category,
                                            parsedAmount,
                                            parsedEst.coerceAtLeast(0L),
                                            parsedReturn.coerceAtLeast(0L),
                                            if (debitAccount) selectedAccount.trim() else "Hors caisse",
                                            notes.trim(),
                                            selectedDateMillis
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1.4f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (isEdit) "Mettre à jour" else "Enregistrer",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestmentDetailDialog(
    investment: BusinessInvestmentEntity,
    dateFormatter: SimpleDateFormat,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val cat = categoryInfo(investment.category)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val netValueDelta = (investment.estimatedValueAr + investment.generatedReturnAr) - investment.amountAr
    val individualRoi = if (investment.amountAr > 0L) {
        ((netValueDelta.toDouble() / investment.amountAr.toDouble()) * 100.0).roundToInt()
    } else 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(cat.emoji, fontSize = 24.sp)
                Column {
                    Text(
                        text = investment.title,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${cat.label} • ${dateFormatter.format(Date(investment.dateMillis))}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        KeyValueRow("Montant investi", CurrencyFormatter.formatAriary(investment.amountAr), colors.textSecondary, colors.textPrimary)
                        KeyValueRow("Valeur actuelle", CurrencyFormatter.formatAriary(investment.estimatedValueAr), colors.textSecondary, colors.cyanAccent)
                        KeyValueRow("Retours générés", CurrencyFormatter.formatAriary(investment.generatedReturnAr), colors.textSecondary, colors.successAccent)
                        KeyValueRow("Rendement net", "${if (individualRoi >= 0) "+" else ""}$individualRoi %", colors.textSecondary, if (individualRoi >= 0) colors.successAccent else colors.dangerAccent)
                        KeyValueRow("Financement", investment.accountName, colors.textSecondary, colors.textPrimary)
                    }
                }

                Text(
                    text = "Statut rapide",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ACTIF" to "Actif", "AMORTI" to "Amorti", "CEDE" to "Cédé").forEach { (code, label) ->
                        val isSel = investment.status == code
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onStatusChange(code) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) CyanPrimary else colors.innerCardBg
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.Black else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                if (investment.notes.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.innerCardBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = investment.notes,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Modifier", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.DeleteOutline, null, tint = colors.dangerAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Supprimer", fontFamily = PlusJakartaSans, color = colors.dangerAccent, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss) {
                    Text("Fermer", fontFamily = PlusJakartaSans)
                }
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = colors.cardBg,
            title = {
                Text(
                    text = "Supprimer cet investissement ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = colors.dangerAccent
                )
            },
            text = {
                val txNote = if (investment.linkedTransactionId != null) {
                    " Le débit associé sur le compte « ${investment.accountName} » sera également annulé."
                } else ""
                Text(
                    text = "Voulez-vous supprimer « ${investment.title} » ?$txNote",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent, contentColor = Color.White)
                ) {
                    Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }
}

@Composable
private fun CapitalPatrimonyDetailDialog(
    metrics: CapitalPatrimonyMetrics,
    investments: List<BusinessInvestmentEntity>,
    dateFormatter: SimpleDateFormat,
    onDismiss: () -> Unit,
    onApportClick: () -> Unit,
    onRetraitClick: () -> Unit,
    onAddInvestmentClick: () -> Unit,
    onInvestmentClick: (BusinessInvestmentEntity) -> Unit,
    onDeleteCapitalTx: (BusinessTransactionEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    var selectedTab by remember { mutableStateOf("Patrimoine") } // "Patrimoine", "Investissements", "Capital"
    var pendingDeleteTx by remember { mutableStateOf<BusinessTransactionEntity?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBg)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
            ) {
                // Header
                Surface(color = colors.cardBg, shadowElevation = 1.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Close, "Fermer", tint = colors.textPrimary)
                            }
                            Column {
                                Text(
                                    text = "Investissement & Capital",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Patrimoine net : ${CurrencyFormatter.formatAriary(metrics.netPatrimonyAr)}",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.cyanAccent
                                )
                            }
                        }

                        Button(
                            onClick = onAddInvestmentClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Investir", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Sub-tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Patrimoine" to "Patrimoine",
                        "Investissements" to "Actifs (${investments.size})",
                        "Capital" to "Capital (${metrics.capitalMovements.size})"
                    ).forEach { (key, label) ->
                        val isSel = selectedTab == key
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = key },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) CyanPrimary else colors.cardBg,
                            border = if (isSel) null else BorderStroke(1.dp, colors.border)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSel) Color.Black else colors.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedTab) {
                        "Patrimoine" -> {
                            // Bilan Actifs / Passifs
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = colors.cardBg,
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "ACTIFS DU BUSINESS",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = colors.successAccent
                                    )
                                    KeyValueRow("Trésorerie liquide (Ariary)", CurrencyFormatter.formatAriary(metrics.totalTreasuryAr), colors.textSecondary, colors.textPrimary)
                                    KeyValueRow("Portefeuille Yuans (¥)", CurrencyFormatter.formatAriary(metrics.yuanValueAr), colors.textSecondary, colors.textPrimary)
                                    KeyValueRow("Stock disponible en rayon", CurrencyFormatter.formatAriary(metrics.stockValueAr), colors.textSecondary, colors.textPrimary)
                                    KeyValueRow("Commandes / Achats en transit", CurrencyFormatter.formatAriary(metrics.inRouteValueAr), colors.textSecondary, colors.textPrimary)
                                    KeyValueRow("Matériel & Projets actifs", CurrencyFormatter.formatAriary(metrics.activeInvestmentsValueAr), colors.textSecondary, colors.textPrimary)
                                    KeyValueRow("Créances clients à encaisser", CurrencyFormatter.formatAriary(metrics.receivablesAr), colors.textSecondary, colors.textPrimary)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(colors.border)
                                    )
                                    KeyValueRow("Total Actifs", CurrencyFormatter.formatAriary(metrics.totalAssetsAr), colors.textPrimary, colors.cyanAccent)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = colors.cardBg,
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "PASSIFS & SYNTHÈSE",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = colors.dangerAccent
                                    )
                                    KeyValueRow("Dettes / Fret / Compte associé", CurrencyFormatter.formatAriary(metrics.totalLiabilitiesAr), colors.textSecondary, colors.dangerAccent)
                                    KeyValueRow("Patrimoine Net Réel", CurrencyFormatter.formatAriary(metrics.netPatrimonyAr), colors.textPrimary, colors.violetAccent)
                                    KeyValueRow("Capital Net Engagé", CurrencyFormatter.formatAriary(metrics.netCapital), colors.textSecondary, colors.textPrimary)
                                    KeyValueRow(
                                        "Bénéfice cumulé (Ventes + Retours)",
                                        "${if (metrics.totalProfitAr >= 0L) "+" else ""}${CurrencyFormatter.formatAriary(metrics.totalProfitAr)}",
                                        colors.textSecondary,
                                        if (metrics.totalProfitAr >= 0L) colors.successAccent else colors.dangerAccent
                                    )
                                }
                            }
                        }

                        "Investissements" -> {
                            if (investments.isEmpty()) {
                                EmptyCapitalBox(
                                    message = "Aucun investissement matériel ou projet enregistré.",
                                    actionLabel = "+ Ajouter un investissement",
                                    colors = colors,
                                    onAction = onAddInvestmentClick
                                )
                            } else {
                                investments.forEach { inv ->
                                    InvestmentRowCard(
                                        investment = inv,
                                        dateFormatter = dateFormatter,
                                        colors = colors,
                                        onClick = { onInvestmentClick(inv) }
                                    )
                                }
                            }
                        }

                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onApportClick,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("+ Apport capital", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = onRetraitClick,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("- Retrait perso", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.dangerAccent)
                                }
                            }

                            if (metrics.capitalMovements.isEmpty()) {
                                EmptyCapitalBox(
                                    message = "Aucun apport en capital ni retrait personnel enregistré.",
                                    actionLabel = "+ Enregistrer un apport initial",
                                    colors = colors,
                                    onAction = onApportClick
                                )
                            } else {
                                metrics.capitalMovements.forEach { tx ->
                                    val isIn = isCapitalContributionTx(tx)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(colors.cardBg)
                                            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = tx.title,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "${dateFormatter.format(Date(tx.dateMillis))} • ${tx.accountName}",
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${if (isIn) "+" else "-"}${CurrencyFormatter.formatAriary(tx.amountAr)}",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isIn) colors.successAccent else colors.dangerAccent
                                            )
                                            IconButton(
                                                onClick = { pendingDeleteTx = tx },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, "Supprimer mouvement", tint = colors.dangerAccent, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    pendingDeleteTx?.let { tx ->
        AlertDialog(
            onDismissRequest = { pendingDeleteTx = null },
            containerColor = colors.cardBg,
            title = {
                Text("Supprimer ce mouvement ?", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = colors.dangerAccent)
            },
            text = {
                val isHorsCaisse = tx.type == "CAPITAL_IN" || tx.type == "CAPITAL_OUT"
                Text(
                    text = if (isHorsCaisse) {
                        "Supprimer « ${tx.title} » (${CurrencyFormatter.formatAriary(tx.amountAr)}) ? Ce mouvement hors caisse sera retiré du suivi du capital."
                    } else {
                        "Supprimer « ${tx.title} » (${CurrencyFormatter.formatAriary(tx.amountAr)}) ajustera le solde du compte « ${tx.accountName} »."
                    },
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCapitalTx(tx)
                        pendingDeleteTx = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent, contentColor = Color.White)
                ) {
                    Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteTx = null }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }
}

@Composable
private fun EmptyCapitalBox(
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
private fun KeyValueRow(
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
