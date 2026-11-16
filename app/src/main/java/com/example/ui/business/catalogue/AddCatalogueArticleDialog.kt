package com.example.ui.business.catalogue

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.business.CatalogueFamilySummaryUi
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.BusinessImageStorage
import com.example.ui.business.shared.VariantColorUtils
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale
import kotlin.math.round
import kotlinx.coroutines.launch

@Composable
fun AddCatalogueArticleDialog(
    availableFamilies: List<String> = listOf("Électronique & Audio", "Mode & Maroquinerie", "Maison & Rangement", "Accessoires Mobiles"),
    familySummaries: List<CatalogueFamilySummaryUi> = emptyList(),
    availablePlatforms: List<String> = listOf("1688.com", "Taobao", "Pinduoduo", "Fournisseur Direct WeChat", "Xianyu (闲鱼)"),
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    cmupRate: Long = 650L,
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
        platform: String,
        imageUrl: String,
        freightMode: String,
        notes: String,
        estimatedFreightPerKgAr: Long
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val colors = rememberTransitaireColors()

    val families = remember(availableFamilies, familySummaries) {
        if (familySummaries.isNotEmpty()) familySummaries.map { it.name }
        else availableFamilies.ifEmpty { listOf("Électronique & Audio", "Mode & Maroquinerie", "Maison & Rangement", "Accessoires Mobiles") }
    }
    val platformOptions = remember(availablePlatforms) {
        availablePlatforms.ifEmpty { listOf("1688.com", "Taobao", "Pinduoduo", "Fournisseur Direct WeChat") }
    }
    val effectiveCmupRate = if (cmupRate > 0L) cmupRate.toDouble() else 650.0

    val activeTransitaires = remember(transitaires) { transitaires.filter { !it.isArchived && it.isActive } }
    val defaultAirRate = remember(activeTransitaires) {
        val rates = activeTransitaires.mapNotNull { it.airRateAr.takeIf { r -> r > 0L } }
        if (rates.isNotEmpty()) rates.average().toLong() else 45000L
    }
    val defaultSeaRate = remember(activeTransitaires) {
        val rates = activeTransitaires.mapNotNull { it.seaRateAr.takeIf { r -> r > 0L } }
        if (rates.isNotEmpty()) rates.average().toLong() else 18000L
    }

    var product by remember { mutableStateOf("") }
    var family by remember { mutableStateOf(families.first()) }
    var referenceCode by remember { mutableStateOf("") }
    var variantsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var priceYuanText by remember { mutableStateOf("") }
    var resaleArText by remember { mutableStateOf("") }
    var stockCountText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf(platformOptions.first()) }
    var selectedFreightMode by remember { mutableStateOf("AÉRIEN") }
    var selectedVisual by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var creationMode by remember { mutableStateOf("REFERENTIEL") }
    var validationError by remember { mutableStateOf<String?>(null) }

    var showAddVariantModal by remember { mutableStateOf(false) }
    var showEmojiPickerDialog by remember { mutableStateOf(false) }

    val selectedFamilyObj = remember(family, familySummaries) {
        familySummaries.firstOrNull { it.name.equals(family, ignoreCase = true) }
    }
    val familyEmoji = selectedFamilyObj?.emoji ?: "📦"

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { pickedUri ->
            coroutineScope.launch {
                val persisted = BusinessImageStorage.persistPickedPhoto(context, pickedUri)
                if (persisted != null) {
                    selectedVisual = persisted
                    Toast.makeText(context, "Photo sélectionnée", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Impossible d'enregistrer la photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val priceYuan = priceYuanText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val purchaseAr = round(priceYuan * effectiveCmupRate).toLong()
    val weightKgVal = weightText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val weightGrams = round(weightKgVal * 1000.0).toInt()
    val volumeM3 = if (weightGrams > 0) String.format(Locale.US, "%.4f", weightGrams * 0.0000067) else "—"
    val ratePerKg = if (selectedFreightMode == "MARITIME") defaultSeaRate else defaultAirRate
    val estUnitFreightAr = if (weightKgVal > 0.0) round(weightKgVal * ratePerKg).toLong() else 0L
    val landedUnitCostAr = purchaseAr + estUnitFreightAr
    val resaleAr = resaleArText.toLongOrNull() ?: 0L
    val marginAr = if (resaleAr > 0L && landedUnitCostAr > 0L) resaleAr - landedUnitCostAr else 0L
    val marginPercent = if (resaleAr > 0L && landedUnitCostAr > 0L) {
        ((marginAr.toDouble() / resaleAr.toDouble()) * 100.0).toInt()
    } else 0

    val quickColorSuggestions = listOf("Noir", "Blanc", "Gris", "Bleu Nuit", "Rose", "Or", "Argent", "Vert")

    val trySave = {
        val stockInt = if (creationMode == "STOCK") (stockCountText.toIntOrNull() ?: 0) else 0
        when {
            product.isBlank() -> {
                validationError = "Veuillez saisir le nom de l'article."
            }
            creationMode == "STOCK" && stockInt <= 0 -> {
                validationError = "Indiquez une quantité initiale supérieure à 0 ou choisissez Fiche Référence."
            }
            else -> {
                validationError = null
                onConfirm(
                    product.trim(),
                    family.trim().ifBlank { families.first() },
                    referenceCode.trim(),
                    variantsList.joinToString(", "),
                    priceYuan,
                    resaleAr,
                    stockInt,
                    weightKgVal,
                    selectedPlatform.trim().ifBlank { platformOptions.first() },
                    selectedVisual.trim(),
                    selectedFreightMode,
                    notes.trim(),
                    ratePerKg
                )
                onDismiss()
            }
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
            color = colors.pageBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
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
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = colors.textPrimary
                                )
                            }
                            Column {
                                Text(
                                    text = "Nouvel Article",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Catalogue & calcul de marge",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Button(
                            onClick = { trySave() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("btn_save_catalogue_article")
                        ) {
                            Text(
                                text = if (creationMode == "REFERENTIEL") "Créer fiche" else "Créer + stock",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
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
                        AnimatedVisibility(visible = validationError != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = colors.dangerSoftBg,
                                border = BorderStroke(1.dp, colors.dangerAccent.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = validationError ?: "",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.dangerAccent,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        AddCatalogueModeAndVisualSection(
                            creationMode = creationMode,
                            onModeChange = {
                                creationMode = it
                                validationError = null
                            },
                            selectedVisual = selectedVisual,
                            familyEmoji = familyEmoji,
                            productName = product,
                            colors = colors,
                            onPickPhoto = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onOpenEmojiPicker = { showEmojiPickerDialog = true },
                            onClearVisual = { selectedVisual = "" }
                        )

                        AddCatalogueIdentityAndPricingSection(
                            product = product,
                            onProductChange = {
                                product = it
                                if (validationError != null) validationError = null
                            },
                            referenceCode = referenceCode,
                            onReferenceCodeChange = { referenceCode = it },
                            family = family,
                            onFamilyChange = { family = it },
                            families = families,
                            familySummaries = familySummaries,
                            cmupRate = cmupRate,
                            effectiveCmupRate = effectiveCmupRate,
                            priceYuanText = priceYuanText,
                            onPriceYuanChange = { priceYuanText = it },
                            resaleArText = resaleArText,
                            onResaleArChange = { resaleArText = it },
                            purchaseAr = purchaseAr,
                            estUnitFreightAr = estUnitFreightAr,
                            selectedFreightMode = selectedFreightMode,
                            landedUnitCostAr = landedUnitCostAr,
                            resaleAr = resaleAr,
                            marginAr = marginAr,
                            marginPercent = marginPercent,
                            hasValidationError = validationError != null,
                            colors = colors
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = colors.cardBg,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "3. Déclinaisons & Couleurs",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = colors.violetAccent
                                    )
                                    Text(
                                        text = if (variantsList.isEmpty()) "Optionnel" else "${variantsList.size} choix",
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    quickColorSuggestions.forEach { sugg ->
                                        val isAdded = variantsList.any { it.equals(sugg, ignoreCase = true) }
                                        val dotColor = VariantColorUtils.getVariantColor(sugg)
                                        Surface(
                                            shape = RoundedCornerShape(9999.dp),
                                            color = if (isAdded) colors.violetSoftBg else colors.innerCardBg,
                                            border = BorderStroke(1.dp, if (isAdded) colors.violetAccent else colors.border),
                                            modifier = Modifier.clickable {
                                                variantsList = if (isAdded) {
                                                    variantsList.filterNot { it.equals(sugg, ignoreCase = true) }
                                                } else {
                                                    variantsList + sugg
                                                }
                                            }
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
                                                        .then(
                                                            if (VariantColorUtils.isLightColor(dotColor)) Modifier.border(1.dp, colors.border, CircleShape)
                                                            else Modifier
                                                        )
                                                )
                                                Text(
                                                    text = sugg,
                                                    fontFamily = PlusJakartaSans,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isAdded) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isAdded) colors.violetAccent else colors.textPrimary
                                                )
                                                if (isAdded) {
                                                    Icon(Icons.Default.Check, null, tint = colors.violetAccent, modifier = Modifier.size(13.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                val customVariants = variantsList.filter { v ->
                                    quickColorSuggestions.none { it.equals(v, ignoreCase = true) }
                                }
                                if (customVariants.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        customVariants.forEach { v ->
                                            Surface(
                                                shape = RoundedCornerShape(9999.dp),
                                                color = colors.violetSoftBg,
                                                border = BorderStroke(1.dp, colors.violetAccent)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(v, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.violetAccent)
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Retirer $v",
                                                        tint = colors.violetAccent,
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable { variantsList = variantsList - v }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = colors.cyanSoftBg,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clickable { showAddVariantModal = true }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AddCircle, null, tint = colors.cyanAccent, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Ajouter une variante personnalisée",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.cyanAccent
                                        )
                                    }
                                }
                            }
                        }

                        AddCatalogueLogisticsSection(
                            creationMode = creationMode,
                            stockCountText = stockCountText,
                            onStockCountChange = {
                                stockCountText = it.filter { ch -> ch.isDigit() }
                                if (validationError != null) validationError = null
                            },
                            weightText = weightText,
                            onWeightTextChange = { raw ->
                                val cleaned = raw.replace(',', '.').filter { it.isDigit() || it == '.' }
                                if (cleaned.count { it == '.' } <= 1) weightText = cleaned
                            },
                            selectedFreightMode = selectedFreightMode,
                            onFreightModeChange = { selectedFreightMode = it },
                            defaultAirRate = defaultAirRate,
                            defaultSeaRate = defaultSeaRate,
                            weightKgVal = weightKgVal,
                            weightGrams = weightGrams,
                            volumeM3 = volumeM3,
                            estUnitFreightAr = estUnitFreightAr,
                            colors = colors
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = colors.cardBg,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "5. Plateforme & Notes",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = colors.cyanAccent
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    platformOptions.forEach { plat ->
                                        val isSelected = selectedPlatform.equals(plat, ignoreCase = true)
                                        Surface(
                                            modifier = Modifier.clickable { selectedPlatform = plat },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) colors.cyanSoftBg else colors.innerCardBg,
                                            border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) colors.cyanAccent else colors.border)
                                        ) {
                                            Text(
                                                text = plat,
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) colors.cyanAccent else colors.textPrimary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = notes,
                                    onValueChange = { notes = it },
                                    label = { Text("Notes fournisseur / qualité (optionnel)") },
                                    placeholder = { Text("ex. Lien boutique, MOQ 5 pcs, emballage boîte...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    maxLines = 3
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            if (showAddVariantModal) {
                AddVariantDialog(
                    onDismiss = { showAddVariantModal = false },
                    onConfirm = { newVar ->
                        variantsList = (variantsList + newVar).distinct()
                        showAddVariantModal = false
                    }
                )
            }

            if (showEmojiPickerDialog) {
                ArticleEmojiPickerDialog(
                    currentEmoji = if (!selectedVisual.contains("/")) selectedVisual else "",
                    onDismiss = { showEmojiPickerDialog = false },
                    onSelect = { emoji ->
                        selectedVisual = emoji
                        showEmojiPickerDialog = false
                    }
                )
            }
        }
    }
}
