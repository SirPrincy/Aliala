package com.example.ui.business.sheets

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessClientUi
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.HumanAvatarCircle
import com.example.ui.business.shared.HumanChipRow
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanPrimaryButton
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSegmentedSwitch
import com.example.ui.business.shared.HumanSheetContainer
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.HumanTextField
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.ProductVisualThumb
import com.example.ui.business.shared.parseLongInput
import com.example.ui.business.shared.pastelToneForKey
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.VioletSecondary
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
internal fun AddSaleSheet(
    uiState: BusinessUiState,
    accounts: List<BusinessAccountEntity>,
    initialArticle: BusinessOrderEntity?,
    onDismiss: () -> Unit,
    onCreateSale: (
        productName: String,
        clientName: String,
        clientPhone: String,
        quantity: Int,
        unitPriceAr: Long,
        unitCostAr: Long,
        paidAmountAr: Long,
        saleSource: String,
        domain: String,
        linkedOrderId: Long?,
        accountName: String,
        saleDateMillis: Long
    ) -> Unit
) {
    val colors = rememberTransitaireColors()
    val stockItems = remember(uiState.orders) {
        uiState.orders.filter { it.stockCount > 0 && !it.isHidden }.sortedByDescending { it.stockCount }
    }
    val seedOrder = initialArticle ?: stockItems.firstOrNull()
    val ariaryAccounts = remember(accounts) { accounts.filter { !it.isYuanAccount && it.isActive } }

    var selectedOrderId by remember { mutableStateOf(seedOrder?.id) }
    var productName by remember { mutableStateOf(seedOrder?.product ?: "") }
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var unitPriceText by remember {
        mutableStateOf(
            if (seedOrder != null && seedOrder.resalePriceAr > 0L) seedOrder.resalePriceAr.toString() else ""
        )
    }
    var unitCostText by remember {
        val cost = seedOrder?.let {
            if (it.unitCostAr > 0L) it.unitCostAr
            else (it.priceYuan * it.exchangeRate).toLong() + (if (it.quantityBought > 0) it.freightCostAr / it.quantityBought else 0L)
        } ?: 0L
        mutableStateOf(if (cost > 0L) cost.toString() else "")
    }
    var payMode by remember { mutableStateOf("FULL") } // FULL, PARTIAL, CREDIT
    var paidInputText by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf(uiState.salesDomain) }
    var selectedAccount by remember { mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "Hors caisse") }
    var dateText by remember { mutableStateOf(DateUtils.getTodayFrenchDate()) }
    var showStockSuggestions by remember { mutableStateOf(initialArticle == null && stockItems.isNotEmpty()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val matchingStock = remember(productName, stockItems, selectedOrderId) {
        val q = productName.trim()
        if (q.isBlank() || selectedOrderId != null) {
            stockItems.take(6)
        } else {
            stockItems.filter { item ->
                item.product.contains(q, ignoreCase = true) ||
                    item.referenceCode.contains(q, ignoreCase = true) ||
                    item.variants.contains(q, ignoreCase = true) ||
                    item.lotNumber.contains(q, ignoreCase = true)
            }.take(6)
        }
    }
    val selectedLot = remember(selectedOrderId, stockItems) {
        stockItems.firstOrNull { it.id == selectedOrderId }
    }

    val qty = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val unitPrice = parseLongInput(unitPriceText) ?: 0L
    val totalAr = qty * unitPrice
    val effectivePaidAr = when (payMode) {
        "FULL" -> totalAr
        "CREDIT" -> 0L
        else -> (parseLongInput(paidInputText) ?: 0L).coerceAtMost(totalAr)
    }

    HumanSheetContainer(
        title = "Nouvelle vente",
        subtitle = "Facture générée automatiquement et stock mis à jour",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanTextField(
            value = productName,
            onValueChange = { typed ->
                productName = typed
                selectedOrderId = null
                showStockSuggestions = stockItems.isNotEmpty()
                errorText = null
            },
            label = "Produit vendu *",
            placeholder = "Tapez pour rechercher dans le stock...",
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (productName.isNotBlank()) {
                    IconButton(onClick = {
                        productName = ""
                        selectedOrderId = null
                        showStockSuggestions = stockItems.isNotEmpty()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Effacer", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                    }
                } else if (stockItems.isNotEmpty()) {
                    IconButton(onClick = { showStockSuggestions = !showStockSuggestions }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Voir le stock", tint = colors.cyanAccent)
                    }
                }
            },
            colors = colors,
            modifier = Modifier.testTag("input_sale_product")
        )

        if (selectedLot != null && !showStockSuggestions) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.successSoftBg)
                    .clickable { showStockSuggestions = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.successAccent, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Lot lié · ${selectedLot.stockCount} pcs en stock",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.successAccent
                    )
                }
                Text(
                    text = "Changer",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = colors.cyanAccent
                )
            }
        }

        AnimatedVisibility(visible = showStockSuggestions && matchingStock.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.innerCardBg,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Articles disponibles en stock",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "Fermer",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = colors.cyanAccent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showStockSuggestions = false }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    matchingStock.forEachIndexed { idx, item ->
                        if (idx > 0) HumanHairline(colors)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedOrderId = item.id
                                    productName = item.product
                                    if (item.resalePriceAr > 0L) unitPriceText = item.resalePriceAr.toString()
                                    val cost = if (item.unitCostAr > 0L) item.unitCostAr else (item.priceYuan * item.exchangeRate).toLong()
                                    if (cost > 0L) unitCostText = cost.toString()
                                    showStockSuggestions = false
                                    errorText = null
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductVisualThumb(
                                imageUrl = item.imageUrl,
                                productName = item.product,
                                family = item.family,
                                colors = colors,
                                size = 38.dp,
                                cornerRadius = 10.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = buildString {
                                        append("Stock : ${item.stockCount} pcs")
                                        if (item.variants.isNotBlank()) append(" · ${item.variants}")
                                    },
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (item.resalePriceAr > 0L) {
                                Text(
                                    text = CurrencyFormatter.formatCompactAriary(item.resalePriceAr),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = colors.cyanAccent
                                )
                            }
                        }
                    }
                }
            }
        }

        HumanTextField(
            value = clientName,
            onValueChange = { clientName = it; errorText = null },
            label = "Client *",
            placeholder = "Nom du client",
            colors = colors
        )

        if (uiState.allClients.isNotEmpty() && clientName.isBlank()) {
            HumanChipRow(
                items = uiState.allClients.take(6).map { it.name to it.name },
                selectedKey = clientName,
                onSelect = { name ->
                    clientName = name
                    uiState.allClients.firstOrNull { it.name == name }?.phone?.let { clientPhone = it }
                },
                colors = colors
            )
        }

        HumanTextField(
            value = clientPhone,
            onValueChange = { clientPhone = it },
            label = "Téléphone (optionnel)",
            keyboardType = KeyboardType.Phone,
            colors = colors
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HumanTextField(
                value = quantityText,
                onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                label = "Quantité *",
                keyboardType = KeyboardType.Number,
                suffixText = "pcs",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            HumanTextField(
                value = unitPriceText,
                onValueChange = { unitPriceText = it.filter { c -> c.isDigit() } },
                label = "Prix unitaire *",
                keyboardType = KeyboardType.Number,
                suffixText = "Ar",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }

        HumanSegmentedSwitch(
            options = listOf("BUSINESS" to "Business Pro", "FAMILY_FRIENDS" to "Famille & Amis"),
            selectedKey = domain,
            onSelect = { domain = it },
            colors = colors
        )

        HumanSegmentedSwitch(
            options = listOf("FULL" to "Tout payer", "PARTIAL" to "Acompte", "CREDIT" to "À crédit"),
            selectedKey = payMode,
            onSelect = { payMode = it },
            colors = colors
        )

        if (payMode == "PARTIAL") {
            HumanTextField(
                value = paidInputText,
                onValueChange = { paidInputText = it.filter { c -> c.isDigit() } },
                label = "Montant encaissé maintenant",
                keyboardType = KeyboardType.Number,
                suffixText = "Ar",
                colors = colors
            )
        }

        if (payMode != "CREDIT") {
            val accList = ariaryAccounts.map { it.name to it.name } + ("Hors caisse" to "Hors caisse")
            Text("Caisse créditée", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
            HumanChipRow(
                items = accList,
                selectedKey = selectedAccount,
                onSelect = { selectedAccount = it },
                colors = colors
            )
        }

        HumanTextField(
            value = dateText,
            onValueChange = { dateText = it.filter { c -> c.isDigit() || c == '/' }.take(10) },
            label = "Date (jj/mm/aaaa)",
            colors = colors
        )

        if (totalAr > 0L) {
            Surface(shape = RoundedCornerShape(16.dp), color = colors.cyanSoftBg, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total facture", fontFamily = Inter, fontSize = 13.sp, color = colors.textPrimary)
                    Text(
                        text = CurrencyFormatter.formatAriary(totalAr),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.cyanAccent
                    )
                }
            }
        }

        if (!errorText.isNullOrBlank()) {
            Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        }

        HumanPrimaryButton(
            text = "Valider la vente",
            icon = Icons.Default.Check,
            onClick = {
                if (productName.isBlank()) {
                    errorText = "Veuillez préciser le produit."
                    return@HumanPrimaryButton
                }
                if (clientName.isBlank()) {
                    errorText = "Veuillez saisir le nom du client."
                    return@HumanPrimaryButton
                }
                if (unitPrice <= 0L) {
                    errorText = "Veuillez saisir un prix unitaire valide."
                    return@HumanPrimaryButton
                }
                val parsedDate = if (DateUtils.isValidFrenchDate(dateText)) DateUtils.frenchDateToEpochMillis(dateText) else System.currentTimeMillis()
                val unitCost = parseLongInput(unitCostText) ?: 0L
                val source = if (selectedOrderId != null) "STOCK" else "ORDER"
                onCreateSale(
                    productName.trim(),
                    clientName.trim(),
                    clientPhone.trim(),
                    qty,
                    unitPrice,
                    unitCost,
                    effectivePaidAr,
                    source,
                    domain,
                    selectedOrderId,
                    selectedAccount,
                    parsedDate
                )
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_sale")
        )
    }
}

@Composable
internal fun RecordPaymentSheet(
    title: String,
    subtitle: String,
    maxDueAr: Long,
    accounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (amountAr: Long, accountName: String) -> Unit
) {
    val colors = rememberTransitaireColors()
    val ariaryAccounts = remember(accounts) { accounts.filter { !it.isYuanAccount && it.isActive } }
    var amountText by remember(maxDueAr) { mutableStateOf(if (maxDueAr > 0L) maxDueAr.toString() else "") }
    var selectedAccount by remember { mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "Hors caisse") }
    var errorText by remember { mutableStateOf<String?>(null) }

    HumanSheetContainer(
        title = title,
        subtitle = subtitle,
        onDismiss = onDismiss,
        colors = colors
    ) {
        if (maxDueAr > 0L) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HumanTonalButton(
                    text = "Solde (${CurrencyFormatter.formatCompactAriary(maxDueAr)})",
                    onClick = { amountText = maxDueAr.toString() },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                if (maxDueAr >= 2L) {
                    HumanTonalButton(
                        text = "50%",
                        onClick = { amountText = (maxDueAr / 2L).toString() },
                        colors = colors,
                        containerColor = colors.innerCardBg,
                        contentColor = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        HumanTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { c -> c.isDigit() }; errorText = null },
            label = "Montant encaissé *",
            keyboardType = KeyboardType.Number,
            suffixText = "Ar",
            colors = colors
        )

        Text("Compte crédité", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
        HumanChipRow(
            items = ariaryAccounts.map { it.name to it.name } + ("Hors caisse" to "Hors caisse"),
            selectedKey = selectedAccount,
            onSelect = { selectedAccount = it },
            colors = colors
        )

        if (!errorText.isNullOrBlank()) {
            Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        }

        HumanPrimaryButton(
            text = "Enregistrer l'encaissement",
            icon = Icons.Default.Payments,
            onClick = {
                val amt = parseLongInput(amountText) ?: 0L
                if (amt <= 0L) {
                    errorText = "Veuillez saisir un montant valide."
                    return@HumanPrimaryButton
                }
                onConfirm(amt, selectedAccount)
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_confirm_payment")
        )
    }
}

@Composable
internal fun ClientDetailSheet(
    client: BusinessClientUi,
    clientSales: List<BusinessSaleEntity>,
    onDismiss: () -> Unit,
    onTransitionSheet: (BusinessActiveSheet) -> Unit
) {
    val colors = rememberTransitaireColors()
    val (bg, fg) = pastelToneForKey(client.name, colors)

    HumanSheetContainer(
        title = client.name,
        subtitle = "${client.category} · ${client.phone.ifBlank { "Téléphone non renseigné" }}",
        onDismiss = onDismiss,
        colors = colors
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HumanAvatarCircle(initials = client.initials, tint = fg, backgroundColor = bg, size = 50.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Total achats : ${CurrencyFormatter.formatAriary(client.totalPurchasesAr)}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )
                Text(
                    text = "Déjà payé : ${CurrencyFormatter.formatAriary(client.totalPaidAr)}",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
            HumanStatusPill(
                text = if (client.balanceDueAr > 0L) "Dû ${CurrencyFormatter.formatCompactAriary(client.balanceDueAr)}" else "À jour",
                backgroundColor = if (client.balanceDueAr > 0L) colors.warningSoftBg else colors.successSoftBg,
                contentColor = if (client.balanceDueAr > 0L) colors.warningAccent else colors.successAccent
            )
        }

        if (client.balanceDueAr > 0L) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HumanPrimaryButton(
                    text = "Encaisser",
                    icon = Icons.Default.Payments,
                    onClick = { onTransitionSheet(BusinessActiveSheet.RecordClientPayment(client)) },
                    modifier = Modifier.weight(1f)
                )
                HumanPrimaryButton(
                    text = "Relancer",
                    icon = Icons.AutoMirrored.Filled.Send,
                    onClick = {
                        onTransitionSheet(
                            BusinessActiveSheet.WhatsAppRelance(
                                clientName = client.name,
                                phone = client.phone,
                                balanceDueAr = client.balanceDueAr,
                                customMessage = client.suggestedRelanceMsg
                            )
                        )
                    },
                    containerColor = VioletSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (clientSales.isNotEmpty()) {
            HumanSectionCard(colors = colors, contentPadding = PaddingValues(14.dp)) {
                Text(
                    text = "Dernières commandes (${clientSales.size})",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                clientSales.take(5).forEachIndexed { idx, sale ->
                    if (idx > 0) HumanHairline(colors)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${sale.productName} (${sale.quantity})",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = DateUtils.formatShortDate(sale.saleDate),
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                        Text(
                            text = CurrencyFormatter.formatAriary(sale.totalPriceAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.cyanAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AddClientSheet(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, phone: String, initialPurchaseAr: Long) -> Unit
) {
    val colors = rememberTransitaireColors()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Business") }
    var initialPurchaseText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    HumanSheetContainer(
        title = "Nouveau client",
        subtitle = "Ajoutez un contact Business ou Famille & amis",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanTextField(
            value = name,
            onValueChange = { name = it; errorText = null },
            label = "Nom du client *",
            colors = colors
        )
        HumanTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Téléphone",
            keyboardType = KeyboardType.Phone,
            colors = colors
        )
        HumanSegmentedSwitch(
            options = listOf("Business" to "Business", "Famille & amis" to "Famille & amis"),
            selectedKey = category,
            onSelect = { category = it },
            colors = colors
        )
        HumanTextField(
            value = initialPurchaseText,
            onValueChange = { initialPurchaseText = it.filter { c -> c.isDigit() } },
            label = "Achat initial optionnel (Ar)",
            keyboardType = KeyboardType.Number,
            suffixText = "Ar",
            colors = colors
        )
        if (!errorText.isNullOrBlank()) {
            Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        }
        HumanPrimaryButton(
            text = "Enregistrer le client",
            icon = Icons.Default.Check,
            onClick = {
                if (name.isBlank()) {
                    errorText = "Veuillez saisir le nom du client."
                    return@HumanPrimaryButton
                }
                onConfirm(name.trim(), category, phone.trim(), parseLongInput(initialPurchaseText) ?: 0L)
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_client")
        )
    }
}

@Composable
internal fun WhatsAppRelanceSheet(
    clientName: String,
    phone: String,
    balanceDueAr: Long,
    initialMessage: String,
    onDismiss: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val context = LocalContext.current
    val defaultMsg = remember(clientName, balanceDueAr, initialMessage) {
        when {
            initialMessage.isNotBlank() -> initialMessage
            clientName.isNotBlank() && balanceDueAr > 0L ->
                "Bonjour $clientName ! J'espère que tu vas bien. Petit rappel amical concernant ton solde de ${CurrencyFormatter.formatAriary(balanceDueAr)}. Merci d'avance pour le règlement !"
            clientName.isNotBlank() ->
                "Bonjour $clientName ! J'espère que tu vas bien. Petit message amical concernant le règlement de ta commande."
            else ->
                "Bonjour ! J'espère que tu vas bien. Petit rappel amical pour le règlement de ta commande. Merci !"
        }
    }
    var messageText by remember(defaultMsg) { mutableStateOf(defaultMsg) }

    val copyMessage = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Relance WhatsApp", messageText))
        Toast.makeText(context, "Message copié dans le presse-papier", Toast.LENGTH_SHORT).show()
    }

    HumanSheetContainer(
        title = if (clientName.isNotBlank()) "Relancer $clientName" else "Relance WhatsApp",
        subtitle = if (balanceDueAr > 0L) "Solde dû : ${CurrencyFormatter.formatAriary(balanceDueAr)}" else "Message prêt à envoyer",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanTextField(
            value = messageText,
            onValueChange = { messageText = it },
            label = "Message de relance",
            singleLine = false,
            colors = colors
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HumanTonalButton(
                text = "Copier",
                icon = Icons.Default.ContentCopy,
                onClick = {
                    copyMessage()
                    onDismiss()
                },
                colors = colors,
                containerColor = colors.innerCardBg,
                contentColor = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            HumanPrimaryButton(
                text = "WhatsApp",
                icon = Icons.AutoMirrored.Filled.Send,
                onClick = {
                    val rawDigits = phone.replace(Regex("[^0-9]"), "")
                    val normalizedPhone = when {
                        rawDigits.startsWith("261") -> rawDigits
                        rawDigits.startsWith("0") && rawDigits.length >= 10 -> "261" + rawDigits.substring(1)
                        rawDigits.isNotBlank() -> "261$rawDigits"
                        else -> ""
                    }
                    val uri = if (normalizedPhone.isNotBlank()) {
                        Uri.parse("https://wa.me/$normalizedPhone?text=${Uri.encode(messageText)}")
                    } else {
                        Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(messageText)}")
                    }
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (_: Exception) {
                        copyMessage()
                    }
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
