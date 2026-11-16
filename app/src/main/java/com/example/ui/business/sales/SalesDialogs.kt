package com.example.ui.business.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun RecordSalePaymentDialog(
    sale: BusinessSaleEntity,
    accounts: List<BusinessAccountEntity> = emptyList(),
    onDismiss: () -> Unit,
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

@Composable
fun AddSaleDialog(
    availableStockOrders: List<BusinessOrderEntity>,
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
        accountName: String
    ) -> Unit
) {
    val ariaryAccounts = remember(accounts) {
        accounts.filter { !it.isYuanAccount && it.isActive }
    }
    val initialStockItem = initialArticle ?: availableStockOrders.firstOrNull()
    var selectedStockOrderId by remember { mutableStateOf<Long?>(initialStockItem?.id ?: -1L) }
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf(initialStockItem?.product ?: "") }
    var quantityText by remember { mutableStateOf("1") }
    var unitPriceText by remember { mutableStateOf(initialStockItem?.let { if (it.resalePriceAr > 0) it.resalePriceAr.toString() else "" } ?: "") }
    val initialCost = initialStockItem?.let { if (it.unitCostAr > 0L) it.unitCostAr else (it.priceYuan * it.exchangeRate).toLong() } ?: 0L
    var unitCostText by remember { mutableStateOf(if (initialCost > 0L) initialCost.toString() else "") }
    var paidAmountText by remember { mutableStateOf(initialStockItem?.let { if (it.resalePriceAr > 0) it.resalePriceAr.toString() else "" } ?: "") }
    var saleSource by remember { mutableStateOf("STOCK") }
    var domain by remember { mutableStateOf("BUSINESS") }
    var selectedAccountName by remember(ariaryAccounts) {
        mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "")
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

                Text(
                    text = "Sélection de l'article :",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isNewSelected = selectedStockOrderId == -1L
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isNewSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable {
                                selectedStockOrderId = -1L
                                productName = ""
                                unitPriceText = ""
                                unitCostText = ""
                                paidAmountText = ""
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("picker_new_article_sale")
                    ) {
                        Text(
                            text = "+ Nouvel article",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isNewSelected) Color.White else CyanPrimary
                        )
                    }

                    availableStockOrders.distinctBy { it.product }.forEach { order ->
                        val isSelected = selectedStockOrderId == order.id
                        val unitFifo = if (order.unitCostAr > 0L) {
                            order.unitCostAr
                        } else if (order.priceYuan > 0.0) {
                            (order.priceYuan * order.exchangeRate).toLong() + (if (order.quantityBought > 0) order.freightCostAr / order.quantityBought else 0L)
                        } else {
                            0L
                        }
                        val suggestedPrice = order.resalePriceAr
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    selectedStockOrderId = order.id
                                    productName = order.product
                                    unitCostText = if (unitFifo > 0L) unitFifo.toString() else ""
                                    unitPriceText = if (suggestedPrice > 0L) suggestedPrice.toString() else ""
                                    paidAmountText = if (suggestedPrice > 0L) suggestedPrice.toString() else ""
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "${order.product} (${order.stockCount} pcs)",
                                fontFamily = PlusJakartaSans,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
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

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Article vendu") },
                    modifier = Modifier.fillMaxWidth().testTag("input_sale_product"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantityText.toIntOrNull() ?: 1
                    val pPrice = unitPriceText.toLongOrNull() ?: 0L
                    val cCost = unitCostText.toLongOrNull() ?: 0L
                    val paid = paidAmountText.toLongOrNull() ?: 0L
                    if (clientName.isNotBlank() && productName.isNotBlank()) {
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
                            selectedAccountName
                        )
                        onDismiss()
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
