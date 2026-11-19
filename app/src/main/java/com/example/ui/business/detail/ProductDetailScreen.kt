package com.example.ui.business.detail

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessNavigationState
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.shared.BusinessImageStorage
import com.example.ui.business.shared.BusinessResponsiveContainer
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanHeroCard
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSectionHeader
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.ProductVisualThumb
import com.example.ui.business.shared.VariantChipsEditor
import com.example.ui.business.shared.VariantColorUtils
import com.example.ui.business.shared.formatYuan
import com.example.ui.business.shared.lotStatusLabel
import com.example.ui.business.shared.lotStatusTone
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ProductDetailScreen(
    article: CatalogueArticleSummaryUi,
    navState: BusinessNavigationState,
    onBack: () -> Unit,
    onToggleHide: (CatalogueArticleSummaryUi) -> Unit,
    onDeleteArticle: (CatalogueArticleSummaryUi) -> Unit,
    onUpdatePhoto: (String) -> Unit,
    onUpdateVariants: (String) -> Unit
) {
    val colors = rememberTransitaireColors()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val primary = article.primaryOrder
    val variants = remember(primary.variants, article.allVariants) {
        (article.allVariants + VariantColorUtils.parseVariants(primary.variants)).distinct()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val persisted = BusinessImageStorage.persistPickedPhoto(context, uri)
                if (!persisted.isNullOrBlank()) {
                    onUpdatePhoto(persisted)
                }
            }
        }
    }

    BackHandler(onBack = onBack)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = primary.product,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_product")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navState.activeSheet = BusinessActiveSheet.EditArticle(article) },
                        modifier = Modifier.testTag("btn_edit_product")
                    ) {
                        Icon(Icons.Default.Edit, "Modifier l'article")
                    }
                    IconButton(
                        onClick = { onToggleHide(article) },
                        modifier = Modifier.testTag("btn_hide_product")
                    ) {
                        Icon(
                            imageVector = if (article.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (article.isHidden) "Réafficher" else "Masquer"
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("btn_delete_product")
                    ) {
                        Icon(Icons.Default.DeleteOutline, "Supprimer", tint = colors.dangerAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            BusinessResponsiveContainer { _, _ ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 1. Hero Produit
                    HumanHeroCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                            ) {
                                ProductVisualThumb(
                                    imageUrl = primary.imageUrl,
                                    productName = primary.product,
                                    family = primary.family,
                                    colors = colors,
                                    size = 70.dp,
                                    cornerRadius = 20.dp
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Changer la photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = primary.product,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = buildString {
                                        if (primary.referenceCode.isNotBlank()) append("${primary.referenceCode} · ")
                                        append(primary.family.ifBlank { "Divers" })
                                        if (primary.platform.isNotBlank()) append(" · ${primary.platform}")
                                    },
                                    fontFamily = Inter,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProductMetricPill("En stock", "${article.totalStockCount} pcs", Modifier.weight(1f))
                            ProductMetricPill("Coût moyen", CurrencyFormatter.formatCompactAriary(article.latestUnitCostAr), Modifier.weight(1f))
                            ProductMetricPill("Revente (${article.marginPercent}%)", CurrencyFormatter.formatCompactAriary(article.resalePriceAr), Modifier.weight(1f))
                        }
                    }

                    // 2. 3 actions directes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HumanTonalButton(
                            text = "Vendre",
                            icon = Icons.Default.PointOfSale,
                            onClick = { navState.activeSheet = BusinessActiveSheet.AddSale(primary) },
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                        HumanTonalButton(
                            text = "Racheter",
                            icon = Icons.Default.AddShoppingCart,
                            onClick = { navState.activeSheet = BusinessActiveSheet.OrderForm(initialArticle = primary) },
                            colors = colors,
                            containerColor = colors.violetSoftBg,
                            contentColor = colors.violetAccent,
                            modifier = Modifier.weight(1f)
                        )
                        HumanTonalButton(
                            text = "Ajuster",
                            icon = Icons.Default.Tune,
                            onClick = { navState.activeSheet = BusinessActiveSheet.AdjustStock(primary) },
                            colors = colors,
                            containerColor = colors.innerCardBg,
                            contentColor = colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 3. Variantes (gestion par puces)
                    HumanSectionCard(colors = colors) {
                        HumanSectionHeader(
                            title = if (variants.isEmpty()) "Variantes" else "Variantes (${variants.size})",
                            subtitle = "Ajoutez ou retirez les puces couleur / modèle",
                            colors = colors
                        )
                        VariantChipsEditor(
                            variantsText = variants.joinToString(" · "),
                            onVariantsChange = { updated -> onUpdateVariants(updated) },
                            label = "",
                            placeholder = "Ajouter une variante (ex : Noir, Blanc)",
                            colors = colors
                        )
                    }

                    // 4. Historique des lots
                    HumanSectionCard(colors = colors) {
                        HumanSectionHeader(
                            title = "Lots d'approvisionnement (${article.lots.size})",
                            subtitle = "Touchez un lot pour voir le détail",
                            colors = colors
                        )
                        val lots = article.lots.ifEmpty { listOf(primary) }
                        lots.forEachIndexed { idx, lot ->
                            if (idx > 0) HumanHairline(colors)
                            val (bg, fg) = lotStatusTone(lot.status, colors)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { navState.openLot(lot.id) }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${lot.lotNumber.ifBlank { "Lot #${lot.id}" }} · ${lot.quantityBought} pcs",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "${DateUtils.formatShortDate(lot.orderDate)} · ${formatYuan(lot.priceYuan)}/pc · ${lot.stockCount} en stock",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    HumanStatusPill(lotStatusLabel(lot.status), bg, fg)
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = colors.textSecondary)
                                }
                            }
                        }
                    }

                    // 5. Historique des ventes
                    HumanSectionCard(colors = colors) {
                        HumanSectionHeader(
                            title = "Ventes récentes (${article.salesHistory.size})",
                            subtitle = "${article.totalSoldCount} vendus · CA ${CurrencyFormatter.formatCompactAriary(article.totalRevenueAr)}",
                            colors = colors
                        )
                        if (article.salesHistory.isEmpty()) {
                            Text(
                                text = "Aucune vente enregistrée pour cet article.",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                        } else {
                            article.salesHistory.take(10).forEachIndexed { idx, sale ->
                                if (idx > 0) HumanHairline(colors)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${sale.clientName} · ${sale.quantity} pcs",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = DateUtils.formatShortDate(sale.saleDate),
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                    Text(
                                        text = CurrencyFormatter.formatAriary(sale.totalPriceAr),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.cyanAccent
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer cet article ?", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "L'article « ${primary.product} » et ses lots liés seront supprimés.",
                    fontFamily = Inter
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteArticle(article)
                        onBack()
                    }
                ) {
                    Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = colors.dangerAccent)
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
private fun ProductMetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(label, fontFamily = Inter, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White, maxLines = 1)
        }
    }
}
