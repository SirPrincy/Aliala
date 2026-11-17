package com.example.ui.business.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.ui.business.BusinessClientUi
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSalePaymentDialog(
    sale: BusinessSaleEntity,
    accounts: List<BusinessAccountEntity> = emptyList(),
    onDismiss: () -> Unit,
    onUpdateSaleDate: (Long) -> Unit = {},
    onConfirmPayment: (additionalAmountAr: Long, accountName: String) -> Unit,
    onDeleteSale: () -> Unit
) {
    val ariaryAccounts = remember(accounts) {
        accounts.filter { !it.isYuanAccount && it.isActive }
    }
    val remaining = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
    var paymentText by remember { mutableStateOf(remaining.toString()) }
    var selectedAccountName by remember(ariaryAccounts) {
        mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "")
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSaleDatePicker by remember { mutableStateOf(false) }

    if (showSaleDatePicker) {
        val initialUtcMillis = DateUtils.frenchDateToUtcPickerMillis(DateUtils.formatShortDate(sale.saleDate))
            ?: DateUtils.frenchDateToUtcPickerMillis(DateUtils.getTodayFrenchDate())
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)
        DatePickerDialog(
            onDismissRequest = { showSaleDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { picked ->
                            val pickedFrench = DateUtils.utcPickerMillisToFrenchDate(picked)
                            onUpdateSaleDate(DateUtils.frenchDateToEpochMillis(pickedFrench))
                        }
                        showSaleDatePicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showSaleDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text("Confirmer la suppression", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A))
            },
            text = {
                Text(
                    text = "Voulez-vous vraiment annuler et supprimer la facture ${sale.invoiceNumber.ifBlank { "#---" }} de ${sale.clientName} ?\nLe stock correspondant (${sale.quantity} unités) sera réintégré automatiquement.",
                    fontFamily = Inter,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteSale()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                ) {
                    Text("Supprimer définitivement", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Enregistrer un règlement",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "${sale.clientName} • Facture ${sale.invoiceNumber.ifBlank { "#---" }}",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Date de vente", fontFamily = Inter, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showSaleDatePicker = true }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .testTag("btn_edit_sale_date")
                            ) {
                                Text(
                                    text = DateUtils.formatShortDate(sale.saleDate),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = CyanPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modifier la date de vente",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total facture", fontFamily = Inter, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Text(CurrencyFormatter.formatAriary(sale.totalPriceAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Déjà réglé", fontFamily = Inter, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Text(CurrencyFormatter.formatAriary(sale.paidAmountAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF16A34A))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Reste dû", fontFamily = Inter, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Text(CurrencyFormatter.formatAriary(remaining), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFDC2626))
                        }
                    }
                }

                OutlinedTextField(
                    value = paymentText,
                    onValueChange = { paymentText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Montant encaissé (Ar)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text(
                    text = "Caisse d'encaissement",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isOffCash = selectedAccountName.isBlank()
                    Surface(
                        modifier = Modifier.clickable { selectedAccountName = "" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isOffCash) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Hors caisse",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isOffCash) Color.Black else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                    ariaryAccounts.forEach { acc ->
                        val isSelected = selectedAccountName.equals(acc.name, ignoreCase = true)
                        Surface(
                            modifier = Modifier.clickable { selectedAccountName = acc.name },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
                                Text(
                                    text = acc.name,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = CurrencyFormatter.formatAriary(acc.balanceAr),
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.Black.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (remaining > 0) {
                    TextButton(
                        onClick = { paymentText = remaining.toString() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Régler la totalité (${CurrencyFormatter.formatAriary(remaining)})", fontFamily = PlusJakartaSans, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = paymentText.toLongOrNull() ?: 0L
                    if (amount > 0) {
                        onConfirmPayment(amount, selectedAccountName)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Enregistrer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = { showDeleteConfirm = true }
                ) {
                    Text("Supprimer", fontFamily = PlusJakartaSans, color = Color(0xFFBA1A1A))
                }
                TextButton(onClick = onDismiss) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleDialog(
    availableStockOrders: List<BusinessOrderEntity>,
    availableClients: List<BusinessClientUi> = emptyList(),
    initialArticle: BusinessOrderEntity? = null,
    accounts: List<BusinessAccountEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
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
    val ariaryAccounts = remember(accounts) {
        accounts.filter { !it.isYuanAccount && it.isActive }
    }
    val distinctArticles = remember(availableStockOrders) {
        availableStockOrders
            .filter { it.product.isNotBlank() }
            .groupBy { it.product.trim().lowercase() }
            .values
            .map { group -> group.maxByOrNull { it.stockCount } ?: group.first() }
            .sortedWith(compareByDescending<BusinessOrderEntity> { it.stockCount > 0 }.thenBy { it.product.lowercase() })
    }
    val initialStockItem = initialArticle ?: distinctArticles.firstOrNull { it.stockCount > 0 } ?: distinctArticles.firstOrNull()
    val prematchedClient = remember(initialArticle, availableClients) {
        val assigned = initialArticle?.assignedClient?.trim().orEmpty()
        if (assigned.isNotBlank()) {
            availableClients.firstOrNull { it.name.trim().equals(assigned, ignoreCase = true) }
        } else null
    }
    var isCustomClientMode by remember(availableClients, prematchedClient) {
        mutableStateOf(availableClients.isEmpty())
    }
    var selectedExistingClient by remember(prematchedClient) {
        mutableStateOf(prematchedClient)
    }
    var clientMenuExpanded by remember { mutableStateOf(false) }

    var isCustomArticleMode by remember(distinctArticles, initialStockItem) {
        mutableStateOf(initialStockItem == null)
    }
    var selectedStockOrderId by remember(initialStockItem) {
        mutableStateOf<Long?>(if (initialStockItem != null && initialStockItem.stockCount > 0) initialStockItem.id else -1L)
    }
    var articleMenuExpanded by remember { mutableStateOf(false) }

    var clientName by remember(prematchedClient, initialArticle) {
        mutableStateOf(prematchedClient?.name ?: initialArticle?.assignedClient?.trim().orEmpty())
    }
    var clientPhone by remember(prematchedClient) {
        mutableStateOf(prematchedClient?.phone ?: "")
    }
    var productName by remember(initialStockItem) { mutableStateOf(initialStockItem?.product ?: "") }
    var saleDateText by remember { mutableStateOf(DateUtils.getTodayFrenchDate()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var quantityText by remember { mutableStateOf("1") }
    var unitPriceText by remember(initialStockItem) {
        mutableStateOf(initialStockItem?.let { if (it.resalePriceAr > 0) it.resalePriceAr.toString() else "" } ?: "")
    }
    val initialCost = initialStockItem?.let {
        when {
            it.unitCostAr > 0L -> it.unitCostAr
            it.priceYuan > 0.0 -> (it.priceYuan * it.exchangeRate).toLong() + (if (it.quantityBought > 0) it.freightCostAr / it.quantityBought else 0L)
            else -> 0L
        }
    } ?: 0L
    var unitCostText by remember(initialStockItem) { mutableStateOf(if (initialCost > 0L) initialCost.toString() else "") }
    var paidAmountText by remember(initialStockItem) {
        mutableStateOf(initialStockItem?.let { if (it.resalePriceAr > 0) it.resalePriceAr.toString() else "" } ?: "")
    }
    var saleSource by remember { mutableStateOf("STOCK") }
    var domain by remember(prematchedClient) {
        mutableStateOf(
            if (prematchedClient?.category == "Famille & amis") "FAMILY_FRIENDS" else "BUSINESS"
        )
    }
    var selectedAccountName by remember(ariaryAccounts) {
        mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        val initialUtcMillis = DateUtils.frenchDateToUtcPickerMillis(saleDateText)
            ?: DateUtils.frenchDateToUtcPickerMillis(DateUtils.getTodayFrenchDate())
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { picked ->
                            saleDateText = DateUtils.utcPickerMillisToFrenchDate(picked)
                            errorMessage = null
                        }
                        showDatePicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nouvelle vente",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (domain == "BUSINESS") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { domain = "BUSINESS" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Business Pro",
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (domain == "BUSINESS") FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (domain == "BUSINESS") CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (domain == "FAMILY_FRIENDS") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { domain = "FAMILY_FRIENDS" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Famille & Amis",
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (domain == "FAMILY_FRIENDS") FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (domain == "FAMILY_FRIENDS") Color(0xFF712AE2) else MaterialTheme.colorScheme.onSurfaceVariant
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (saleSource == "STOCK") CyanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { saleSource = "STOCK" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sur stock",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (saleSource == "STOCK") CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (saleSource == "ORDER") Color(0xFF712AE2).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { saleSource = "ORDER" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sur commande",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (saleSource == "ORDER") Color(0xFF712AE2) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    val clientDisplayText = when {
                        isCustomClientMode -> "+ Nouveau client"
                        selectedExistingClient != null -> {
                            val c = selectedExistingClient!!
                            if (c.phone.isNotBlank()) "${c.name} • ${c.phone}" else c.name
                        }
                        clientName.isNotBlank() -> clientName
                        else -> "Choisir un client..."
                    }
                    SaleDropdownSelector(
                        label = "Client",
                        valueText = clientDisplayText,
                        isPlaceholder = !isCustomClientMode && selectedExistingClient == null && clientName.isBlank(),
                        expanded = clientMenuExpanded,
                        leadingIcon = Icons.Default.Person,
                        onClick = { clientMenuExpanded = true },
                        testTag = "selector_client"
                    )
                    DropdownMenu(
                        expanded = clientMenuExpanded,
                        onDismissRequest = { clientMenuExpanded = false },
                        modifier = Modifier
                            .widthIn(min = 240.dp)
                            .heightIn(max = 280.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "+ Nouveau client",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CyanPrimary
                                )
                            },
                            leadingIcon = {
                                if (isCustomClientMode) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                }
                            },
                            onClick = {
                                clientMenuExpanded = false
                                isCustomClientMode = true
                                selectedExistingClient = null
                                clientName = ""
                                clientPhone = ""
                                errorMessage = null
                            }
                        )
                        if (availableClients.isNotEmpty()) {
                            HorizontalDivider()
                            availableClients.forEach { client ->
                                val isSelected = !isCustomClientMode && selectedExistingClient?.name.equals(client.name, ignoreCase = true)
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = client.name,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = client.phone.ifBlank { client.category },
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                        }
                                    },
                                    onClick = {
                                        clientMenuExpanded = false
                                        isCustomClientMode = false
                                        selectedExistingClient = client
                                        clientName = client.name
                                        clientPhone = client.phone
                                        domain = if (client.category == "Famille & amis") "FAMILY_FRIENDS" else "BUSINESS"
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    }
                }

                if (isCustomClientMode) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = {
                            clientName = it
                            errorMessage = null
                        },
                        label = { Text("Nom du client (ex. Rina)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_sale_client"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Téléphone (ex. 034 11 222 33)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("input_sale_phone"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    val selectedOrder = distinctArticles.firstOrNull {
                        !isCustomArticleMode && it.product.equals(productName, ignoreCase = true)
                    }
                    val articleDisplayText = when {
                        isCustomArticleMode -> "+ Nouvel article"
                        selectedOrder != null -> {
                            if (selectedOrder.stockCount > 0) "${selectedOrder.product} (${selectedOrder.stockCount} pcs)"
                            else selectedOrder.product
                        }
                        productName.isNotBlank() -> productName
                        else -> "Choisir un article..."
                    }
                    SaleDropdownSelector(
                        label = "Article",
                        valueText = articleDisplayText,
                        isPlaceholder = !isCustomArticleMode && selectedOrder == null && productName.isBlank(),
                        expanded = articleMenuExpanded,
                        leadingIcon = Icons.Default.Inventory2,
                        onClick = { articleMenuExpanded = true },
                        testTag = "selector_article"
                    )
                    DropdownMenu(
                        expanded = articleMenuExpanded,
                        onDismissRequest = { articleMenuExpanded = false },
                        modifier = Modifier
                            .widthIn(min = 240.dp)
                            .heightIn(max = 280.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "+ Nouvel article",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CyanPrimary
                                )
                            },
                            leadingIcon = {
                                if (isCustomArticleMode) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                }
                            },
                            onClick = {
                                articleMenuExpanded = false
                                isCustomArticleMode = true
                                selectedStockOrderId = -1L
                                productName = ""
                                unitPriceText = ""
                                unitCostText = ""
                                paidAmountText = ""
                                errorMessage = null
                            }
                        )
                        if (distinctArticles.isNotEmpty()) {
                            HorizontalDivider()
                            distinctArticles.forEach { order ->
                                val isSelected = !isCustomArticleMode && productName.equals(order.product, ignoreCase = true)
                                val unitFifo = if (order.unitCostAr > 0L) {
                                    order.unitCostAr
                                } else if (order.priceYuan > 0.0) {
                                    (order.priceYuan * order.exchangeRate).toLong() + (if (order.quantityBought > 0) order.freightCostAr / order.quantityBought else 0L)
                                } else {
                                    0L
                                }
                                val suggestedPrice = order.resalePriceAr
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = order.product,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp
                                            )
                                            val stockLabel = if (order.stockCount > 0) "${order.stockCount} pcs en stock" else "Sur commande / catalogue"
                                            val priceLabel = if (suggestedPrice > 0L) " • ${CurrencyFormatter.formatAriary(suggestedPrice)}" else ""
                                            Text(
                                                text = "$stockLabel$priceLabel",
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                        }
                                    },
                                    onClick = {
                                        articleMenuExpanded = false
                                        isCustomArticleMode = false
                                        selectedStockOrderId = if (order.stockCount > 0) order.id else -1L
                                        productName = order.product
                                        unitCostText = if (unitFifo > 0L) unitFifo.toString() else ""
                                        unitPriceText = if (suggestedPrice > 0L) suggestedPrice.toString() else ""
                                        paidAmountText = if (suggestedPrice > 0L) suggestedPrice.toString() else ""
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    }
                }

                if (isCustomArticleMode) {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = {
                            productName = it
                            errorMessage = null
                        },
                        label = { Text("Article vendu") },
                        modifier = Modifier.fillMaxWidth().testTag("input_sale_product"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = saleDateText,
                    onValueChange = {
                        saleDateText = it
                        errorMessage = null
                    },
                    label = { Text("Date de vente (jj/mm/aaaa)") },
                    placeholder = { Text("ex. 17/11/2026") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Choisir la date de vente",
                                tint = CyanPrimary
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_sale_date"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = {
                            quantityText = it.filter { ch -> ch.isDigit() }
                            errorMessage = null
                        },
                        label = { Text("Quantité") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f).testTag("input_sale_quantity"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = { unitPriceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Prix vente (Ar)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f).testTag("input_sale_unit_price"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = unitCostText,
                        onValueChange = { unitCostText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Coût unitaire (Ar)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_sale_unit_cost"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = paidAmountText,
                        onValueChange = { paidAmountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Acompte / Réglé (Ar)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_sale_paid_amount"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Text(
                    text = "Caisse d'encaissement",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isOffCash = selectedAccountName.isBlank()
                    Surface(
                        modifier = Modifier.clickable { selectedAccountName = "" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isOffCash) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Hors caisse",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isOffCash) Color.Black else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                    ariaryAccounts.forEach { acc ->
                        val isSelected = selectedAccountName.equals(acc.name, ignoreCase = true)
                        Surface(
                            modifier = Modifier.clickable { selectedAccountName = acc.name },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
                                Text(
                                    text = acc.name,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = CurrencyFormatter.formatAriary(acc.balanceAr),
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.Black.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantityText.toIntOrNull() ?: 0
                    val pPrice = unitPriceText.toLongOrNull() ?: 0L
                    val cCost = unitCostText.toLongOrNull() ?: 0L
                    val paid = paidAmountText.toLongOrNull() ?: 0L
                    when {
                        clientName.isBlank() -> errorMessage = if (isCustomClientMode) {
                            "Veuillez saisir le nom du client."
                        } else {
                            "Veuillez sélectionner un client."
                        }
                        productName.isBlank() -> errorMessage = if (isCustomArticleMode) {
                            "Veuillez saisir l'article vendu."
                        } else {
                            "Veuillez sélectionner un article."
                        }
                        !DateUtils.isValidFrenchDate(saleDateText) -> errorMessage = "Date invalide (format attendu : jj/mm/aaaa)."
                        q <= 0 -> errorMessage = "La quantité doit être supérieure à 0."
                        else -> {
                            onConfirm(
                                productName.trim(),
                                clientName.trim(),
                                clientPhone.trim(),
                                q,
                                pPrice,
                                cCost,
                                paid,
                                saleSource,
                                domain,
                                if (selectedStockOrderId != null && selectedStockOrderId!! > 0L) selectedStockOrderId else null,
                                selectedAccountName,
                                DateUtils.frenchDateToEpochMillis(saleDateText)
                            )
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Créer la vente", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
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
private fun SaleDropdownSelector(
    label: String,
    valueText: String,
    isPlaceholder: Boolean,
    expanded: Boolean,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .border(
                width = 1.dp,
                color = if (expanded) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = valueText,
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (isPlaceholder) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = "Ouvrir le sélecteur $label",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
