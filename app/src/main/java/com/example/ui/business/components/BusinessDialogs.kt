package com.example.ui.business.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.CurrencyYuan
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Warning
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun BuyYuansDialog(
    accounts: List<com.example.data.local.entity.BusinessAccountEntity> = emptyList(),
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
                            else "¥ ${String.format(java.util.Locale.US, "%.2f", acc.balanceYuan)}"
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
    onDismiss: () -> Unit,
    onConfirm: (amountYuan: Double, rateAr: Double, platform: String, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf("1688") }

    val amountYuan = amountText.toDoubleOrNull() ?: 0.0
    val totalAr = (amountYuan * cmupRate).toLong()

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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Solde disponible : ¥ ${availableYuans.toInt()} (CMUP ${cmupRate} Ar)",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Montant débité (¥)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_pay_amount")
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Motif / Produit") },
                    modifier = Modifier.fillMaxWidth().testTag("input_pay_note")
                )

                // Platform selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1688", "Taobao", "Alipay").forEach { platform ->
                        val isSelected = selectedPlatform == platform
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedPlatform = platform }
                                .padding(vertical = 10.dp),
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
                    if (amountYuan > 0) {
                        onConfirm(amountYuan, cmupRate.toDouble(), selectedPlatform, noteText)
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



@Composable
fun WhatsAppRelanceDialog(
    clientName: String? = null,
    clientPhone: String? = null,
    balanceDueAr: Long = 0L,
    customMessage: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val message = customMessage ?: if (!clientName.isNullOrBlank() && balanceDueAr > 0L) {
        "Bonjour $clientName ! J'espère que tu vas bien. Petit rappel amical concernant ton solde de ${CurrencyFormatter.formatAriary(balanceDueAr)}. Merci d'avance pour le règlement !"
    } else if (!clientName.isNullOrBlank()) {
        "Bonjour $clientName ! J'espère que tu vas bien. Petit message amical pour le règlement de ta commande."
    } else {
        "Bonjour ! J'espère que tu vas bien. Petit message amical pour le règlement de ta commande chez nous. N'hésite pas à m'envoyer la confirmation dès que c'est fait. Merci !"
    }

    val openWhatsApp = {
        val rawDigits = (clientPhone ?: "").replace(Regex("[^0-9]"), "")
        val normalizedPhone = when {
            rawDigits.startsWith("261") -> rawDigits
            rawDigits.startsWith("0") && rawDigits.length >= 10 -> "261" + rawDigits.substring(1)
            rawDigits.isNotBlank() -> "261$rawDigits"
            else -> ""
        }
        val uri = if (normalizedPhone.isNotBlank()) {
            android.net.Uri.parse("https://wa.me/$normalizedPhone?text=${android.net.Uri.encode(message)}")
        } else {
            android.net.Uri.parse("https://api.whatsapp.com/send?text=${android.net.Uri.encode(message)}")
        }
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Relance WhatsApp", message)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Message copié dans le presse-papier", Toast.LENGTH_SHORT).show()
        }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (!clientName.isNullOrBlank()) "Relance WhatsApp : $clientName" else "Relance WhatsApp",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Message prêt à envoyer :",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = message,
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = openWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ouvrir WhatsApp", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Relance WhatsApp", message)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Message copié dans le presse-papier", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text("Copier", fontFamily = PlusJakartaSans)
            }
        }
    )
}

@Composable
fun AdjustStockDialog(
    order: com.example.data.local.entity.BusinessOrderEntity,
    onDismiss: () -> Unit,
    onConfirm: (stockCount: Int, reservedCount: Int, defectiveCount: Int, assignedClient: String) -> Unit
) {
    var stockCountText by remember { mutableStateOf(order.stockCount.toString()) }
    var reservedCountText by remember { mutableStateOf(order.reservedCount.toString()) }
    var defectiveCountText by remember { mutableStateOf(order.defectiveCount.toString()) }
    var assignedClientText by remember { mutableStateOf(order.assignedClient) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Ajuster le stock",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = order.product + if (order.referenceCode.isNotBlank()) " (${order.referenceCode})" else "",
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
                // Pièces disponibles en rayon
                OutlinedTextField(
                    value = stockCountText,
                    onValueChange = { stockCountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Quantité en rayon (pcs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Pièces réservées
                OutlinedTextField(
                    value = reservedCountText,
                    onValueChange = { reservedCountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Pièces réservées client") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Client assigné
                OutlinedTextField(
                    value = assignedClientText,
                    onValueChange = { assignedClientText = it },
                    label = { Text("Client assigné (ex. Rina)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Pièces défectueuses
                OutlinedTextField(
                    value = defectiveCountText,
                    onValueChange = { defectiveCountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Défectueux / SAV (pcs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sCount = stockCountText.toIntOrNull() ?: order.stockCount
                    val rCount = reservedCountText.toIntOrNull() ?: order.reservedCount
                    val dCount = defectiveCountText.toIntOrNull() ?: order.defectiveCount
                    onConfirm(sCount, rCount, dCount, assignedClientText.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Enregistrer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
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
fun SelectOrderToAdjustDialog(
    orders: List<com.example.data.local.entity.BusinessOrderEntity>,
    onDismiss: () -> Unit,
    onOrderSelected: (com.example.data.local.entity.BusinessOrderEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choisir l'article à ajuster",
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (orders.isEmpty()) {
                    Text(
                        text = "Aucun article disponible en stock",
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    orders.forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    onOrderSelected(order)
                                    onDismiss()
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = order.product,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "SKU: ${order.referenceCode.ifBlank { "N/A" }} • ${order.family}",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${order.stockCount} pcs",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = CyanPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", fontFamily = PlusJakartaSans)
            }
        }
    )
}

@Composable
fun RecordSalePaymentDialog(
    sale: com.example.data.local.entity.BusinessSaleEntity,
    onDismiss: () -> Unit,
    onConfirmPayment: (additionalAmountAr: Long) -> Unit,
    onDeleteSale: () -> Unit
) {
    val remaining = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
    var paymentText by remember { mutableStateOf(remaining.toString()) }
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
                // Info résumé
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

                // Saisie montant
                OutlinedTextField(
                    value = paymentText,
                    onValueChange = { paymentText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Montant encaissé (Ar)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Raccourci tout solder
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
                        onConfirmPayment(amount)
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
                    onClick = {
                        showDeleteConfirm = true
                    }
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
    availableStockOrders: List<com.example.data.local.entity.BusinessOrderEntity>,
    initialArticle: com.example.data.local.entity.BusinessOrderEntity? = null,
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
        linkedOrderId: Long?
    ) -> Unit
) {
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
                // Domaine
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

                // Type de vente (Stock / Commande)
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

                // Article Picker (Starts with + Nouvel article)
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
                    // + Nouvel article
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

                // Nom client
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nom du client (ex. Rina)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_sale_client"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Téléphone
                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("Téléphone (ex. 034 11 222 33)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("input_sale_phone"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Produit
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Article vendu") },
                    modifier = Modifier.fillMaxWidth().testTag("input_sale_product"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Quantité & Prix unitaire
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

                // Coût unitaire & Acompte
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
                            if (selectedStockOrderId != null && selectedStockOrderId!! > 0L) selectedStockOrderId else null
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



// ---------------------------------------------------------------------------
// EDIT CATALOGUE ARTICLE DIALOG (REFONTE COMPLÈTE)
// ---------------------------------------------------------------------------
@Composable
fun EditCatalogueArticleDialog(
    article: com.example.ui.business.CatalogueArticleSummaryUi,
    availableFamilies: List<com.example.ui.business.CatalogueFamilySummaryUi> = emptyList(),
    availablePlatforms: List<String> = listOf("1688.com", "Taobao", "Pinduoduo", "Fournisseur Direct WeChat"),
    transitaires: List<com.example.ui.business.CatalogueTransitaireSummaryUi> = emptyList(),
    cmupRate: Long = 650L,
    onDismiss: () -> Unit,
    onConfirm: (
        newProduct: String,
        newReferenceCode: String,
        newFamily: String,
        newPlatform: String,
        newPriceYuan: Double,
        newResaleAr: Long,
        newWeightKg: Double?,
        newFreightMode: String,
        newVariants: String,
        newImageUrl: String,
        newNotes: String,
        isHidden: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val colors = rememberTransitaireColors()
    val order = article.primaryOrder

    val familyNames = remember(availableFamilies, order.family) {
        val base = availableFamilies.map { it.name }.ifEmpty {
            listOf("Électronique & Audio", "Mode & Maroquinerie", "Maison & Rangement", "Accessoires Mobiles")
        }
        if (order.family.isNotBlank() && base.none { it.equals(order.family, ignoreCase = true) }) {
            listOf(order.family) + base
        } else base
    }
    val platformOptions = remember(availablePlatforms, order.platform) {
        val base = availablePlatforms.ifEmpty { listOf("1688.com", "Taobao", "Pinduoduo", "Fournisseur Direct WeChat") }
        if (order.platform.isNotBlank() && base.none { it.equals(order.platform, ignoreCase = true) }) {
            listOf(order.platform) + base
        } else base
    }

    var productName by remember { mutableStateOf(order.product) }
    var referenceCode by remember { mutableStateOf(order.referenceCode) }
    var selectedFamily by remember { mutableStateOf(order.family.ifBlank { familyNames.first() }) }
    var selectedPlatform by remember { mutableStateOf(order.platform.ifBlank { platformOptions.first() }) }
    var priceYuanText by remember {
        mutableStateOf(
            if (article.latestPriceYuan > 0.0) {
                if (article.latestPriceYuan % 1.0 == 0.0) article.latestPriceYuan.toInt().toString()
                else article.latestPriceYuan.toString()
            } else ""
        )
    }
    var resaleArText by remember {
        mutableStateOf(if (article.resalePriceAr > 0L) article.resalePriceAr.toString() else "")
    }
    var weightText by remember {
        mutableStateOf(
            if ((order.weightKg ?: 0.0) > 0.0) order.weightKg.toString() else ""
        )
    }
    var selectedFreightMode by remember {
        mutableStateOf(if (order.freightMode.equals("MARITIME", ignoreCase = true)) "MARITIME" else "AÉRIEN")
    }
    var variantsList by remember {
        mutableStateOf(article.allVariants.ifEmpty { VariantColorUtils.parseVariants(order.variants) })
    }
    var visualValue by remember { mutableStateOf(order.imageUrl) }
    var notes by remember { mutableStateOf(order.receptionNotes) }
    var isHidden by remember { mutableStateOf(article.isHidden) }

    var showAddVariantDialog by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            visualValue = it.toString()
            Toast.makeText(context, "Photo sélectionnée", Toast.LENGTH_SHORT).show()
        }
    }

    val effectiveCmupRate = if (cmupRate > 0L) cmupRate.toDouble() else 650.0
    val activeTransitaires = remember(transitaires) { transitaires.filter { !it.isArchived && it.isActive } }
    val airRate = remember(activeTransitaires) {
        val rates = activeTransitaires.mapNotNull { it.airRateAr.takeIf { r -> r > 0L } }
        if (rates.isNotEmpty()) rates.average().toLong() else 45000L
    }
    val seaRate = remember(activeTransitaires) {
        val rates = activeTransitaires.mapNotNull { it.seaRateAr.takeIf { r -> r > 0L } }
        if (rates.isNotEmpty()) rates.average().toLong() else 18000L
    }

    val priceYuan = priceYuanText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val resaleAr = resaleArText.toLongOrNull() ?: 0L
    val weightKg = weightText.replace(',', '.').toDoubleOrNull()
    val purchaseAr = kotlin.math.round(priceYuan * effectiveCmupRate).toLong()
    val realUnitFreight = if (order.freightCostAr > 0L && order.quantityBought > 0) order.freightCostAr / order.quantityBought else 0L
    val estUnitFreight = if (realUnitFreight > 0L) realUnitFreight else if ((weightKg ?: 0.0) > 0.0) {
        kotlin.math.round((weightKg ?: 0.0) * (if (selectedFreightMode == "MARITIME") seaRate else airRate)).toLong()
    } else 0L
    val landedCostAr = purchaseAr + estUnitFreight
    val marginAr = if (resaleAr > 0L && landedCostAr > 0L) resaleAr - landedCostAr else 0L
    val marginPct = if (resaleAr > 0L && landedCostAr > 0L) ((marginAr.toDouble() / resaleAr.toDouble()) * 100.0).toInt() else 0

    val famEmoji = availableFamilies.firstOrNull { it.name.equals(selectedFamily, ignoreCase = true) }?.emoji ?: article.familyEmoji

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.pageBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.cardBg,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Fermer", tint = colors.textPrimary)
                            }
                            Column {
                                Text(
                                    text = "Modifier l'article",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = order.referenceCode.ifBlank { "REF-${order.id}" },
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (productName.isBlank()) {
                                    errorMessage = "Le nom de l'article est obligatoire."
                                } else {
                                    onConfirm(
                                        productName.trim(),
                                        referenceCode.trim(),
                                        selectedFamily,
                                        selectedPlatform,
                                        priceYuan,
                                        resaleAr,
                                        weightKg?.takeIf { it > 0.0 },
                                        selectedFreightMode,
                                        variantsList.joinToString(", "),
                                        visualValue.trim(),
                                        notes.trim(),
                                        isHidden
                                    )
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(42.dp)
                        ) {
                            Text("Enregistrer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentMaxWidth = if (maxWidth >= 680.dp) 680.dp else maxWidth
                    Column(
                        modifier = Modifier
                            .widthIn(max = contentMaxWidth)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.dangerSoftBg
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = colors.dangerAccent,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Visual Picker Card
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = colors.cardBg,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isPhoto = visualValue.startsWith("content://") ||
                                    visualValue.startsWith("file://") ||
                                    visualValue.startsWith("http")
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.innerCardBg)
                                        .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPhoto) {
                                        AsyncImage(
                                            model = visualValue,
                                            contentDescription = productName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(text = visualValue.ifBlank { famEmoji }, fontSize = 34.sp)
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Visuel de l'article",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = colors.cyanSoftBg,
                                            modifier = Modifier.clickable {
                                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.PhotoCamera, null, tint = colors.cyanAccent, modifier = Modifier.size(14.dp))
                                                Text("Photo", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.cyanAccent)
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = colors.violetSoftBg,
                                            modifier = Modifier.clickable { showEmojiPicker = true }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.EmojiEmotions, null, tint = colors.violetAccent, modifier = Modifier.size(14.dp))
                                                Text("Émoji", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.violetAccent)
                                            }
                                        }
                                        if (visualValue.isNotBlank()) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = colors.dangerSoftBg,
                                                modifier = Modifier.clickable { visualValue = "" }
                                            ) {
                                                Box(modifier = Modifier.padding(7.dp)) {
                                                    Icon(Icons.Default.DeleteOutline, "Retirer", tint = colors.dangerAccent, modifier = Modifier.size(15.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Identity & Family & Platform
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = colors.cardBg,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Identité & Classement", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.cyanAccent)

                                OutlinedTextField(
                                    value = productName,
                                    onValueChange = {
                                        productName = it
                                        errorMessage = null
                                    },
                                    label = { Text("Nom du produit *") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = referenceCode,
                                    onValueChange = { referenceCode = it },
                                    label = { Text("Code SKU / Référence") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Text("Famille", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textSecondary)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    familyNames.forEach { fam ->
                                        val isSelected = selectedFamily.equals(fam, ignoreCase = true)
                                        val e = availableFamilies.firstOrNull { it.name.equals(fam, ignoreCase = true) }?.emoji ?: ""
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) colors.cyanSoftBg else colors.innerCardBg,
                                            border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) colors.cyanAccent else colors.border),
                                            modifier = Modifier.clickable { selectedFamily = fam }
                                        ) {
                                            Text(
                                                text = if (e.isNotBlank()) "$e $fam" else fam,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = if (isSelected) colors.cyanAccent else colors.textPrimary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }

                                Text("Plateforme sourcing", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textSecondary)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    platformOptions.forEach { plat ->
                                        val isSelected = selectedPlatform.equals(plat, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) colors.violetSoftBg else colors.innerCardBg,
                                            border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) colors.violetAccent else colors.border),
                                            modifier = Modifier.clickable { selectedPlatform = plat }
                                        ) {
                                            Text(
                                                text = plat,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = if (isSelected) colors.violetAccent else colors.textPrimary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Prices & Weight
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = colors.cardBg,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Tarifs & Logistique", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.cyanAccent)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = priceYuanText,
                                        onValueChange = { raw ->
                                            val cleaned = raw.replace(',', '.').filter { it.isDigit() || it == '.' }
                                            if (cleaned.count { it == '.' } <= 1) priceYuanText = cleaned
                                        },
                                        label = { Text("Achat (¥)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = resaleArText,
                                        onValueChange = { resaleArText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Revente (Ar)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = weightText,
                                        onValueChange = { raw ->
                                            val cleaned = raw.replace(',', '.').filter { it.isDigit() || it == '.' }
                                            if (cleaned.count { it == '.' } <= 1) weightText = cleaned
                                        },
                                        label = { Text("Poids unit. (kg)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )
                                    Row(
                                        modifier = Modifier.weight(1.2f).padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("AÉRIEN", "MARITIME").forEach { mode ->
                                            val sel = selectedFreightMode == mode
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .clickable { selectedFreightMode = mode },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (sel) colors.expressSoftBg else colors.innerCardBg,
                                                border = BorderStroke(1.dp, if (sel) colors.expressAccent else colors.border)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = if (mode == "AÉRIEN") "Aérien" else "Maritime",
                                                        fontFamily = PlusJakartaSans,
                                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 12.sp,
                                                        color = if (sel) colors.expressAccent else colors.textPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (landedCostAr > 0L || resaleAr > 0L) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = colors.innerCardBg
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Coût de revient est.", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                                                Text(CurrencyFormatter.formatAriary(landedCostAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
                                            }
                                            if (resaleAr > 0L && landedCostAr > 0L) {
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("Marge estimée", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                                                    Text(
                                                        text = "${CurrencyFormatter.formatAriary(marginAr)} ($marginPct%)",
                                                        fontFamily = PlusJakartaSans,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = if (marginPct >= 20) colors.successAccent else if (marginPct > 0) colors.cyanAccent else colors.dangerAccent
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Variants & Notes & Hide
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = colors.cardBg,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Déclinaisons & Notes", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.violetAccent)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = colors.cyanSoftBg,
                                        modifier = Modifier.clickable { showAddVariantDialog = true }
                                    ) {
                                        Text(
                                            text = "+ Variante",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = colors.cyanAccent,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                if (variantsList.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        variantsList.forEachIndexed { idx, v ->
                                            val dotColor = VariantColorUtils.getVariantColor(v, idx)
                                            Surface(
                                                shape = RoundedCornerShape(9999.dp),
                                                color = colors.innerCardBg,
                                                border = BorderStroke(1.dp, colors.border)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(dotColor)
                                                    )
                                                    Text(v, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = colors.textPrimary)
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Supprimer $v",
                                                        tint = colors.textSecondary,
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable { variantsList = variantsList - v }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Aucune déclinaison configurée.",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }

                                OutlinedTextField(
                                    value = notes,
                                    onValueChange = { notes = it },
                                    label = { Text("Notes / remarques") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    maxLines = 3
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.innerCardBg)
                                        .clickable { isHidden = !isHidden }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Masquer du catalogue",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "Retire l'article des vues actives (tous les lots liés)",
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                    androidx.compose.material3.Switch(
                                        checked = isHidden,
                                        onCheckedChange = { isHidden = it }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            if (showAddVariantDialog) {
                AddVariantDialog(
                    onDismiss = { showAddVariantDialog = false },
                    onConfirm = { newVar ->
                        variantsList = (variantsList + newVar).distinct()
                        showAddVariantDialog = false
                    }
                )
            }

            if (showEmojiPicker) {
                ArticleEmojiPickerDialog(
                    currentEmoji = if (!visualValue.contains("/")) visualValue else "",
                    onDismiss = { showEmojiPicker = false },
                    onSelect = { emoji ->
                        visualValue = emoji
                        showEmojiPicker = false
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ADD FAMILY DIALOG (REDESIGNED WITH LIVE PREVIEW)
// ---------------------------------------------------------------------------
@Composable
fun AddFamilyDialog(
    onDismiss: () -> Unit,
    onConfirm: (familyName: String, emoji: String, colorHex: String) -> Unit
) {
    var familyName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🎧") }
    var selectedColorHex by remember { mutableStateOf("#06B6D4") }

    val emojiChoices = listOf(
        "🎧", "👗", "🏠", "📱", "💄", "👟", "🧸", "⚡",
        "💻", "🧴", "💍", "⚽", "🚗", "🧹", "📦", "✨"
    )

    val colorChoices = listOf(
        "#06B6D4", // Cyan
        "#EC4899", // Pink
        "#F97316", // Orange
        "#8B5CF6", // Purple
        "#10B981", // Emerald
        "#F59E0B", // Amber
        "#3B82F6", // Blue
        "#6366F1"  // Indigo
    )

    val previewColor = remember(selectedColorHex) {
        try {
            Color(android.graphics.Color.parseColor(selectedColorHex))
        } catch (e: Exception) {
            CyanPrimary
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Nouvelle famille d'articles",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Card Preview
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.5.dp, previewColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(previewColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedEmoji, fontSize = 24.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = familyName.ifBlank { "Nom de la famille" },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (familyName.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Aperçu de la famille • 0 article",
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Family Name Input
                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text("Nom de la famille *") },
                    placeholder = { Text("ex. Audio High-Tech, Beauté, Sacs...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Emoji Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Icône Émoji",
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
                        emojiChoices.forEach { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Surface(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clickable { selectedEmoji = emoji },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) previewColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) BorderStroke(1.5.dp, previewColor) else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }

                // Color Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Couleur du thème",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorChoices.forEach { colorHex ->
                            val isSelected = selectedColorHex.equals(colorHex, ignoreCase = true)
                            val c = try {
                                Color(android.graphics.Color.parseColor(colorHex))
                            } catch (e: Exception) {
                                CyanPrimary
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .clickable { selectedColorHex = colorHex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (familyName.isNotBlank()) {
                        onConfirm(familyName.trim(), selectedEmoji, selectedColorHex)
                        onDismiss()
                    }
                },
                enabled = familyName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = previewColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Créer la famille",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
fun NewActionMenuSheet(
    onDismiss: () -> Unit,
    onNewArticleClick: () -> Unit,
    onNewPurchaseClick: () -> Unit,
    onNewSaleClick: () -> Unit
) {
    val colors = rememberTransitaireColors()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}, // prevent click-through
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = colors.cardBg,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NOUVELLE OPÉRATION",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = colors.violetAccent,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Que souhaites-tu créer ?",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = colors.textPrimary
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_close_action_menu")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = colors.textPrimary)
                        }
                    }

                    // Option 1 : Nouvel Article (Catalogue)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onNewArticleClick()
                            }
                            .testTag("btn_menu_new_article"),
                        shape = RoundedCornerShape(16.dp),
                        color = colors.innerCardBg
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.cyanSolidBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nouvel Article",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Ajouter une référence au catalogue produit",
                                    fontFamily = com.example.ui.theme.Inter,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    // Option 2 : Nouvel Achat (Chine / 1688)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onNewPurchaseClick()
                            }
                            .testTag("btn_menu_new_purchase"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (colors.isDark) Color(0xFF431407).copy(alpha = 0.45f) else Color(0xFFFFF7ED)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEA580C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nouvel Achat (Chine)",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Lancer une commande d'importation 1688",
                                    fontFamily = com.example.ui.theme.Inter,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    // Option 3 : Nouvelle Vente
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onNewSaleClick()
                            }
                            .testTag("btn_menu_new_sale"),
                        shape = RoundedCornerShape(16.dp),
                        color = colors.successSoftBg
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.successAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sell,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nouvelle Vente",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Enregistrer une vente directe à un client",
                                    fontFamily = com.example.ui.theme.Inter,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


