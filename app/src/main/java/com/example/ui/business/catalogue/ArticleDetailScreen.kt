package com.example.ui.business.catalogue

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.BusinessImageStorage
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.round
import kotlinx.coroutines.launch

@Composable
fun ArticleDetailScreen(
    article: CatalogueArticleSummaryUi,
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    cmupRate: Long = 650L,
    onBackClick: () -> Unit,
    onBuyClick: (BusinessOrderEntity) -> Unit = {},
    onSellClick: (BusinessOrderEntity) -> Unit = {},
    onAdjustStockClick: (BusinessOrderEntity) -> Unit = {},
    onEditArticleClick: (CatalogueArticleSummaryUi) -> Unit = {},
    onToggleHideArticle: (CatalogueArticleSummaryUi) -> Unit = {},
    onDuplicateArticle: (BusinessOrderEntity) -> Unit = {},
    onDeleteArticle: (CatalogueArticleSummaryUi) -> Unit = {},
    onUpdateVisual: (String) -> Unit = {},
    onUpdateVariants: (String) -> Unit = {},
    onLotClick: (BusinessOrderEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val colors = rememberTransitaireColors()
    val scrollState = rememberScrollState()
    val primaryOrder = article.primaryOrder

    val stockTargetLot = remember(article) {
        article.lots.maxByOrNull { it.stockCount } ?: primaryOrder
    }

    var showMenu by remember { mutableStateOf(false) }
    var showAddColorDialog by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { pickedUri ->
            coroutineScope.launch {
                val persisted = BusinessImageStorage.persistPickedPhoto(context, pickedUri)
                if (persisted != null) {
                    onUpdateVisual(persisted)
                    Toast.makeText(context, "Photo de l'article mise à jour", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Impossible d'enregistrer la photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val familyColor = remember(article.familyColorHex, colors) {
        try {
            Color(android.graphics.Color.parseColor(article.familyColorHex))
        } catch (_: Exception) {
            colors.cyanAccent
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }

    val activeCarrier = remember(primaryOrder.carrier, transitaires) {
        transitaires.firstOrNull { !it.isArchived && it.name.equals(primaryOrder.carrier, ignoreCase = true) }
            ?: transitaires.firstOrNull { !it.isArchived && it.isActive }
            ?: transitaires.firstOrNull { !it.isArchived }
    }
    val weightKg = primaryOrder.weightKg ?: 0.0
    val weightGrams = if (weightKg > 0.0) (weightKg * 1000.0).toInt() else 0
    val volumeM3Str = when {
        primaryOrder.volumeM3 != null && primaryOrder.volumeM3 > 0.0 ->
            String.format(Locale.FRENCH, "%.5f", primaryOrder.volumeM3)
        weightKg > 0.0 ->
            String.format(Locale.FRENCH, "%.5f", weightKg * 0.0067)
        else -> "—"
    }
    val unitFreightRealAr = if (primaryOrder.freightCostAr > 0L && primaryOrder.quantityBought > 0) {
        primaryOrder.freightCostAr / primaryOrder.quantityBought
    } else 0L

    val isIndicativeFreight = when {
        primaryOrder.freightMode.contains("EXPRESS", ignoreCase = true) ->
            activeCarrier?.airFastRateAr?.takeIf { it > 0L } == null
        primaryOrder.freightMode.contains("MARITIME", ignoreCase = true) ->
            activeCarrier?.seaRateAr?.takeIf { it > 0L } == null
        else ->
            activeCarrier?.airRateAr?.takeIf { it > 0L } == null
    }
    val freightPerKgAr = when {
        primaryOrder.freightMode.contains("EXPRESS", ignoreCase = true) ->
            activeCarrier?.airFastRateAr?.takeIf { it > 0L } ?: 60000L
        primaryOrder.freightMode.contains("MARITIME", ignoreCase = true) ->
            activeCarrier?.seaRateAr?.takeIf { it > 0L } ?: 18000L
        else ->
            activeCarrier?.airRateAr?.takeIf { it > 0L } ?: 45000L
    }
    val estFreightUnitAr = if (unitFreightRealAr > 0L) {
        unitFreightRealAr
    } else if (weightKg > 0.0) {
        round(weightKg * freightPerKgAr).toLong()
    } else 0L

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.pageBg,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.cardBg,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_back_article_detail")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = colors.textPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fiche Article",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = primaryOrder.referenceCode.ifBlank { "REF-${primaryOrder.id}" },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        DetailStatusBadge(
                            statusTag = if (article.isHidden) "Masqué" else article.statusTag,
                            stockCount = article.totalStockCount,
                            transitCount = article.totalInTransitCount,
                            colors = colors
                        )

                        IconButton(
                            onClick = { onEditArticleClick(article) },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_edit_article_detail")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier l'article",
                                tint = colors.cyanAccent
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_article_options")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options de l'article",
                                    tint = colors.textPrimary
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Modifier la fiche", fontFamily = PlusJakartaSans) },
                                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = colors.cyanAccent) },
                                    onClick = {
                                        showMenu = false
                                        onEditArticleClick(article)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Nouvel achat", fontFamily = PlusJakartaSans) },
                                    leadingIcon = { Icon(Icons.Default.ShoppingCart, null, tint = colors.violetAccent) },
                                    onClick = {
                                        showMenu = false
                                        onBuyClick(primaryOrder)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Dupliquer", fontFamily = PlusJakartaSans) },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = colors.textSecondary) },
                                    onClick = {
                                        showMenu = false
                                        onDuplicateArticle(primaryOrder)
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (article.isHidden) "Réafficher" else "Masquer du catalogue",
                                            fontFamily = PlusJakartaSans
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (article.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = colors.textSecondary
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onToggleHideArticle(article)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Supprimer", fontFamily = PlusJakartaSans, color = colors.dangerAccent) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = colors.dangerAccent) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirm = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWide = maxWidth >= 680.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ArticleHeroVisualCard(
                                article = article,
                                familyColor = familyColor,
                                colors = colors,
                                onPickPhoto = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onPickEmoji = { showEmojiPicker = true },
                                onClearVisual = { onUpdateVisual("") }
                            )
                            ArticleHeaderAndActionsCard(
                                article = article,
                                familyColor = familyColor,
                                colors = colors,
                                onBuy = { onBuyClick(primaryOrder) },
                                onSell = { onSellClick(stockTargetLot) },
                                onAdjust = { onAdjustStockClick(stockTargetLot) }
                            )
                            ArticleStockConsolidatedCard(article = article, colors = colors)
                            ArticleVariantsCard(
                                variants = article.allVariants,
                                colors = colors,
                                onRemoveVariant = { toRemove ->
                                    val updated = article.allVariants.filterNot { it.equals(toRemove, ignoreCase = true) }
                                    onUpdateVariants(updated.joinToString(", "))
                                },
                                onAddVariant = { showAddColorDialog = true }
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ArticleSalesPerformanceCard(
                                article = article,
                                dateFormatter = dateFormatter,
                                colors = colors
                            )
                            ArticleLotsHistoryCard(
                                article = article,
                                cmupRate = cmupRate,
                                dateFormatter = dateFormatter,
                                colors = colors,
                                onLotClick = onLotClick,
                                onNewLotClick = { onBuyClick(primaryOrder) }
                            )
                            ArticleFreightSpecsCard(
                                primaryOrder = primaryOrder,
                                weightGrams = weightGrams,
                                volumeM3Str = volumeM3Str,
                                unitFreightRealAr = unitFreightRealAr,
                                estFreightUnitAr = estFreightUnitAr,
                                freightPerKgAr = freightPerKgAr,
                                activeCarrierName = activeCarrier?.name,
                                isIndicativeFreight = isIndicativeFreight,
                                colors = colors
                            )
                            ArticleSourcingNotesCard(
                                article = article,
                                colors = colors,
                                onEditNotesClick = { onEditArticleClick(article) }
                            )
                        }
                    }
                } else {
                    ArticleHeroVisualCard(
                        article = article,
                        familyColor = familyColor,
                        colors = colors,
                        onPickPhoto = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onPickEmoji = { showEmojiPicker = true },
                        onClearVisual = { onUpdateVisual("") }
                    )
                    ArticleHeaderAndActionsCard(
                        article = article,
                        familyColor = familyColor,
                        colors = colors,
                        onBuy = { onBuyClick(primaryOrder) },
                        onSell = { onSellClick(stockTargetLot) },
                        onAdjust = { onAdjustStockClick(stockTargetLot) }
                    )
                    ArticleStockConsolidatedCard(article = article, colors = colors)
                    ArticleVariantsCard(
                        variants = article.allVariants,
                        colors = colors,
                        onRemoveVariant = { toRemove ->
                            val updated = article.allVariants.filterNot { it.equals(toRemove, ignoreCase = true) }
                            onUpdateVariants(updated.joinToString(", "))
                        },
                        onAddVariant = { showAddColorDialog = true }
                    )
                    ArticleFreightSpecsCard(
                        primaryOrder = primaryOrder,
                        weightGrams = weightGrams,
                        volumeM3Str = volumeM3Str,
                        unitFreightRealAr = unitFreightRealAr,
                        estFreightUnitAr = estFreightUnitAr,
                        freightPerKgAr = freightPerKgAr,
                        activeCarrierName = activeCarrier?.name,
                        isIndicativeFreight = isIndicativeFreight,
                        colors = colors
                    )
                    ArticleLotsHistoryCard(
                        article = article,
                        cmupRate = cmupRate,
                        dateFormatter = dateFormatter,
                        colors = colors,
                        onLotClick = onLotClick,
                        onNewLotClick = { onBuyClick(primaryOrder) }
                    )
                    ArticleSalesPerformanceCard(
                        article = article,
                        dateFormatter = dateFormatter,
                        colors = colors
                    )
                    ArticleSourcingNotesCard(
                        article = article,
                        colors = colors,
                        onEditNotesClick = { onEditArticleClick(article) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAddColorDialog) {
        AddVariantDialog(
            onDismiss = { showAddColorDialog = false },
            onConfirm = { newColor ->
                val updated = (article.allVariants + newColor.trim())
                    .filter { it.isNotBlank() }
                    .distinctBy { it.lowercase() }
                    .joinToString(", ")
                onUpdateVariants(updated)
                showAddColorDialog = false
            }
        )
    }

    if (showEmojiPicker) {
        val currentEmoji = if (primaryOrder.imageUrl.isNotBlank() && !primaryOrder.imageUrl.contains("/")) {
            primaryOrder.imageUrl
        } else {
            article.familyEmoji
        }
        ArticleEmojiPickerDialog(
            currentEmoji = currentEmoji,
            onDismiss = { showEmojiPicker = false },
            onSelect = { chosen ->
                onUpdateVisual(chosen)
                showEmojiPicker = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Supprimer cet article ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = colors.dangerAccent
                )
            },
            text = {
                val lotsSuffix = if (article.lotsCount > 1) " et ses ${article.lotsCount} lots liés" else ""
                Text(
                    text = "Voulez-vous supprimer définitivement « ${primaryOrder.product} »$lotsSuffix ? Cette action est irréversible.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteArticle(article)
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

@Composable
private fun DetailStatusBadge(
    statusTag: String,
    stockCount: Int,
    transitCount: Int,
    colors: TransitaireColors
) {
    val (bg, fg, label) = when (statusTag) {
        "Masqué" -> Triple(colors.badgeNeutralBg, colors.textSecondary, "Masqué")
        "En stock" -> Triple(colors.successSoftBg, colors.successAccent, "$stockCount dispo")
        "Stock faible" -> Triple(colors.expressSoftBg, colors.expressAccent, "$stockCount bas")
        "En transit" -> Triple(colors.violetSoftBg, colors.violetAccent, "$transitCount transit")
        "Référentiel" -> Triple(colors.cyanSoftBg, colors.cyanAccent, "Référentiel")
        else -> Triple(colors.dangerSoftBg, colors.dangerAccent, "Rupture")
    }
    Surface(
        shape = RoundedCornerShape(9999.dp),
        color = bg
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1
        )
    }
}
