package com.example.ui.business.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CurrencyYen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
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
import com.example.data.local.entity.BusinessCategoryEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.data.local.entity.YuanTransactionEntity
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessNavigationState
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.HumanChipRow
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanHeroCard
import com.example.ui.business.shared.HumanIconBadge
import com.example.ui.business.shared.HumanPrimaryButton
import com.example.ui.business.shared.HumanSearchBar
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSectionHeader
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.formatYuan
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.business.shared.resolveCategoryColors
import com.example.ui.business.shared.resolveCategoryIcon
import com.example.ui.business.shared.resolveTransactionCategory
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.VioletSecondary
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
internal fun FinancesTab(
    uiState: BusinessUiState,
    accounts: List<BusinessAccountEntity>,
    transactions: List<BusinessTransactionEntity>,
    investments: List<BusinessInvestmentEntity>,
    debts: List<BusinessDebtEntity>,
    categories: List<BusinessCategoryEntity> = emptyList(),
    navState: BusinessNavigationState,
    onDeleteInvestment: (BusinessInvestmentEntity) -> Unit,
    onDeleteDebt: (BusinessDebtEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    var journalFilter by remember { mutableStateOf("Tout") }
    var journalSearch by remember { mutableStateOf("") }
    var advancedExpanded by remember { mutableStateOf(false) }

    val activeAccounts = remember(accounts) { accounts.filter { it.isActive } }
    val ariaryAccounts = remember(activeAccounts) { activeAccounts.filter { !it.isYuanAccount } }
    val totalTreasuryAr = ariaryAccounts.sumOf { it.balanceAr }
    val globalLiquidityAr = totalTreasuryAr + uiState.estimatedYuanValueAr

    val totalInAr = remember(transactions) {
        transactions.filter { it.type == "IN" || it.type == "INCOME" }.sumOf { it.amountAr }
    }
    val totalOutAr = remember(transactions) {
        transactions.filter { it.type == "OUT" || it.type == "EXPENSE" || it.type == "YUAN" }.sumOf { it.amountAr }
    }

    val unpaidFreightOrders = remember(uiState.orders) {
        uiState.orders.filter { !it.isFreightPaid && it.freightCostAr > 0L && it.status != "CATALOGUE" }
    }
    val unpaidSales = remember(uiState.sales) {
        uiState.sales.filter { !it.isFullyPaid && it.totalPriceAr > it.paidAmountAr }
    }

    val linkedYuanIds = remember(transactions) {
        transactions.mapNotNull { it.yuanTransactionId }.toSet()
    }
    val filteredJournal = remember(transactions, uiState.yuanTransactions, categories, linkedYuanIds, journalFilter, journalSearch) {
        val q = journalSearch.trim()
        val ariaryRows = transactions.map { JournalRowUi.AriaryTx(it) }
        val yuanRows = uiState.yuanTransactions
            .filter { ytx -> ytx.type == "SPEND" || ytx.type == "REFUND" || ytx.id !in linkedYuanIds }
            .map { JournalRowUi.YuanTx(it) }

        (ariaryRows + yuanRows)
            .filter { row ->
                val matchType = when (row) {
                    is JournalRowUi.AriaryTx -> when (journalFilter) {
                        "Entrées" -> row.tx.type in setOf("IN", "INCOME", "CAPITAL_IN")
                        "Sorties" -> row.tx.type in setOf("OUT", "EXPENSE", "CAPITAL_OUT")
                        "Yuans" -> row.tx.type == "YUAN" || row.tx.yuanTransactionId != null
                        else -> true
                    }
                    is JournalRowUi.YuanTx -> when (journalFilter) {
                        "Entrées" -> row.ytx.type in setOf("BUY", "REFUND")
                        "Sorties" -> row.ytx.type == "SPEND"
                        "Yuans" -> true
                        else -> true
                    }
                }
                val matchQuery = if (q.isBlank()) true else when (row) {
                    is JournalRowUi.AriaryTx -> {
                        val catName = resolveTransactionCategory(row.tx, categories)?.name.orEmpty()
                        row.tx.title.contains(q, ignoreCase = true) ||
                            row.tx.accountName.contains(q, ignoreCase = true) ||
                            row.tx.referenceTag.contains(q, ignoreCase = true) ||
                            catName.contains(q, ignoreCase = true)
                    }
                    is JournalRowUi.YuanTx ->
                        row.ytx.note.contains(q, ignoreCase = true) ||
                            row.ytx.platform.contains(q, ignoreCase = true) ||
                            "yuan".contains(q, ignoreCase = true)
                }
                matchType && matchQuery
            }
            .sortedByDescending { it.dateMillis }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Hero Trésorerie & Liquidités
        HumanHeroCard {
            Text(
                text = "Trésorerie disponible",
                fontFamily = Inter,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = CurrencyFormatter.formatAriary(globalLiquidityAr),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 29.sp,
                color = Color.White
            )
            Text(
                text = "Caisses ${CurrencyFormatter.formatCompactAriary(totalTreasuryAr)} · Yuans ${formatYuan(uiState.availableYuans)} (~${CurrencyFormatter.formatCompactAriary(uiState.estimatedYuanValueAr)})",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Encaissements", fontFamily = Inter, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            CurrencyFormatter.formatCompactAriary(totalInAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Décaissements", fontFamily = Inter, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            CurrencyFormatter.formatCompactAriary(totalOutAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 2. 2 boutons d'action principaux
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HumanPrimaryButton(
                text = "Acheter ¥",
                icon = Icons.Default.CurrencyYen,
                onClick = { navState.activeSheet = BusinessActiveSheet.BuyYuans },
                containerColor = CyanPrimary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_finances_buy_yuans")
            )
            HumanPrimaryButton(
                text = "Opération",
                icon = Icons.Default.Add,
                onClick = { navState.activeSheet = BusinessActiveSheet.AddFinanceOperation() },
                containerColor = VioletSecondary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_finances_new_op")
            )
        }

        // 3. Carrousel horizontal des comptes actifs
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HumanSectionHeader(
                title = "Comptes (${activeAccounts.size})",
                actionText = "+ Nouveau",
                onActionClick = { navState.activeSheet = BusinessActiveSheet.AccountForm(null) },
                colors = colors
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                activeAccounts.forEach { acc ->
                    AccountCarouselCard(
                        account = acc,
                        cmupRate = uiState.cmupRate,
                        colors = colors,
                        onClick = { navState.activeSheet = BusinessActiveSheet.AccountForm(existing = acc) }
                    )
                }
            }
        }

        // 4. À payer / À encaisser (conditionnel)
        if (unpaidFreightOrders.isNotEmpty() || unpaidSales.isNotEmpty()) {
            HumanSectionCard(colors = colors) {
                HumanSectionHeader(
                    title = "Échéances en attente",
                    subtitle = "Fret et factures clients à solder",
                    colors = colors
                )
                unpaidFreightOrders.take(3).forEachIndexed { idx, ord ->
                    if (idx > 0) HumanHairline(colors)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            HumanIconBadge(
                                icon = Icons.Default.LocalShipping,
                                tint = colors.warningAccent,
                                backgroundColor = colors.warningSoftBg,
                                size = 38.dp,
                                iconSize = 18.dp
                            )
                            Column {
                                Text(
                                    text = "Fret · ${ord.product}",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${ord.carrier.ifBlank { "Transitaire" }} · ${CurrencyFormatter.formatAriary(ord.freightCostAr)}",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        HumanTonalButton(
                            text = "Régler",
                            onClick = { navState.activeSheet = BusinessActiveSheet.PayFreightOrder(ord) },
                            colors = colors,
                            containerColor = colors.warningSoftBg,
                            contentColor = colors.warningAccent
                        )
                    }
                }

                if (unpaidFreightOrders.isNotEmpty() && unpaidSales.isNotEmpty()) {
                    HumanHairline(colors)
                }

                unpaidSales.take(3).forEachIndexed { idx, sale ->
                    if (idx > 0) HumanHairline(colors)
                    val rem = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            HumanIconBadge(
                                icon = Icons.Default.ReceiptLong,
                                tint = colors.cyanAccent,
                                backgroundColor = colors.cyanSoftBg,
                                size = 38.dp,
                                iconSize = 18.dp
                            )
                            Column {
                                Text(
                                    text = "Client · ${sale.clientName}",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${sale.productName} · Reste ${CurrencyFormatter.formatAriary(rem)}",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        HumanTonalButton(
                            text = "Encaisser",
                            onClick = { navState.activeSheet = BusinessActiveSheet.RecordSalePayment(sale) },
                            colors = colors
                        )
                    }
                }
            }
        }

        // 5. Journal des opérations
        HumanSectionCard(colors = colors) {
            HumanSectionHeader(
                title = "Journal des opérations",
                subtitle = "${filteredJournal.size} mouvement${if (filteredJournal.size > 1) "s" else ""}",
                actionText = "Catégories",
                onActionClick = {
                    navState.activeSheet = BusinessActiveSheet.ManageCategories(
                        initialType = if (journalFilter == "Entrées") "IN" else "OUT"
                    )
                },
                colors = colors
            )

            HumanSearchBar(
                query = journalSearch,
                onQueryChange = { journalSearch = it },
                placeholder = "Filtrer par libellé, compte ou catégorie...",
                colors = colors
            )

            HumanChipRow(
                items = listOf(
                    "Tout" to "Tout",
                    "Entrées" to "Entrées",
                    "Sorties" to "Sorties",
                    "Yuans" to "Yuans"
                ),
                selectedKey = journalFilter,
                onSelect = { journalFilter = it },
                colors = colors
            )

            if (filteredJournal.isEmpty()) {
                Text(
                    text = "Aucune opération ne correspond à ce filtre.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                filteredJournal.take(30).forEachIndexed { idx, row ->
                    if (idx > 0) HumanHairline(colors)
                    when (row) {
                        is JournalRowUi.AriaryTx -> TransactionRow(
                            tx = row.tx,
                            categories = categories,
                            colors = colors,
                            onClick = { navState.activeSheet = BusinessActiveSheet.TransactionDetail(row.tx) }
                        )
                        is JournalRowUi.YuanTx -> YuanTransactionRow(
                            ytx = row.ytx,
                            colors = colors,
                            onClick = { navState.activeSheet = BusinessActiveSheet.YuanTransactionDetail(row.ytx) }
                        )
                    }
                }
            }
        }

        // 6. Accordéon replié par défaut : Gestion avancée
        HumanSectionCard(
            colors = colors,
            contentPadding = PaddingValues(16.dp),
            onClick = { advancedExpanded = !advancedExpanded },
            modifier = Modifier.testTag("btn_toggle_advanced_finance")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    HumanIconBadge(
                        icon = Icons.Default.Tune,
                        tint = colors.violetAccent,
                        backgroundColor = colors.violetSoftBg,
                        size = 38.dp,
                        iconSize = 19.dp
                    )
                    Column {
                        Text(
                            text = "Gestion avancée",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Capital, investissements, dettes & prévisions 30j",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
                Icon(
                    imageVector = if (advancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (advancedExpanded) "Replier" else "Déplier",
                    tint = colors.textSecondary
                )
            }
        }

        AnimatedVisibility(visible = advancedExpanded) {
            FinancesAdvancedSection(
                uiState = uiState,
                ariaryAccounts = ariaryAccounts,
                transactions = transactions,
                investments = investments,
                debts = debts,
                unpaidSales = unpaidSales,
                unpaidFreightOrders = unpaidFreightOrders,
                navState = navState,
                colors = colors,
                onDeleteInvestment = onDeleteInvestment,
                onDeleteDebt = onDeleteDebt
            )
        }
    }
}

@Composable
private fun AccountCarouselCard(
    account: BusinessAccountEntity,
    cmupRate: Long,
    colors: TransitaireColors,
    onClick: () -> Unit
) {
    val accent = if (account.isYuanAccount) colors.violetAccent else colors.cyanAccent
    val softBg = if (account.isYuanAccount) colors.violetSoftBg else colors.cyanSoftBg
    val mainBalance = if (account.isYuanAccount) {
        formatYuan(account.balanceYuan)
    } else {
        CurrencyFormatter.formatAriary(account.balanceAr)
    }

    Surface(
        modifier = Modifier
            .width(172.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("account_card_${account.id}"),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HumanIconBadge(
                    icon = if (account.isYuanAccount) Icons.Default.CurrencyYen else Icons.Default.AccountBalanceWallet,
                    tint = accent,
                    backgroundColor = softBg,
                    size = 34.dp,
                    iconSize = 17.dp,
                    cornerRadius = 11.dp
                )
                HumanStatusPill(
                    text = if (account.isYuanAccount) "Chine ¥" else "Ariary",
                    backgroundColor = softBg,
                    contentColor = accent
                )
            }
            Text(
                text = account.name,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = mainBalance,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = accent,
                maxLines = 1
            )
            if (account.isYuanAccount && cmupRate > 0L) {
                Text(
                    text = "~${CurrencyFormatter.formatCompactAriary((account.balanceYuan * cmupRate).toLong())}",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            } else if (account.subtitle.isNotBlank()) {
                Text(
                    text = account.subtitle,
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

@Composable
private fun TransactionRow(
    tx: BusinessTransactionEntity,
    categories: List<BusinessCategoryEntity>,
    colors: TransitaireColors,
    onClick: () -> Unit
) {
    val isIn = tx.type in setOf("IN", "INCOME", "CAPITAL_IN")
    val isTransfer = tx.type == "TRANSFER"
    val matchedCategory = remember(tx, categories) { resolveTransactionCategory(tx, categories) }
    val (icon, tint, softBg) = when {
        matchedCategory != null && tx.type in setOf("IN", "INCOME", "OUT", "EXPENSE") -> {
            val (catTint, catSoftBg) = resolveCategoryColors(matchedCategory.colorKey, colors)
            Triple(resolveCategoryIcon(matchedCategory.iconKey), catTint, catSoftBg)
        }
        isTransfer -> Triple(Icons.Default.SwapHoriz, colors.violetAccent, colors.violetSoftBg)
        tx.type == "YUAN" -> Triple(Icons.Default.CurrencyYen, colors.cyanAccent, colors.cyanSoftBg)
        isIn -> Triple(Icons.Default.ArrowDownward, colors.successAccent, colors.successSoftBg)
        else -> Triple(Icons.Default.ArrowUpward, colors.dangerAccent, colors.dangerSoftBg)
    }

    val subtitleText = remember(tx, matchedCategory) {
        buildString {
            if (matchedCategory != null) {
                append(matchedCategory.name)
                append(" · ")
            }
            append(tx.accountName)
            append(" · ")
            append(DateUtils.formatShortDate(tx.dateMillis))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .testTag("tx_row_${tx.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            HumanIconBadge(icon = icon, tint = tint, backgroundColor = softBg, size = 38.dp, iconSize = 18.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                    text = subtitleText,
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when {
                isTransfer -> CurrencyFormatter.formatAriary(tx.amountAr)
                isIn -> "+${CurrencyFormatter.formatAriary(tx.amountAr)}"
                else -> "-${CurrencyFormatter.formatAriary(tx.amountAr)}"
            },
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = when {
                isTransfer -> colors.violetAccent
                isIn -> colors.successAccent
                else -> colors.textPrimary
            }
        )
    }
}

private sealed class JournalRowUi(val dateMillis: Long) {
    data class AriaryTx(val tx: BusinessTransactionEntity) : JournalRowUi(tx.dateMillis)
    data class YuanTx(val ytx: YuanTransactionEntity) : JournalRowUi(ytx.date)
}

@Composable
private fun YuanTransactionRow(
    ytx: YuanTransactionEntity,
    colors: TransitaireColors,
    onClick: () -> Unit
) {
    val isSpend = ytx.type == "SPEND"
    val isRefund = ytx.type == "REFUND"
    val tint = when {
        isSpend -> colors.violetAccent
        isRefund -> colors.successAccent
        else -> colors.cyanAccent
    }
    val softBg = when {
        isSpend -> colors.violetSoftBg
        isRefund -> colors.successSoftBg
        else -> colors.cyanSoftBg
    }
    val defaultTitle = when (ytx.type) {
        "SPEND" -> "Paiement fournisseur ${ytx.platform}"
        "REFUND" -> "Remboursement ${ytx.platform}"
        else -> "Recharge ${ytx.platform}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .testTag("yuan_tx_row_${ytx.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            HumanIconBadge(icon = Icons.Default.CurrencyYen, tint = tint, backgroundColor = softBg, size = 38.dp, iconSize = 18.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = ytx.note.ifBlank { defaultTitle },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${ytx.platform} · ${DateUtils.formatShortDate(ytx.date)}",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (if (isSpend) "-" else "+") + formatYuan(ytx.amountYuan),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSpend) colors.violetAccent else colors.successAccent
            )
            if (ytx.amountAr > 0L) {
                Text(
                    text = "~${CurrencyFormatter.formatCompactAriary(ytx.amountAr)}",
                    fontFamily = Inter,
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

