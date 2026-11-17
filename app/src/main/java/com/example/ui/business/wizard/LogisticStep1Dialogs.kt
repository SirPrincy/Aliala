package com.example.ui.business.wizard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.model.DomesticParcelItem
import com.example.ui.business.model.DomesticParcelStorage
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Step1EditOrderDialog(
    order: BusinessOrderEntity,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onSave: (BusinessOrderEntity) -> Unit
) {
    var prodInput by remember { mutableStateOf(order.product) }
    var refInput by remember { mutableStateOf(order.referenceCode) }
    var platformInput by remember { mutableStateOf(order.platform) }
    var supplierInput by remember { mutableStateOf(order.supplier) }
    var qtyInput by remember { mutableStateOf(if (order.quantityBought > 0) order.quantityBought.toString() else "") }
    var priceYuanInput by remember { mutableStateOf(if (order.priceYuan > 0.0) formatYuanCompact(order.priceYuan) else "") }
    var domesticYuanInput by remember { mutableStateOf(formatYuanCompact(order.domesticShippingYuan)) }
    var orderDateInput by remember { mutableStateOf(DateUtils.formatShortDate(order.orderDate)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        val initialUtcMillis = DateUtils.frenchDateToUtcPickerMillis(orderDateInput)
            ?: DateUtils.frenchDateToUtcPickerMillis(DateUtils.getTodayFrenchDate())
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { picked ->
                            orderDateInput = DateUtils.utcPickerMillisToFrenchDate(picked)
                            errorText = null
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
            Text("Modifier la commande", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = prodInput,
                    onValueChange = { prodInput = it },
                    label = { Text("Nom de l'article") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = orderDateInput,
                    onValueChange = {
                        orderDateInput = it
                        errorText = null
                    },
                    label = { Text("Date d'achat (jj/mm/aaaa)") },
                    placeholder = { Text("ex. 15/03/2025") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Choisir la date d'achat",
                                tint = colors.cyanAccent
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Quantité") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = priceYuanInput,
                        onValueChange = { priceYuanInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Prix usine (¥)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = domesticYuanInput,
                    onValueChange = { domesticYuanInput = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Frais de port intérieurs Chine (¥)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = platformInput,
                        onValueChange = { platformInput = it },
                        label = { Text("Plateforme") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = refInput,
                        onValueChange = { refInput = it },
                        label = { Text("Référence") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = supplierInput,
                    onValueChange = { supplierInput = it },
                    label = { Text("Fournisseur") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                errorText?.let { msg ->
                    Text(text = msg, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!DateUtils.isValidFrenchDate(orderDateInput)) {
                        errorText = "Date invalide (format attendu : jj/mm/aaaa)"
                    } else {
                        val newQty = qtyInput.toIntOrNull()?.coerceAtLeast(1) ?: order.quantityBought
                        val newPrice = priceYuanInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: order.priceYuan
                        val newDomestic = domesticYuanInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                        onSave(
                            order.copy(
                                product = prodInput.trim().ifBlank { order.product },
                                referenceCode = refInput.trim(),
                                platform = platformInput.trim(),
                                supplier = supplierInput.trim(),
                                quantityBought = newQty,
                                priceYuan = newPrice,
                                domesticShippingYuan = newDomestic,
                                orderDate = DateUtils.frenchDateToEpochMillis(orderDateInput)
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
            ) {
                Text("Enregistrer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step1EditDomesticShippingDialog(
    order: BusinessOrderEntity,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onApply: (Double) -> Unit
) {
    var domesticInput by remember {
        mutableStateOf(formatYuanCompact(order.domesticShippingYuan))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Frais de port intérieurs (Chine)", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Montant facturé par le fournisseur pour la livraison vers l'entrepôt transitaire en Chine.",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                OutlinedTextField(
                    value = domesticInput,
                    onValueChange = { domesticInput = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Frais de port (¥)") },
                    suffix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("0" to "0 ¥ (Gratuit)", "8" to "8 ¥", "10" to "10 ¥", "15" to "15 ¥", "20" to "20 ¥").forEach { (v, lbl) ->
                        val selected = domesticInput == v
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) colors.cyanAccent else colors.badgeNeutralBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { domesticInput = v }
                        ) {
                            Text(
                                text = lbl,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = if (selected) Color.White else colors.cyanAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(domesticInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
            ) {
                Text("Appliquer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step1EditReferenceDialog(
    order: BusinessOrderEntity,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onSave: (platform: String, referenceCode: String) -> Unit
) {
    var refCodeText by remember { mutableStateOf(order.referenceCode) }
    var platformText by remember { mutableStateOf(order.platform) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Commande fournisseur", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = platformText,
                    onValueChange = { platformText = it },
                    label = { Text("Plateforme (ex: 1688, Taobao, Alibaba)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1688", "Taobao", "Pinduoduo", "Alibaba", "WeChat").forEach { plat ->
                        val selected = platformText.equals(plat, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) colors.cyanAccent else colors.badgeNeutralBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { platformText = plat }
                        ) {
                            Text(
                                text = plat,
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = if (selected) Color.White else colors.cyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = refCodeText,
                    onValueChange = { refCodeText = it },
                    label = { Text("Numéro de commande / Réf") },
                    placeholder = { Text("Ex: 248910384710") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(platformText.trim(), refCodeText.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
            ) {
                Text("Enregistrer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step1EditDispatchDialog(
    order: BusinessOrderEntity,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var dispatchText by remember { mutableStateOf(order.supplierDispatchDelay) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Délai d'expédition usine", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = dispatchText,
                    onValueChange = { dispatchText = it },
                    label = { Text("Délai d'envoi depuis l'usine") },
                    placeholder = { Text("Ex: 2 - 3 j ouvrés") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Immédiat (24h)", "1 - 2 j ouvrés", "2 - 3 j ouvrés", "3 - 5 j ouvrés", "5 - 7 j ouvrés", "Expédié").forEach { preset ->
                        val selected = dispatchText == preset
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) colors.cyanAccent else colors.badgeNeutralBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { dispatchText = preset }
                        ) {
                            Text(
                                text = preset,
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = if (selected) Color.White else colors.cyanAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(dispatchText.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
            ) {
                Text("Valider", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step1AddTrackingDialog(
    order: BusinessOrderEntity,
    parcels: List<DomesticParcelItem>,
    editingOldTracking: String?,
    initialTracking: String,
    initialCarrier: String,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onSave: (BusinessOrderEntity) -> Unit
) {
    var trackingInput by remember(editingOldTracking, initialTracking) { mutableStateOf(initialTracking) }
    var carrierInput by remember(editingOldTracking, initialCarrier) { mutableStateOf(initialCarrier) }
    var trackingError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (editingOldTracking != null) "Modifier le numéro de suivi" else "Ajouter un numéro de suivi",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Transporteur intérieur :", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DomesticParcelStorage.commonCarriers.take(6).forEach { carrier ->
                        val selected = carrierInput.equals(carrier, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) colors.cyanAccent else colors.badgeNeutralBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { carrierInput = carrier }
                        ) {
                            Text(
                                text = carrier,
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = if (selected) Color.White else colors.cyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = carrierInput,
                    onValueChange = { carrierInput = it },
                    label = { Text("Transporteur") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = trackingInput,
                    onValueChange = {
                        trackingInput = it
                        trackingError = null
                    },
                    label = { Text("Numéro de suivi *") },
                    placeholder = { Text("Ex: SF1234567890") },
                    singleLine = true,
                    isError = trackingError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                trackingError?.let { msg ->
                    Text(text = msg, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleaned = trackingInput.trim()
                    if (cleaned.isEmpty()) {
                        trackingError = "Veuillez saisir un numéro de suivi."
                    } else if (
                        !cleaned.equals(editingOldTracking ?: "", ignoreCase = true) &&
                        parcels.any { it.trackingNumber.equals(cleaned, ignoreCase = true) }
                    ) {
                        trackingError = "Ce numéro est déjà enregistré."
                    } else {
                        onSave(
                            DomesticParcelStorage.upsertTracking(
                                order = order,
                                oldTracking = editingOldTracking,
                                newTracking = cleaned,
                                carrierName = carrierInput.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
            ) {
                Text(if (editingOldTracking != null) "Enregistrer" else "Ajouter", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
