package com.example.ui.business.finances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

@Composable
internal fun AddCapitalMovementDialog(
    isContribution: Boolean,
    ariaryAccounts: List<BusinessAccountEntity>,
    onDismiss: () -> Unit,
    onCreateAccountClick: () -> Unit = {},
    onConfirm: (
        title: String,
        amountAr: Long,
        accountName: String,
        note: String,
        affectAccount: Boolean,
        dateMillis: Long
    ) -> Unit
) {
    val colors = rememberTransitaireColors()
    var title by remember {
        mutableStateOf(if (isContribution) "Apport de capital" else "Retrait personnel")
    }
    var amountText by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var affectAccount by remember(ariaryAccounts, isContribution) {
        mutableStateOf(if (isContribution) true else ariaryAccounts.isNotEmpty())
    }
    var selectedAccount by remember(ariaryAccounts) {
        mutableStateOf(ariaryAccounts.firstOrNull()?.name ?: "")
    }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val parsedAmount = amountText.toLongOrNull() ?: 0L
    val currentAccountBalance = ariaryAccounts.firstOrNull {
        it.name.equals(selectedAccount, ignoreCase = true)
    }?.balanceAr ?: 0L
    val isContributionBlocked = isContribution && ariaryAccounts.isEmpty()
    val willOverdraw = affectAccount && !isContribution && parsedAmount > currentAccountBalance && ariaryAccounts.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Text(
                text = if (isContribution) "Apport en capital" else "Retrait personnel",
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
                if (isContributionBlocked) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.dangerSoftBg,
                        border = BorderStroke(1.dp, colors.dangerAccent.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Action bloquée : aucun compte disponible.",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.dangerAccent
                            )
                            Text(
                                text = "Tout apport doit être enregistré dans un compte (ex. Espèces, Mvola). Créez d'abord un compte dans Comptes & Modes.",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                            Button(
                                onClick = onCreateAccountClick,
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Créer un compte", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = null
                        },
                        label = { Text("Libellé *") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { ch -> ch.isDigit() }
                            errorMessage = null
                        },
                        label = { Text("Montant (Ar) *") },
                        placeholder = { Text("ex. 500000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    CapitalDateField(
                        label = if (isContribution) "Date de l'apport" else "Date du retrait",
                        dateMillis = selectedDateMillis,
                        colors = colors,
                        onDateSelected = { selectedDateMillis = it }
                    )

                    if (!isContribution) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (ariaryAccounts.isNotEmpty()) {
                                        affectAccount = !affectAccount
                                        errorMessage = null
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = colors.innerCardBg,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Impacter un compte & Transactions",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (affectAccount) {
                                            "Débite le compte et ajoute une sortie dans Transactions"
                                        } else {
                                            "Hors caisse : suivi du capital uniquement (sans transaction)"
                                        },
                                        fontFamily = Inter,
                                        fontSize = 10.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Switch(
                                    checked = affectAccount,
                                    onCheckedChange = {
                                        if (ariaryAccounts.isNotEmpty() || !it) {
                                            affectAccount = it
                                            errorMessage = null
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (isContribution || affectAccount) {
                        Text(
                            text = if (isContribution) "Compte crédité (obligatoire) *" else "Compte Ariary débité",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ariaryAccounts.forEach { acc ->
                                val isSel = selectedAccount.equals(acc.name, ignoreCase = true)
                                Surface(
                                    modifier = Modifier.clickable {
                                        selectedAccount = acc.name
                                        errorMessage = null
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) CyanPrimary else colors.innerCardBg,
                                    border = if (isSel) null else BorderStroke(1.dp, colors.border)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                        Text(
                                            text = acc.name,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
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

                        if (willOverdraw) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.warningSoftBg
                            ) {
                                Text(
                                    text = "Solde actuel sur $selectedAccount : ${CurrencyFormatter.formatAriary(currentAccountBalance)}. Le solde deviendra négatif après ce retrait.",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.warningAccent,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note / Précision (optionnel)") },
                        placeholder = { Text("ex. Épargne perso, Bénéfice Q1") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
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
            }
        },
        confirmButton = {
            Button(
                enabled = !isContributionBlocked,
                onClick = {
                    when {
                        isContributionBlocked -> errorMessage = "Créez d'abord un compte."
                        title.isBlank() -> errorMessage = "Veuillez saisir un libellé."
                        parsedAmount <= 0L -> errorMessage = "Veuillez saisir un montant supérieur à 0 Ar."
                        (isContribution || affectAccount) && selectedAccount.isBlank() ->
                            errorMessage = "Veuillez choisir un compte."
                        else -> onConfirm(
                            title.trim(),
                            parsedAmount,
                            if (isContribution || affectAccount) selectedAccount.trim() else "Hors caisse",
                            note.trim(),
                            isContribution || affectAccount,
                            selectedDateMillis
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isContribution) CyanPrimary else colors.dangerAccent,
                    contentColor = if (isContribution) Color.Black else Color.White
                )
            ) {
                Text(
                    text = if (isContribution) "Enregistrer l'apport" else "Enregistrer le retrait",
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

@Composable
internal fun InvestmentDetailDialog(
    investment: BusinessInvestmentEntity,
    dateFormatter: SimpleDateFormat,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val cat = categoryInfo(investment.category)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val netValueDelta = (investment.estimatedValueAr + investment.generatedReturnAr) - investment.amountAr
    val individualRoi = if (investment.amountAr > 0L) {
        ((netValueDelta.toDouble() / investment.amountAr.toDouble()) * 100.0).roundToInt()
    } else 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(cat.emoji, fontSize = 24.sp)
                Column {
                    Text(
                        text = investment.title,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${cat.label} • ${dateFormatter.format(Date(investment.dateMillis))}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CapitalKeyValueRow("Montant investi", CurrencyFormatter.formatAriary(investment.amountAr), colors.textSecondary, colors.textPrimary)
                        CapitalKeyValueRow("Valeur actuelle", CurrencyFormatter.formatAriary(investment.estimatedValueAr), colors.textSecondary, colors.cyanAccent)
                        CapitalKeyValueRow("Retours générés", CurrencyFormatter.formatAriary(investment.generatedReturnAr), colors.textSecondary, colors.successAccent)
                        CapitalKeyValueRow("Rendement net", "${if (individualRoi >= 0) "+" else ""}$individualRoi %", colors.textSecondary, if (individualRoi >= 0) colors.successAccent else colors.dangerAccent)
                        CapitalKeyValueRow("Financement", investment.accountName, colors.textSecondary, colors.textPrimary)
                    }
                }

                Text(
                    text = "Statut rapide",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ACTIF" to "Actif", "AMORTI" to "Amorti", "CEDE" to "Cédé").forEach { (code, label) ->
                        val isSel = investment.status == code
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onStatusChange(code) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) CyanPrimary else colors.innerCardBg
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.Black else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                if (investment.notes.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.innerCardBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = investment.notes,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Modifier", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.DeleteOutline, null, tint = colors.dangerAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Supprimer", fontFamily = PlusJakartaSans, color = colors.dangerAccent, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss) {
                    Text("Fermer", fontFamily = PlusJakartaSans)
                }
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = colors.cardBg,
            title = {
                Text(
                    text = "Supprimer cet investissement ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = colors.dangerAccent
                )
            },
            text = {
                val txNote = if (investment.linkedTransactionId != null) {
                    " Le débit associé sur le compte « ${investment.accountName} » sera également annulé."
                } else ""
                Text(
                    text = "Voulez-vous supprimer « ${investment.title} » ?$txNote",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent, contentColor = Color.White)
                ) {
                    Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }
}
