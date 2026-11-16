package com.example.ui.business.finances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.data.local.entity.BusinessDebtPaymentEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordBusinessDebtPaymentDialog(
    debt: BusinessDebtEntity,
    ariaryAccounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        amountAr: Long,
        accountName: String,
        affectAccount: Boolean,
        dateMillis: Long,
        note: String
    ) -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }
    val remaining = debt.remainingAr()
    val isBorrowed = debt.direction == "BORROWED"

    var amountText by remember { mutableStateOf(if (remaining > 0L) remaining.toString() else "") }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var affectAccount by remember { mutableStateOf(ariaryAccounts.isNotEmpty()) }
    var selectedAccount by remember(ariaryAccounts) {
        mutableStateOf(
            ariaryAccounts.firstOrNull { it.name.equals(debt.accountName, ignoreCase = true) }?.name
                ?: ariaryAccounts.firstOrNull()?.name
                ?: ""
        )
    }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = debtLocalMillisToUtcPickerMillis(dateMillis))
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { dateMillis = debtUtcPickerMillisToLocalNoon(it) }
                        showDatePicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) { DatePicker(state = state) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Text(
                text = if (isBorrowed) "Rembourser : ${debt.title}" else "Encaisser : ${debt.title}",
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
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.innerCardBg
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Reste à régler", fontSize = 12.sp, color = colors.textSecondary)
                        Text(
                            text = CurrencyFormatter.formatAriary(remaining),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.violetAccent
                        )
                    }
                }

                if (remaining > 0L) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0.25 to "25%",
                            0.50 to "50%",
                            1.0 to "Solder (100%)"
                        ).forEach { (ratio, label) ->
                            val quickVal = (remaining.toDouble() * ratio).roundToLong().coerceAtLeast(1L)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        amountText = quickVal.toString()
                                        errorMessage = null
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = colors.violetSoftBg
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = colors.violetAccent,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        amountText = input.filter { it.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Montant du règlement (Ar) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_debt_payment_amount")
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = colors.innerCardBg,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Date : ${dateFmt.format(Date(dateMillis))}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
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
                                        if (isBorrowed) "Débite le compte choisi (-OUT)"
                                        else "Crédite le compte choisi (+IN)"
                                    } else "Hors caisse",
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
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Aucun compte Ariary disponible. Créez un compte ou désactivez l'impact sur compte.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        TextButton(onClick = onCreateAccountClick) {
                                            Text("+ Créer un compte", fontWeight = FontWeight.Bold)
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

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optionnel)") },
                    singleLine = true,
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
            val blocked = affectAccount && ariaryAccounts.isEmpty()
            Button(
                enabled = !blocked,
                onClick = {
                    val amt = amountText.toLongOrNull() ?: 0L
                    when {
                        amt <= 0L -> errorMessage = "Veuillez saisir un montant valide (> 0)."
                        remaining > 0L && amt > remaining -> errorMessage = "Le montant dépasse le reste dû (${CurrencyFormatter.formatAriary(remaining)})."
                        affectAccount && selectedAccount.isBlank() -> errorMessage = "Veuillez choisir un compte."
                        else -> onConfirm(
                            amt,
                            if (affectAccount) selectedAccount else "Hors caisse",
                            affectAccount,
                            dateMillis,
                            note.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBorrowed) colors.violetAccent else colors.successAccent,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("btn_confirm_debt_payment")
            ) {
                Text("Valider", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
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
internal fun BusinessDebtDetailDialog(
    debt: BusinessDebtEntity,
    payments: List<BusinessDebtPaymentEntity>,
    onDismiss: () -> Unit,
    onPayClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }
    val totalDue = debt.totalDueWithInterestAr()
    val remaining = debt.remainingAr()
    val isBorrowed = debt.direction == "BORROWED"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = debt.title,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (isBorrowed) "Emprunt (Je dois)" else "Prêt accordé (On me doit)",
                        fontSize = 11.sp,
                        color = if (isBorrowed) colors.dangerAccent else colors.successAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (debt.counterparty.isNotBlank()) {
                    DebtDetailRow("Tiers", debt.counterparty, colors.textSecondary, colors.textPrimary)
                }
                DebtDetailRow("Montant initial", CurrencyFormatter.formatAriary(debt.principalAr), colors.textSecondary, colors.textPrimary)
                DebtDetailRow("Intérêts (${debt.interestPercent}%)", CurrencyFormatter.formatAriary(totalDue - debt.principalAr), colors.textSecondary, colors.textPrimary)
                DebtDetailRow("Total dû", CurrencyFormatter.formatAriary(totalDue), colors.textSecondary, colors.textPrimary)
                DebtDetailRow("Déjà réglé", CurrencyFormatter.formatAriary(debt.paidAr), colors.textSecondary, colors.successAccent)
                DebtDetailRow("Reste à régler", CurrencyFormatter.formatAriary(remaining), colors.textSecondary, colors.dangerAccent)
                DebtDetailRow("Date opération", dateFmt.format(Date(debt.createdAtMillis)), colors.textSecondary, colors.textPrimary)
                debt.dueDateMillis?.let {
                    DebtDetailRow("Échéance (${debt.repaymentFrequency})", dateFmt.format(Date(it)), colors.textSecondary, colors.violetAccent)
                }
                DebtDetailRow("Compte initial", debt.accountName, colors.textSecondary, colors.textPrimary)
                if (debt.notes.isNotBlank()) {
                    DebtDetailRow("Notes", debt.notes, colors.textSecondary, colors.textPrimary)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Historique des règlements (${payments.size})",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = colors.textPrimary
                )
                if (payments.isEmpty()) {
                    Text(
                        text = "Aucun remboursement enregistré pour l'instant.",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                } else {
                    payments.forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.innerCardBg
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${dateFmt.format(Date(p.dateMillis))} • ${p.accountName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    if (p.note.isNotBlank()) {
                                        Text(
                                            text = p.note,
                                            fontSize = 10.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = CurrencyFormatter.formatAriary(p.amountAr),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = colors.successAccent
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (remaining > 0L) {
                    Button(
                        onClick = onPayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.violetAccent, contentColor = Color.White)
                    ) {
                        Text(if (isBorrowed) "Rembourser" else "Encaisser", fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(onClick = onEditClick) {
                    Text("Modifier")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDeleteClick) {
                Text("Supprimer", color = colors.dangerAccent, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DebtDetailRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = labelColor)
        Text(
            text = value,
            fontFamily = PlusJakartaSans,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
