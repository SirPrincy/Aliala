package com.example.ui.business.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessNavigationState
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanIconBadge
import com.example.ui.business.shared.HumanProgressBar
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSectionHeader
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import kotlin.math.roundToLong

internal fun BusinessDebtEntity.debtTotalDueAr(): Long {
    val interest = (principalAr.toDouble() * interestPercent / 100.0).roundToLong().coerceAtLeast(0L)
    return principalAr + interest
}

internal fun BusinessDebtEntity.debtRemainingAr(): Long {
    return (debtTotalDueAr() - paidAr).coerceAtLeast(0L)
}

internal fun isCapitalContributionTx(tx: BusinessTransactionEntity): Boolean =
    (tx.type == "IN" || tx.type == "INCOME" || tx.type == "CAPITAL_IN") &&
        (tx.referenceTag.contains("Capital", ignoreCase = true) || tx.title.contains("Apport", ignoreCase = true))

internal fun isCapitalWithdrawalTx(tx: BusinessTransactionEntity): Boolean =
    (tx.type == "OUT" || tx.type == "EXPENSE" || tx.type == "CAPITAL_OUT") &&
        (tx.referenceTag.contains("Retrait", ignoreCase = true) || tx.title.contains("Retrait", ignoreCase = true))

internal fun isCapitalMovementTx(tx: BusinessTransactionEntity): Boolean =
    isCapitalContributionTx(tx) || isCapitalWithdrawalTx(tx)

@Composable
internal fun FinancesAdvancedSection(
    uiState: BusinessUiState,
    ariaryAccounts: List<BusinessAccountEntity>,
    transactions: List<BusinessTransactionEntity>,
    investments: List<BusinessInvestmentEntity>,
    debts: List<BusinessDebtEntity>,
    unpaidSales: List<BusinessSaleEntity>,
    unpaidFreightOrders: List<BusinessOrderEntity>,
    navState: BusinessNavigationState,
    colors: TransitaireColors,
    onDeleteInvestment: (BusinessInvestmentEntity) -> Unit,
    onDeleteDebt: (BusinessDebtEntity) -> Unit
) {
    val totalTreasuryAr = ariaryAccounts.sumOf { it.balanceAr }
    val capitalMovements = remember(transactions) {
        transactions.filter { isCapitalMovementTx(it) }.sortedByDescending { it.dateMillis }
    }
    val apportsAr = remember(capitalMovements) {
        capitalMovements.filter { isCapitalContributionTx(it) }.sumOf { it.amountAr }
    }
    val retraitsAr = remember(capitalMovements) {
        capitalMovements.filter { isCapitalWithdrawalTx(it) }.sumOf { it.amountAr }
    }
    val netCapital = apportsAr - retraitsAr
    val investmentsValue = investments.filter { it.status == "ACTIF" }.sumOf { it.estimatedValueAr }
    val borrowedDue = debts.filter { it.direction == "BORROWED" }.sumOf { it.debtRemainingAr() }
    val lentDue = debts.filter { it.direction == "LENT" }.sumOf { it.debtRemainingAr() }
    val netPatrimony = (totalTreasuryAr + uiState.estimatedYuanValueAr + uiState.totalStockValueAr + uiState.inRouteAmountAr + investmentsValue + uiState.clientReceivablesAmount + lentDue) -
        (borrowedDue + uiState.freightDueAmount)

    val forecastIn = unpaidSales.sumOf { (it.totalPriceAr - it.paidAmountAr).coerceAtLeast(0L) } + lentDue
    val forecastOut = unpaidFreightOrders.sumOf { it.freightCostAr } + borrowedDue
    val projected30d = totalTreasuryAr + forecastIn - forecastOut

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. Capital & Patrimoine
        HumanSectionCard(colors = colors) {
            HumanSectionHeader(
                title = "Capital & Patrimoine",
                subtitle = "Valeur nette estimée (tréso + stock + yuans + créances)",
                colors = colors
            )
            Text(
                text = CurrencyFormatter.formatAriary(netPatrimony),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = colors.cyanAccent
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HumanTonalButton(
                    text = "Apport",
                    icon = Icons.Default.ArrowDownward,
                    onClick = { navState.activeSheet = BusinessActiveSheet.CapitalMovement(isContribution = true) },
                    colors = colors,
                    containerColor = colors.successSoftBg,
                    contentColor = colors.successAccent,
                    modifier = Modifier.weight(1f)
                )
                HumanTonalButton(
                    text = "Retrait",
                    icon = Icons.Default.ArrowUpward,
                    onClick = { navState.activeSheet = BusinessActiveSheet.CapitalMovement(isContribution = false) },
                    colors = colors,
                    containerColor = colors.roseSoftBg,
                    contentColor = colors.roseAccent,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "Capital net engagé : ${CurrencyFormatter.formatAriary(netCapital)}",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = colors.textSecondary
            )

            if (capitalMovements.isNotEmpty()) {
                capitalMovements.forEach { tx ->
                    val isApport = isCapitalContributionTx(tx)
                    HumanHairline(colors)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navState.activeSheet = BusinessActiveSheet.CapitalMovement(
                                    isContribution = isApport,
                                    existing = tx
                                )
                            }
                            .padding(vertical = 4.dp)
                            .testTag("capital_row_${tx.id}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.title,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${DateUtils.formatShortDate(tx.dateMillis)} · ${tx.accountName}",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = (if (isApport) "+" else "-") + CurrencyFormatter.formatCompactAriary(tx.amountAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isApport) colors.successAccent else colors.roseAccent
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Investissements
        HumanSectionCard(colors = colors) {
            HumanSectionHeader(
                title = "Investissements (${investments.size})",
                actionText = "+ Ajouter",
                onActionClick = { navState.activeSheet = BusinessActiveSheet.InvestmentForm(null) },
                colors = colors
            )
            if (investments.isEmpty()) {
                Text(
                    text = "Aucun matériel, local ou placement enregistré.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            } else {
                investments.forEachIndexed { idx, inv ->
                    if (idx > 0) HumanHairline(colors)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navState.activeSheet = BusinessActiveSheet.InvestmentForm(inv) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = inv.title,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${DateUtils.formatShortDate(inv.dateMillis)} · Valeur ${CurrencyFormatter.formatCompactAriary(inv.estimatedValueAr)}",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                        IconButton(onClick = { onDeleteInvestment(inv) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.DeleteOutline, "Supprimer", tint = colors.dangerAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // 3. Emprunts & Prêts
        HumanSectionCard(colors = colors) {
            HumanSectionHeader(
                title = "Emprunts & Prêts (${debts.size})",
                actionText = "+ Ajouter",
                onActionClick = { navState.activeSheet = BusinessActiveSheet.DebtForm(null) },
                colors = colors
            )
            if (debts.isEmpty()) {
                Text(
                    text = "Aucun emprunt ni prêt en cours.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            } else {
                debts.forEachIndexed { idx, debt ->
                    if (idx > 0) HumanHairline(colors)
                    val totalDue = debt.debtTotalDueAr()
                    val rem = debt.debtRemainingAr()
                    val progress = if (totalDue > 0L) (debt.paidAr.toFloat() / totalDue.toFloat()).coerceIn(0f, 1f) else 1f
                    val isBorrowed = debt.direction == "BORROWED"

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = debt.title,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = (if (isBorrowed) "Emprunt" else "Prêt") +
                                        (if (debt.counterparty.isNotBlank()) " · ${debt.counterparty}" else "") +
                                        " · Reste ${CurrencyFormatter.formatCompactAriary(rem)}",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                            if (rem > 0L) {
                                HumanTonalButton(
                                    text = if (isBorrowed) "Rembourser" else "Encaisser",
                                    icon = Icons.Default.Payments,
                                    onClick = { navState.activeSheet = BusinessActiveSheet.DebtPayment(debt) },
                                    colors = colors,
                                    containerColor = if (isBorrowed) colors.violetSoftBg else colors.successSoftBg,
                                    contentColor = if (isBorrowed) colors.violetAccent else colors.successAccent,
                                    modifier = Modifier.testTag("btn_pay_debt_${debt.id}")
                                )
                            } else {
                                HumanStatusPill("Soldé", colors.successSoftBg, colors.successAccent)
                            }
                            IconButton(onClick = { onDeleteDebt(debt) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.DeleteOutline, "Supprimer", tint = colors.dangerAccent, modifier = Modifier.size(18.dp))
                            }
                        }
                        HumanProgressBar(
                            progress = progress,
                            trackColor = colors.innerCardBg,
                            fillColor = if (rem == 0L) colors.successAccent else colors.violetAccent
                        )
                    }
                }
            }
        }

        // 4. Prévision de trésorerie
        HumanSectionCard(colors = colors) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HumanIconBadge(
                    icon = Icons.Default.TrendingUp,
                    tint = colors.cyanAccent,
                    backgroundColor = colors.cyanSoftBg
                )
                Column {
                    Text(
                        text = "Prévision à 30 jours",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Solde projeté : ${CurrencyFormatter.formatAriary(projected30d)}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (projected30d >= 0L) colors.successAccent else colors.dangerAccent
                    )
                }
            }
            Text(
                text = "Entrées prévues : +${CurrencyFormatter.formatCompactAriary(forecastIn)} · Sorties prévues : -${CurrencyFormatter.formatCompactAriary(forecastOut)}",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }
    }
}
