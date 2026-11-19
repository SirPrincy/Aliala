package com.example.ui.business.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyYen
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessNavigationState
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.shared.BusinessResponsiveContainer
import com.example.ui.business.shared.HumanChipRow
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanHeroCard
import com.example.ui.business.shared.HumanPrimaryButton
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSectionHeader
import com.example.ui.business.shared.HumanSegmentedSwitch
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.HumanTextField
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.MilestoneDateCard
import com.example.ui.business.shared.ProductVisualThumb
import com.example.ui.business.shared.formatYuan
import com.example.ui.business.shared.lotStatusLabel
import com.example.ui.business.shared.lotStatusTone
import com.example.ui.business.shared.parseDoubleInput
import com.example.ui.business.shared.parseLongInput
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.VioletSecondary
import com.example.util.BordereauUtils
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.LotCostCalculator
import java.util.Locale

private val LOT_PIPELINE_STAGES = listOf(
    "ORDERED" to "Commandé",
    "WAREHOUSE" to "Fournisseur",
    "GUANGZHOU" to "Dépôt Chine",
    "FLIGHT" to "En transit",
    "CUSTOMS" to "Douane Mada",
    "DELIVERY_READY" to "En stock"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LotDetailScreen(
    order: BusinessOrderEntity,
    cmupRate: Long,
    availableYuans: Double,
    accounts: List<BusinessAccountEntity>,
    transitaires: List<CatalogueTransitaireSummaryUi>,
    navState: BusinessNavigationState,
    onBack: () -> Unit,
    onUpdateOrder: (BusinessOrderEntity) -> Unit,
    onDeleteOrder: (BusinessOrderEntity) -> Unit,
    onSpendYuans: (amountYuan: Double, rateAr: Double, platform: String, note: String, orderId: Long, skipCheck: Boolean, debitDateMillis: Long, onResult: (Boolean) -> Unit) -> Unit,
    onRefundYuans: (amountYuan: Double, rateAr: Double, platform: String, note: String, orderId: Long) -> Unit
) {
    val colors = rememberTransitaireColors()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Inline transport / logistics editable fields
    var selectedMode by remember(order.id, order.freightMode) {
        mutableStateOf(if (BordereauUtils.isMaritime(order.freightMode)) "MARITIME" else "AÉRIEN")
    }
    var selectedCarrier by remember(order.id, order.carrier) { mutableStateOf(order.carrier) }
    var weightText by remember(order.id, order.weightKg) {
        mutableStateOf(order.weightKg?.let { String.format(Locale.US, "%.2f", it) } ?: "")
    }
    var volumeText by remember(order.id, order.volumeM3) {
        mutableStateOf(order.volumeM3?.let { String.format(Locale.US, "%.3f", it) } ?: "")
    }
    var freightCostText by remember(order.id, order.freightCostAr) {
        mutableStateOf(if (order.freightCostAr > 0L) order.freightCostAr.toString() else "")
    }
    var trackingText by remember(order.id, order.trackingNumbers) { mutableStateOf(order.trackingNumbers) }

    // Inline stock closure fields
    val initialConformes = remember(order.id, order.stockCount, order.receivedUnitsCount, order.quantityBought, order.defectiveCount) {
        when {
            order.receivedUnitsCount > 0 -> order.receivedUnitsCount
            order.stockCount > 0 -> order.stockCount
            else -> (order.quantityBought - order.defectiveCount).coerceAtLeast(0)
        }
    }
    var conformesCount by remember(order.id) { mutableIntStateOf(initialConformes) }
    var defectiveCount by remember(order.id) { mutableIntStateOf(order.defectiveCount) }
    var customsTaxText by remember(order.id, order.customsTaxAr) {
        mutableStateOf(if (order.customsTaxAr > 0L) order.customsTaxAr.toString() else "")
    }
    var resalePriceText by remember(order.id, order.resalePriceAr) {
        mutableStateOf(if (order.resalePriceAr > 0L) order.resalePriceAr.toString() else "")
    }

    // Inline Yuan payment / refund states
    val yuanAccounts = remember(accounts) { accounts.filter { it.isYuanAccount && it.isActive } }
    var selectedYuanWallet by remember(yuanAccounts) {
        mutableStateOf(yuanAccounts.firstOrNull()?.name ?: "Alipay")
    }
    var refundYuanInput by remember(order.id) { mutableStateOf("") }
    var showInlineRefundBox by remember { mutableStateOf(false) }
    var isDebitingYuan by remember(order.id, order.yuanDebited) { mutableStateOf(false) }
    var showDebitDatePicker by remember(order.id, order.yuanDebited) { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    val matchedTransitaire = remember(selectedCarrier, transitaires) {
        transitaires.firstOrNull {
            selectedCarrier.isNotBlank() &&
                (it.name.equals(selectedCarrier, ignoreCase = true) || selectedCarrier.contains(it.name, ignoreCase = true))
        }
    }

    val totalYuanOrder = (order.priceYuan * order.quantityBought) + order.domesticShippingYuan
    val effectiveRate = if (order.exchangeRate > 0.0) order.exchangeRate else if (cmupRate > 0L) cmupRate.toDouble() else 650.0
    val (statusBg, statusFg) = lotStatusTone(order.status, colors)
    val bordereauCode = BordereauUtils.resolveCodeOrNull(order)

    val liveBreakdown = remember(order, cmupRate, customsTaxText, freightCostText, selectedMode, weightText, volumeText, matchedTransitaire) {
        val parsedFreight = parseLongInput(freightCostText) ?: order.freightCostAr
        val tempOrder = order.copy(
            freightMode = selectedMode,
            weightKg = parseDoubleInput(weightText) ?: order.weightKg,
            volumeM3 = parseDoubleInput(volumeText) ?: order.volumeM3,
            freightCostAr = parsedFreight
        )
        LotCostCalculator.calculate(
            order = tempOrder,
            cmupRate = cmupRate,
            customsTaxArInput = parseLongInput(customsTaxText) ?: order.customsTaxAr,
            transitaireAirRateAr = matchedTransitaire?.airRateAr,
            transitaireSeaRateAr = matchedTransitaire?.seaRateAr
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = order.lotNumber.ifBlank { "Lot #${order.id}" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_lot")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navState.activeSheet = BusinessActiveSheet.OrderForm(editingOrder = order) },
                        modifier = Modifier.testTag("btn_edit_lot")
                    ) {
                        Icon(Icons.Default.Edit, "Modifier ce lot")
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("btn_delete_lot")
                    ) {
                        Icon(Icons.Default.DeleteOutline, "Supprimer ce lot", tint = colors.dangerAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            BusinessResponsiveContainer { _, _ ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 1. Carte En-tête Produit
                    HumanHeroCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            ProductVisualThumb(
                                imageUrl = order.imageUrl,
                                productName = order.product,
                                family = order.family,
                                colors = colors,
                                size = 56.dp,
                                cornerRadius = 18.dp
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = order.product,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = buildString {
                                        append("${order.quantityBought} pcs")
                                        if (order.platform.isNotBlank()) append(" · ${order.platform}")
                                        if (!bordereauCode.isNullOrBlank()) append(" · $bordereauCode")
                                    },
                                    fontFamily = Inter,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                                Text(
                                    text = lotStatusLabel(order.status),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Text(
                                text = "Coût estimé : ${CurrencyFormatter.formatAriary(liveBreakdown.unitCostAr)}/pc",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }

                    // 2. Suivi des étapes
                    HumanSectionCard(colors = colors) {
                        HumanSectionHeader(
                            title = "Étape d'acheminement",
                            subtitle = "Touchez une étape pour mettre à jour le suivi",
                            colors = colors
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LOT_PIPELINE_STAGES.forEach { (code, label) ->
                                val isSelected = order.status == code
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) CyanPrimary else colors.innerCardBg,
                                    border = if (isSelected) null else BorderStroke(1.dp, colors.border),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            val nowMs = System.currentTimeMillis()
                                            val updated = when (code) {
                                                "GUANGZHOU" -> order.copy(status = code, warehouseArrivalDate = order.warehouseArrivalDate ?: nowMs)
                                                "FLIGHT" -> order.copy(status = code, madaDispatchDate = order.madaDispatchDate ?: nowMs)
                                                "CUSTOMS" -> order.copy(status = code, madaArrivalDate = order.madaArrivalDate ?: nowMs)
                                                else -> order.copy(status = code)
                                            }
                                            onUpdateOrder(updated)
                                        }
                                        .testTag("lot_stage_$code")
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else colors.textPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Paiement Yuans & Remboursement usine (Inline - Zéro sous-modale)
                    HumanSectionCard(colors = colors) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Paiement fournisseur (¥)",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "${formatYuan(order.priceYuan)} × ${order.quantityBought} + port ${formatYuan(order.domesticShippingYuan)}",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                            HumanStatusPill(
                                text = if (order.yuanDebited) "Payé ¥" else "À payer",
                                backgroundColor = if (order.yuanDebited) colors.successSoftBg else colors.warningSoftBg,
                                contentColor = if (order.yuanDebited) colors.successAccent else colors.warningAccent
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total : ${formatYuan(totalYuanOrder)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = colors.cyanAccent
                            )
                            Text(
                                text = "~${CurrencyFormatter.formatAriary((totalYuanOrder * effectiveRate).toLong())}",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                        }

                        if (!order.yuanDebited && totalYuanOrder > 0.0) {
                            if (yuanAccounts.isNotEmpty()) {
                                HumanChipRow(
                                    items = yuanAccounts.map { it.name to "${it.name} (${formatYuan(it.balanceYuan)})" },
                                    selectedKey = selectedYuanWallet,
                                    onSelect = { selectedYuanWallet = it },
                                    colors = colors,
                                    accentColor = VioletSecondary
                                )
                            }
                            HumanPrimaryButton(
                                text = if (isDebitingYuan) "Débit en cours..." else "Débiter ${formatYuan(totalYuanOrder)} sur $selectedYuanWallet",
                                icon = Icons.Default.CurrencyYen,
                                enabled = !isDebitingYuan,
                                onClick = {
                                    if (!isDebitingYuan) {
                                        showDebitDatePicker = true
                                    }
                                },
                                containerColor = VioletSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_debit_yuan_lot")
                            )
                        } else if (order.yuanDebited) {
                            HumanTonalButton(
                                text = if (showInlineRefundBox) "Masquer remboursement" else "Enregistrer un remboursement usine ¥",
                                onClick = { showInlineRefundBox = !showInlineRefundBox },
                                colors = colors,
                                containerColor = colors.innerCardBg,
                                contentColor = colors.textPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (showInlineRefundBox) {
                                HumanTextField(
                                    value = refundYuanInput,
                                    onValueChange = { refundYuanInput = it },
                                    label = "Montant remboursé (¥)",
                                    placeholder = "Ex : 45",
                                    keyboardType = KeyboardType.Decimal,
                                    suffixText = "¥",
                                    colors = colors
                                )
                                HumanPrimaryButton(
                                    text = "Créditer le remboursement ¥",
                                    onClick = {
                                        val amt = parseDoubleInput(refundYuanInput) ?: 0.0
                                        if (amt > 0.0) {
                                            onRefundYuans(amt, effectiveRate, selectedYuanWallet, "Remboursement ${order.product}", order.id)
                                            showInlineRefundBox = false
                                            refundYuanInput = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // 4. Transport, Dates jalons & Fret
                    HumanSectionCard(colors = colors) {
                        HumanSectionHeader(
                            title = "Transport & Transitaire",
                            subtitle = "Mode d'envoi, mesures et règlement fret",
                            colors = colors
                        )

                        HumanSegmentedSwitch(
                            options = listOf("AÉRIEN" to "Aérien (kg)", "MARITIME" to "Maritime (m³)"),
                            selectedKey = selectedMode,
                            onSelect = { selectedMode = it },
                            colors = colors
                        )

                        if (transitaires.isNotEmpty()) {
                            HumanChipRow(
                                items = transitaires.filter { !it.isArchived }.map { it.name to it.name },
                                selectedKey = selectedCarrier,
                                onSelect = { selectedCarrier = it },
                                colors = colors
                            )
                        }

                        val modeTariffs = remember(matchedTransitaire, selectedMode) {
                            if (matchedTransitaire == null) emptyList()
                            else TariffGridParser.parse(
                                raw = matchedTransitaire.goodsCategoryDelays,
                                airRateAr = matchedTransitaire.airRateAr,
                                airFastRateAr = matchedTransitaire.airFastRateAr,
                                seaRateAr = matchedTransitaire.seaRateAr
                            ).filter { it.mode == selectedMode }
                        }
                        var selectedTariffId by remember(matchedTransitaire?.id, selectedMode) {
                            mutableStateOf("")
                        }
                        val activeMeasure = if (selectedMode == "MARITIME") {
                            parseDoubleInput(volumeText) ?: 0.0
                        } else {
                            parseDoubleInput(weightText) ?: 0.0
                        }
                        val activeTariff = remember(modeTariffs, selectedTariffId, selectedMode, activeMeasure) {
                            modeTariffs.firstOrNull { it.id == selectedTariffId }
                                ?: TariffGridParser.matchTariffForMeasure(modeTariffs, selectedMode, activeMeasure)
                        }

                        if (matchedTransitaire != null && modeTariffs.isNotEmpty()) {
                            val unit = if (selectedMode == "MARITIME") "m³" else "kg"
                            HumanChipRow(
                                items = modeTariffs.map { item ->
                                    item.id to "${item.shipmentType} · ${CurrencyFormatter.formatCompactAriary(item.priceAr)}/$unit"
                                },
                                selectedKey = activeTariff?.id ?: "",
                                onSelect = { selectedTariffId = it },
                                colors = colors,
                                accentColor = VioletSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HumanTextField(
                                value = if (selectedMode == "MARITIME") volumeText else weightText,
                                onValueChange = {
                                    if (selectedMode == "MARITIME") volumeText = it else weightText = it
                                },
                                label = if (selectedMode == "MARITIME") "Volume (m³)" else "Poids (kg)",
                                keyboardType = KeyboardType.Decimal,
                                suffixText = if (selectedMode == "MARITIME") "m³" else "kg",
                                colors = colors,
                                modifier = Modifier.weight(1f)
                            )
                            HumanTextField(
                                value = freightCostText,
                                onValueChange = { freightCostText = it },
                                label = "Fret (Ar)",
                                keyboardType = KeyboardType.Number,
                                suffixText = "Ar",
                                colors = colors,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (activeTariff != null && activeMeasure > 0.0) {
                            val suggestedFreight = (activeMeasure * activeTariff.priceAr).toLong()
                            val currentFreight = parseLongInput(freightCostText) ?: 0L
                            if (suggestedFreight > 0L && suggestedFreight != currentFreight) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = colors.violetSoftBg,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { freightCostText = suggestedFreight.toString() }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Grille ${activeTariff.shipmentType} : ${CurrencyFormatter.formatAriary(suggestedFreight)}",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = colors.textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "Appliquer",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = colors.violetAccent
                                        )
                                    }
                                }
                            }
                        }

                        HumanTextField(
                            value = trackingText,
                            onValueChange = { trackingText = it },
                            label = "Numéro(s) de suivi",
                            placeholder = "Ex : SF123456, ZTO9988",
                            colors = colors
                        )

                        HumanTonalButton(
                            text = "Enregistrer les infos logistiques",
                            icon = Icons.Default.LocalShipping,
                            onClick = {
                                val parsedW = parseDoubleInput(weightText)
                                val parsedV = parseDoubleInput(volumeText)
                                val parsedF = parseLongInput(freightCostText) ?: order.freightCostAr
                                onUpdateOrder(
                                    order.copy(
                                        freightMode = selectedMode,
                                        carrier = selectedCarrier.trim(),
                                        weightKg = parsedW,
                                        volumeM3 = parsedV,
                                        freightCostAr = parsedF,
                                        trackingNumbers = trackingText.trim()
                                    )
                                )
                            },
                            colors = colors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!order.isFreightPaid && liveBreakdown.freightCostAr > 0L) {
                            HumanTonalButton(
                                text = "Régler le fret (${CurrencyFormatter.formatAriary(liveBreakdown.freightCostAr)})",
                                icon = Icons.Default.Payments,
                                onClick = {
                                    navState.activeSheet = BusinessActiveSheet.PayFreightOrder(
                                        order.copy(
                                            freightCostAr = liveBreakdown.freightCostAr,
                                            carrier = selectedCarrier
                                        )
                                    )
                                },
                                colors = colors,
                                containerColor = colors.warningSoftBg,
                                contentColor = colors.warningAccent,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Dates jalons
                    MilestoneDateCard(
                        title = "Arrivée dépôt Chine",
                        subtitle = "Date de réception chez votre transitaire",
                        currentDateMillis = order.warehouseArrivalDate,
                        referenceEarlierMillis = order.orderDate,
                        referenceLabel = "la commande",
                        colors = colors,
                        testTagPrefix = "lot_warehouse_date",
                        onDateSelected = { newMs -> onUpdateOrder(order.copy(warehouseArrivalDate = newMs)) }
                    )
                    MilestoneDateCard(
                        title = "Expédition vers Madagascar",
                        subtitle = "Départ vol aérien ou conteneur maritime",
                        currentDateMillis = order.madaDispatchDate,
                        referenceEarlierMillis = order.warehouseArrivalDate ?: order.orderDate,
                        referenceLabel = "l'arrivée dépôt Chine",
                        colors = colors,
                        testTagPrefix = "lot_dispatch_date",
                        onDateSelected = { newMs -> onUpdateOrder(order.copy(madaDispatchDate = newMs)) }
                    )
                    MilestoneDateCard(
                        title = "Arrivée à Madagascar",
                        subtitle = "Réception effective sur place",
                        currentDateMillis = order.madaArrivalDate,
                        referenceEarlierMillis = order.madaDispatchDate ?: order.orderDate,
                        referenceLabel = "le départ vers Mada",
                        colors = colors,
                        testTagPrefix = "lot_arrival_date",
                        onDateSelected = { newMs -> onUpdateOrder(order.copy(madaArrivalDate = newMs)) }
                    )

                    // 5. Clôture & Mise en stock
                    HumanSectionCard(colors = colors) {
                        HumanSectionHeader(
                            title = "Clôture & Mise en stock",
                            subtitle = "Contrôlez les quantités reçues et le prix de vente",
                            colors = colors
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QtyStepperBox(
                                label = "Conformes",
                                value = conformesCount,
                                onMinus = { if (conformesCount > 0) conformesCount-- },
                                onPlus = { conformesCount++ },
                                accent = colors.successAccent,
                                modifier = Modifier.weight(1f)
                            )
                            QtyStepperBox(
                                label = "Défectueux",
                                value = defectiveCount,
                                onMinus = { if (defectiveCount > 0) defectiveCount-- },
                                onPlus = { defectiveCount++ },
                                accent = colors.dangerAccent,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HumanTextField(
                                value = customsTaxText,
                                onValueChange = { customsTaxText = it },
                                label = "Douane / Taxes (Ar)",
                                keyboardType = KeyboardType.Number,
                                suffixText = "Ar",
                                colors = colors,
                                modifier = Modifier.weight(1f)
                            )
                            HumanTextField(
                                value = resalePriceText,
                                onValueChange = { resalePriceText = it },
                                label = "Prix revente (Ar)",
                                keyboardType = KeyboardType.Number,
                                suffixText = "Ar",
                                colors = colors,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        val resaleLong = parseLongInput(resalePriceText) ?: order.resalePriceAr
                        val marginPct = if (resaleLong > 0L && liveBreakdown.unitCostAr > 0L) {
                            (((resaleLong - liveBreakdown.unitCostAr).toDouble() / resaleLong.toDouble()) * 100).toInt()
                        } else order.marginPercent

                        HumanHairline(colors)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Coût revient : ${CurrencyFormatter.formatAriary(liveBreakdown.unitCostAr)}/pc",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Marge : $marginPct%",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (marginPct >= 20) colors.successAccent else colors.warningAccent
                            )
                        }

                        HumanPrimaryButton(
                            text = "Mettre $conformesCount pcs en stock",
                            icon = Icons.Default.CheckCircle,
                            onClick = {
                                val customs = parseLongInput(customsTaxText) ?: order.customsTaxAr
                                val resale = parseLongInput(resalePriceText) ?: order.resalePriceAr
                                onUpdateOrder(
                                    order.copy(
                                        status = "DELIVERY_READY",
                                        stockCount = conformesCount,
                                        receivedUnitsCount = conformesCount,
                                        defectiveCount = defectiveCount,
                                        customsTaxAr = customs,
                                        freightCostAr = liveBreakdown.freightCostAr,
                                        unitCostAr = liveBreakdown.unitCostAr,
                                        resalePriceAr = resale,
                                        marginPercent = marginPct,
                                        madaArrivalDate = order.madaArrivalDate ?: System.currentTimeMillis()
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_put_in_stock")
                        )
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Supprimer ce lot ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (order.yuanDebited) {
                        "Le lot « ${order.product} » sera supprimé et les Yuans débités seront restitués."
                    } else {
                        "Le lot « ${order.product} » sera définitivement supprimé."
                    },
                    fontFamily = Inter
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteOrder(order)
                        onBack()
                    }
                ) {
                    Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = colors.dangerAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }

    if (showDebitDatePicker && !order.yuanDebited) {
        val initialUtcMillis = remember(order.id) {
            DateUtils.frenchDateToUtcPickerMillis(DateUtils.getTodayFrenchDate())
        }
        val debitDateState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)
        DatePickerDialog(
            onDismissRequest = { showDebitDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val chosenMillis = debitDateState.selectedDateMillis?.let { utcMs ->
                            DateUtils.frenchDateToEpochMillis(DateUtils.utcPickerMillisToFrenchDate(utcMs))
                        } ?: System.currentTimeMillis()
                        showDebitDatePicker = false
                        if (!isDebitingYuan) {
                            isDebitingYuan = true
                            onSpendYuans(
                                totalYuanOrder,
                                effectiveRate,
                                selectedYuanWallet,
                                "Achat ${order.product}",
                                order.id,
                                false,
                                chosenMillis
                            ) { success ->
                                if (!success) isDebitingYuan = false
                            }
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_debit_date")
                ) {
                    Text("Débiter", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDebitDatePicker = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        ) {
            DatePicker(
                state = debitDateState,
                title = {
                    Text(
                        text = "Date du débit ($selectedYuanWallet)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun QtyStepperBox(
    label: String,
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = rememberTransitaireColors()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.innerCardBg
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onMinus, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Remove, "Diminuer", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = "$value",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = accent
                )
                IconButton(onClick = onPlus, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Add, "Augmenter", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
