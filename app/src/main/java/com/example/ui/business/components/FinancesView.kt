package com.example.ui.business.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessUiState
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import com.example.util.CurrencyFormatter

@Composable
fun FinancesView(
    uiState: BusinessUiState,
    accounts: List<com.example.data.local.entity.BusinessAccountEntity> = emptyList(),
    transactions: List<com.example.data.local.entity.BusinessTransactionEntity> = emptyList(),
    investments: List<com.example.data.local.entity.BusinessInvestmentEntity> = emptyList(),
    onAddAccount: (
        name: String,
        subtitle: String,
        initialBalanceAr: Long,
        isYuanAccount: Boolean,
        initialBalanceYuan: Double
    ) -> Unit = { _, _, _, _, _ -> },
    onUpdateAccount: (
        existing: com.example.data.local.entity.BusinessAccountEntity,
        newName: String,
        newSubtitle: String,
        newBalanceAr: Long,
        newBalanceYuan: Double
    ) -> Unit = { _, _, _, _, _ -> },
    onDeleteAccount: (com.example.data.local.entity.BusinessAccountEntity) -> Unit = {},
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
    onDeleteTransaction: (com.example.data.local.entity.BusinessTransactionEntity) -> Unit = {},
    onRecordCapitalMovement: (title: String, isContribution: Boolean, amountAr: Long, accountName: String, note: String, affectAccount: Boolean, dateMillis: Long) -> Unit = { _, _, _, _, _, _, _ -> },
    onAddInvestment: (title: String, category: String, amountAr: Long, estimatedValueAr: Long, generatedReturnAr: Long, accountName: String, notes: String, dateMillis: Long) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onUpdateInvestment: (com.example.data.local.entity.BusinessInvestmentEntity) -> Unit = {},
    onDeleteInvestment: (com.example.data.local.entity.BusinessInvestmentEntity) -> Unit = {},
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onBuyYuansClick: () -> Unit,
    onPay1688Click: () -> Unit,
    onNewOperationClick: () -> Unit,
    onRecordTransitairePayment: () -> Unit,
    onWhatsAppRelance: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberTransitaireColors()
    var forecastHorizon by remember { mutableStateOf("30 j") } // "30 j", "60 j", "90 j"
    var selectedTxFilter by remember { mutableStateOf("Tout") } // "Tout", "Entrées", "Sorties", "Transferts", "Change ¥"
    var showTxSearchBar by remember { mutableStateOf(false) }
    var showTxAccountFilter by remember { mutableStateOf(false) }
    var txSearchQuery by remember { mutableStateOf("") }
    var selectedAccountFilter by remember { mutableStateOf("Tous") }
    var showAllTransactions by remember { mutableStateOf(false) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var defaultNewAccountIsYuan by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<com.example.data.local.entity.BusinessAccountEntity?>(null) }
    var deletingAccount by remember { mutableStateOf<com.example.data.local.entity.BusinessAccountEntity?>(null) }
    var selectedTxForDetail by remember { mutableStateOf<com.example.data.local.entity.BusinessTransactionEntity?>(null) }
    var presetTxTitle by remember { mutableStateOf("") }
    var presetTxType by remember { mutableStateOf("IN") }
    var presetTxTag by remember { mutableStateOf("") }

    val openAddTx = { title: String, type: String, tag: String ->
        presetTxTitle = title
        presetTxType = type
        presetTxTag = tag
        showAddTxDialog = true
    }

    val activeAccounts = remember(accounts) {
        accounts.filter { it.isActive }
    }
    val ariaryAccounts = remember(activeAccounts) {
        activeAccounts.filter { !it.isYuanAccount }
    }
    val yuanAccounts = remember(activeAccounts) {
        activeAccounts.filter { it.isYuanAccount }
    }
    val totalTreasuryAr = remember(ariaryAccounts) {
        ariaryAccounts.sumOf { it.balanceAr }
    }
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

    // Compte Associé
    val avancesPerso = remember(transactions) {
        transactions.filter { it.referenceTag.contains("Associé", ignoreCase = true) && it.type == "IN" }.sumOf { it.amountAr }
    }
    val remboursementsPerso = remember(transactions) {
        transactions.filter { it.referenceTag.contains("Associé", ignoreCase = true) && (it.type == "OUT" || it.type == "EXPENSE") }.sumOf { it.amountAr }
    }
    val soldeAssocié = avancesPerso - remboursementsPerso

    // Emprunts & Dettes
    val totalEmpruntsDû = remember(transactions) {
        transactions.filter { it.referenceTag.contains("Emprunt", ignoreCase = true) && it.type == "IN" }.sumOf { it.amountAr }
    }
    val totalPrêtsTiers = remember(transactions) {
        transactions.filter { it.referenceTag.contains("Prêt", ignoreCase = true) && (it.type == "OUT" || it.type == "EXPENSE") }.sumOf { it.amountAr }
    }

    val orangeColor = Color(0xFFEA580C)
    val violetColor = colors.violetAccent
    val cyanColor = colors.cyanAccent
    val emeraldColor = colors.successAccent
    val redColor = colors.dangerAccent

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // -------------------------------------------------------------
            // 1. SCOPE SWITCH & MONTH SELECTOR
            // -------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Month switcher bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onPrevMonth,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Mois précédent",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = cyanColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.selectedMonthLabel,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = onNextMonth,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Mois suivant",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (showTxSearchBar) cyanColor.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { showTxSearchBar = !showTxSearchBar },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Rechercher une transaction",
                                    tint = if (showTxSearchBar) cyanColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (showTxAccountFilter) cyanColor.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { showTxAccountFilter = !showTxAccountFilter },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filtrer par compte",
                                    tint = if (showTxAccountFilter) cyanColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 2. HERO CARD (Gradient Orange -> Violet)
            // -------------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFEA580C),
                                    Color(0xFFC026D3),
                                    Color(0xFF712AE2)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TRÉSORERIE TOTALE (Ar + ¥)",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val yuanHeroFmt = if (uiState.availableYuans % 1.0 == 0.0) {
                                        "${uiState.availableYuans.toInt()}"
                                    } else {
                                        String.format(java.util.Locale.US, "%.2f", uiState.availableYuans)
                                    }
                                    Text(
                                        text = "${cashTransactions.size} op. • ¥ $yuanHeroFmt",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = CurrencyFormatter.formatAriary(totalLiquidityAr).replace(" Ar", ""),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 32.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ar",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Text(
                                text = "Ariary : ${CurrencyFormatter.formatAriary(totalTreasuryAr)} (${ariaryAccounts.size} cpte) • Yuans : ≈ ${CurrencyFormatter.formatAriary(uiState.estimatedYuanValueAr)} (${yuanAccounts.size} cpte ¥)",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        // Mini stats grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Entrées
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF34D399))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Entrées",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = CurrencyFormatter.formatAriary(totalTxInAr),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFA7F3D0)
                                    )
                                }
                            }

                            // Sorties & Change ¥
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFDA4AF))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Sorties & ¥",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = CurrencyFormatter.formatAriary(totalTxOutAr),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFE4E6)
                                    )
                                }
                            }

                            // Flux net
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFCD34D))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Flux net",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${if (netTxFlowAr >= 0) "+" else ""}${CurrencyFormatter.formatAriary(netTxFlowAr)}",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Progress indicator
                        val entriesForRatio = uiState.salesTotalCollectedAr.toDouble()
                        val exitsForRatio = (uiState.salesCost + uiState.freightAndExpenses).toDouble()
                        val coverageRatio = if (exitsForRatio > 0.0) entriesForRatio / exitsForRatio else if (entriesForRatio > 0.0) 2.0 else 0.0
                        val ratioFormatted = String.format(java.util.Locale.US, "%.2fx", coverageRatio)
                        val ratioStatus = when {
                            coverageRatio >= 1.2 -> "Sécurisé"
                            coverageRatio >= 1.0 -> "Équilibré"
                            coverageRatio > 0.0 -> "À surveiller"
                            else -> "Aucune donnée"
                        }
                        val ratioColor = when {
                            coverageRatio >= 1.2 -> Color(0xFFA7F3D0)
                            coverageRatio >= 1.0 -> Color(0xFFFDE68A)
                            else -> Color(0xFFFCA5A5)
                        }
                        val ratioFraction = (coverageRatio / 2.0).toFloat().coerceIn(0.05f, 1.0f)

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Ratio de couverture des charges ($ratioFormatted)",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = ratioStatus,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ratioColor
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.25f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratioFraction)
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(ratioColor)
                                )
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 3. COMPTES & MODES DE RÈGLEMENT
            // -------------------------------------------------------------
            Surface(
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
                                    .background(cyanColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountTree,
                                    contentDescription = null,
                                    tint = cyanColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Comptes & Modes",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${ariaryAccounts.size} compte(s) Ar • ${yuanAccounts.size} compte(s) ¥",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    defaultNewAccountIsYuan = false
                                    editingAccount = null
                                    showAddAccountDialog = true
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("btn_add_account")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Ajouter un compte",
                                tint = cyanColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Ajouter",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = cyanColor
                            )
                        }
                    }

                    // Dynamic Accounts Horizontal List
                    if (activeAccounts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucun compte enregistré. Créez Espèces, Mvola, Alipay ou Wechat Pay.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            activeAccounts.forEach { acc ->
                                val isYuan = acc.isYuanAccount
                                val isFilteredHere = selectedAccountFilter.equals(acc.name, ignoreCase = true)
                                val accInAr = remember(transactions, acc.name) {
                                    transactions.filter {
                                        (it.type == "IN" || it.type == "INCOME" || it.type == "CAPITAL_IN") &&
                                            it.accountName.equals(acc.name, ignoreCase = true) ||
                                            (it.type == "TRANSFER" && it.destinationAccount.equals(acc.name, ignoreCase = true))
                                    }.sumOf { it.amountAr }
                                }
                                val accOutAr = remember(transactions, acc.name) {
                                    transactions.filter {
                                        ((it.type == "OUT" || it.type == "EXPENSE" || it.type == "YUAN" || it.type == "TRANSFER" || it.type == "CAPITAL_OUT") &&
                                            it.accountName.equals(acc.name, ignoreCase = true))
                                    }.sumOf { it.amountAr }
                                }

                                Box(
                                    modifier = Modifier
                                        .width(214.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isFilteredHere) cyanColor.copy(alpha = 0.14f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .border(
                                            width = if (isFilteredHere) 1.5.dp else 0.dp,
                                            color = if (isFilteredHere) cyanColor else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            selectedAccountFilter = if (isFilteredHere) "Tous" else acc.name
                                        }
                                        .padding(12.dp)
                                        .testTag("account_card_${acc.id}")
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                val badgeAccent = when {
                                                    acc.name.contains("Alipay", ignoreCase = true) -> Color(0xFF0EA5E9)
                                                    acc.name.contains("Wechat", ignoreCase = true) -> Color(0xFF10B981)
                                                    isYuan -> colors.cyanAccent
                                                    acc.shortCode.equals("MV", ignoreCase = true) -> Color(0xFFF59E0B)
                                                    acc.shortCode.equals("OM", ignoreCase = true) -> orangeColor
                                                    acc.shortCode.equals("BOA", ignoreCase = true) -> violetColor
                                                    else -> emeraldColor
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(badgeAccent.copy(alpha = 0.18f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (acc.shortCode.equals("CASH", ignoreCase = true)) {
                                                        Icon(
                                                            imageVector = Icons.Default.Payments,
                                                            contentDescription = null,
                                                            tint = badgeAccent,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    } else if (acc.shortCode.equals("BOA", ignoreCase = true)) {
                                                        Icon(
                                                            imageVector = Icons.Default.AccountBalance,
                                                            contentDescription = null,
                                                            tint = badgeAccent,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    } else {
                                                        Text(
                                                            text = if (isYuan) "¥" else acc.shortCode.take(3),
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 11.sp,
                                                            color = badgeAccent
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = acc.subtitle.ifBlank { if (isYuan) "Chine (¥)" else "Compte" },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isYuan) colors.cyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { editingAccount = acc },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .testTag("btn_edit_account_${acc.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Modifier ${acc.name}",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { deletingAccount = acc },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .testTag("btn_delete_account_${acc.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteOutline,
                                                        contentDescription = "Supprimer ${acc.name}",
                                                        tint = redColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = acc.name,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val yuanRateForCard = if (uiState.cmupRate > 0L) uiState.cmupRate.toDouble() else 650.0
                                        Text(
                                            text = if (isYuan) {
                                                val yVal = acc.balanceYuan
                                                if (yVal % 1.0 == 0.0) "¥ ${yVal.toInt()}"
                                                else "¥ ${String.format(java.util.Locale.US, "%.2f", yVal)}"
                                            } else {
                                                CurrencyFormatter.formatAriary(acc.balanceAr)
                                            },
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        if (isYuan) {
                                            val estCardAr = kotlin.math.round(acc.balanceYuan * yuanRateForCard).toLong()
                                            Text(
                                                text = "≈ ${CurrencyFormatter.formatAriary(estCardAr)} (${uiState.cmupRate} Ar/¥)",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "+${CurrencyFormatter.formatAriary(accInAr)}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = emeraldColor
                                                )
                                                Text(
                                                    text = "-${CurrencyFormatter.formatAriary(accOutAr)}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = redColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Dynamic Encaissements / Flux breakdown bar
                    val positiveAccounts = remember(ariaryAccounts) {
                        ariaryAccounts.filter { it.balanceAr > 0L }
                    }
                    val positiveTotalAr = remember(positiveAccounts) {
                        positiveAccounts.sumOf { it.balanceAr }
                    }
                    val segmentColors = listOf(Color(0xFFF59E0B), emeraldColor, orangeColor, cyanColor, violetColor)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Répartition des soldes Ariary",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(totalTreasuryAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (positiveTotalAr > 0L) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                positiveAccounts.forEachIndexed { idx, acc ->
                                    val weight = (acc.balanceAr.toFloat() / positiveTotalAr.toFloat()).coerceAtLeast(0.02f)
                                    Box(
                                        modifier = Modifier
                                            .weight(weight)
                                            .height(8.dp)
                                            .background(segmentColors[idx % segmentColors.size])
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                positiveAccounts.forEachIndexed { idx, acc ->
                                    val pct = ((acc.balanceAr.toDouble() / positiveTotalAr.toDouble()) * 100).toInt()
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(segmentColors[idx % segmentColors.size])
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${acc.name} ($pct%)",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 4. PORTEFEUILLE YUANS (Sourcing Chine 1688 / Taobao)
            // -------------------------------------------------------------
            Surface(
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
                                    .background(orangeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "¥",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = orangeColor
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Portefeuille Yuans",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFFEDD5)
                                    ) {
                                        Text(
                                            text = "Sourcing",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF9A3412),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Alipay & WeChat Chine",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "Taux CMUP",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = cyanColor
                        )
                    }

                    // Balance Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "STOCK DISPONIBLE",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "¥ ${uiState.availableYuans.toInt()}",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "RMB",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "VALEUR VALORISÉE",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = CurrencyFormatter.formatAriary(uiState.estimatedYuanValueAr),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = cyanColor
                                )
                                Text(
                                    text = "CMUP : ${uiState.cmupRate} Ar / ¥",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Quick buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onBuyYuansClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_buy_yuans"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = orangeColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Acheter des ¥", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onPay1688Click,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_pay_1688"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = violetColor.copy(alpha = 0.15f),
                                contentColor = violetColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCartCheckout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Payer 1688", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Mini history pills (real BUY Yuan transactions from Room)
                    val recentYuanBuys = remember(uiState.yuanTransactions) {
                        uiState.yuanTransactions
                            .filter { it.type == "BUY" }
                            .sortedByDescending { it.date }
                            .take(6)
                    }
                    val yuanDateFmt = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE) }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Derniers approvisionnements de yuans",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (recentYuanBuys.isEmpty()) {
                            Text(
                                text = "Aucun achat de yuans enregistré.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                recentYuanBuys.forEach { yuanTx ->
                                    val amountFmt = if (yuanTx.amountYuan % 1.0 == 0.0) {
                                        "¥ ${yuanTx.amountYuan.toInt()}"
                                    } else {
                                        "¥ ${String.format(java.util.Locale.US, "%.2f", yuanTx.amountYuan)}"
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(emeraldColor))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = yuanDateFmt.format(java.util.Date(yuanTx.date)),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = amountFmt,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "à ${yuanTx.exchangeRate.toInt()} Ar",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 5. PRÉVISION DE TRÉSORERIE & CASHFLOW
            // -------------------------------------------------------------
            Surface(
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
                                    .background(Color(0xFF005AC2).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueryStats,
                                    contentDescription = null,
                                    tint = Color(0xFF005AC2),
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
                                    text = "Projection de trésorerie",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Horizon switcher
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
                                        .clickable { forecastHorizon = horizon }
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

                    // Visual chart box
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
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(emeraldColor))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "À recevoir : ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = "240 000 Ar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = emeraldColor)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(redColor))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "À payer : ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = "310 000 Ar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = redColor)
                                }
                            }

                            // Canvas Chart
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                            ) {
                                val w = size.width
                                val h = size.height

                                // Draw bar charts
                                val barWidth = 14.dp.toPx()
                                val cornerRad = CornerRadius(4.dp.toPx())

                                // Week 1 bars
                                drawRoundRect(
                                    color = redColor.copy(alpha = 0.8f),
                                    topLeft = Offset(w * 0.1f, h * 0.4f),
                                    size = Size(barWidth, h * 0.5f),
                                    cornerRadius = cornerRad
                                )
                                drawRoundRect(
                                    color = emeraldColor.copy(alpha = 0.85f),
                                    topLeft = Offset(w * 0.1f + barWidth + 4.dp.toPx(), h * 0.25f),
                                    size = Size(barWidth, h * 0.65f),
                                    cornerRadius = cornerRad
                                )

                                // Week 2 (Stress point)
                                drawRoundRect(
                                    color = redColor,
                                    topLeft = Offset(w * 0.35f, h * 0.1f),
                                    size = Size(barWidth, h * 0.8f),
                                    cornerRadius = cornerRad
                                )
                                drawRoundRect(
                                    color = emeraldColor.copy(alpha = 0.85f),
                                    topLeft = Offset(w * 0.35f + barWidth + 4.dp.toPx(), h * 0.55f),
                                    size = Size(barWidth, h * 0.35f),
                                    cornerRadius = cornerRad
                                )

                                // Week 3
                                drawRoundRect(
                                    color = redColor.copy(alpha = 0.8f),
                                    topLeft = Offset(w * 0.6f, h * 0.5f),
                                    size = Size(barWidth, h * 0.4f),
                                    cornerRadius = cornerRad
                                )
                                drawRoundRect(
                                    color = emeraldColor.copy(alpha = 0.85f),
                                    topLeft = Offset(w * 0.6f + barWidth + 4.dp.toPx(), h * 0.3f),
                                    size = Size(barWidth, h * 0.6f),
                                    cornerRadius = cornerRad
                                )

                                // Week 4
                                drawRoundRect(
                                    color = redColor.copy(alpha = 0.8f),
                                    topLeft = Offset(w * 0.82f, h * 0.6f),
                                    size = Size(barWidth, h * 0.3f),
                                    cornerRadius = cornerRad
                                )
                                drawRoundRect(
                                    color = emeraldColor.copy(alpha = 0.85f),
                                    topLeft = Offset(w * 0.82f + barWidth + 4.dp.toPx(), h * 0.2f),
                                    size = Size(barWidth, h * 0.7f),
                                    cornerRadius = cornerRad
                                )

                                // Cash curve line
                                val path = Path().apply {
                                    moveTo(w * 0.05f, h * 0.45f)
                                    cubicTo(w * 0.2f, h * 0.8f, w * 0.35f, h * 0.85f, w * 0.4f, h * 0.8f)
                                    cubicTo(w * 0.55f, h * 0.5f, w * 0.7f, h * 0.4f, w * 0.95f, h * 0.38f)
                                }
                                drawPath(
                                    path = path,
                                    color = cyanColor,
                                    style = Stroke(width = 3.dp.toPx())
                                )

                                // Stress point circle
                                drawCircle(
                                    color = redColor,
                                    radius = 5.dp.toPx(),
                                    center = Offset(w * 0.38f, h * 0.8f)
                                )
                                // End circle
                                drawCircle(
                                    color = cyanColor,
                                    radius = 5.dp.toPx(),
                                    center = Offset(w * 0.95f, h * 0.38f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Semaine 1", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "14 Nov (Tension)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = redColor)
                                Text(text = "Semaine 3", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "30 jours : 1 050 000 Ar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    // Alert warning card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Risque de tension : solde estimé le plus bas le 14 nov. suite au dédouanement SinoExpress.",
                                fontSize = 12.sp,
                                color = Color(0xFF78350F),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Voir le détail prévisionnel",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = cyanColor
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = cyanColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 6. ÉCHÉANCES & CRÉANCES (Top 4)
            // -------------------------------------------------------------
            Surface(
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
                        Column {
                            Text(
                                text = "Échéances & Créances",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Top opérations à venir",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val totalDueCount = unpaidFreightOrders.size + unpaidSales.size
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = cyanColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Tout ($totalDueCount)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cyanColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "Dû (${unpaidFreightOrders.size})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (unpaidFreightOrders.isEmpty() && unpaidSales.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aucune échéance ou créance en attente.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            unpaidFreightOrders.forEach { order ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(redColor.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Sailing,
                                                    contentDescription = null,
                                                    tint = redColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Fret ${order.carrier.ifBlank { "Transitaire" }}",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = order.lotNumber.ifBlank { order.referenceCode.ifBlank { "Commande #${order.id}" } },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = redColor
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "-${CurrencyFormatter.formatAriary(order.freightCostAr)}",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = redColor
                                            )
                                            Text(
                                                text = order.freightMode,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            unpaidSales.forEach { sale ->
                                val remaining = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(emeraldColor.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = emeraldColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = sale.clientName.ifBlank { "Client" },
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = sale.productName,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "+${CurrencyFormatter.formatAriary(remaining)}",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = emeraldColor
                                                )
                                                Text(
                                                    text = "Créance",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(emeraldColor)
                                                    .clickable { onWhatsAppRelance(sale.clientName) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Chat,
                                                    contentDescription = "WhatsApp",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 7. FACTURES TRANSITAIRES (Fret Chine ➔ Madagascar)
            // -------------------------------------------------------------
            Surface(
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
                                    .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlightTakeoff,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Factures Transitaires",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Fret Chine ➔ Madagascar",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "RESTE DÛ",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(uiState.freightDueAmount),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = redColor
                            )
                        }
                    }

                    if (unpaidFreightOrders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucune facture transitaire impayée.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        unpaidFreightOrders.forEach { order ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = order.carrier.ifBlank { "Transitaire" },
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFD8E2FF)
                                                ) {
                                                    Text(
                                                        text = order.lotNumber.ifBlank { order.referenceCode.ifBlank { "Lot #${order.id}" } },
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF001A42),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${order.freightMode} • ${order.referenceCode.ifBlank { "Commande #${order.id}" }}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFFEF3C7)
                                        ) {
                                            Text(
                                                text = "À régler",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF9A3412),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Reste à payer",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = CurrencyFormatter.formatAriary(order.freightCostAr),
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = redColor
                                        )
                                    }

                                    Button(
                                        onClick = onRecordTransitairePayment,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                    .testTag("btn_pay_transitaire"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = cyanColor)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PriceCheck,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "+ Paiement transitaire", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    }
                    }
                }
            }

            // -------------------------------------------------------------
            // 8. INVESTISSEMENT & CAPITAL DU BUSINESS
            // -------------------------------------------------------------
            InvestissementCapitalCard(
                uiState = uiState,
                ariaryAccounts = ariaryAccounts,
                transactions = transactions,
                investments = investments,
                empruntsDueAr = totalEmpruntsDû,
                associeDueAr = soldeAssocié,
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

            // -------------------------------------------------------------
            // 9. COMPTE ASSOCIÉ (Argent Perso ⟷ Argent Business)
            // -------------------------------------------------------------
            Surface(
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
                                    .background(cyanColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SyncAlt,
                                    contentDescription = null,
                                    tint = cyanColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Compte Associé",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Fonds perso avancés",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = emeraldColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "En ma faveur",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = emeraldColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(text = "Le business me doit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = CurrencyFormatter.formatAriary(if (soldeAssocié >= 0) soldeAssocié else 0L), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = emeraldColor)
                                Text(text = "À récupérer en trésorerie", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(text = "Je dois au business", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = CurrencyFormatter.formatAriary(if (soldeAssocié < 0) -soldeAssocié else 0L), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (soldeAssocié < 0) redColor else MaterialTheme.colorScheme.onSurface)
                                Text(text = if (soldeAssocié < 0) "Avance à régulariser" else "Compte régulier", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = if (soldeAssocié < 0) redColor else emeraldColor)
                            }
                        }
                    }

                    Text(
                        text = "Permet d'avancer des frais opérationnels ponctuels (carburant, emballages) avec vos fonds personnels sans modifier le capital social.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable { openAddTx("Avance de frais personnels", "IN", "Compte Associé") }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Avancer des frais",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Button(
                            onClick = { openAddTx("Remboursement compte associé", "OUT", "Compte Associé") },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = emeraldColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Se rembourser", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 10. EMPRUNTS & DETTES (Accents Violets)
            // -------------------------------------------------------------
            Surface(
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
                                    .background(violetColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Handshake,
                                    contentDescription = null,
                                    tint = violetColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Emprunts & Dettes",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Engagements financiers CT",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { openAddTx("Emprunt / Prêt", "IN", "Emprunt") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Nouveau",
                                tint = violetColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Nouveau",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = violetColor
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
                                .clip(RoundedCornerShape(14.dp))
                                .background(redColor.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(text = "Emprunté (dû)", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = redColor)
                                Text(text = CurrencyFormatter.formatAriary(totalEmpruntsDû), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = redColor)
                                Text(text = "Engagements financiers", fontSize = 9.sp, color = redColor.copy(alpha = 0.8f))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(emeraldColor.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(text = "Prêté à des tiers", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = emeraldColor)
                                Text(text = CurrencyFormatter.formatAriary(totalPrêtsTiers), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = emeraldColor)
                                Text(text = "À recouvrer", fontSize = 9.sp, color = emeraldColor.copy(alpha = 0.8f))
                            }
                        }
                    }

                    // Active Loan Item
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Prêt Tontine / Ami",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = violetColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "0% Intérêt",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = violetColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Initial : 400 000 Ar · Reste : 300 000 Ar",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "Dans 12 jours",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = violetColor
                                )
                            }

                            // Gauge
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.25f)
                                            .height(6.dp)
                                            .clip(CircleShape)
                                            .background(violetColor)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "25% remboursé (100k / 400k)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "Échéance mensuelle", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 11. TRANSACTIONS RÉCENTES DU BUSINESS
            // -------------------------------------------------------------
            Surface(
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
                                    .background(cyanColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = cyanColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Transactions",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Journal des flux",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = if (showAllTransactions) "Réduire" else "Voir tout (${cashTransactions.size})",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = cyanColor,
                            modifier = Modifier.clickable { showAllTransactions = !showAllTransactions }
                        )
                    }

                    if (showTxSearchBar) {
                        OutlinedTextField(
                            value = txSearchQuery,
                            onValueChange = { txSearchQuery = it },
                            placeholder = { Text("Rechercher libellé, compte, note...") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (showTxAccountFilter && activeAccounts.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            (listOf("Tous") + activeAccounts.map { it.name }).forEach { accName ->
                                val isAccSel = selectedAccountFilter.equals(accName, ignoreCase = true)
                                Surface(
                                    modifier = Modifier.clickable { selectedAccountFilter = accName },
                                    shape = CircleShape,
                                    color = if (isAccSel) colors.violetSolidBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = accName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isAccSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Filter chips horizontal scroll
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Tout", "Entrées", "Sorties", "Transferts", "Change ¥").forEach { chip ->
                            val isSel = selectedTxFilter == chip
                            Surface(
                                modifier = Modifier.clickable { selectedTxFilter = chip },
                                shape = CircleShape,
                                color = if (isSel) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = chip,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (selectedAccountFilter != "Tous") {
                        Surface(
                            modifier = Modifier.clickable { selectedAccountFilter = "Tous" },
                            shape = CircleShape,
                            color = colors.violetSoftBg
                        ) {
                            Text(
                                text = "Compte : $selectedAccountFilter ✕",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.violetAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    val filteredTxs = remember(cashTransactions, selectedTxFilter, selectedAccountFilter, txSearchQuery) {
                        val byType = when (selectedTxFilter) {
                            "Entrées" -> cashTransactions.filter { it.type == "IN" || it.type == "INCOME" }
                            "Sorties" -> cashTransactions.filter { it.type == "OUT" || it.type == "EXPENSE" }
                            "Transferts" -> cashTransactions.filter { it.type == "TRANSFER" }
                            "Change ¥" -> cashTransactions.filter { it.type == "YUAN" }
                            else -> cashTransactions
                        }
                        val byAccount = if (selectedAccountFilter == "Tous") {
                            byType
                        } else {
                            byType.filter {
                                it.accountName.equals(selectedAccountFilter, ignoreCase = true) ||
                                    it.destinationAccount.equals(selectedAccountFilter, ignoreCase = true)
                            }
                        }
                        val q = txSearchQuery.trim()
                        if (q.isBlank()) {
                            byAccount
                        } else {
                            byAccount.filter {
                                it.title.contains(q, ignoreCase = true) ||
                                    it.accountName.contains(q, ignoreCase = true) ||
                                    it.destinationAccount.contains(q, ignoreCase = true) ||
                                    it.referenceTag.contains(q, ignoreCase = true)
                            }
                        }
                    }

                    val displayedTxs = remember(filteredTxs, showAllTransactions) {
                        if (showAllTransactions) filteredTxs else filteredTxs.take(8)
                    }

                    if (filteredTxs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucune opération enregistrée pour ce filtre.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        val dateFormat = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE) }
                        val timeFormat = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.FRANCE) }
                        val groupedTxs = remember(displayedTxs) {
                            displayedTxs.groupBy { dateFormat.format(java.util.Date(it.dateMillis)) }
                        }

                        groupedTxs.forEach { (dateLabel, dayTxs) ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = dateLabel,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )

                                dayTxs.forEach { tx ->
                                    val (badgeText, badgeColor, amountPrefix) = when (tx.type) {
                                        "IN", "INCOME" -> Triple("IN", emeraldColor, "+")
                                        "OUT", "EXPENSE" -> Triple("OUT", redColor, "-")
                                        "TRANSFER" -> Triple("TRF", cyanColor, "")
                                        "YUAN" -> Triple("¥", orangeColor, "-")
                                        else -> Triple("OP", violetColor, "")
                                    }
                                    val amountColor = when (tx.type) {
                                        "IN", "INCOME" -> emeraldColor
                                        "OUT", "EXPENSE", "YUAN" -> redColor
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable { selectedTxForDetail = tx }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(badgeColor.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = badgeText,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = badgeColor
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = tx.title,
                                                        fontFamily = PlusJakartaSans,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    val accountDisplay = if (tx.type == "TRANSFER" && tx.destinationAccount.isNotBlank()) {
                                                        "${tx.accountName} ➔ ${tx.destinationAccount}"
                                                    } else {
                                                        tx.accountName
                                                    }
                                                    val sub = listOfNotNull(
                                                        accountDisplay.takeIf { it.isNotBlank() },
                                                        tx.referenceTag.takeIf { it.isNotBlank() }
                                                    ).joinToString(" • ")
                                                    if (sub.isNotBlank()) {
                                                        Text(
                                                            text = sub,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "$amountPrefix${CurrencyFormatter.formatAriary(tx.amountAr)}",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = amountColor
                                                )
                                                Text(
                                                    text = timeFormat.format(java.util.Date(tx.dateMillis)),
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!showAllTransactions && filteredTxs.size > 8) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAllTransactions = true },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = "Afficher tout (${filteredTxs.size} opérations)",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = cyanColor,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button (+ Opération)
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
}

