package com.example.ui.business.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.BusinessUiState
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.CatalogueFamilySummaryUi
import com.example.ui.business.CataloguePlatformSummaryUi
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun CatalogueView(
    uiState: BusinessUiState,
    onBackClick: () -> Unit,
    onSubTabSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onFamilyFilterSelected: (String) -> Unit,
    onStockFilterSelected: (String) -> Unit = {},
    onToggleShowHidden: () -> Unit,
    onSortChanged: (String) -> Unit,
    onViewModeChanged: (String) -> Unit,
    onArticleClick: (CatalogueArticleSummaryUi) -> Unit,
    onEditArticleClick: (CatalogueArticleSummaryUi) -> Unit = {},
    onQuickRestockArticle: (CatalogueArticleSummaryUi) -> Unit = {},
    onToggleHideArticle: (CatalogueArticleSummaryUi) -> Unit,
    onDeleteArticle: (CatalogueArticleSummaryUi) -> Unit = {},
    onNewArticleClick: () -> Unit,
    onNewFamilyClick: () -> Unit,
    onCreateFamily: (name: String, emoji: String, colorHex: String) -> Unit = { _, _, _ -> },
    onTogglePlatformActive: (Long, Boolean) -> Unit = { _, _ -> },
    onUpdatePlatformShippingTag: (Long, String) -> Unit = { _, _ -> },
    onCreatePlatform: (name: String, subtitle: String, shippingTag: String, secondaryTag: String, iconType: String, colorHex: String) -> Unit = { _, _, _, _, _, _ -> },
    onDeletePlatform: (Long) -> Unit = {},
    onCreateTransitaire: (
        name: String,
        hub: String,
        airRate: Long,
        seaRate: Long,
        contact: String,
        chinaWarehouseAddress: String,
        isActive: Boolean,
        rateApplicationDate: String,
        airLeadDays: String,
        seaLeadDays: String,
        internalNotes: String,
        airFastRate: Long,
        airFastLeadDays: String,
        goodsCategoryDelays: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onUpdateTransitaire: (
        id: Long,
        name: String,
        hub: String,
        airRate: Long,
        seaRate: Long,
        contact: String,
        chinaWarehouseAddress: String,
        isActive: Boolean,
        rateApplicationDate: String,
        airLeadDays: String,
        seaLeadDays: String,
        internalNotes: String,
        airFastRate: Long,
        airFastLeadDays: String,
        goodsCategoryDelays: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onToggleTransitaireArchived: (Long) -> Unit = {},
    onDeleteTransitaire: (Long) -> Unit = {},
    onUpdateTransitaireRates: (Long, Long, Long) -> Unit = { _, _, _ -> },
    onRecordTransitairePayment: (Long, Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val colors = rememberTransitaireColors()
    var showSortMenu by remember { mutableStateOf(false) }
    var showAddTransitaireDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (uiState.catalogueSubTab == "Articles") {
                FloatingActionButton(
                    onClick = onNewArticleClick,
                    containerColor = CyanPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(9999.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .padding(bottom = 8.dp, end = 8.dp)
                        .testTag("fab_new_catalogue_article")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Nouvel article",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (uiState.catalogueSubTab == "Transitaires") {
                FloatingActionButton(
                    onClick = { showAddTransitaireDialog = true },
                    containerColor = Color(0xFFF97316),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(9999.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .padding(bottom = 8.dp, end = 8.dp)
                        .testTag("fab_new_transitaire")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Nouveau transitaire",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // -------------------------------------------------------------
            // 1. TOP HEADER (Back button 48dp, Title, Subtitle, Actions)
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Back button with >= 48dp touch target
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onBackClick() }
                            .testTag("catalogue_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour Business",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (uiState.catalogueSubTab == "Transitaires") "Transitaires" else "Catalogue",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (uiState.catalogueSubTab) {
                                "Familles" -> "Gestion des assortiments"
                                "Plateformes" -> "Sourcing & Fournisseurs"
                                "Transitaires" -> "Partenaires logistiques & Fret"
                                else -> "${uiState.activeCatalogueCount} références actives"
                            },
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 2. 4 PILL SUB-TABS (Articles, Familles, Plateformes, Transitaires)
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .horizontalScroll(rememberScrollState())
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Triple("Articles", Icons.Default.Inventory2, "Articles"),
                    Triple("Familles", Icons.Default.Category, "Familles"),
                    Triple("Plateformes", Icons.Default.Hub, "Plateformes"),
                    Triple("Transitaires", Icons.Default.LocalShipping, "Transitaires")
                ).forEach { (tabKey, icon, label) ->
                    val isSelected = uiState.catalogueSubTab == tabKey
                    val selectedBg = when (tabKey) {
                        "Plateformes" -> colors.violetSolidBg
                        "Transitaires" -> Color(0xFF06B6D4)
                        else -> CyanPrimary
                    }
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) selectedBg else Color.Transparent)
                            .clickable { onSubTabSelected(tabKey) }
                            .padding(horizontal = 14.dp)
                            .testTag("catalogue_subtab_$tabKey"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = label,
                                fontFamily = PlusJakartaSans,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 3. MAIN CONTENT BASED ON SUB-TAB
            // -------------------------------------------------------------
            when (uiState.catalogueSubTab) {
                "Articles" -> {
                    ArticlesSubTabContent(
                        uiState = uiState,
                        onSearchQueryChanged = onSearchQueryChanged,
                        onFamilyFilterSelected = onFamilyFilterSelected,
                        onStockFilterSelected = onStockFilterSelected,
                        onToggleShowHidden = onToggleShowHidden,
                        onSortChanged = onSortChanged,
                        onViewModeChanged = onViewModeChanged,
                        onArticleClick = onArticleClick,
                        onEditArticleClick = onEditArticleClick,
                        onQuickRestockArticle = onQuickRestockArticle,
                        onToggleHideArticle = onToggleHideArticle,
                        onDeleteArticle = onDeleteArticle,
                        onNewFamilyClick = onNewFamilyClick,
                        onNewArticleClick = onNewArticleClick,
                        showSortMenu = showSortMenu,
                        onDismissSortMenu = { showSortMenu = false },
                        onOpenSortMenu = { showSortMenu = true }
                    )
                }
                "Familles" -> {
                    FamillesSubTabContent(
                        families = uiState.catalogueFamilies,
                        onNewFamilyClick = onNewFamilyClick,
                        onCreateFamily = onCreateFamily,
                        onFamilyClick = { fam ->
                            onFamilyFilterSelected(fam.name)
                            onSubTabSelected("Articles")
                        }
                    )
                }
                "Plateformes" -> {
                    PlateformesSubTabContent(
                        platforms = uiState.cataloguePlatforms,
                        onTogglePlatformActive = onTogglePlatformActive,
                        onUpdatePlatformShippingTag = onUpdatePlatformShippingTag,
                        onCreatePlatform = onCreatePlatform,
                        onDeletePlatform = onDeletePlatform
                    )
                }
                "Transitaires" -> {
                    TransitairesSubTabContent(
                        transitaires = uiState.catalogueTransitaires,
                        onCreateTransitaire = { name, hub, air, sea ->
                            val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE).format(java.util.Date())
                            val defaultGrid = com.example.ui.business.model.TariffGridParser.encode(
                                com.example.ui.business.model.TariffGridParser.defaultTariffs(air, 60000L, sea)
                            )
                            onCreateTransitaire(name, hub, air, sea, "", "", true, today, "7 à 10 jours", "30 à 45 jours", "", 60000L, "3 à 5 jours", defaultGrid)
                        },
                        onUpdateTransitaire = onUpdateTransitaire,
                        onToggleArchived = onToggleTransitaireArchived,
                        onDeleteTransitaire = onDeleteTransitaire,
                        onUpdateRates = onUpdateTransitaireRates,
                        onRecordPayment = onRecordTransitairePayment,
                        onAddClick = { showAddTransitaireDialog = true }
                    )
                }
            }
        }
    }

    if (showAddTransitaireDialog) {
        AddTransitaireDialog(
            onDismiss = { showAddTransitaireDialog = false },
            onConfirm = { name, hub, airRate, seaRate, contact, address, isActive, dateApp, airLead, seaLead, notes, airFastRate, airFastLead, goodsDelays ->
                onCreateTransitaire(name, hub, airRate, seaRate, contact, address, isActive, dateApp, airLead, seaLead, notes, airFastRate, airFastLead, goodsDelays)
                showAddTransitaireDialog = false
            }
        )
    }
}

// ---------------------------------------------------------------------------
// SUB-TAB: ARTICLES CONTENT (REFONTE COMPLÈTE)
// ---------------------------------------------------------------------------
@Composable
private fun ArticlesSubTabContent(
    uiState: BusinessUiState,
    onSearchQueryChanged: (String) -> Unit,
    onFamilyFilterSelected: (String) -> Unit,
    onStockFilterSelected: (String) -> Unit,
    onToggleShowHidden: () -> Unit,
    onSortChanged: (String) -> Unit,
    onViewModeChanged: (String) -> Unit,
    onArticleClick: (CatalogueArticleSummaryUi) -> Unit,
    onEditArticleClick: (CatalogueArticleSummaryUi) -> Unit,
    onQuickRestockArticle: (CatalogueArticleSummaryUi) -> Unit,
    onToggleHideArticle: (CatalogueArticleSummaryUi) -> Unit,
    onDeleteArticle: (CatalogueArticleSummaryUi) -> Unit,
    onNewFamilyClick: () -> Unit,
    onNewArticleClick: () -> Unit,
    showSortMenu: Boolean,
    onDismissSortMenu: () -> Unit,
    onOpenSortMenu: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val colors = rememberTransitaireColors()
    var articlePendingDelete by remember { mutableStateOf<CatalogueArticleSummaryUi?>(null) }

    val activeArticles = remember(uiState.allCatalogueArticles) {
        uiState.allCatalogueArticles.filter { !it.isHidden }
    }
    val totalStockPieces = remember(activeArticles) { activeArticles.sumOf { it.totalStockCount } }
    val lowOrRuptureCount = remember(activeArticles) {
        activeArticles.count { !it.isReferenceOnly && it.totalStockCount <= 5 && it.totalInTransitCount == 0 }
    }
    val totalStockValueAr = remember(activeArticles) {
        activeArticles.sumOf { it.totalStockCount.toLong() * it.latestUnitCostAr }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Compact KPI Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ArticleKpiChip(
                label = "Actifs",
                value = "${activeArticles.size}",
                accent = colors.cyanAccent,
                bg = colors.cyanSoftBg
            )
            ArticleKpiChip(
                label = "Stock rayon",
                value = "$totalStockPieces pcs",
                accent = colors.successAccent,
                bg = colors.successSoftBg
            )
            ArticleKpiChip(
                label = "Alerte / Rupture",
                value = "$lowOrRuptureCount",
                accent = if (lowOrRuptureCount > 0) colors.dangerAccent else colors.textSecondary,
                bg = if (lowOrRuptureCount > 0) colors.dangerSoftBg else colors.badgeNeutralBg
            )
            ArticleKpiChip(
                label = "Valeur stock",
                value = CurrencyFormatter.formatAriary(totalStockValueAr),
                accent = colors.violetAccent,
                bg = colors.violetSoftBg
            )
        }

        // 2. Search Bar
        OutlinedTextField(
            value = uiState.catalogueSearchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("catalogue_search_input"),
            placeholder = {
                Text(
                    text = "Rechercher nom, SKU, variante, plateforme...",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (uiState.catalogueSearchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChanged("") },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Effacer la recherche",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.cardBg,
                unfocusedContainerColor = colors.cardBg,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = colors.border
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        // 3. Family Pills with real emojis + "+ Famille"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            uiState.catalogueFamilyPills.forEach { pill ->
                val isSelected = uiState.catalogueFamilyFilter.equals(pill.name, ignoreCase = true)
                val famEmoji = uiState.catalogueFamilies.firstOrNull {
                    it.name.equals(pill.name, ignoreCase = true)
                }?.emoji ?: ""
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (isSelected) CyanPrimary else colors.cardBg,
                    border = if (isSelected) null else BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .height(36.dp)
                        .clickable { onFamilyFilterSelected(pill.name) }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (famEmoji.isNotBlank()) "$famEmoji ${pill.name} (${pill.count})" else "${pill.name} (${pill.count})",
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.Black else colors.textSecondary
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = colors.innerCardBg,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .height(36.dp)
                    .clickable { onNewFamilyClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = colors.cyanAccent, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Famille",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.cyanAccent
                    )
                }
            }
        }

        // 4. Stock Status Chips + Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Tous", "En stock", "En transit", "Rupture", "Référentiel").forEach { filterOption ->
                val isSelected = uiState.catalogueStockFilter == filterOption
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) colors.violetSoftBg else colors.innerCardBg,
                    border = if (isSelected) BorderStroke(1.dp, colors.violetAccent) else null,
                    modifier = Modifier.clickable { onStockFilterSelected(filterOption) }
                ) {
                    Text(
                        text = filterOption,
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (isSelected) colors.violetAccent else colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 5. Utility Bar: Hidden toggle | Sort | List/Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (uiState.catalogueShowHidden) colors.cyanSoftBg else colors.innerCardBg,
                modifier = Modifier.clickable { onToggleShowHidden() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.catalogueShowHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (uiState.catalogueShowHidden) colors.cyanAccent else colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Masqués (${uiState.hiddenCatalogueCount})",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (uiState.catalogueShowHidden) colors.cyanAccent else colors.textSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.innerCardBg,
                        modifier = Modifier.clickable { onOpenSortMenu() }
                    ) {
                        Text(
                            text = "Tri : ${uiState.catalogueSort}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = onDismissSortMenu
                    ) {
                        listOf("Plus vendus", "Marge élevée", "Stock bas", "Dernier coût", "Nom", "Plus récents").forEach { sortOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = sortOption,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (uiState.catalogueSort == sortOption) FontWeight.Bold else FontWeight.Normal,
                                        color = if (uiState.catalogueSort == sortOption) colors.cyanAccent else colors.textPrimary
                                    )
                                },
                                onClick = {
                                    onSortChanged(sortOption)
                                    onDismissSortMenu()
                                }
                            )
                        }
                    }
                }

                // List / Grid toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.innerCardBg)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uiState.catalogueViewMode == "LIST") colors.cardBg else Color.Transparent)
                            .clickable { onViewModeChanged("LIST") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewList,
                            contentDescription = "Vue liste",
                            tint = if (uiState.catalogueViewMode == "LIST") colors.cyanAccent else colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uiState.catalogueViewMode == "GRID") colors.cardBg else Color.Transparent)
                            .clickable { onViewModeChanged("GRID") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Vue grille",
                            tint = if (uiState.catalogueViewMode == "GRID") colors.cyanAccent else colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 6. Articles List / Responsive Grid
        if (uiState.visibleCatalogueArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Text(
                        text = "Aucun article trouvé",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Ajustez vos filtres ou ajoutez une nouvelle fiche produit.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Button(
                        onClick = onNewArticleClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Créer un article", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val isTablet = maxWidth >= 600.dp

                if (uiState.catalogueViewMode == "GRID") {
                    val columns = if (isTablet) GridCells.Adaptive(185.dp) else GridCells.Fixed(2)
                    LazyVerticalGrid(
                        columns = columns,
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 84.dp)
                    ) {
                        items(uiState.visibleCatalogueArticles, key = { it.articleKey }) { article ->
                            if (article.isHidden) {
                                HiddenCatalogueCard(
                                    article = article,
                                    onRestoreClick = { onToggleHideArticle(article) },
                                    onClick = { onArticleClick(article) }
                                )
                            } else {
                                ActiveCatalogueGridCard(
                                    article = article,
                                    onArticleClick = { onArticleClick(article) },
                                    onEditClick = { onEditArticleClick(article) },
                                    onQuickRestock = { onQuickRestockArticle(article) },
                                    onToggleHide = { onToggleHideArticle(article) },
                                    onDeleteRequest = { articlePendingDelete = article }
                                )
                            }
                        }
                    }
                } else if (isTablet) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 84.dp)
                    ) {
                        items(uiState.visibleCatalogueArticles, key = { it.articleKey }) { article ->
                            if (article.isHidden) {
                                HiddenCatalogueCard(
                                    article = article,
                                    onRestoreClick = { onToggleHideArticle(article) },
                                    onClick = { onArticleClick(article) }
                                )
                            } else {
                                ActiveCatalogueListCard(
                                    article = article,
                                    onArticleClick = { onArticleClick(article) },
                                    onEditClick = { onEditArticleClick(article) },
                                    onQuickRestock = { onQuickRestockArticle(article) },
                                    onToggleHide = { onToggleHideArticle(article) },
                                    onDeleteRequest = { articlePendingDelete = article }
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 84.dp)
                    ) {
                        items(uiState.visibleCatalogueArticles, key = { it.articleKey }) { article ->
                            if (article.isHidden) {
                                HiddenCatalogueCard(
                                    article = article,
                                    onRestoreClick = { onToggleHideArticle(article) },
                                    onClick = { onArticleClick(article) }
                                )
                            } else {
                                ActiveCatalogueListCard(
                                    article = article,
                                    onArticleClick = { onArticleClick(article) },
                                    onEditClick = { onEditArticleClick(article) },
                                    onQuickRestock = { onQuickRestockArticle(article) },
                                    onToggleHide = { onToggleHideArticle(article) },
                                    onDeleteRequest = { articlePendingDelete = article }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    articlePendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { articlePendingDelete = null },
            title = {
                Text(
                    text = "Supprimer cet article ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = colors.dangerAccent
                )
            },
            text = {
                val lotsSuffix = if (pending.lotsCount > 1) " et ses ${pending.lotsCount} lots liés" else ""
                Text(
                    text = "Voulez-vous supprimer définitivement « ${pending.primaryOrder.product} »$lotsSuffix ? Cette action est irréversible. Astuce : vous pouvez plutôt masquer l'article.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteArticle(pending)
                        articlePendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent, contentColor = Color.White)
                ) {
                    Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { articlePendingDelete = null }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }
}

@Composable
private fun ArticleKpiChip(
    label: String,
    value: String,
    accent: Color,
    bg: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 10.sp,
                color = accent.copy(alpha = 0.85f)
            )
            Text(
                text = value,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = accent
            )
        }
    }
}

@Composable
private fun ArticleVisualThumbnail(
    imageUrl: String,
    fallbackEmoji: String,
    familyColor: Color,
    productName: String,
    modifier: Modifier = Modifier,
    emojiFontSize: Int = 30
) {
    val isPhoto = imageUrl.startsWith("content://") ||
        imageUrl.startsWith("file://") ||
        imageUrl.startsWith("http")
    val displayEmoji = if (imageUrl.isNotBlank() && !imageUrl.contains("/")) imageUrl else fallbackEmoji

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(
                    familyColor.copy(alpha = 0.16f),
                    MaterialTheme.colorScheme.surfaceVariant
                )
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        if (isPhoto) {
            AsyncImage(
                model = imageUrl,
                contentDescription = productName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = displayEmoji,
                fontSize = emojiFontSize.sp
            )
        }
    }
}

private fun parseColorSafe(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

private fun formatYuanPrecise(value: Double): String {
    if (value <= 0.0) return "—"
    return if (value % 1.0 == 0.0) "¥ ${value.toInt()}"
    else String.format(Locale.US, "¥ %.2f", value)
}

@Composable
private fun ArticleStatusPill(
    statusTag: String,
    stockCount: Int,
    transitCount: Int,
    colors: TransitaireColors
) {
    val (bg, fg, label) = when (statusTag) {
        "En stock" -> Triple(colors.successSoftBg, colors.successAccent, "$stockCount en stock")
        "Stock faible" -> Triple(colors.expressSoftBg, colors.expressAccent, "$stockCount stock bas")
        "En transit" -> Triple(colors.violetSoftBg, colors.violetAccent, "$transitCount en transit")
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// ACTIVE CATALOGUE LIST CARD
// ---------------------------------------------------------------------------
@Composable
private fun ActiveCatalogueListCard(
    article: CatalogueArticleSummaryUi,
    onArticleClick: () -> Unit,
    onEditClick: () -> Unit,
    onQuickRestock: () -> Unit,
    onToggleHide: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val colors = rememberTransitaireColors()
    val order = article.primaryOrder
    val familyColor = remember(article.familyColorHex, colors) {
        parseColorSafe(article.familyColorHex, colors.cyanAccent)
    }
    val isTop = article.totalSoldCount >= 10
    val unitProfitAr = if (article.resalePriceAr > 0L && article.latestUnitCostAr > 0L) {
        article.resalePriceAr - article.latestUnitCostAr
    } else 0L

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onArticleClick() },
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    ArticleVisualThumbnail(
                        imageUrl = order.imageUrl,
                        fallbackEmoji = article.familyEmoji,
                        familyColor = familyColor,
                        productName = order.product,
                        modifier = Modifier.fillMaxSize(),
                        emojiFontSize = 32
                    )

                    if (isTop) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF59E0B))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(9.dp))
                                Text("Top", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }

                    if (article.allVariants.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.72f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${article.allVariants.size} var.",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = familyColor.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = order.family.ifBlank { "Catalogue" },
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = familyColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            if (article.lotsCount > 1) {
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = colors.badgeNeutralBg
                                ) {
                                    Text(
                                        text = "${article.lotsCount} lots",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.MoreVert, "Options", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                            }
                            ArticleActionDropdown(
                                expanded = showMenu,
                                isHidden = article.isHidden,
                                onDismiss = { showMenu = false },
                                onOpenDetail = { showMenu = false; onArticleClick() },
                                onEdit = { showMenu = false; onEditClick() },
                                onRestock = { showMenu = false; onQuickRestock() },
                                onToggleHide = { showMenu = false; onToggleHide() },
                                onDelete = { showMenu = false; onDeleteRequest() }
                            )
                        }
                    }

                    Text(
                        text = order.product.ifBlank { "Article #${order.id}" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.innerCardBg
                        ) {
                            Text(
                                text = order.referenceCode.ifBlank { "REF-${order.id}" },
                                fontFamily = Inter,
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (order.platform.isNotBlank()) {
                            Text(
                                text = "• ${order.platform}",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Pricing & Margin Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.innerCardBg)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Coût revient", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (article.latestUnitCostAr > 0L) CurrencyFormatter.formatAriary(article.latestUnitCostAr) else "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = formatYuanPrecise(article.latestPriceYuan),
                            fontFamily = Inter,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Prix vente", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                    Text(
                        text = if (article.resalePriceAr > 0L) CurrencyFormatter.formatAriary(article.resalePriceAr) else "À définir",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.cyanAccent
                    )
                }

                val marginColor = when {
                    article.marginPercent >= 25 -> colors.successAccent
                    article.marginPercent > 0 -> colors.violetAccent
                    article.resalePriceAr > 0L && unitProfitAr <= 0L -> colors.dangerAccent
                    else -> colors.textSecondary
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = marginColor.copy(alpha = 0.14f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (article.resalePriceAr > 0L && article.latestUnitCostAr > 0L) "${article.marginPercent}%" else "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = marginColor
                        )
                        if (unitProfitAr != 0L) {
                            Text(
                                text = (if (unitProfitAr > 0) "+" else "") + CurrencyFormatter.formatAriary(unitProfitAr),
                                fontFamily = Inter,
                                fontSize = 9.sp,
                                color = marginColor
                            )
                        }
                    }
                }
            }

            // Bottom Footer: Stock status + Sold + Weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArticleStatusPill(
                    statusTag = article.statusTag,
                    stockCount = article.totalStockCount,
                    transitCount = article.totalInTransitCount,
                    colors = colors
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (article.totalSoldCount > 0) {
                        Text(
                            text = "${article.totalSoldCount} vendus",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = colors.violetAccent
                        )
                    }
                    if (order.weightKg != null && order.weightKg > 0.0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.Scale, null, tint = colors.textSecondary, modifier = Modifier.size(12.dp))
                            Text(
                                text = "${(order.weightKg * 1000).toInt()} g",
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

// ---------------------------------------------------------------------------
// ACTIVE CATALOGUE GRID CARD (COMPACT VERTICAL LAYOUT)
// ---------------------------------------------------------------------------
@Composable
private fun ActiveCatalogueGridCard(
    article: CatalogueArticleSummaryUi,
    onArticleClick: () -> Unit,
    onEditClick: () -> Unit,
    onQuickRestock: () -> Unit,
    onToggleHide: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val colors = rememberTransitaireColors()
    val order = article.primaryOrder
    val familyColor = remember(article.familyColorHex, colors) {
        parseColorSafe(article.familyColorHex, colors.cyanAccent)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onArticleClick() },
        shape = RoundedCornerShape(18.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column {
            // Top Visual Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                ArticleVisualThumbnail(
                    imageUrl = order.imageUrl,
                    fallbackEmoji = article.familyEmoji,
                    familyColor = familyColor,
                    productName = order.product,
                    modifier = Modifier.fillMaxSize(),
                    emojiFontSize = 40
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    ArticleStatusPill(
                        statusTag = article.statusTag,
                        stockCount = article.totalStockCount,
                        transitCount = article.totalInTransitCount,
                        colors = colors
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(colors.cardBg.copy(alpha = 0.88f))
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MoreVert, "Options", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                    ArticleActionDropdown(
                        expanded = showMenu,
                        isHidden = article.isHidden,
                        onDismiss = { showMenu = false },
                        onOpenDetail = { showMenu = false; onArticleClick() },
                        onEdit = { showMenu = false; onEditClick() },
                        onRestock = { showMenu = false; onQuickRestock() },
                        onToggleHide = { showMenu = false; onToggleHide() },
                        onDelete = { showMenu = false; onDeleteRequest() }
                    )
                }

                if (article.lotsCount > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.72f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${article.lotsCount} lots",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = order.family.ifBlank { "Catalogue" },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = familyColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = order.product.ifBlank { "Article #${order.id}" },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = if (article.resalePriceAr > 0L) CurrencyFormatter.formatAriary(article.resalePriceAr) else "À définir",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = colors.cyanAccent
                        )
                        Text(
                            text = formatYuanPrecise(article.latestPriceYuan),
                            fontFamily = Inter,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                    if (article.resalePriceAr > 0L && article.latestUnitCostAr > 0L) {
                        val mColor = if (article.marginPercent >= 20) colors.successAccent else colors.violetAccent
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = mColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "${article.marginPercent}%",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = mColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleActionDropdown(
    expanded: Boolean,
    isHidden: Boolean,
    onDismiss: () -> Unit,
    onOpenDetail: () -> Unit,
    onEdit: () -> Unit,
    onRestock: () -> Unit,
    onToggleHide: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = rememberTransitaireColors()
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Fiche complète", fontFamily = PlusJakartaSans) },
            leadingIcon = { Icon(Icons.Default.Info, null, tint = colors.cyanAccent) },
            onClick = onOpenDetail
        )
        DropdownMenuItem(
            text = { Text("Modifier", fontFamily = PlusJakartaSans) },
            leadingIcon = { Icon(Icons.Default.Edit, null, tint = colors.textSecondary) },
            onClick = onEdit
        )
        DropdownMenuItem(
            text = { Text("Nouvel achat", fontFamily = PlusJakartaSans) },
            leadingIcon = { Icon(Icons.Default.ShoppingCart, null, tint = colors.violetAccent) },
            onClick = onRestock
        )
        DropdownMenuItem(
            text = { Text(if (isHidden) "Réafficher" else "Masquer", fontFamily = PlusJakartaSans) },
            leadingIcon = { Icon(if (isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = colors.textSecondary) },
            onClick = onToggleHide
        )
        DropdownMenuItem(
            text = { Text("Supprimer", fontFamily = PlusJakartaSans, color = colors.dangerAccent) },
            leadingIcon = { Icon(Icons.Default.Delete, null, tint = colors.dangerAccent) },
            onClick = onDelete
        )
    }
}

// ---------------------------------------------------------------------------
// HIDDEN CATALOGUE ARTICLE CARD (Masked state)
// ---------------------------------------------------------------------------
@Composable
private fun HiddenCatalogueCard(
    article: CatalogueArticleSummaryUi,
    onRestoreClick: () -> Unit,
    onClick: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val order = article.primaryOrder
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = colors.innerCardBg,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.badgeNeutralBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VisibilityOff, null, tint = colors.textSecondary, modifier = Modifier.size(24.dp))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Masqué • ${order.family.ifBlank { "Sans famille" }}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = order.product,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = order.referenceCode.ifBlank { "REF-${order.id}" },
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${article.totalStockCount} pcs • ${article.lotsCount} lot(s)",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )

                Button(
                    onClick = onRestoreClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.cyanSoftBg,
                        contentColor = colors.cyanAccent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Réafficher", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }
    }
}






