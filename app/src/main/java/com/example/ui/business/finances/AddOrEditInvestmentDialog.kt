package com.example.ui.business.finances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun AddOrEditInvestmentDialog(
    existing: BusinessInvestmentEntity?,
    ariaryAccounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onConfirmCreate: (
        title: String,
        category: String,
        amountAr: Long,
        estimatedValueAr: Long,
        generatedReturnAr: Long,
        accountName: String,
        notes: String,
        dateMillis: Long
    ) -> Unit,
    onConfirmUpdate: (BusinessInvestmentEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    val isEdit = existing != null

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "PLACEMENT") }
    var amountText by remember { mutableStateOf(if (existing != null && existing.amountAr > 0L) existing.amountAr.toString() else "") }
    var estimatedValueText by remember {
        mutableStateOf(if (existing != null) existing.estimatedValueAr.toString() else "")
    }
    var valueManuallyEdited by remember { mutableStateOf(existing != null) }
    var generatedReturnText by remember {
        mutableStateOf(if (existing != null && existing.generatedReturnAr > 0L) existing.generatedReturnAr.toString() else "")
    }
    var status by remember { mutableStateOf(existing?.status ?: "ACTIF") }
    var debitAccount by remember(ariaryAccounts) { mutableStateOf(ariaryAccounts.isNotEmpty()) }
    var selectedAccount by remember(ariaryAccounts) {
        mutableStateOf(existing?.accountName ?: ariaryAccounts.firstOrNull()?.name ?: "Hors caisse")
    }
    var selectedDateMillis by remember { mutableLongStateOf(existing?.dateMillis ?: System.currentTimeMillis()) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val titlePlaceholder = when (category) {
        "PLACEMENT" -> "ex. Épargne bloquée, Parts boutique, Prêt rémunéré..."
        "MATERIEL" -> "ex. Studio photo, Imprimante, Étagères..."
        "LOGISTIQUE" -> "ex. Moto livraison, Balance colis..."
        "STOCK_STRATEGIQUE" -> "ex. Lot réserve haute saison..."
        "MARKETING" -> "ex. Campagne Facebook Ads, Shooting..."
        "INFRASTRUCTURE" -> "ex. Aménagement boutique, Caution local..."
        else -> "Nom du projet ou de l'investissement..."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBg)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
                Surface(color = colors.cardBg, shadowElevation = 1.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Close, "Fermer", tint = colors.textPrimary)
                            }
                            Text(
                                text = if (isEdit) "Modifier l'investissement" else "Nouvel investissement",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = colors.textPrimary
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Type d'investissement",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        INVESTMENT_CATEGORIES.forEach { cat ->
                            val isSel = category == cat.code
                            Surface(
                                modifier = Modifier.clickable { category = cat.code },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) colors.violetSoftBg else colors.cardBg,
                                border = BorderStroke(if (isSel) 1.5.dp else 1.dp, if (isSel) colors.violetAccent else colors.border)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(cat.emoji, fontSize = 15.sp)
                                    Text(
                                        text = cat.label,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSel) colors.violetAccent else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = null
                        },
                        label = { Text("Nom de l'investissement *") },
                        placeholder = { Text(titlePlaceholder) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    CapitalDateField(
                        label = "Date de l'investissement",
                        dateMillis = selectedDateMillis,
                        colors = colors,
                        onDateSelected = { selectedDateMillis = it }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                amountText = filtered
                                if (!valueManuallyEdited) {
                                    estimatedValueText = filtered
                                }
                                errorMessage = null
                            },
                            label = { Text("Montant investi (Ar) *") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = estimatedValueText,
                            onValueChange = { input ->
                                estimatedValueText = input.filter { it.isDigit() }
                                valueManuallyEdited = true
                            },
                            label = { Text("Valeur actuelle (Ar)") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = generatedReturnText,
                        onValueChange = { generatedReturnText = it.filter { ch -> ch.isDigit() } },
                        label = {
                            Text(
                                if (category == "PLACEMENT") "Intérêts / Gains générés (Ar, optionnel)"
                                else "Retours / Revenus générés (Ar, optionnel)"
                            )
                        },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isEdit) {
                        Text(
                            text = "Statut de l'actif",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("ACTIF" to "Actif", "AMORTI" to "Amorti", "CEDE" to "Cédé").forEach { (code, label) ->
                                val isSel = status == code
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { status = code },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) CyanPrimary else colors.cardBg,
                                    border = if (isSel) null else BorderStroke(1.dp, colors.border)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSel) Color.Black else colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { debitAccount = !debitAccount },
                            shape = RoundedCornerShape(14.dp),
                            color = colors.cardBg,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Débiter un compte & Transactions",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (debitAccount) {
                                            "Débite le compte choisi et enregistre la sortie dans Transactions"
                                        } else {
                                            "Hors caisse : suivi patrimonial uniquement (aucune transaction)"
                                        },
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Switch(
                                    checked = debitAccount,
                                    onCheckedChange = { debitAccount = it }
                                )
                            }
                        }

                        if (debitAccount) {
                            if (ariaryAccounts.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ariaryAccounts.forEach { acc ->
                                        val isSel = selectedAccount.equals(acc.name, ignoreCase = true)
                                        Surface(
                                            modifier = Modifier.clickable { selectedAccount = acc.name },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSel) CyanPrimary else colors.cardBg,
                                            border = if (isSel) null else BorderStroke(1.dp, colors.border)
                                        ) {
                                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                                Text(
                                                    text = acc.name,
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (isSel) Color.Black else colors.textPrimary
                                                )
                                                Text(
                                                    text = CurrencyFormatter.formatAriary(acc.balanceAr),
                                                    fontFamily = Inter,
                                                    fontSize = 10.sp,
                                                    color = if (isSel) Color.Black.copy(alpha = 0.75f) else colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = selectedAccount,
                                    onValueChange = { selectedAccount = it },
                                    label = { Text("Nom du compte à débiter") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Détails (optionnel)") },
                        placeholder = { Text("Conditions, taux, échéance, garantie...") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.dangerAccent
                        )
                    }
                }

                Surface(color = colors.cardBg, shadowElevation = 4.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Annuler", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val parsedAmount = amountText.toLongOrNull() ?: 0L
                                val parsedEst = estimatedValueText.toLongOrNull() ?: parsedAmount
                                val parsedReturn = generatedReturnText.toLongOrNull() ?: 0L
                                when {
                                    title.isBlank() -> errorMessage = "Veuillez saisir le nom de l'investissement."
                                    parsedAmount <= 0L -> errorMessage = "Veuillez saisir un montant investi valide."
                                    isEdit && existing != null -> {
                                        onConfirmUpdate(
                                            existing.copy(
                                                title = title.trim(),
                                                category = category,
                                                amountAr = parsedAmount,
                                                estimatedValueAr = parsedEst.coerceAtLeast(0L),
                                                generatedReturnAr = parsedReturn.coerceAtLeast(0L),
                                                status = status,
                                                dateMillis = selectedDateMillis,
                                                notes = notes.trim()
                                            )
                                        )
                                    }
                                    else -> {
                                        onConfirmCreate(
                                            title.trim(),
                                            category,
                                            parsedAmount,
                                            parsedEst.coerceAtLeast(0L),
                                            parsedReturn.coerceAtLeast(0L),
                                            if (debitAccount) selectedAccount.trim() else "Hors caisse",
                                            notes.trim(),
                                            selectedDateMillis
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1.4f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (isEdit) "Mettre à jour" else "Enregistrer",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
