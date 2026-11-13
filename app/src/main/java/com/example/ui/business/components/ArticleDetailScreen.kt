package com.example.ui.business.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

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
    val colors = rememberTransitaireColors()
    val scrollState = rememberScrollState()
    val primaryOrder = article.primaryOrder

    // Lot le plus pertinent pour Vendre / Ajuster (lot avec le plus de stock disponible ou primaire)
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
        uri?.let {
            onUpdateVisual(it.toString())
            Toast.makeText(context, "Photo de l'article mise à jour", Toast.LENGTH_SHORT).show()
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

    // Estimations de fret basées sur les transitaires actifs
    val activeCarrier = remember(primaryOrder.carrier, transitaires) {
        transitaires.firstOrNull { !it.isArchived && it.name.equals(primaryOrder.carrier, ignoreCase = true) }
            ?: transitaires.firstOrNull { !it.isArchived && it.isActive }
            ?: transitaires.firstOrNull { !it.isArchived }
    }
    val weightKg = primaryOrder.weightKg ?: 0.0
    val weightGrams = if (weightKg > 0.0) (weightKg * 1000.0).toInt() else 0
    val volumeM3Str = when {
        primaryOrder.volumeM3 != null && primaryOrder.volumeM3 > 0.0 ->
            String.format(Locale.US, "%.4f", primaryOrder.volumeM3)
        weightKg > 0.0 ->
            String.format(Locale.US, "%.4f", weightKg * 0.0067)
        else -> "—"
    }
    val unitFreightRealAr = if (primaryOrder.freightCostAr > 0L && primaryOrder.quantityBought > 0) {
        primaryOrder.freightCostAr / primaryOrder.quantityBought
    } else 0L

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
private fun ArticleHeroVisualCard(
    article: CatalogueArticleSummaryUi,
    familyColor: Color,
    colors: TransitaireColors,
    onPickPhoto: () -> Unit,
    onPickEmoji: () -> Unit,
    onClearVisual: () -> Unit
) {
    val primaryOrder = article.primaryOrder
    val imageUrl = primaryOrder.imageUrl
    val isPhoto = imageUrl.startsWith("content://") || imageUrl.startsWith("file://") || imageUrl.startsWith("http")
    val displayEmoji = if (imageUrl.isNotBlank() && !imageUrl.contains("/")) imageUrl else article.familyEmoji

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(216.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        familyColor.copy(alpha = 0.18f),
                        colors.innerCardBg
                    )
                )
            )
            .border(1.dp, colors.border, RoundedCornerShape(22.dp))
            .testTag("article_detail_carousel")
    ) {
        if (isPhoto) {
            AsyncImage(
                model = imageUrl,
                contentDescription = primaryOrder.product,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = displayEmoji,
                    fontSize = 68.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = primaryOrder.family.ifBlank { "Catalogue" },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }

        // Badges haut gauche
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = if (article.isHidden) colors.badgeNeutralBg else CyanPrimary.copy(alpha = 0.94f)
            ) {
                Text(
                    text = if (article.isHidden) "Masqué" else "Actif",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (article.isHidden) colors.textPrimary else Color.Black,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            if (article.lotsCount > 1) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.cardBg.copy(alpha = 0.92f)
                ) {
                    Text(
                        text = "${article.lotsCount} lots",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.violetAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Actions visuel bas gauche
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = Color.Black.copy(alpha = 0.68f),
                modifier = Modifier.clickable(onClick = onPickPhoto)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(
                        text = if (isPhoto) "Changer photo" else "Photo",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = Color.Black.copy(alpha = 0.68f),
                modifier = Modifier.clickable(onClick = onPickEmoji)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.EmojiEmotions, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Émoji",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }

            if (imageUrl.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = Color.Black.copy(alpha = 0.68f),
                    modifier = Modifier.clickable(onClick = onClearVisual)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Text(
                            text = "Retirer",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleHeaderAndActionsCard(
    article: CatalogueArticleSummaryUi,
    familyColor: Color,
    colors: TransitaireColors,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    onAdjust: () -> Unit
) {
    val primaryOrder = article.primaryOrder
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = familyColor.copy(alpha = 0.14f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = article.familyEmoji, fontSize = 13.sp)
                        Text(
                            text = primaryOrder.family.ifBlank { "Sans famille" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = familyColor
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.innerCardBg
                ) {
                    Text(
                        text = primaryOrder.platform.ifBlank { primaryOrder.supplier.ifBlank { "1688" } },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (article.isReferenceOnly) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.cyanSoftBg
                    ) {
                        Text(
                            text = "Référentiel",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.cyanAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = primaryOrder.product.ifBlank { "Article #${primaryOrder.id}" },
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = colors.textPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (article.resalePriceAr > 0L) CurrencyFormatter.formatAriary(article.resalePriceAr) else "Prix de vente à définir",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = colors.cyanAccent
                )
                if (article.latestPriceYuan > 0.0) {
                    val yuanLabel = if (article.latestPriceYuan % 1.0 == 0.0) {
                        "¥ ${article.latestPriceYuan.toInt()}"
                    } else {
                        String.format(Locale.US, "¥ %.2f", article.latestPriceYuan)
                    }
                    Text(
                        text = "Achat : $yuanLabel",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        // Barre d'actions rapides 48dp+
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBuy,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_article_buy"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Acheter", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
            }

            Button(
                onClick = onSell,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_article_sell"),
                colors = ButtonDefaults.buttonColors(containerColor = colors.violetSoftBg, contentColor = colors.violetAccent),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.PointOfSale, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Vendre", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
            }

            Button(
                onClick = onAdjust,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_article_adjust"),
                colors = ButtonDefaults.buttonColors(containerColor = colors.badgeNeutralBg, contentColor = colors.textPrimary),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Tune, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Ajuster", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun ArticleStockConsolidatedCard(
    article: CatalogueArticleSummaryUi,
    colors: TransitaireColors
) {
    val totalStock = article.totalStockCount
    val reserved = article.totalReservedCount
    val inTransit = article.totalInTransitCount
    val defective = article.totalDefectiveCount
    val totalInventory = totalStock + reserved + inTransit + defective

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Inventory2, null, tint = colors.cyanAccent, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "État des Stocks consolidé",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Total suivi : $totalInventory unité(s) • ${article.lotsCount} lot(s)",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // 2x2 Grid pour lisibilité parfaite sur Xiaomi 14 et tablettes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StockMiniBox(
                    label = "Disponible",
                    value = totalStock,
                    subtitle = "En rayon",
                    accent = colors.successAccent,
                    bg = colors.innerCardBg,
                    modifier = Modifier.weight(1f)
                )
                StockMiniBox(
                    label = "En transit",
                    value = inTransit,
                    subtitle = "Chine → Mada",
                    accent = colors.violetAccent,
                    bg = colors.innerCardBg,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StockMiniBox(
                    label = "Réservé",
                    value = reserved,
                    subtitle = "Clients",
                    accent = colors.cyanAccent,
                    bg = colors.innerCardBg,
                    modifier = Modifier.weight(1f)
                )
                StockMiniBox(
                    label = "Défectueux",
                    value = defective,
                    subtitle = "SAV / Perte",
                    accent = colors.dangerAccent,
                    bg = colors.innerCardBg,
                    modifier = Modifier.weight(1f)
                )
            }

            // Barre de répartition corrigée : uniquement les segments > 0
            if (totalInventory > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(colors.badgeNeutralBg)
                    ) {
                        if (totalStock > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(totalStock.toFloat())
                                    .fillMaxHeight()
                                    .background(colors.successAccent)
                            )
                        }
                        if (inTransit > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(inTransit.toFloat())
                                    .fillMaxHeight()
                                    .background(colors.violetAccent)
                            )
                        }
                        if (reserved > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(reserved.toFloat())
                                    .fillMaxHeight()
                                    .background(colors.cyanAccent)
                            )
                        }
                        if (defective > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(defective.toFloat())
                                    .fillMaxHeight()
                                    .background(colors.dangerAccent)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val pctDispo = (totalStock * 100) / totalInventory
                        val pctTransit = (inTransit * 100) / totalInventory
                        val pctRes = (reserved * 100) / totalInventory
                        val pctDef = (defective * 100) / totalInventory
                        Text("$pctDispo% Dispo", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                        Text("$pctTransit% Transit", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                        Text("$pctRes% Rés.", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                        Text("$pctDef% Déf.", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Aucune unité en stock ni en transit pour cet article.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun StockMiniBox(
    label: String,
    value: Int,
    subtitle: String,
    accent: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(subtitle, fontFamily = Inter, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            }
            Text(
                text = "$value",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = accent
            )
        }
    }
}

@Composable
private fun ArticleVariantsCard(
    variants: List<String>,
    colors: TransitaireColors,
    onRemoveVariant: (String) -> Unit,
    onAddVariant: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.violetSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Palette, null, tint = colors.violetAccent, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "Déclinaisons & Couleurs",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (variants.isNotEmpty()) "${variants.size} déclinaison(s)" else "Aucune variante configurée",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            if (variants.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    variants.forEachIndexed { idx, varText ->
                        val dotColor = VariantColorUtils.getVariantColor(varText, idx)
                        val isLight = VariantColorUtils.isLightColor(dotColor)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.innerCardBg)
                                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                        .then(
                                            if (isLight) Modifier.border(1.dp, colors.border, CircleShape)
                                            else Modifier
                                        )
                                )
                                Text(
                                    text = varText,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                            }
                            IconButton(
                                onClick = { onRemoveVariant(varText) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Retirer $varText",
                                    tint = colors.dangerAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Aucune couleur ou taille spécifique ajoutée.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.cyanSoftBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddCircle, null, tint = colors.cyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ajouter une couleur / variante",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.cyanAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleFreightSpecsCard(
    primaryOrder: BusinessOrderEntity,
    weightGrams: Int,
    volumeM3Str: String,
    unitFreightRealAr: Long,
    estFreightUnitAr: Long,
    freightPerKgAr: Long,
    activeCarrierName: String?,
    colors: TransitaireColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.expressSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalShipping, null, tint = colors.expressAccent, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "Spécifications & Fret",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Mode : ${primaryOrder.freightMode.ifBlank { "AÉRIEN" }}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Poids unitaire", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = if (weightGrams > 0) "$weightGrams g" else "Non renseigné",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Volume estimé", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = if (volumeM3Str != "—") "$volumeM3Str m³" else "Non renseigné",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.expressSoftBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Flight, null, tint = colors.expressAccent, modifier = Modifier.size(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (unitFreightRealAr > 0L) "Fret réel sur le dernier lot" else "Estimation fret transitaire",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.expressAccent
                        )
                        val desc = when {
                            unitFreightRealAr > 0L ->
                                "${CurrencyFormatter.formatAriary(unitFreightRealAr)} / unité (lot : ${CurrencyFormatter.formatAriary(primaryOrder.freightCostAr)})"
                            estFreightUnitAr > 0L -> {
                                val carrierInfo = activeCarrierName?.let { "$it · " } ?: ""
                                "≈ ${CurrencyFormatter.formatAriary(estFreightUnitAr)} / unité ($carrierInfo${CurrencyFormatter.formatAriary(freightPerKgAr)}/kg)"
                            }
                            else -> "Renseignez le poids unitaire dans Modifier pour estimer le fret."
                        }
                        Text(
                            text = desc,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleLotsHistoryCard(
    article: CatalogueArticleSummaryUi,
    cmupRate: Long,
    dateFormatter: SimpleDateFormat,
    colors: TransitaireColors,
    onLotClick: (BusinessOrderEntity) -> Unit,
    onNewLotClick: () -> Unit
) {
    val sortedLots = remember(article.lots) {
        article.lots.sortedByDescending { it.orderDate }
    }
    val effectiveRate = if (cmupRate > 0L) cmupRate.toDouble() else 650.0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.TrendingDown, null, tint = colors.cyanAccent, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "Historique des Lots (${sortedLots.size})",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${article.totalBoughtCount} pcs achetées au total",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.cyanSoftBg,
                    modifier = Modifier.clickable(onClick = onNewLotClick)
                ) {
                    Text(
                        text = "+ Lot",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.cyanAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (sortedLots.isEmpty() || (sortedLots.size == 1 && sortedLots.first().quantityBought == 0 && sortedLots.first().status == "CATALOGUE")) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Fiche référentiel sans lot acheté. Utilisez « Acheter » pour lancer votre premier approvisionnement.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sortedLots.forEach { lot ->
                        val lotUnitCostAr = when {
                            lot.unitCostAr > 0L -> lot.unitCostAr
                            lot.priceYuan > 0.0 -> {
                                val rate = if (lot.exchangeRate > 0.0) lot.exchangeRate else effectiveRate
                                val unitFreight = if (lot.freightCostAr > 0L && lot.quantityBought > 0) lot.freightCostAr / lot.quantityBought else 0L
                                round(lot.priceYuan * rate).toLong() + unitFreight
                            }
                            else -> 0L
                        }
                        val statusLabel = translateLotStatus(lot.status)
                        val yuanStr = if (lot.priceYuan > 0.0) {
                            if (lot.priceYuan % 1.0 == 0.0) "¥ ${lot.priceYuan.toInt()}" else String.format(Locale.US, "¥ %.2f", lot.priceYuan)
                        } else "—"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.innerCardBg)
                                .clickable { onLotClick(lot) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.ReceiptLong, null, tint = colors.cyanAccent, modifier = Modifier.size(18.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = lot.lotNumber.ifBlank { "Lot #${lot.id}" },
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.textPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = colors.badgeNeutralBg
                                        ) {
                                            Text(
                                                text = statusLabel,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp,
                                                color = colors.textSecondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${dateFormatter.format(Date(lot.orderDate))} • ${lot.quantityBought} ach. / ${lot.stockCount} dispo",
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                    if (lot.carrier.isNotBlank() || lot.platform.isNotBlank()) {
                                        Text(
                                            text = listOf(lot.platform, lot.carrier).filter { it.isNotBlank() }.joinToString(" · "),
                                            fontFamily = Inter,
                                            fontSize = 10.sp,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (lotUnitCostAr > 0L) CurrencyFormatter.formatAriary(lotUnitCostAr) else "—",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "$yuanStr / u",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Ouvrir le suivi du lot",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
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
private fun ArticleSalesPerformanceCard(
    article: CatalogueArticleSummaryUi,
    dateFormatter: SimpleDateFormat,
    colors: TransitaireColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QueryStats, null, tint = colors.violetAccent, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "Performances & Ventes",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${article.salesHistory.size} transaction(s) enregistrée(s)",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                if (article.resalePriceAr > 0L && article.latestUnitCostAr > 0L) {
                    val mColor = if (article.marginPercent >= 20) colors.successAccent else colors.violetAccent
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = mColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "Marge ${article.marginPercent}%",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = mColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Vendues", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = "${article.totalSoldCount} pcs",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("CA Réalisé", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = CurrencyFormatter.formatAriary(article.totalRevenueAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = colors.cyanAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Profit net", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        val profitColor = if (article.realProfitAr >= 0L) colors.successAccent else colors.dangerAccent
                        Text(
                            text = CurrencyFormatter.formatAriary(article.realProfitAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = profitColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (article.salesHistory.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "DERNIÈRES VENTES",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    article.salesHistory.take(5).forEach { sale ->
                        SaleHistoryRow(sale = sale, dateFormatter = dateFormatter, colors = colors)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Aucune vente enregistrée pour cet article pour le moment.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SaleHistoryRow(
    sale: BusinessSaleEntity,
    dateFormatter: SimpleDateFormat,
    colors: TransitaireColors
) {
    val totalAr = if (sale.totalPriceAr > 0L) sale.totalPriceAr else sale.unitResalePriceAr * sale.quantity
    val isPaid = sale.paidAmountAr >= totalAr && totalAr > 0L
    val isPartial = sale.paidAmountAr in 1L until totalAr

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.innerCardBg)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = sale.clientName.ifBlank { "Client comptoir" },
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${dateFormatter.format(Date(sale.saleDate))} • ${sale.quantity} pc(s)",
                fontFamily = Inter,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = CurrencyFormatter.formatAriary(totalAr),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.textPrimary
            )
            val (badgeBg, badgeFg, badgeText) = when {
                isPaid -> Triple(colors.successSoftBg, colors.successAccent, "Réglée")
                isPartial -> Triple(colors.expressSoftBg, colors.expressAccent, "Partielle")
                else -> Triple(colors.dangerSoftBg, colors.dangerAccent, "Impayée")
            }
            Surface(shape = RoundedCornerShape(6.dp), color = badgeBg) {
                Text(
                    text = badgeText,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = badgeFg,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ArticleSourcingNotesCard(
    article: CatalogueArticleSummaryUi,
    colors: TransitaireColors,
    onEditNotesClick: () -> Unit
) {
    val primaryOrder = article.primaryOrder
    val recordedPlatform = primaryOrder.platform.ifBlank { primaryOrder.supplier.ifBlank { "Non renseignée" } }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.badgeNeutralBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storefront, null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "Sourcing & Notes",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Fournisseur, transitaire et remarques",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                TextButton(onClick = onEditNotesClick) {
                    Text("Modifier", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = colors.cyanAccent)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.innerCardBg)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Plateforme / Fournisseur",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = recordedPlatform,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transitaire",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = primaryOrder.carrier.ifBlank { "Non assigné" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.cyanAccent
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.innerCardBg)
                    .padding(12.dp)
            ) {
                Text(
                    text = primaryOrder.receptionNotes.ifBlank {
                        "Aucune note ajoutée. Appuyez sur Modifier pour noter un lien 1688, un contact usine ou des instructions qualité."
                    },
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = if (primaryOrder.receptionNotes.isNotBlank()) colors.textPrimary else colors.textSecondary
                )
            }
        }
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

private fun translateLotStatus(status: String): String {
    return when (status) {
        "DRAFT" -> "Brouillon"
        "ORDERED" -> "Commandé"
        "WAREHOUSE" -> "Entrepôt"
        "GUANGZHOU" -> "Guangzhou"
        "FLIGHT" -> "En transit"
        "CUSTOMS" -> "Douane"
        "QUALITY_CONTROL" -> "Contrôle"
        "DELIVERY_READY" -> "En stock"
        "SOLD" -> "Soldé"
        "CATALOGUE" -> "Référentiel"
        else -> status
    }
}

@Composable
fun AddVariantDialog(
    onDismiss: () -> Unit,
    onConfirm: (variantName: String) -> Unit
) {
    var variantName by remember { mutableStateOf("") }
    val colorSuggestions = listOf("Noir", "Blanc", "Gris", "Rose", "Bleu Nuit", "Rouge", "Vert Émeraude", "Violet", "Or", "Argent")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ajouter une couleur / déclinaison",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = variantName,
                    onValueChange = { variantName = it },
                    label = { Text("Nom de la couleur / variante") },
                    placeholder = { Text("ex. Bleu Nuit, Noir Mat...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text(
                    text = "Suggestions rapides :",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    colorSuggestions.forEach { colorStr ->
                        val suggColor = VariantColorUtils.getVariantColor(colorStr)
                        val isLight = VariantColorUtils.isLightColor(suggColor)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { variantName = colorStr }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(suggColor)
                                        .then(
                                            if (isLight) Modifier.border(1.dp, Color(0xFFCCCCCC), CircleShape)
                                            else Modifier
                                        )
                                )
                                Text(
                                    text = colorStr,
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (variantName.isNotBlank()) {
                        onConfirm(variantName.trim())
                        onDismiss()
                    }
                },
                enabled = variantName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ajouter", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", fontFamily = PlusJakartaSans)
            }
        }
    )
}
