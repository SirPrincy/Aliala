package com.example.ui.business.finances

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.data.local.entity.BusinessDebtPaymentEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.PlusJakartaSans

@Composable
fun FinancesView(
    uiState: BusinessUiState,
    accounts: List<BusinessAccountEntity> = emptyList(),
    transactions: List<BusinessTransactionEntity> = emptyList(),
    investments: List<BusinessInvestmentEntity> = emptyList(),
    debts: List<BusinessDebtEntity> = emptyList(),
    debtPayments: List<BusinessDebtPaymentEntity> = emptyList(),
    onAddAccount: (
        name: String,
        subtitle: String,
        initialBalanceAr: Long,
        isYuanAccount: Boolean,
        initialBalanceYuan: Double
    ) -> Unit = { _, _, _, _, _ -> },
    onUpdateAccount: (
        existing: BusinessAccountEntity,
        newName: String,
        newSubtitle: String,
        newBalanceAr: Long,
        newBalanceYuan: Double
    ) -> Unit = { _, _, _, _, _ -> },
    onDeleteAccount: (BusinessAccountEntity) -> Unit = {},
    onAddTransaction: (
        title: String,
        type: String,
        amountAr: Long,
        accountName: String,
        referenceTag: String,
        destinationAccount: String
    ) -> Unit = { _, _, _, _, _, _ -> },
    onBuyYuansFromTx: (
        amountYuan: Double,
        rateAr: Double,
        platform: String,
        sourceAccountName: String
    ) -> Unit = { _, _, _, _ -> },
    onDeleteTransaction: (BusinessTransactionEntity) -> Unit = {},
    onRecordCapitalMovement: (title: String, isContribution: Boolean, amountAr: Long, accountName: String, note: String, affectAccount: Boolean, dateMillis: Long) -> Unit = { _, _, _, _, _, _, _ -> },
    onAddInvestment: (title: String, category: String, amountAr: Long, estimatedValueAr: Long, generatedReturnAr: Long, accountName: String, notes: String, dateMillis: Long) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onUpdateInvestment: (BusinessInvestmentEntity) -> Unit = {},
    onDeleteInvestment: (BusinessInvestmentEntity) -> Unit = {},
    onAddDebt: (
        title: String,
        direction: String,
        counterparty: String,
        principalAr: Long,
        interestPercent: Double,
        dueDateMillis: Long?,
        createdAtMillis: Long,
        repaymentFrequency: String,
        accountName: String,
        affectAccount: Boolean,
        notes: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
    onRecordDebtPayment: (
        debt: BusinessDebtEntity,
        amountAr: Long,
        accountName: String,
        affectAccount: Boolean,
        dateMillis: Long,
        note: String
    ) -> Unit = { _, _, _, _, _, _ -> },
    onUpdateDebt: (BusinessDebtEntity) -> Unit = {},
    onDeleteDebt: (BusinessDebtEntity) -> Unit = {},
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onBuyYuansClick: () -> Unit,
    onPay1688Click: () -> Unit,
    onNewOperationClick: () -> Unit,
    onRecordTransitairePayment: (order: BusinessOrderEntity, amountAr: Long, accountName: String) -> Unit,
    onWhatsAppRelance: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberTransitaireColors()
    var forecastHorizon by remember { mutableStateOf("30 j") }
    var showForecastDetailDialog by remember { mutableStateOf(false) }
    var dueSectionFilter by remember { mutableStateOf("Tout") }
    var selectedTxFilter by remember { mutableStateOf("Tout") }
    var showTxSearchBar by remember { mutableStateOf(false) }
    var showTxAccountFilter by remember { mutableStateOf(false) }
    var txSearchQuery by remember { mutableStateOf("") }
    var selectedAccountFilter by remember { mutableStateOf("Tous") }
    var showAllTransactions by remember { mutableStateOf(false) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var defaultNewAccountIsYuan by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<BusinessAccountEntity?>(null) }
    var deletingAccount by remember { mutableStateOf<BusinessAccountEntity?>(null) }
    var selectedTxForDetail by remember { mutableStateOf<BusinessTransactionEntity?>(null) }
    var freightOrderToPay by remember { mutableStateOf<BusinessOrderEntity?>(null) }
    var presetTxTitle by remember { mutableStateOf("") }
    var presetTxType by remember { mutableStateOf("IN") }
    var presetTxTag by remember { mutableStateOf("") }

    val openAddTx = { title: String, type: String, tag: String ->
        presetTxTitle = title
        presetTxType = type
        presetTxTag = tag
        showAddTxDialog = true
    }

    val activeAccounts = remember(accounts) { accounts.filter { it.isActive } }
    val ariaryAccounts = remember(activeAccounts) { activeAccounts.filter { !it.isYuanAccount } }
    val yuanAccounts = remember(activeAccounts) { activeAccounts.filter { it.isYuanAccount } }
    val totalTreasuryAr = remember(ariaryAccounts) { ariaryAccounts.sumOf { it.balanceAr } }
    val totalLiquidityAr = totalTreasuryAr + uiState.estimatedYuanValueAr
    val cashTransactions = remember(transactions) {
        transactions.filter { it.type != "CAPITAL_IN" && it.type != "CAPITAL_OUT" }
    }
    val totalTxInAr = remember(cashTransactions) {
        cashTransactions.filter { it.type == "IN" || it.type == "INCOME" }.sumOf { it.amountAr }
    }
    val totalTxOutAr = remember(cashTransactions) {
        cashTransactions.filter { it.type == "OUT" || it.type == "EXPENSE" || it.type == "YUAN" }.sumOf { it.amountAr }
    }
    val netTxFlowAr = totalTxInAr - totalTxOutAr

    val unpaidSales = remember(uiState.sales) {
        uiState.sales.filter { !it.isFullyPaid && (it.totalPriceAr - it.paidAmountAr) > 0L }
    }
    val unpaidFreightOrders = remember(uiState.orders) {
        uiState.orders.filter { !it.isFreightPaid && it.freightCostAr > 0L }
    }

    val avancesPerso = remember(transactions) {
        transactions.filter { it.referenceTag.contains("Associé", ignoreCase = true) && it.type == "IN" }.sumOf { it.amountAr }
    }
    val remboursementsPerso = remember(transactions) {
        transactions.filter { it.referenceTag.contains("Associé", ignoreCase = true) && (it.type == "OUT" || it.type == "EXPENSE") }.sumOf { it.amountAr }
    }
    val soldeAssocie = avancesPerso - remboursementsPerso

    val totalEmpruntsDu = remember(debts) {
        debts.filter { it.direction == "BORROWED" }.sumOf { it.remainingAr() }
    }

    val forecastData = remember(forecastHorizon, totalTreasuryAr, unpaidSales, unpaidFreightOrders, debts) {
        buildCashflowForecast(
            horizonLabel = forecastHorizon,
            startingTreasuryAr = totalTreasuryAr,
            unpaidSales = unpaidSales,
            unpaidFreightOrders = unpaidFreightOrders,
            debts = debts
        )
    }

    val orangeColor = Color(0xFFEA580C)
    val cyanColor = colors.cyanAccent

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FinancesMonthHeaderBar(
                selectedMonthLabel = uiState.selectedMonthLabel,
                showTxSearchBar = showTxSearchBar,
                showTxAccountFilter = showTxAccountFilter,
                cyanColor = cyanColor,
                onPrevMonth = onPrevMonth,
                onNextMonth = onNextMonth,
                onToggleSearch = { showTxSearchBar = !showTxSearchBar },
                onToggleAccountFilter = { showTxAccountFilter = !showTxAccountFilter }
            )

            FinancesTreasuryHeroCard(
                uiState = uiState,
                totalLiquidityAr = totalLiquidityAr,
                totalTreasuryAr = totalTreasuryAr,
                ariaryAccountCount = ariaryAccounts.size,
                yuanAccountCount = yuanAccounts.size,
                cashTransactionCount = cashTransactions.size,
                totalTxInAr = totalTxInAr,
                totalTxOutAr = totalTxOutAr,
                netTxFlowAr = netTxFlowAr
            )

            FinancesAccountsCard(
                activeAccounts = activeAccounts,
                ariaryAccounts = ariaryAccounts,
                yuanAccounts = yuanAccounts,
                transactions = transactions,
                totalTreasuryAr = totalTreasuryAr,
                cmupRate = uiState.cmupRate,
                selectedAccountFilter = selectedAccountFilter,
                onSelectAccountFilter = { selectedAccountFilter = it },
                onAddAccountClick = {
                    defaultNewAccountIsYuan = false
                    editingAccount = null
                    showAddAccountDialog = true
                },
                onEditAccountClick = { editingAccount = it },
                onDeleteAccountClick = { deletingAccount = it }
            )

            FinancesYuanWalletCard(
                uiState = uiState,
                yuanAccounts = yuanAccounts,
                onBuyYuansClick = onBuyYuansClick,
                onPay1688Click = onPay1688Click
            )

            FinancesForecastCard(
                forecastHorizon = forecastHorizon,
                totalTreasuryAr = totalTreasuryAr,
                forecastData = forecastData,
                onSelectHorizon = { forecastHorizon = it },
                onOpenDetail = { showForecastDetailDialog = true }
            )

            FinancesDuesAndReceivablesCard(
                unpaidFreightOrders = unpaidFreightOrders,
                unpaidSales = unpaidSales,
                dueSectionFilter = dueSectionFilter,
                onSelectFilter = { dueSectionFilter = it },
                onPayFreightClick = { freightOrderToPay = it },
                onWhatsAppRelance = onWhatsAppRelance
            )

            FinancesTransitaireInvoicesCard(
                freightDueAmount = uiState.freightDueAmount,
                unpaidFreightOrders = unpaidFreightOrders,
                onPayFreightClick = { freightOrderToPay = it }
            )

            InvestissementCapitalCard(
                uiState = uiState,
                ariaryAccounts = ariaryAccounts,
                transactions = transactions,
                investments = investments,
                empruntsDueAr = totalEmpruntsDu,
                associeDueAr = soldeAssocie,
                onRecordCapitalMovement = onRecordCapitalMovement,
                onAddInvestment = onAddInvestment,
                onUpdateInvestment = onUpdateInvestment,
                onDeleteInvestment = onDeleteInvestment,
                onDeleteCapitalTransaction = onDeleteTransaction,
                onCreateAccountClick = {
                    defaultNewAccountIsYuan = false
                    editingAccount = null
                    showAddAccountDialog = true
                }
            )

            FinancesAssociateAccountCard(
                soldeAssocie = soldeAssocie,
                onAdvanceExpenseClick = { openAddTx("Avance de frais personnels", "IN", "Compte Associé") },
                onReimburseClick = { openAddTx("Remboursement compte associé", "OUT", "Compte Associé") }
            )

            EmpruntsDettesCard(
                debts = debts,
                payments = debtPayments,
                ariaryAccounts = ariaryAccounts,
                onAddDebt = onAddDebt,
                onRecordPayment = onRecordDebtPayment,
                onUpdateDebt = onUpdateDebt,
                onDeleteDebt = onDeleteDebt,
                onCreateAccountClick = {
                    defaultNewAccountIsYuan = false
                    editingAccount = null
                    showAddAccountDialog = true
                }
            )

            FinancesTransactionsSectionCard(
                cashTransactions = cashTransactions,
                activeAccounts = activeAccounts,
                showTxSearchBar = showTxSearchBar,
                showTxAccountFilter = showTxAccountFilter,
                txSearchQuery = txSearchQuery,
                selectedAccountFilter = selectedAccountFilter,
                selectedTxFilter = selectedTxFilter,
                showAllTransactions = showAllTransactions,
                onSearchQueryChange = { txSearchQuery = it },
                onSelectAccountFilter = { selectedAccountFilter = it },
                onSelectTxFilter = { selectedTxFilter = it },
                onToggleShowAll = { showAllTransactions = !showAllTransactions },
                onTransactionClick = { selectedTxForDetail = it }
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 4.dp)
                .clickable {
                    presetTxTitle = ""
                    presetTxType = "IN"
                    presetTxTag = ""
                    showAddTxDialog = true
                }
                .testTag("btn_fab_new_operation"),
            shape = CircleShape,
            color = orangeColor,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Opération",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }

    if (showAddTxDialog) {
        AddBusinessTransactionDialog(
            accounts = accounts,
            cmupRate = uiState.cmupRate,
            initialTitle = presetTxTitle,
            initialType = presetTxType,
            initialTag = presetTxTag,
            onDismiss = { showAddTxDialog = false },
            onConfirm = { title, type, amountAr, accountName, referenceTag, destinationAccount ->
                onAddTransaction(title, type, amountAr, accountName, referenceTag, destinationAccount)
                showAddTxDialog = false
            },
            onConfirmBuyYuans = { amountYuan, rateAr, platform, sourceAccountName ->
                onBuyYuansFromTx(amountYuan, rateAr, platform, sourceAccountName)
                showAddTxDialog = false
            }
        )
    }

    if (showAddAccountDialog || editingAccount != null) {
        AddOrEditBusinessAccountDialog(
            existing = editingAccount,
            existingAccounts = accounts,
            defaultIsYuan = defaultNewAccountIsYuan,
            onDismiss = {
                showAddAccountDialog = false
                editingAccount = null
            },
            onConfirmCreate = { name, subtitle, initialBalanceAr, isYuanAccount, initialBalanceYuan ->
                onAddAccount(name, subtitle, initialBalanceAr, isYuanAccount, initialBalanceYuan)
                showAddAccountDialog = false
            },
            onConfirmUpdate = { existing, newName, newSubtitle, newBalanceAr, newBalanceYuan ->
                onUpdateAccount(existing, newName, newSubtitle, newBalanceAr, newBalanceYuan)
                editingAccount = null
            }
        )
    }

    deletingAccount?.let { accToDelete ->
        DeleteBusinessAccountConfirmDialog(
            account = accToDelete,
            onDismiss = { deletingAccount = null },
            onConfirmDelete = {
                if (selectedAccountFilter.equals(accToDelete.name, ignoreCase = true)) {
                    selectedAccountFilter = "Tous"
                }
                onDeleteAccount(accToDelete)
                deletingAccount = null
            }
        )
    }

    selectedTxForDetail?.let { txDetail ->
        TransactionDetailDialog(
            tx = txDetail,
            onDismiss = { selectedTxForDetail = null },
            onDelete = { tx ->
                onDeleteTransaction(tx)
                selectedTxForDetail = null
            }
        )
    }

    freightOrderToPay?.let { ord ->
        PayFreightOrderDialog(
            order = ord,
            ariaryAccounts = ariaryAccounts,
            onDismiss = { freightOrderToPay = null },
            onConfirm = { effectiveAmount, acc ->
                onRecordTransitairePayment(ord, effectiveAmount, acc)
                freightOrderToPay = null
            }
        )
    }

    if (showForecastDetailDialog) {
        ForecastDetailDialog(
            startingTreasuryAr = totalTreasuryAr,
            forecast = forecastData,
            onDismiss = { showForecastDetailDialog = false }
        )
    }
}
