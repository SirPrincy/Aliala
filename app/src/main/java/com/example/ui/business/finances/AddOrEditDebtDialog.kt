package com.example.ui.business.finances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddOrEditBusinessDebtDialog(
    existing: BusinessDebtEntity? = null,
    ariaryAccounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onConfirmCreate: (
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
    ) -> Unit,
    onConfirmUpdate: (BusinessDebtEntity) -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val isEdit = existing != null
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }

    var direction by remember { mutableStateOf(existing?.direction ?: "BORROWED") }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var counterparty by remember { mutableStateOf(existing?.counterparty ?: "") }
    var principalText by remember { mutableStateOf(existing?.principalAr?.toString() ?: "") }
    var interestText by remember {
        mutableStateOf(
            when {
                existing == null -> "0"
                existing.interestPercent % 1.0 == 0.0 -> existing.interestPercent.toInt().toString()
                else -> existing.interestPercent.toString()
            }
        )
    }
    var frequency by remember { mutableStateOf(existing?.repaymentFrequency ?: "Mensuel") }
    var createdAtMillis by remember { mutableLongStateOf(existing?.createdAtMillis ?: System.currentTimeMillis()) }
    var hasDueDate by remember { mutableStateOf(existing?.dueDateMillis != null) }
    var dueDateMillis by remember {
        mutableLongStateOf(
            existing?.dueDateMillis ?: (System.currentTimeMillis() + 30L * 86_400_000L)
        )
    }
    var showCreatedPicker by remember { mutableStateOf(false) }
    var showDuePicker by remember { mutableStateOf(false) }
    var affectAccount by remember { mutableStateOf(ariaryAccounts.isNotEmpty()) }
    var selectedAccount by remember(ariaryAccounts) {
        mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "")
    }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showCreatedPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = debtLocalMillisToUtcPickerMillis(createdAtMillis))
        DatePickerDialog(
            onDismissRequest = { showCreatedPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { createdAtMillis = debtUtcPickerMillisToLocalNoon(it) }
                        showCreatedPicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreatedPicker = false }) { Text("Annuler") }
            }
        ) { DatePicker(state = state) }
    }

    if (showDuePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = debtLocalMillisToUtcPickerMillis(dueDateMillis))
        DatePickerDialog(
            onDismissRequest = { showDuePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { dueDateMillis = debtUtcPickerMillisToLocalNoon(it) }
                        showDuePicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDuePicker = false }) { Text("Annuler") }
            }
        ) { DatePicker(state = state) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Text(
                text = if (isEdit) "Modifier l'engagement" else "Nouvel emprunt / prêt",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isEdit) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("BORROWED" to "Emprunt (Je dois)", "LENT" to "Prêt (On me doit)").forEach { (dir, label) ->
                            val isSel = direction == dir
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { direction = dir },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) colors.violetAccent else colors.innerCardBg,
                                border = if (isSel) null else BorderStroke(1.dp, colors.border)
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.White else colors.textPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
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
                    label = { Text("Libellé *") },
                    placeholder = { Text("ex. Prêt Tontine / Avance stock") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_debt_title")
                )

                OutlinedTextField(
                    value = counterparty,
                    onValueChange = { counterparty = it },
                    label = { Text(if (direction == "BORROWED") "Prêteur / Organisme" else "Bénéficiaire / Tiers") },
                    placeholder = { Text("ex. Ami, Banque BNI, Tontine") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_debt_counterparty")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = principalText,
                        onValueChange = { input ->
                            principalText = input.filter { it.isDigit() }
                            errorMessage = null
                        },
                        label = { Text("Montant (Ar) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f).testTag("input_debt_principal")
                    )

                    OutlinedTextField(
                        value = interestText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1) {
                                interestText = filtered
                            }
                        },
                        label = { Text("Intérêt (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("input_debt_interest")
                    )
                }

                Text(
                    text = "Échéancier",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Ponctuel", "Hebdomadaire", "Mensuel").forEach { freq ->
                        val isSel = frequency == freq
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { frequency = freq },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) CyanPrimary else colors.innerCardBg
                        ) {
                            Text(
                                text = freq,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSel) Color.Black else colors.textPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCreatedPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = colors.innerCardBg,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Choisir la date",
                                tint = colors.violetAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Date : ${dateFmt.format(Date(createdAtMillis))}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                        }
                        Text(
                            text = "Modifier",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.violetAccent
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.innerCardBg,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Définir une date d'échéance",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                            Switch(
                                checked = hasDueDate,
                                onCheckedChange = { hasDueDate = it }
                            )
                        }
                        if (hasDueDate) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showDuePicker = true }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Échéance : ${dateFmt.format(Date(dueDateMillis))}",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = colors.violetAccent
                                )
                                Text(
                                    text = "Choisir",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = colors.violetAccent
                                )
                            }
                        }
                    }
                }

                if (!isEdit) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.innerCardBg,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Impacter un compte Ariary",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (affectAccount) {
                                            if (direction == "BORROWED") "Crédite le compte choisi (+IN)"
                                            else "Débite le compte choisi (-OUT)"
                                        } else "Hors caisse (suivi uniquement)",
                                        fontSize = 10.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Switch(
                                    checked = affectAccount,
                                    onCheckedChange = {
                                        affectAccount = it
                                        errorMessage = null
                                    }
                                )
                            }

                            if (affectAccount) {
                                if (ariaryAccounts.isEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Aucun compte Ariary disponible. Créez un compte ou désactivez l'option ci-dessus.",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                            TextButton(onClick = onCreateAccountClick) {
                                                Text(
                                                    text = "+ Créer un compte",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    DebtAccountSelectorChips(
                                        accounts = ariaryAccounts,
                                        selectedAccount = selectedAccount,
                                        onSelect = { selectedAccount = it }
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Conditions") },
                    singleLine = false,
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = colors.dangerAccent,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            val isBlockedNoAccount = !isEdit && affectAccount && ariaryAccounts.isEmpty()
            Button(
                enabled = !isBlockedNoAccount,
                onClick = {
                    val principal = principalText.toLongOrNull() ?: 0L
                    val interest = interestText.toDoubleOrNull() ?: 0.0
                    when {
                        title.isBlank() -> errorMessage = "Veuillez saisir un libellé."
                        principal <= 0L -> errorMessage = "Veuillez saisir un montant valide (> 0)."
                        !isEdit && affectAccount && selectedAccount.isBlank() -> errorMessage = "Veuillez choisir un compte Ariary."
                        isEdit && existing != null -> {
                            onConfirmUpdate(
                                existing.copy(
                                    title = title.trim(),
                                    counterparty = counterparty.trim(),
                                    principalAr = principal,
                                    interestPercent = interest.coerceAtLeast(0.0),
                                    dueDateMillis = if (hasDueDate) dueDateMillis else null,
                                    createdAtMillis = createdAtMillis,
                                    repaymentFrequency = frequency,
                                    notes = notes.trim()
                                )
                            )
                        }
                        else -> {
                            onConfirmCreate(
                                title.trim(),
                                direction,
                                counterparty.trim(),
                                principal,
                                interest.coerceAtLeast(0.0),
                                if (hasDueDate) dueDateMillis else null,
                                createdAtMillis,
                                frequency,
                                if (affectAccount) selectedAccount else "Hors caisse",
                                affectAccount,
                                notes.trim()
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.violetAccent, contentColor = Color.White),
                modifier = Modifier.testTag("btn_confirm_save_debt")
            ) {
                Text(
                    text = if (isEdit) "Mettre à jour" else "Enregistrer",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", fontFamily = PlusJakartaSans)
            }
        }
    )
}
