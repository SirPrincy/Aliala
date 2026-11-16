package com.example.ui.business.finances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddOrEditBusinessAccountDialog(
    existing: BusinessAccountEntity? = null,
    existingAccounts: List<BusinessAccountEntity> = emptyList(),
    defaultIsYuan: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmCreate: (
        name: String,
        subtitle: String,
        initialBalanceAr: Long,
        isYuanAccount: Boolean,
        initialBalanceYuan: Double
    ) -> Unit,
    onConfirmUpdate: (
        existing: BusinessAccountEntity,
        newName: String,
        newSubtitle: String,
        newBalanceAr: Long,
        newBalanceYuan: Double
    ) -> Unit = { _, _, _, _, _ -> }
) {
    val colors = rememberTransitaireColors()
    val isEdit = existing != null
    var isYuanAccount by remember { mutableStateOf(existing?.isYuanAccount ?: defaultIsYuan) }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var subtitle by remember { mutableStateOf(existing?.subtitle ?: "") }
    var balanceText by remember {
        mutableStateOf(
            when {
                existing == null -> ""
                existing.isYuanAccount -> if (existing.balanceYuan == 0.0) "0"
                else if (existing.balanceYuan % 1.0 == 0.0) existing.balanceYuan.toInt().toString()
                else existing.balanceYuan.toString()
                else -> existing.balanceAr.toString()
            }
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presets = listOf(
        Triple("Espèces", "Caisse", false),
        Triple("Mvola", "Mobile Money", false),
        Triple("Alipay", "Chine (¥)", true),
        Triple("Wechat Pay", "Chine (¥)", true)
    ).filter { (presetName, _, _) ->
        existingAccounts.none { it.name.equals(presetName, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Text(
                text = if (isEdit) "Modifier le compte" else "Nouveau compte financier",
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
                if (!isEdit && presets.isNotEmpty()) {
                    Text(
                        text = "Comptes pré-faits rapides",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { (presetName, presetSub, presetIsYuan) ->
                            val isSel = name.equals(presetName, ignoreCase = true)
                            Surface(
                                modifier = Modifier.clickable {
                                    name = presetName
                                    subtitle = presetSub
                                    isYuanAccount = presetIsYuan
                                    errorMessage = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) CyanPrimary else colors.innerCardBg,
                                border = if (isSel) null else BorderStroke(1.dp, colors.border)
                            ) {
                                Text(
                                    text = if (presetIsYuan) "$presetName (¥)" else presetName,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.Black else colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (!isEdit) {
                    Text(
                        text = "Devise du compte",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(false to "Ariary (Ar)", true to "Yuan (¥)").forEach { (yuanFlag, label) ->
                            val isSel = isYuanAccount == yuanFlag
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        isYuanAccount = yuanFlag
                                        balanceText = ""
                                        errorMessage = null
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) CyanPrimary else colors.innerCardBg,
                                border = if (isSel) null else BorderStroke(1.dp, colors.border)
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSel) Color.Black else colors.textPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nom du compte *") },
                    placeholder = { Text(if (isYuanAccount) "ex. Alipay, Wechat Pay" else "ex. Espèces, Mvola") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Type / Libellé court") },
                    placeholder = { Text(if (isYuanAccount) "ex. Chine (¥)" else "ex. Caisse, Mobile Money") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { input ->
                        balanceText = if (isYuanAccount) {
                            val filtered = input.filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1) filtered else balanceText
                        } else {
                            input.filter { it.isDigit() || it == '-' }
                        }
                        errorMessage = null
                    },
                    label = {
                        Text(
                            when {
                                isEdit && isYuanAccount -> "Solde actuel (¥)"
                                isEdit -> "Solde actuel (Ar)"
                                isYuanAccount -> "Solde initial (¥)"
                                else -> "Solde initial (Ar)"
                            }
                        )
                    },
                    placeholder = { Text("0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isYuanAccount) KeyboardType.Decimal else KeyboardType.Number
                    ),
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
            Button(
                onClick = {
                    val cleanName = name.trim()
                    val hasDuplicate = existingAccounts.any {
                        it.id != (existing?.id ?: -1L) && it.name.equals(cleanName, ignoreCase = true)
                    }
                    when {
                        cleanName.isBlank() -> errorMessage = "Veuillez saisir le nom du compte."
                        hasDuplicate -> errorMessage = "Un compte nommé « $cleanName » existe déjà."
                        isEdit && existing != null -> {
                            onConfirmUpdate(
                                existing,
                                cleanName,
                                subtitle.trim().ifBlank { if (isYuanAccount) "Chine (¥)" else "Compte" },
                                if (isYuanAccount) 0L else (balanceText.toLongOrNull() ?: existing.balanceAr),
                                if (isYuanAccount) (balanceText.toDoubleOrNull() ?: existing.balanceYuan) else 0.0
                            )
                        }
                        else -> {
                            onConfirmCreate(
                                cleanName,
                                subtitle.trim().ifBlank { if (isYuanAccount) "Chine (¥)" else "Compte" },
                                if (isYuanAccount) 0L else (balanceText.toLongOrNull() ?: 0L),
                                isYuanAccount,
                                if (isYuanAccount) (balanceText.toDoubleOrNull() ?: 0.0) else 0.0
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
            ) {
                Text(
                    text = if (isEdit) "Mettre à jour" else "Ajouter",
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
fun DeleteBusinessAccountConfirmDialog(
    account: BusinessAccountEntity,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val balStr = if (account.isYuanAccount) {
        if (account.balanceYuan % 1.0 == 0.0) "¥ ${account.balanceYuan.toInt()}"
        else "¥ ${String.format(Locale.US, "%.2f", account.balanceYuan)}"
    } else {
        CurrencyFormatter.formatAriary(account.balanceAr)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Text(
                text = "Supprimer le compte « ${account.name} » ?",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                color = colors.dangerAccent
            )
        },
        text = {
            Text(
                text = "Solde actuel : $balStr. La suppression retirera ce compte des modes de règlement disponibles (les anciennes transactions restent dans l'historique).",
                fontSize = 13.sp,
                color = colors.textPrimary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent, contentColor = Color.White)
            ) {
                Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
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
fun TransactionDetailDialog(
    tx: BusinessTransactionEntity,
    onDismiss: () -> Unit,
    onDelete: (BusinessTransactionEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    val dateStr = remember(tx.dateMillis) {
        SimpleDateFormat("dd/MM/yyyy 'à' HH:mm", Locale.FRANCE).format(Date(tx.dateMillis))
    }
    val typeLabel = when (tx.type) {
        "IN", "INCOME" -> "Entrée de fonds"
        "OUT", "EXPENSE" -> "Sortie de fonds"
        "TRANSFER" -> "Transfert interne"
        "YUAN" -> "Achat de Yuans (Change ¥)"
        else -> "Opération"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Text(
                text = tx.title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Type", typeLabel, colors.textSecondary, colors.textPrimary)
                DetailRow("Montant", CurrencyFormatter.formatAriary(tx.amountAr), colors.textSecondary, colors.cyanAccent)
                DetailRow(
                    label = if (tx.type == "TRANSFER") "Source ➔ Cible" else "Compte",
                    value = if (tx.type == "TRANSFER" && tx.destinationAccount.isNotBlank()) {
                        "${tx.accountName} ➔ ${tx.destinationAccount}"
                    } else {
                        tx.accountName.ifBlank { "Non spécifié" }
                    },
                    labelColor = colors.textSecondary,
                    valueColor = colors.textPrimary
                )
                DetailRow("Date", dateStr, colors.textSecondary, colors.textPrimary)
                if (tx.referenceTag.isNotBlank()) {
                    DetailRow("Référence", tx.referenceTag, colors.textSecondary, colors.textPrimary)
                }
                if (tx.yuanTransactionId != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.warningSoftBg
                    ) {
                        Text(
                            text = "Liée au portefeuille Yuans : la suppression annulera également le crédit de yuans correspondant et ajustera le CMUP.",
                            fontSize = 11.sp,
                            color = colors.warningAccent,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDelete(tx)
                    onDismiss()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = colors.dangerAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Supprimer",
                    color = colors.dangerAccent,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun DetailRow(
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
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
