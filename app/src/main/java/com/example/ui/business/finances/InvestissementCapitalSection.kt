package com.example.ui.business.finances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Locale

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
