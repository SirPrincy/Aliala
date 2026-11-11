package com.example.ui.business.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
    onDismiss: () -> Unit,
    onConfirm: (amountYuan: Double, rateAr: Double, platform: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf("Alipay") }

    val amountYuan = amountText.toDoubleOrNull() ?: 0.0
    val rateAr = rateText.toDoubleOrNull() ?: 0.0
    val totalAr = (amountYuan * rateAr).toLong()

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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Montant en Yuans (¥)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_yuan_amount")
                )

                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Taux de change (Ar / ¥)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_yuan_rate")
                )

                // Platform selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Alipay", "WeChat Pay").forEach { platform ->
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

                // Summary
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
                            text = "Coût total en Ariary :",
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
                    if (amountYuan > 0 && rateAr > 0) {
                        onConfirm(amountYuan, rateAr, selectedPlatform)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Valider l'achat", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
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

data class VariantItemState(
    val id: Int,
    val name: String,
    val colorHex: Long,
    val quantity: Int
)

@Composable
fun AddOrderDialog(
    cmupRate: Long,
    availableYuans: Double = 0.0,
    availableArticles: List<com.example.data.local.entity.BusinessOrderEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
        product: String,
        supplier: String,
        quantity: Int,
        priceYuan: Double,
        resaleAr: Long,
        category: String,
        carrier: String,
        variants: String,
        freightAr: Long
    ) -> Unit
) {
    val context = LocalContext.current
    val initialArticle = availableArticles.firstOrNull()
    var selectedArticleId by remember { mutableStateOf<Long?>(null) }
    var product by remember { mutableStateOf("") }
    var supplierRef by remember { mutableStateOf("") }
    var familyCategory by remember { mutableStateOf("Électronique") }
    var selectedSupplier by remember { mutableStateOf("1688.com") }
    var priceYuanText by remember { mutableStateOf("") }
    var isShippingPaid by remember { mutableStateOf(false) }
    var shippingCostYuanText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("BUSINESS") }
    var carrier by remember { mutableStateOf("SinoExpress") }
    var resaleArText by remember { mutableStateOf("") }

    var variantsList by remember {
        mutableStateOf(
            listOf(
                VariantItemState(1, "Noir", 0xFF0F172A, 10),
                VariantItemState(2, "Blanc", 0xFFFFFFFF, 5)
            )
        )
    }

    val totalUnits = variantsList.sumOf { it.quantity }.coerceAtLeast(1)
    val priceYuan = priceYuanText.toDoubleOrNull() ?: 0.0
    val effectiveCmupRate = if (cmupRate > 0) cmupRate else 650L
    val unitPriceAr = (priceYuan * effectiveCmupRate).toLong()
    val goodsTotalYuan = totalUnits * priceYuan
    val shippingCostYuan = if (isShippingPaid) (shippingCostYuanText.toDoubleOrNull() ?: 0.0) else 0.0
    val orderTotalYuan = goodsTotalYuan + shippingCostYuan
    val orderTotalAr = (orderTotalYuan * effectiveCmupRate).toLong()
    val shippingCostAr = (shippingCostYuan * effectiveCmupRate).toLong()
    val effectiveAvailableYuans = availableYuans
    val isSoldeSuffisant = effectiveAvailableYuans >= orderTotalYuan

    val scrollState = rememberScrollState()

    val saveAction = {
        if (product.isBlank()) {
            Toast.makeText(context, "Veuillez saisir le nom de l'article", Toast.LENGTH_SHORT).show()
        } else if (priceYuan <= 0.0) {
            Toast.makeText(context, "Veuillez indiquer le prix unitaire en Yuans (¥)", Toast.LENGTH_SHORT).show()
        } else {
            val variantsStr = variantsList.filter { it.name.isNotBlank() }.joinToString(", ") { "${it.name} ×${it.quantity}" }
            val resaleAr = resaleArText.toLongOrNull() ?: (priceYuan * effectiveCmupRate * 1.5).toLong()
            onConfirm(
                product.trim(),
                selectedSupplier.trim(),
                totalUnits,
                priceYuan,
                resaleAr,
                category,
                carrier.trim().ifBlank { "SinoExpress" },
                variantsStr.ifBlank { "Standard ×$totalUnits" },
                shippingCostAr
            )
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 76.dp)
                ) {
                    // TOP FIXED HEADER
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { onDismiss() }
                                        .testTag("btn_close_new_order"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fermer",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = "Nouvel Achat",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(
                                    onClick = saveAction,
                                    modifier = Modifier.testTag("top_save_action")
                                ) {
                                    Text(
                                        text = "Enregistrer",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profil",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // SCROLLABLE BODY
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // SUB-HEADER & STEPPER PROGRESS BAR
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MODULE BUSINESS",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Étape 2 sur 4",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "Quantités & Prix",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // 4-Stage Progress Bar Grid
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Step 1: Article (Validated)
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF06B6D4))
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "Article",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Step 2: Quantités (Active Focus)
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RadioButtonChecked,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "Quantités",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Step 3: Livraison
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                    Text(
                                        text = "Livraison",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }

                                // Step 4: Paiement
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                    Text(
                                        text = "Paiement",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                            }
                        }

                        // 1. ARTICLE & IDENTIFICATION CARD
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Article & Identification",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (availableArticles.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = "${availableArticles.distinctBy { it.product }.size} en catalogue",
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // PROMINENT CATALOGUE SELECTION SECTION (ALWAYS SHOWN IF AVAILABLE)
                                if (availableArticles.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Sélectionner un article du catalogue :",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            availableArticles.distinctBy { it.product }.forEach { art ->
                                                val isThis = product.equals(art.product, ignoreCase = true)
                                                Surface(
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = if (isThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                    shadowElevation = 1.dp,
                                                    modifier = Modifier.clickable {
                                                        product = art.product
                                                        if (art.referenceCode.isNotBlank()) supplierRef = art.referenceCode
                                                        if (art.family.isNotBlank()) familyCategory = art.family
                                                        if (art.priceYuan > 0) priceYuanText = String.format(java.util.Locale.US, "%.2f", art.priceYuan)
                                                        if (art.resalePriceAr > 0) resaleArText = art.resalePriceAr.toString()
                                                        if (art.platform.isNotBlank()) selectedSupplier = art.platform
                                                        if (art.carrier.isNotBlank()) carrier = art.carrier
                                                    }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        if (isThis) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                        Text(
                                                            text = art.product,
                                                            fontSize = 12.sp,
                                                            fontFamily = PlusJakartaSans,
                                                            fontWeight = if (isThis) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isThis) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }

                                            // Clear / Free input chip
                                            if (product.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    modifier = Modifier.clickable {
                                                        product = ""
                                                        supplierRef = ""
                                                        priceYuanText = ""
                                                        resaleArText = ""
                                                    }
                                                ) {
                                                    Text(
                                                        text = "+ Autre article",
                                                        fontSize = 12.sp,
                                                        fontFamily = PlusJakartaSans,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = product,
                                    onValueChange = { product = it },
                                    label = { Text("Nom du produit / Article *") },
                                    placeholder = { Text("Ex: Montre connectée, Coque, Sac...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_order_product_name"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = supplierRef,
                                        onValueChange = { supplierRef = it },
                                        label = { Text("Réf / SKU Fournisseur") },
                                        placeholder = { Text("Ex: SKU-2026-01") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_order_sku"),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                    OutlinedTextField(
                                        value = familyCategory,
                                        onValueChange = { familyCategory = it },
                                        label = { Text("Famille / Catégorie") },
                                        placeholder = { Text("Ex: Électronique") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_order_family"),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Électronique", "Audio", "Mode & Beauté", "Maison", "Accessoires").forEach { fam ->
                                        val isFam = familyCategory.equals(fam, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(9999.dp),
                                            color = if (isFam) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable { familyCategory = fam }
                                        ) {
                                            Text(
                                                text = fam,
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 11.sp,
                                                fontWeight = if (isFam) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isFam) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. SUPPLIER PLATFORM & CARRIER SELECTION
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Plateforme Fournisseur
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Plateforme Fournisseur Chine",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("1688.com", "Taobao", "Pinduoduo", "Fournisseur direct").forEach { platform ->
                                        val isSelected = selectedSupplier == platform
                                        Surface(
                                            shape = RoundedCornerShape(9999.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            shadowElevation = if (isSelected) 1.dp else 0.dp,
                                            modifier = Modifier
                                                .clickable { selectedSupplier = platform }
                                                .testTag("platform_chip_$platform")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Verified,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Text(
                                                    text = platform,
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 12.sp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Transitaire vers Madagascar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Transitaire (Fret international)",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("SinoExpress", "SF Express", "MadaCargo", "Autre").forEach { c ->
                                        val isSelected = carrier == c
                                        Surface(
                                            shape = RoundedCornerShape(9999.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            shadowElevation = if (isSelected) 1.dp else 0.dp,
                                            modifier = Modifier
                                                .clickable { carrier = c }
                                                .testTag("carrier_chip_$c")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Text(
                                                    text = c,
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 12.sp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. UNIT PRICE & YUAN CONVERSION
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CurrencyYuan,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Prix d'achat unitaire",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Devise ¥ (CNY)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontFamily = PlusJakartaSans
                                        )
                                    }
                                }

                                // Large Currency Input Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "¥",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 24.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            OutlinedTextField(
                                                value = priceYuanText,
                                                onValueChange = { priceYuanText = it },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("input_order_price_yuan"),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color.Transparent,
                                                    unfocusedBorderColor = Color.Transparent,
                                                    focusedContainerColor = Color.Transparent,
                                                    unfocusedContainerColor = Color.Transparent
                                                ),
                                                textStyle = androidx.compose.ui.text.TextStyle(
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 24.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                ),
                                                placeholder = { Text("0.00", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
                                            )
                                        }

                                        Text(
                                            text = "par unité",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }
                                }

                                // Live Exchange Rate Hint
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CurrencyExchange,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "≈ ${CurrencyFormatter.formatAriary(unitPriceAr)} / unité au taux moyen CMUP (${effectiveCmupRate} Ar/¥)",
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // 4. VARIANTS & QUANTITIES (Quantités par couleur)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Quantités par couleur",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.clickable {
                                            val newId = (variantsList.maxOfOrNull { it.id } ?: 0) + 1
                                            variantsList = variantsList + VariantItemState(newId, "Nouvelle couleur", 0xFF6366F1, 1)
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Ajouter",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Ajouter",
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                // Variant Rows
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    variantsList.forEachIndexed { index, variant ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    // Color Dot Indicator
                                                    Box(
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color(variant.colorHex))
                                                    )

                                                    Column {
                                                        Text(
                                                            text = variant.name,
                                                            fontFamily = PlusJakartaSans,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 13.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "Stock transit : 0",
                                                            fontFamily = Inter,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                // Stepper Container
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = MaterialTheme.colorScheme.surface,
                                                    shadowElevation = 1.dp
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(2.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                                .clickable {
                                                                    if (variant.quantity > 0) {
                                                                        variantsList = variantsList.toMutableList().also {
                                                                            it[index] = variant.copy(quantity = variant.quantity - 1)
                                                                        }
                                                                    }
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Remove,
                                                                contentDescription = "Diminuer",
                                                                modifier = Modifier.size(14.dp),
                                                                tint = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }

                                                        Text(
                                                            text = "${variant.quantity}",
                                                            fontFamily = PlusJakartaSans,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 15.sp,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.width(32.dp),
                                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                        )

                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                                .clickable {
                                                                    variantsList = variantsList.toMutableList().also {
                                                                        it[index] = variant.copy(quantity = variant.quantity + 1)
                                                                    }
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Add,
                                                                contentDescription = "Augmenter",
                                                                modifier = Modifier.size(14.dp),
                                                                tint = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Total Calculated Badge
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Volume sélectionné",
                                        fontFamily = Inter,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Inventory2,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Total : $totalUnits unités",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 5. DOMESTIC SHIPPING IN CHINA (Frais de port intérieurs Chine)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Frais de port intérieurs (Chine)",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "Vers transitaire",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "Expédition de l'usine jusqu'au hub de Guangzhou / Yiwu.",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Gratuit vs Payant Toggle Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (!isShippingPaid) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        shadowElevation = if (!isShippingPaid) 1.dp else 0.dp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clickable { isShippingPaid = false }
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = "Gratuit (包邮)",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = if (!isShippingPaid) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = if (!isShippingPaid) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isShippingPaid) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        shadowElevation = if (isShippingPaid) 1.dp else 0.dp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clickable { isShippingPaid = true }
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            if (isShippingPaid) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = "Payant",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = if (isShippingPaid) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = if (isShippingPaid) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Shipping Amount Input Row
                                if (isShippingPaid) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Montant Fret Chine",
                                                fontFamily = Inter,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "¥",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                OutlinedTextField(
                                                    value = shippingCostYuanText,
                                                    onValueChange = { shippingCostYuanText = it },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.width(80.dp),
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Color.Transparent,
                                                        unfocusedBorderColor = Color.Transparent,
                                                        focusedContainerColor = Color.Transparent,
                                                        unfocusedContainerColor = Color.Transparent
                                                    ),
                                                    textStyle = androidx.compose.ui.text.TextStyle(
                                                        fontFamily = PlusJakartaSans,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                                                    ),
                                                    placeholder = { Text("0.00") }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 6. REAL-TIME FINANCIAL SUMMARY WIDGET (Synthèse de commande)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SYNTHÈSE DE COMMANDE",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        letterSpacing = 0.5.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Étape 2 active",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Total marchandises ($totalUnits × ¥ ${String.format(java.util.Locale.US, "%.0f", priceYuan)})",
                                        fontFamily = Inter,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "¥ ${String.format(java.util.Locale.US, "%.2f", goodsTotalYuan)}",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Fret Chine (Entrepôt)",
                                        fontFamily = Inter,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "¥ ${String.format(java.util.Locale.US, "%.2f", shippingCostYuan)}",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "Total commande",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "¥ ${String.format(java.util.Locale.US, "%.2f", orderTotalYuan)}",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "≈ ${CurrencyFormatter.formatAriary(orderTotalAr)}",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Alipay Balance Indicator Box
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccountBalanceWallet,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = "Solde Alipay dispo",
                                                    fontFamily = PlusJakartaSans,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "¥ ${String.format(java.util.Locale.US, "%.2f", effectiveAvailableYuans)}",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(
                                                    if (isSoldeSuffisant) MaterialTheme.colorScheme.primaryContainer
                                                    else MaterialTheme.colorScheme.errorContainer
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = if (isSoldeSuffisant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = if (isSoldeSuffisant) "Solde suffisant" else "Solde insuffisant",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = if (isSoldeSuffisant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 7. ACTIONS BAR / BOTTOM CONTROLS
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clickable { onDismiss() }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Précédent",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .weight(2f)
                                    .height(52.dp)
                                    .clickable { saveAction() }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Continuer vers Livraison",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // FIXED BOTTOM PRIMARY ACTION (Enregistrer l'achat)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Button(
                            onClick = saveAction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_save_purchase_final")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Enregistrer l'achat",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
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
    val initialStockItem = availableStockOrders.firstOrNull()
    var selectedStockOrderId by remember { mutableStateOf<Long?>(initialStockItem?.id ?: -1L) }
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf(initialStockItem?.product ?: "") }
    var quantityText by remember { mutableStateOf("1") }
    var unitPriceText by remember { mutableStateOf(initialStockItem?.let { if (it.resalePriceAr > 0) it.resalePriceAr.toString() else "" } ?: "") }
    val initialCost = initialStockItem?.let { (it.priceYuan * it.exchangeRate).toLong() } ?: 0L
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
// ADD CATALOGUE ARTICLE DIALOG
// ---------------------------------------------------------------------------
@Composable
fun AddCatalogueArticleDialog(
    availableFamilies: List<String> = listOf("Électronique & Audio", "Mode", "Maison", "Beauté"),
    onDismiss: () -> Unit,
    onConfirm: (
        product: String,
        family: String,
        referenceCode: String,
        variants: String,
        priceYuan: Double,
        resaleAr: Long,
        stockCount: Int,
        weightKg: Double,
        platform: String
    ) -> Unit
) {
    val families = availableFamilies.ifEmpty { listOf("Électronique & Audio", "Mode", "Maison", "Beauté") }
    var product by remember { mutableStateOf("") }
    var family by remember { mutableStateOf(families.first()) }
    var showFamilyMenu by remember { mutableStateOf(false) }
    var referenceCode by remember { mutableStateOf("") }
    var variants by remember { mutableStateOf("") }
    var priceYuanText by remember { mutableStateOf("") }
    var resaleArText by remember { mutableStateOf("") }
    var stockCountText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nouvel article au catalogue",
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
                    value = product,
                    onValueChange = { product = it },
                    label = { Text("Nom de l'article") },
                    placeholder = { Text("ex. Écouteurs TWS Pro 5") },
                    modifier = Modifier.fillMaxWidth().testTag("input_cat_product"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = referenceCode,
                        onValueChange = { referenceCode = it },
                        label = { Text("SKU / Référence") },
                        placeholder = { Text("ex. TWS-P5-PRO") },
                        modifier = Modifier.weight(1f).testTag("input_cat_sku"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = family,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Famille") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Choisir une famille"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_cat_family"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showFamilyMenu = true }
                        )
                        DropdownMenu(
                            expanded = showFamilyMenu,
                            onDismissRequest = { showFamilyMenu = false }
                        ) {
                            families.forEach { famOption ->
                                DropdownMenuItem(
                                    text = { Text(famOption, fontFamily = PlusJakartaSans) },
                                    onClick = {
                                        family = famOption
                                        showFamilyMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = variants,
                    onValueChange = { variants = it },
                    label = { Text("Déclinaisons") },
                    placeholder = { Text("ex. Noir · Blanc · Rouge") },
                    modifier = Modifier.fillMaxWidth().testTag("input_cat_variants"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceYuanText,
                        onValueChange = { priceYuanText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Dernier coût (¥)") },
                        placeholder = { Text("ex. 28") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_cat_price_yuan"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = resaleArText,
                        onValueChange = { resaleArText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Prix revente (Ar)") },
                        placeholder = { Text("ex. 32500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f).testTag("input_cat_resale_ar"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockCountText,
                        onValueChange = { stockCountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Stock initial (pcs)") },
                        placeholder = { Text("ex. 20") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Poids unitaire (kg)") },
                        placeholder = { Text("ex. 0.15") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = platform,
                    onValueChange = { platform = it },
                    label = { Text("Plateforme fournisseur") },
                    placeholder = { Text("ex. 1688, Taobao") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pYuan = priceYuanText.toDoubleOrNull() ?: 0.0
                    val rAr = resaleArText.toLongOrNull() ?: 0L
                    val sCount = stockCountText.toIntOrNull() ?: 0
                    val wKg = weightText.toDoubleOrNull() ?: 0.0
                    if (product.isNotBlank()) {
                        onConfirm(
                            product.trim(),
                            family.trim().ifBlank { "Électronique" },
                            referenceCode.trim(),
                            variants.trim(),
                            pYuan,
                            rAr,
                            sCount,
                            wKg,
                            platform.trim().ifBlank { "1688" }
                        )
                        onDismiss()
                    }
                },
                enabled = product.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Enregistrer l'article", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
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
// EDIT CATALOGUE ARTICLE DIALOG
// ---------------------------------------------------------------------------
@Composable
fun EditCatalogueArticleDialog(
    order: com.example.data.local.entity.BusinessOrderEntity,
    onDismiss: () -> Unit,
    onConfirm: (newPriceYuan: Double, newResaleAr: Long, newVariants: String, isHidden: Boolean) -> Unit
) {
    var priceYuanText by remember { mutableStateOf(order.priceYuan.toInt().toString()) }
    var resaleArText by remember { mutableStateOf(order.resalePriceAr.toString()) }
    var variants by remember { mutableStateOf(order.variants) }
    var isHidden by remember { mutableStateOf(order.isHidden) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Modifier la référence",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "${order.product} • ${order.referenceCode.ifBlank { "SKU" }}",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceYuanText,
                        onValueChange = { priceYuanText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Coût achat (¥)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = resaleArText,
                        onValueChange = { resaleArText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Prix revente (Ar)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = variants,
                    onValueChange = { variants = it },
                    label = { Text("Déclinaisons / Couleurs") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Toggle Masquer du catalogue
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "L'article n'apparaîtra plus dans les suggestions",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = isHidden,
                        onCheckedChange = { isHidden = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pY = priceYuanText.toDoubleOrNull() ?: order.priceYuan
                    val rAr = resaleArText.toLongOrNull() ?: order.resalePriceAr
                    onConfirm(pY, rAr, variants.trim(), isHidden)
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

// ---------------------------------------------------------------------------
// ADD FAMILY DIALOG
// ---------------------------------------------------------------------------
@Composable
fun AddFamilyDialog(
    onDismiss: () -> Unit,
    onConfirm: (familyName: String) -> Unit
) {
    var familyName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ajouter une famille d'articles",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text("Nom de la famille (ex. Beauté, Jouets)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (familyName.isNotBlank()) {
                        onConfirm(familyName.trim())
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Ajouter", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
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
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}, // prevent click-through
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White,
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
                                color = Color(0xFF712AE2),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Que souhaites-tu créer ?",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF131B2E)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_close_action_menu")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color(0xFF131B2E))
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
                        color = Color(0xFFF2F3FF)
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
                                    .background(Color(0xFF00687A)),
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
                                    color = Color(0xFF131B2E)
                                )
                                Text(
                                    text = "Ajouter une référence au catalogue produit",
                                    fontFamily = com.example.ui.theme.Inter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6D797D)
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
                        color = Color(0xFFFFF7ED)
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
                                    color = Color(0xFF131B2E)
                                )
                                Text(
                                    text = "Lancer une commande d'importation 1688",
                                    fontFamily = com.example.ui.theme.Inter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6D797D)
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
                        color = Color(0xFFF0FDF4)
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
                                    .background(Color(0xFF16A34A)),
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
                                    color = Color(0xFF131B2E)
                                )
                                Text(
                                    text = "Enregistrer une vente directe à un client",
                                    fontFamily = com.example.ui.theme.Inter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6D797D)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


