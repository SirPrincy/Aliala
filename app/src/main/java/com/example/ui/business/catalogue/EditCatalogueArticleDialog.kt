package com.example.ui.business.catalogue

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.CatalogueFamilySummaryUi
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.BusinessImageStorage
import com.example.ui.business.shared.VariantColorUtils
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.launch

@Composable
fun EditCatalogueArticleDialog(
    article: CatalogueArticleSummaryUi,
    availableFamilies: List<CatalogueFamilySummaryUi> = emptyList(),
    availablePlatforms: List<String> = listOf("1688.com", "Taobao", "Pinduoduo", "Fournisseur Direct WeChat"),
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
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
    val coroutineScope = rememberCoroutineScope()
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
        mutableStateOf(if ((order.weightKg ?: 0.0) > 0.0) order.weightKg.toString() else "")
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
        uri?.let { pickedUri ->
            coroutineScope.launch {
                val persisted = BusinessImageStorage.persistPickedPhoto(context, pickedUri)
                if (persisted != null) {
                    visualValue = persisted
                    Toast.makeText(context, "Photo sélectionnée", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Impossible d'enregistrer la photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val isIndicativeCmup = cmupRate <= 0L
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
    val isIndicativeFreight = remember(activeTransitaires, selectedFreightMode) {
        if (selectedFreightMode == "MARITIME") {
            activeTransitaires.none { it.seaRateAr > 0L }
        } else {
            activeTransitaires.none { it.airRateAr > 0L }
        }
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
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.pageBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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

                        EditCatalogueArticleVisualCard(
                            productName = productName,
                            visualValue = visualValue,
                            fallbackEmoji = famEmoji,
                            colors = colors,
                            onPickPhoto = {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            onPickEmoji = { showEmojiPicker = true },
                            onClearVisual = { visualValue = "" }
                        )

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
                                    Text("Tarifs & Logistique", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.cyanAccent)
                                    Surface(
                                        shape = RoundedCornerShape(9999.dp),
                                        color = colors.badgeNeutralBg
                                    ) {
                                        Text(
                                            text = if (isIndicativeCmup) "CMUP ${effectiveCmupRate.toLong()} Ar/¥ (indicatif)" else "CMUP ${effectiveCmupRate.toLong()} Ar/¥",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

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
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
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
                                            if (isIndicativeCmup || (realUnitFreight == 0L && isIndicativeFreight)) {
                                                val parts = buildList {
                                                    if (isIndicativeCmup) add("CMUP indicatif (${effectiveCmupRate.toLong()} Ar/¥)")
                                                    if (realUnitFreight == 0L && isIndicativeFreight) add("fret indicatif (${CurrencyFormatter.formatAriary(if (selectedFreightMode == "MARITIME") seaRate else airRate)})")
                                                }
                                                Text(
                                                    text = "Basé sur : ${parts.joinToString(" • ")}",
                                                    fontFamily = Inter,
                                                    fontSize = 11.sp,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

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
                                        Text("Masquer du catalogue", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
                                        Text("Retire l'article des vues actives (tous les lots liés)", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                                    }
                                    Switch(checked = isHidden, onCheckedChange = { isHidden = it })
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
