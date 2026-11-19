package com.example.ui.business.sheets

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.BusinessUiState
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.shared.BusinessImageStorage
import com.example.ui.business.shared.HumanChipRow
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanPrimaryButton
import com.example.ui.business.shared.HumanSegmentedSwitch
import com.example.ui.business.shared.HumanSheetContainer
import com.example.ui.business.shared.HumanTextField
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.ProductVisualThumb
import com.example.ui.business.shared.VariantChipsEditor
import com.example.ui.business.shared.formatYuan
import com.example.ui.business.shared.parseDoubleInput
import com.example.ui.business.shared.parseLongInput
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
internal fun OrderFormSheet(
    uiState: BusinessUiState,
    editingOrder: BusinessOrderEntity?,
    initialArticle: BusinessOrderEntity?,
    onDismiss: () -> Unit,
    onCreateOrder: (
        product: String,
        supplier: String,
        quantity: Int,
        priceYuan: Double,
        resaleAr: Long,
        category: String,
        carrier: String,
        variants: String,
        freightAr: Long,
        platform: String,
        referenceCode: String,
        family: String,
        domesticShippingYuan: Double,
        orderDateMillis: Long
    ) -> Unit,
    onUpdateOrder: (BusinessOrderEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    val seed = editingOrder ?: initialArticle

    var product by remember { mutableStateOf(seed?.product ?: "") }
    var quantityText by remember { mutableStateOf(if (editingOrder != null) editingOrder.quantityBought.toString() else "10") }
    var priceYuanText by remember {
        mutableStateOf(
            if (seed != null && seed.priceYuan > 0.0) String.format(Locale.US, "%.2f", seed.priceYuan) else ""
        )
    }
    var domesticShippingText by remember {
        mutableStateOf(
            if (editingOrder != null && editingOrder.domesticShippingYuan > 0.0) String.format(Locale.US, "%.2f", editingOrder.domesticShippingYuan) else ""
        )
    }
    var resaleArText by remember {
        mutableStateOf(
            if (seed != null && seed.resalePriceAr > 0L) seed.resalePriceAr.toString() else ""
        )
    }
    var freightArText by remember {
        mutableStateOf(
            if (editingOrder != null && editingOrder.freightCostAr > 0L) editingOrder.freightCostAr.toString() else ""
        )
    }
    var variants by remember { mutableStateOf(seed?.variants ?: "") }
    var referenceCode by remember { mutableStateOf(seed?.referenceCode ?: "") }
    var platform by remember {
        mutableStateOf(seed?.platform?.ifBlank { "1688" } ?: "1688")
    }
    var family by remember {
        mutableStateOf(seed?.family?.ifBlank { "Électronique" } ?: "Électronique")
    }
    var carrier by remember { mutableStateOf(editingOrder?.carrier ?: "") }
    var category by remember { mutableStateOf(editingOrder?.category ?: "BUSINESS") }
    var dateText by remember {
        mutableStateOf(DateUtils.formatShortDate(editingOrder?.orderDate ?: System.currentTimeMillis()))
    }
    var showProductSuggestions by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val matchingArticles = remember(product, uiState.allCatalogueArticles) {
        val q = product.trim()
        val source = uiState.allCatalogueArticles.filter { !it.isHidden }
        if (q.isBlank()) {
            source.take(5)
        } else {
            source.filter { art ->
                art.primaryOrder.product.contains(q, ignoreCase = true) ||
                    art.primaryOrder.referenceCode.contains(q, ignoreCase = true) ||
                    art.primaryOrder.family.contains(q, ignoreCase = true)
            }.take(6)
        }
    }

    val platformOptions = remember(uiState.cataloguePlatforms) {
        val list = uiState.cataloguePlatforms.filter { it.isActive }.map { it.name }.ifEmpty {
            listOf("1688", "Taobao", "Pinduoduo", "WeChat")
        }
        if (platform !in list) (list + platform) else list
    }
    val familyOptions = remember(uiState.catalogueFamilies) {
        val list = uiState.catalogueFamilies.map { it.name }.ifEmpty {
            listOf("Électronique", "Mode", "Maison", "Beauté", "Divers")
        }
        if (family !in list) (list + family) else list
    }
    val carrierOptions = remember(uiState.catalogueTransitaires) {
        uiState.catalogueTransitaires.filter { !it.isArchived }.map { it.name }
    }

    HumanSheetContainer(
        title = if (editingOrder != null) "Modifier le lot" else "Nouvel achat Chine",
        subtitle = if (editingOrder != null) editingOrder.lotNumber.ifBlank { "Lot #${editingOrder.id}" } else "Enregistrez un lot à suivre jusqu'à Madagascar",
        onDismiss = onDismiss,
        colors = colors
    ) {
        HumanTextField(
            value = product,
            onValueChange = {
                product = it
                errorText = null
                showProductSuggestions = uiState.allCatalogueArticles.isNotEmpty()
            },
            label = "Nom de l'article *",
            placeholder = "Tapez pour rechercher ou créer...",
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (product.isNotBlank()) {
                    IconButton(onClick = {
                        product = ""
                        showProductSuggestions = uiState.allCatalogueArticles.isNotEmpty()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Effacer", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                    }
                } else if (uiState.allCatalogueArticles.isNotEmpty()) {
                    IconButton(onClick = { showProductSuggestions = !showProductSuggestions }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Suggestions catalogue", tint = colors.cyanAccent)
                    }
                }
            },
            colors = colors,
            modifier = Modifier.testTag("input_order_product")
        )

        AnimatedVisibility(visible = showProductSuggestions && matchingArticles.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.innerCardBg,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Suggestions du catalogue",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "Fermer",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = colors.cyanAccent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showProductSuggestions = false }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    matchingArticles.forEachIndexed { idx, art ->
                        if (idx > 0) HumanHairline(colors)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    product = art.primaryOrder.product
                                    if (art.latestPriceYuan > 0.0) {
                                        priceYuanText = String.format(Locale.US, "%.2f", art.latestPriceYuan)
                                    }
                                    if (art.resalePriceAr > 0L) {
                                        resaleArText = art.resalePriceAr.toString()
                                    }
                                    if (art.primaryOrder.family.isNotBlank()) family = art.primaryOrder.family
                                    if (art.primaryOrder.platform.isNotBlank()) platform = art.primaryOrder.platform
                                    if (art.primaryOrder.variants.isNotBlank()) variants = art.primaryOrder.variants
                                    if (art.primaryOrder.referenceCode.isNotBlank()) referenceCode = art.primaryOrder.referenceCode
                                    showProductSuggestions = false
                                    errorText = null
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductVisualThumb(
                                imageUrl = art.primaryOrder.imageUrl,
                                productName = art.primaryOrder.product,
                                family = art.primaryOrder.family,
                                colors = colors,
                                size = 38.dp,
                                cornerRadius = 10.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = art.primaryOrder.product,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${art.primaryOrder.family} · Stock ${art.totalStockCount} pcs",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (art.latestPriceYuan > 0.0) {
                                    Text(
                                        text = formatYuan(art.latestPriceYuan),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = colors.cyanAccent
                                    )
                                }
                                if (art.resalePriceAr > 0L) {
                                    Text(
                                        text = CurrencyFormatter.formatCompactAriary(art.resalePriceAr),
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
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HumanTextField(
                value = quantityText,
                onValueChange = { quantityText = it.filter { c -> c.isDigit() }; errorText = null },
                label = "Quantité *",
                keyboardType = KeyboardType.Number,
                suffixText = "pcs",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            HumanTextField(
                value = priceYuanText,
                onValueChange = { priceYuanText = it; errorText = null },
                label = "Prix unitaire (¥)",
                keyboardType = KeyboardType.Decimal,
                suffixText = "¥",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HumanTextField(
                value = domesticShippingText,
                onValueChange = { domesticShippingText = it },
                label = "Port Chine (¥)",
                keyboardType = KeyboardType.Decimal,
                suffixText = "¥",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            HumanTextField(
                value = resaleArText,
                onValueChange = { resaleArText = it.filter { c -> c.isDigit() } },
                label = "Prix revente cible",
                keyboardType = KeyboardType.Number,
                suffixText = "Ar",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }

        Text("Plateforme d'achat", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
        HumanChipRow(
            items = platformOptions.map { it to it },
            selectedKey = platform,
            onSelect = { platform = it },
            colors = colors
        )

        Text("Famille", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
        HumanChipRow(
            items = familyOptions.map { it to it },
            selectedKey = family,
            onSelect = { family = it },
            colors = colors
        )

        if (carrierOptions.isNotEmpty()) {
            Text("Transitaire", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
            HumanChipRow(
                items = (listOf("") + carrierOptions).map { it to it.ifBlank { "À choisir plus tard" } },
                selectedKey = carrier,
                onSelect = { carrier = it },
                colors = colors
            )
        }

        HumanSegmentedSwitch(
            options = listOf("BUSINESS" to "Business", "FAMILY_FRIENDS" to "Proches", "PERSONAL" to "Perso"),
            selectedKey = category,
            onSelect = { category = it },
            colors = colors
        )

        VariantChipsEditor(
            variantsText = variants,
            onVariantsChange = { variants = it },
            label = "Variantes",
            placeholder = "Ajouter une puce (ex : Noir, 5 Blanc)",
            colors = colors
        )

        HumanTextField(
            value = dateText,
            onValueChange = { dateText = it.filter { c -> c.isDigit() || c == '/' }.take(10) },
            label = "Date (jj/mm/aaaa)",
            colors = colors
        )

        val qty = quantityText.toIntOrNull() ?: 0
        val unitY = parseDoubleInput(priceYuanText) ?: 0.0
        val domY = parseDoubleInput(domesticShippingText) ?: 0.0
        val totalY = (qty * unitY) + domY
        if (totalY > 0.0) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.cyanSoftBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total fournisseur", fontFamily = Inter, fontSize = 13.sp, color = colors.textPrimary)
                    Text(
                        text = "${formatYuan(totalY)} (~${CurrencyFormatter.formatCompactAriary((totalY * uiState.cmupRate.coerceAtLeast(1L)).toLong())})",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.cyanAccent
                    )
                }
            }
        }

        if (!errorText.isNullOrBlank()) {
            Text(text = errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        }

        HumanPrimaryButton(
            text = if (editingOrder != null) "Enregistrer les modifications" else "Créer la commande",
            icon = Icons.Default.Check,
            onClick = {
                val cleanName = product.trim()
                val parsedQty = quantityText.toIntOrNull() ?: 0
                if (cleanName.isBlank()) {
                    errorText = "Veuillez saisir le nom de l'article."
                    return@HumanPrimaryButton
                }
                if (parsedQty <= 0) {
                    errorText = "La quantité doit être supérieure à 0."
                    return@HumanPrimaryButton
                }
                val parsedYuan = parseDoubleInput(priceYuanText) ?: 0.0
                val parsedDom = parseDoubleInput(domesticShippingText) ?: 0.0
                val parsedResale = parseLongInput(resaleArText) ?: 0L
                val parsedFreight = parseLongInput(freightArText) ?: 0L
                val parsedDate = if (DateUtils.isValidFrenchDate(dateText)) {
                    DateUtils.frenchDateToEpochMillis(dateText)
                } else {
                    editingOrder?.orderDate ?: System.currentTimeMillis()
                }

                if (editingOrder != null) {
                    onUpdateOrder(
                        editingOrder.copy(
                            product = cleanName,
                            quantityBought = parsedQty,
                            priceYuan = parsedYuan,
                            domesticShippingYuan = parsedDom,
                            resalePriceAr = parsedResale,
                            freightCostAr = parsedFreight,
                            variants = variants.trim(),
                            referenceCode = referenceCode.trim(),
                            platform = platform,
                            family = family,
                            carrier = carrier,
                            category = category,
                            orderDate = parsedDate
                        )
                    )
                } else {
                    onCreateOrder(
                        cleanName,
                        platform,
                        parsedQty,
                        parsedYuan,
                        parsedResale,
                        category,
                        carrier,
                        variants.trim(),
                        parsedFreight,
                        platform,
                        referenceCode.trim(),
                        family,
                        parsedDom,
                        parsedDate
                    )
                }
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_order")
        )
    }
}

@Composable
internal fun AdjustStockSheet(
    order: BusinessOrderEntity,
    onDismiss: () -> Unit,
    onConfirm: (stockCount: Int, reservedCount: Int, defectiveCount: Int, assignedClient: String) -> Unit
) {
    val colors = rememberTransitaireColors()
    var stockCount by remember(order.id) { mutableIntStateOf(order.stockCount) }
    var reservedCount by remember(order.id) { mutableIntStateOf(order.reservedCount) }
    var defectiveCount by remember(order.id) { mutableIntStateOf(order.defectiveCount) }
    var assignedClient by remember(order.id) { mutableStateOf(order.assignedClient) }

    HumanSheetContainer(
        title = "Ajuster le stock",
        subtitle = order.product,
        onDismiss = onDismiss,
        colors = colors
    ) {
        StockAdjustRow("Disponible en vente", stockCount, { if (stockCount > 0) stockCount-- }, { stockCount++ })
        StockAdjustRow("Réservé client", reservedCount, { if (reservedCount > 0) reservedCount-- }, { reservedCount++ })
        StockAdjustRow("Défectueux / SAV", defectiveCount, { if (defectiveCount > 0) defectiveCount-- }, { defectiveCount++ })

        HumanTextField(
            value = assignedClient,
            onValueChange = { assignedClient = it },
            label = "Client réservataire (optionnel)",
            placeholder = "Nom du client",
            colors = colors
        )

        HumanPrimaryButton(
            text = "Enregistrer le stock",
            icon = Icons.Default.Check,
            onClick = {
                onConfirm(stockCount, reservedCount, defectiveCount, assignedClient.trim())
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_stock_adjust")
        )
    }
}

@Composable
private fun StockAdjustRow(
    label: String,
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    val colors = rememberTransitaireColors()
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.innerCardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = colors.textPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onMinus, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.Remove, "Diminuer", tint = colors.textPrimary)
                }
                Text(
                    text = "$value",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colors.cyanAccent
                )
                IconButton(onClick = onPlus, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.Add, "Augmenter", tint = colors.textPrimary)
                }
            }
        }
    }
}

@Composable
internal fun ArticleFormSheet(
    uiState: BusinessUiState,
    editingArticle: CatalogueArticleSummaryUi?,
    onDismiss: () -> Unit,
    onAddArticle: (
        product: String,
        family: String,
        referenceCode: String,
        variants: String,
        priceYuan: Double,
        resaleAr: Long,
        stockCount: Int,
        weightKg: Double?,
        platform: String,
        imageUrl: String,
        freightMode: String,
        notes: String
    ) -> Unit,
    onUpdateArticle: (
        article: CatalogueArticleSummaryUi,
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
    val colors = rememberTransitaireColors()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val seed = editingArticle?.primaryOrder

    var product by remember { mutableStateOf(seed?.product ?: "") }
    var sku by remember { mutableStateOf(seed?.referenceCode ?: "") }
    var family by remember { mutableStateOf(seed?.family?.ifBlank { "Électronique" } ?: "Électronique") }
    var platform by remember { mutableStateOf(seed?.platform?.ifBlank { "1688" } ?: "1688") }
    var priceYuanText by remember {
        mutableStateOf(
            if (editingArticle != null && editingArticle.latestPriceYuan > 0.0) String.format(Locale.US, "%.2f", editingArticle.latestPriceYuan)
            else ""
        )
    }
    var resaleArText by remember {
        mutableStateOf(
            if (editingArticle != null && editingArticle.resalePriceAr > 0L) editingArticle.resalePriceAr.toString()
            else ""
        )
    }
    var stockText by remember { mutableStateOf(if (editingArticle == null) "0" else "") }
    var weightText by remember {
        mutableStateOf(seed?.weightKg?.takeIf { it > 0.0 }?.let { String.format(Locale.US, "%.2f", it) } ?: "")
    }
    var variants by remember { mutableStateOf(seed?.variants ?: "") }
    var imageUrl by remember { mutableStateOf(seed?.imageUrl ?: "") }
    var notes by remember { mutableStateOf(seed?.receptionNotes ?: "") }
    var freightMode by remember { mutableStateOf(seed?.freightMode?.ifBlank { "AÉRIEN" } ?: "AÉRIEN") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val saved = BusinessImageStorage.persistPickedPhoto(context, uri)
                if (!saved.isNullOrBlank()) imageUrl = saved
            }
        }
    }

    val familyOptions = remember(uiState.catalogueFamilies) {
        val list = uiState.catalogueFamilies.map { it.name }.ifEmpty {
            listOf("Électronique", "Mode", "Maison", "Beauté", "Divers")
        }
        if (family !in list) (list + family) else list
    }

    HumanSheetContainer(
        title = if (editingArticle != null) "Modifier l'article" else "Nouvel article catalogue",
        subtitle = "Fiche produit réutilisable pour vos achats et ventes",
        onDismiss = onDismiss,
        colors = colors
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                .padding(vertical = 4.dp)
        ) {
            ProductVisualThumb(
                imageUrl = imageUrl,
                productName = product.ifBlank { "Article" },
                family = family,
                colors = colors,
                size = 60.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Photo du produit", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
                Text("Touchez pour choisir une image", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
            }
            HumanTonalButton(
                text = "Photo",
                icon = Icons.Default.AddPhotoAlternate,
                onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                colors = colors
            )
        }

        HumanTextField(
            value = product,
            onValueChange = { product = it; errorText = null },
            label = "Nom du produit *",
            placeholder = "Ex : Montre connectée S9",
            colors = colors
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HumanTextField(
                value = sku,
                onValueChange = { sku = it },
                label = "Référence / SKU",
                placeholder = "Auto si vide",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            if (editingArticle == null) {
                HumanTextField(
                    value = stockText,
                    onValueChange = { stockText = it.filter { c -> c.isDigit() } },
                    label = "Stock initial",
                    keyboardType = KeyboardType.Number,
                    suffixText = "pcs",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HumanTextField(
                value = priceYuanText,
                onValueChange = { priceYuanText = it },
                label = "Coût usine (¥)",
                keyboardType = KeyboardType.Decimal,
                suffixText = "¥",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            HumanTextField(
                value = resaleArText,
                onValueChange = { resaleArText = it.filter { c -> c.isDigit() } },
                label = "Prix revente (Ar)",
                keyboardType = KeyboardType.Number,
                suffixText = "Ar",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }

        Text("Famille", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
        HumanChipRow(
            items = familyOptions.map { it to it },
            selectedKey = family,
            onSelect = { family = it },
            colors = colors
        )

        VariantChipsEditor(
            variantsText = variants,
            onVariantsChange = { variants = it },
            label = "Variantes",
            placeholder = "Ajouter une puce (ex : Noir, Argent)",
            colors = colors
        )

        HumanTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = "Poids unit.",
            placeholder = "Optionnel",
            keyboardType = KeyboardType.Decimal,
            suffixText = "kg",
            colors = colors
        )

        HumanTextField(
            value = notes,
            onValueChange = { notes = it },
            label = "Notes fournisseur / lien",
            singleLine = false,
            colors = colors
        )

        if (!errorText.isNullOrBlank()) {
            Text(errorText!!, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
        }

        Spacer(modifier = Modifier.height(4.dp))
        HumanPrimaryButton(
            text = if (editingArticle != null) "Enregistrer l'article" else "Ajouter au catalogue",
            icon = Icons.Default.Check,
            onClick = {
                val cleanName = product.trim()
                if (cleanName.isBlank()) {
                    errorText = "Le nom de l'article est requis."
                    return@HumanPrimaryButton
                }
                val parsedYuan = parseDoubleInput(priceYuanText) ?: 0.0
                val parsedResale = parseLongInput(resaleArText) ?: 0L
                val parsedWeight = parseDoubleInput(weightText)
                val parsedStock = stockText.toIntOrNull() ?: 0

                if (editingArticle != null) {
                    onUpdateArticle(
                        editingArticle,
                        cleanName,
                        sku.trim(),
                        family,
                        platform,
                        parsedYuan,
                        parsedResale,
                        parsedWeight,
                        freightMode,
                        variants.trim(),
                        imageUrl,
                        notes.trim(),
                        editingArticle.isHidden
                    )
                } else {
                    onAddArticle(
                        cleanName,
                        family,
                        sku.trim(),
                        variants.trim(),
                        parsedYuan,
                        parsedResale,
                        parsedStock,
                        parsedWeight,
                        platform,
                        imageUrl,
                        freightMode,
                        notes.trim()
                    )
                }
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_article")
        )
    }
}
