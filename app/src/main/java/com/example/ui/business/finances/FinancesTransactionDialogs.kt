package com.example.ui.business.finances

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun AddBusinessTransactionDialog(
    accounts: List<BusinessAccountEntity>,
    cmupRate: Long = 0L,
    initialTitle: String = "",
    initialType: String = "IN",
    initialTag: String = "",
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        type: String,
        amountAr: Long,
        accountName: String,
        referenceTag: String,
        destinationAccount: String
    ) -> Unit,
    onConfirmBuyYuans: (
        amountYuan: Double,
        rateAr: Double,
        platform: String,
        sourceAccountName: String
    ) -> Unit = { _, _, _, _ -> }
) {
    val ariaryAccounts = remember(accounts) {
        accounts.filter { !it.isYuanAccount && it.isActive }
    }
    val yuanAccounts = remember(accounts) {
        accounts.filter { it.isYuanAccount && it.isActive }
    }
    var title by remember { mutableStateOf(initialTitle) }
    var selectedType by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var selectedAccount by remember(ariaryAccounts) {
        mutableStateOf(
            (ariaryAccounts.maxByOrNull { it.balanceAr } ?: ariaryAccounts.firstOrNull())?.name
                ?: if (selectedType == "YUAN") "Externe" else ""
        )
    }
    var destinationAccount by remember(ariaryAccounts) {
        mutableStateOf(ariaryAccounts.getOrNull(1)?.name ?: ariaryAccounts.firstOrNull()?.name ?: "")
    }
    var referenceTag by remember { mutableStateOf(initialTag) }

    var yuanAmountText by remember { mutableStateOf("") }
    var yuanRateText by remember { mutableStateOf(if (cmupRate > 0L) cmupRate.toString() else "") }
    var yuanPlatform by remember(yuanAccounts) {
        mutableStateOf(yuanAccounts.firstOrNull()?.name ?: "")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val types = listOf(
        "IN" to "Entrée",
        "OUT" to "Sortie",
        "TRANSFER" to "Transfert",
        "YUAN" to "Change ¥"
    )

    val parsedYuan = yuanAmountText.toDoubleOrNull() ?: 0.0
    val parsedRate = yuanRateText.toDoubleOrNull() ?: 0.0
    val computedYuanAr = (parsedYuan * parsedRate).roundToLong()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (selectedType == "YUAN") "Change Ariary ➔ Yuan (¥)" else "Nouvelle opération",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEach { (code, label) ->
                        val isSelected = selectedType == code
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedType = code
                                    errorMessage = null
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (selectedType == "YUAN") {
                    OutlinedTextField(
                        value = yuanAmountText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1) {
                                yuanAmountText = filtered
                                errorMessage = null
                            }
                        },
                        label = { Text("Montant acheté (¥)") },
                        placeholder = { Text("ex. 1000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = yuanRateText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1) {
                                yuanRateText = filtered
                                errorMessage = null
                            }
                        },
                        label = { Text("Taux de change (Ar / ¥)") },
                        placeholder = { Text("ex. 650") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Portefeuille chinois crédité (Alipay / Wechat Pay)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (yuanAccounts.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Achat bloqué : aucun compte Yuan (Alipay ou Wechat Pay) trouvé. Créez d'abord un compte Alipay ou Wechat Pay.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            yuanAccounts.forEach { acc ->
                                val isSel = yuanPlatform.equals(acc.name, ignoreCase = true)
                                val balText = if (acc.balanceYuan % 1.0 == 0.0) "¥ ${acc.balanceYuan.toInt()}"
                                else "¥ ${String.format(Locale.US, "%.2f", acc.balanceYuan)}"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) CyanPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            width = if (isSel) 1.dp else 0.dp,
                                            color = if (isSel) CyanPrimary else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            yuanPlatform = acc.name
                                            errorMessage = null
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = acc.name,
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSel) CyanPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = balText,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "Compte payeur (Ariary)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AccountChipRow(
                        accounts = ariaryAccounts,
                        selectedAccount = selectedAccount.ifBlank { "Externe" },
                        includeExternal = true,
                        onSelect = { selectedAccount = it }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total débité :",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(computedYuanAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CyanPrimary
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = null
                        },
                        label = {
                            Text(
                                if (selectedType == "TRANSFER") "Libellé (optionnel)"
                                else "Libellé (ex: Apport capital, Loyer)"
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { ch -> ch.isDigit() }
                            errorMessage = null
                        },
                        label = { Text("Montant (Ar)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = when (selectedType) {
                            "TRANSFER" -> "Compte source"
                            "OUT" -> "Compte débité"
                            else -> "Compte crédité"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (ariaryAccounts.isNotEmpty()) {
                        AccountChipRow(
                            accounts = ariaryAccounts,
                            selectedAccount = selectedAccount,
                            includeExternal = false,
                            onSelect = { selectedAccount = it }
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "Aucun compte Ariary actif. Veuillez d'abord créer un compte (Espèces, Mvola...) dans Comptes & Modes.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (selectedType == "TRANSFER" && ariaryAccounts.size >= 2) {
                        Text(
                            text = "Compte destination",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AccountChipRow(
                            accounts = ariaryAccounts,
                            selectedAccount = destinationAccount,
                            includeExternal = false,
                            onSelect = {
                                destinationAccount = it
                                errorMessage = null
                            }
                        )
                    }

                    OutlinedTextField(
                        value = referenceTag,
                        onValueChange = { referenceTag = it },
                        label = { Text("Référence / Catégorie (optionnel)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            val canConfirm = if (selectedType == "YUAN") {
                yuanAccounts.isNotEmpty()
            } else {
                ariaryAccounts.isNotEmpty()
            }
            Button(
                enabled = canConfirm,
                onClick = {
                    if (selectedType == "YUAN") {
                        when {
                            yuanAccounts.isEmpty() -> errorMessage = "Veuillez d'abord créer un compte Alipay ou Wechat Pay."
                            yuanPlatform.isBlank() -> errorMessage = "Sélectionnez Alipay ou Wechat Pay."
                            parsedYuan <= 0.0 -> errorMessage = "Veuillez saisir un montant en yuans valide."
                            parsedRate <= 0.0 -> errorMessage = "Veuillez saisir un taux de change valide."
                            else -> onConfirmBuyYuans(
                                parsedYuan,
                                parsedRate,
                                yuanPlatform,
                                selectedAccount.ifBlank { "Externe" }
                            )
                        }
                    } else {
                        val parsedAmount = amountText.toLongOrNull() ?: 0L
                        val finalTitle = if (selectedType == "TRANSFER" && title.isBlank()) {
                            "Transfert $selectedAccount ➔ $destinationAccount"
                        } else {
                            title.trim()
                        }
                        when {
                            ariaryAccounts.isEmpty() -> errorMessage = "Créez d'abord un compte Ariary."
                            finalTitle.isBlank() -> errorMessage = "Veuillez saisir un libellé."
                            parsedAmount <= 0L -> errorMessage = "Veuillez saisir un montant valide."
                            selectedAccount.isBlank() -> errorMessage = "Veuillez sélectionner un compte."
                            selectedType == "TRANSFER" && ariaryAccounts.size < 2 ->
                                errorMessage = "Ajoutez au moins 2 comptes Ariary pour faire un transfert."
                            selectedType == "TRANSFER" && selectedAccount.equals(destinationAccount, ignoreCase = true) ->
                                errorMessage = "Choisissez deux comptes différents pour le transfert."
                            else -> onConfirm(
                                finalTitle,
                                selectedType,
                                parsedAmount,
                                selectedAccount.trim(),
                                referenceTag.trim(),
                                if (selectedType == "TRANSFER") destinationAccount.trim() else ""
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text(
                    text = if (selectedType == "YUAN") "Acheter des ¥" else "Enregistrer",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun AccountChipRow(
    accounts: List<BusinessAccountEntity>,
    selectedAccount: String,
    includeExternal: Boolean,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        accounts.forEach { acc ->
            val isSelected = selectedAccount.equals(acc.name, ignoreCase = true)
            Surface(
                modifier = Modifier.clickable { onSelect(acc.name) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
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
        if (includeExternal) {
            val isExternal = selectedAccount == "Externe"
            Surface(
                modifier = Modifier.clickable { onSelect("Externe") },
                shape = RoundedCornerShape(10.dp),
                color = if (isExternal) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(
                        text = "Externe",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isExternal) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Hors comptes",
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = if (isExternal) Color.Black.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
