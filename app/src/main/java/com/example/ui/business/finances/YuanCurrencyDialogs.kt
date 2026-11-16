package com.example.ui.business.finances

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
fun BuyYuansDialog(
    accounts: List<BusinessAccountEntity> = emptyList(),
    defaultRate: Long = 650L,
    onDismiss: () -> Unit,
    onConfirm: (amountYuan: Double, rateAr: Double, platform: String, sourceAccountName: String) -> Unit,
    onCreateAccountClick: (() -> Unit)? = null
) {
    val ariaryAccounts = remember(accounts) {
        accounts.filter { !it.isYuanAccount && it.isActive }
    }
    val yuanAccounts = remember(accounts) {
        accounts.filter { it.isYuanAccount && it.isActive }
    }
    var amountText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf(if (defaultRate > 0L) defaultRate.toString() else "650") }
    var selectedPlatform by remember(yuanAccounts) {
        mutableStateOf(yuanAccounts.firstOrNull()?.name ?: "")
    }
    var selectedAccount by remember(ariaryAccounts) {
        mutableStateOf(
            (ariaryAccounts.maxByOrNull { it.balanceAr } ?: ariaryAccounts.firstOrNull())?.name
                ?: "Externe"
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amountYuan = amountText.toDoubleOrNull() ?: 0.0
    val rateAr = rateText.toDoubleOrNull() ?: 0.0
    val totalAr = kotlin.math.round(amountYuan * rateAr).toLong()
    val selectedAriaryBalance = ariaryAccounts.firstOrNull {
        it.name.equals(selectedAccount, ignoreCase = true)
    }?.balanceAr ?: 0L
    val isAriaryInsufficient = selectedAccount != "Externe" && totalAr > 0L && totalAr > selectedAriaryBalance

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Acheter des yuans (¥)",
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
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            amountText = filtered
                            errorMessage = null
                        }
                    },
                    label = { Text("Montant en Yuans (¥)") },
                    placeholder = { Text("ex. 1000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_yuan_amount")
                )

                OutlinedTextField(
                    value = rateText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            rateText = filtered
                            errorMessage = null
                        }
                    },
                    label = { Text("Taux de change (Ar / ¥)") },
                    supportingText = if (defaultRate <= 0L) {
                        {
                            Text(
                                text = "Taux indicatif modifiable (aucun CMUP enregistré)",
                                fontFamily = Inter,
                                fontSize = 11.sp
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_yuan_rate")
                )

                Text(
                    text = "Portefeuille chinois crédité (Alipay / Wechat Pay)",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (yuanAccounts.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
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
                                    text = "Achat bloqué : aucun compte Yuan (Alipay ou Wechat Pay) disponible. Veuillez d'abord créer un compte Alipay ou Wechat Pay.",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            if (onCreateAccountClick != null) {
                                TextButton(
                                    onClick = {
                                        onDismiss()
                                        onCreateAccountClick()
                                    }
                                ) {
                                    Text(
                                        text = "+ Créer un compte Alipay / Wechat Pay",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
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
                            val isSelected = selectedPlatform.equals(acc.name, ignoreCase = true)
                            val yBal = if (acc.balanceYuan % 1.0 == 0.0) "¥ ${acc.balanceYuan.toInt()}"
                            else "¥ ${String.format(Locale.US, "%.2f", acc.balanceYuan)}"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) CyanPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) CyanPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedPlatform = acc.name
                                        errorMessage = null
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = acc.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = yBal,
                                        fontFamily = Inter,
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
                    ariaryAccounts.forEach { acc ->
                        val isSelected = selectedAccount == acc.name
                        Surface(
                            modifier = Modifier.clickable { selectedAccount = acc.name },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
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
                    val isExternal = selectedAccount == "Externe"
                    Surface(
                        modifier = Modifier.clickable { selectedAccount = "Externe" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isExternal) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
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

                // Summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Coût total en Ariary :",
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(totalAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = if (selectedAccount == "Externe") {
                                "Enregistré dans Finances sans débit de compte local."
                            } else {
                                "Débitera « $selectedAccount » et enregistrera la transaction Change ¥."
                            },
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isAriaryInsufficient) {
                            Text(
                                text = "Attention : solde insuffisant sur $selectedAccount (${CurrencyFormatter.formatAriary(selectedAriaryBalance)} dispo).",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
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
            Button(
                enabled = yuanAccounts.isNotEmpty(),
                onClick = {
                    when {
                        yuanAccounts.isEmpty() -> errorMessage = "Créez d'abord un compte Alipay ou Wechat Pay."
                        selectedPlatform.isBlank() -> errorMessage = "Veuillez sélectionner un compte Yuan."
                        amountYuan <= 0.0 -> errorMessage = "Veuillez saisir un montant en yuans valide."
                        rateAr <= 0.0 -> errorMessage = "Veuillez saisir un taux de change valide."
                        else -> {
                            onConfirm(amountYuan, rateAr, selectedPlatform, selectedAccount)
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text(
                    text = "Valider l'achat",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
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
fun PayPlatformDialog(
    cmupRate: Long,
    availableYuans: Double,
    accounts: List<BusinessAccountEntity> = emptyList(),
    platforms: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (amountYuan: Double, rateAr: Double, yuanAccountName: String, note: String) -> Unit
) {
    val context = LocalContext.current
    val yuanAccounts = remember(accounts) { accounts.filter { it.isYuanAccount && it.isActive } }
    var selectedYuanAccountName by remember(yuanAccounts) {
        mutableStateOf(
            (yuanAccounts.maxByOrNull { it.balanceYuan } ?: yuanAccounts.firstOrNull())?.name ?: ""
        )
    }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    val availablePlatforms = remember(platforms) {
        val cleaned = platforms.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (cleaned.isNotEmpty()) cleaned else listOf("1688", "Taobao", "Pinduoduo")
    }
    var selectedPlatform by remember(availablePlatforms) { mutableStateOf(availablePlatforms.first()) }

    val selectedAccount = yuanAccounts.firstOrNull { it.name.equals(selectedYuanAccountName, ignoreCase = true) }
    val selectedBalanceYuan = if (yuanAccounts.sumOf { it.balanceYuan } > 0.0) {
        selectedAccount?.balanceYuan ?: 0.0
    } else {
        availableYuans
    }
    val amountYuan = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val totalAr = kotlin.math.round(amountYuan * cmupRate).toLong()
    val isInsufficient = amountYuan > 0.0 && amountYuan > selectedBalanceYuan + 0.001

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Payer plateforme / Fournisseur",
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
                if (yuanAccounts.isNotEmpty()) {
                    Text(
                        text = "Portefeuille Yuan à débiter :",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        yuanAccounts.forEach { acc ->
                            val isSelected = acc.name.equals(selectedYuanAccountName, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) CyanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedYuanAccountName = acc.name }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = acc.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "¥ ${String.format(Locale.US, "%.2f", acc.balanceYuan)}",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Aucun compte Yuan actif configuré.",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    text = "Solde disponible (${selectedYuanAccountName.ifBlank { "Yuans" }}) : ¥ ${String.format(Locale.US, "%.2f", selectedBalanceYuan)} (CMUP $cmupRate Ar)",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = if (isInsufficient) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
                        if (filtered.count { it == '.' || it == ',' } <= 1) amountText = filtered
                    },
                    label = { Text("Montant débité (¥)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isInsufficient,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_pay_amount")
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Motif / Produit") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_pay_note")
                )

                Text(
                    text = "Plateforme bénéficiaire :",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availablePlatforms.forEach { platform ->
                        val isSelected = selectedPlatform == platform
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedPlatform = platform }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = platform,
                                fontFamily = PlusJakartaSans,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Valeur débitée :",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(totalAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (yuanAccounts.isEmpty()) {
                        Toast.makeText(context, "Créez d'abord un compte Yuan (Alipay ou Wechat Pay)", Toast.LENGTH_SHORT).show()
                    } else if (amountYuan <= 0.0) {
                        Toast.makeText(context, "Saisissez un montant valide en Yuans", Toast.LENGTH_SHORT).show()
                    } else if (isInsufficient) {
                        Toast.makeText(context, "Solde insuffisant sur $selectedYuanAccountName", Toast.LENGTH_SHORT).show()
                    } else {
                        val finalNote = if (noteText.isBlank()) "Paiement $selectedPlatform" else "$selectedPlatform • ${noteText.trim()}"
                        onConfirm(amountYuan, cmupRate.toDouble(), selectedYuanAccountName, finalNote)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Enregistrer le paiement", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", fontFamily = PlusJakartaSans)
            }
        }
    )
}
