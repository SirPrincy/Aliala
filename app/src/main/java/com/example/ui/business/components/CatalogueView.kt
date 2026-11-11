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
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.BusinessUiState
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
    onToggleShowHidden: () -> Unit,
    onSortChanged: (String) -> Unit,
    onViewModeChanged: (String) -> Unit,
    onArticleClick: (BusinessOrderEntity) -> Unit,
    onToggleHideArticle: (BusinessOrderEntity) -> Unit,
    onNewArticleClick: () -> Unit,
    onNewFamilyClick: () -> Unit,
    onCreateFamily: (name: String, emoji: String, colorHex: String) -> Unit = { _, _, _ -> },
    onTogglePlatformActive: (Long, Boolean) -> Unit = { _, _ -> },
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
        internalNotes: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
    onToggleTransitaireArchived: (Long) -> Unit = {},
    onDeleteTransitaire: (Long) -> Unit = {},
    onUpdateTransitaireRates: (Long, Long, Long) -> Unit = { _, _, _ -> },
    onRecordTransitairePayment: (Long, Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable {
                                Toast.makeText(context, "Recherche avancée", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Recherche",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF97316).copy(alpha = 0.15f))
                            .clickable {
                                Toast.makeText(context, "Filtres catalogue", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filtres",
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(19.dp)
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
                        "Plateformes" -> Color(0xFF712AE2)
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
                        onToggleShowHidden = onToggleShowHidden,
                        onSortChanged = onSortChanged,
                        onViewModeChanged = onViewModeChanged,
                        onArticleClick = onArticleClick,
                        onToggleHideArticle = onToggleHideArticle,
                        onNewFamilyClick = onNewFamilyClick,
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
                        onCreatePlatform = onCreatePlatform,
                        onDeletePlatform = onDeletePlatform
                    )
                }
                "Transitaires" -> {
                    TransitairesSubTabContent(
                        transitaires = uiState.catalogueTransitaires,
                        onCreateTransitaire = { name, hub, air, sea ->
                            onCreateTransitaire(name, hub, air, sea, "", "", true, "24/10/2026", "5 à 8 jours", "30 à 45 jours", "")
                        },
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
            onConfirm = { name, hub, airRate, seaRate, contact, address, isActive, dateApp, airLead, seaLead, notes ->
                onCreateTransitaire(name, hub, airRate, seaRate, contact, address, isActive, dateApp, airLead, seaLead, notes)
                showAddTransitaireDialog = false
            }
        )
    }
}

// ---------------------------------------------------------------------------
// SUB-TAB: ARTICLES CONTENT
// ---------------------------------------------------------------------------
@Composable
private fun ArticlesSubTabContent(
    uiState: BusinessUiState,
    onSearchQueryChanged: (String) -> Unit,
    onFamilyFilterSelected: (String) -> Unit,
    onToggleShowHidden: () -> Unit,
    onSortChanged: (String) -> Unit,
    onViewModeChanged: (String) -> Unit,
    onArticleClick: (BusinessOrderEntity) -> Unit,
    onToggleHideArticle: (BusinessOrderEntity) -> Unit,
    onNewFamilyClick: () -> Unit,
    showSortMenu: Boolean,
    onDismissSortMenu: () -> Unit,
    onOpenSortMenu: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Search Bar with Cancel button
        OutlinedTextField(
            value = uiState.catalogueSearchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("catalogue_search_input"),
            placeholder = {
                Text(
                    text = "TWS",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (uiState.catalogueSearchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChanged("") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Effacer la recherche",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        // Family Pills (Scrollable with + Famille)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            uiState.catalogueFamilyPills.forEach { pill ->
                val isSelected = uiState.catalogueFamilyFilter.equals(pill.name, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surface)
                        .clickable { onFamilyFilterSelected(pill.name) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${pill.name} ${pill.count}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // + Famille button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(9999.dp))
                    .clickable { onNewFamilyClick() }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Famille",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Secondary Bar: Afficher masqués (X) | Trier: Plus vendus | List/Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Afficher masqués (N)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (uiState.catalogueShowHidden) CyanPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .clickable { onToggleShowHidden() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (uiState.catalogueShowHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = if (uiState.catalogueShowHidden) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Afficher masqués (${uiState.hiddenCatalogueCount})",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (uiState.catalogueShowHidden) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Trier dropdown trigger
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { onOpenSortMenu() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Trier: ${uiState.catalogueSort}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = onDismissSortMenu
                    ) {
                        listOf("Plus vendus", "Dernier coût", "Nom").forEach { sortOption ->
                            DropdownMenuItem(
                                text = { Text(sortOption, fontFamily = PlusJakartaSans) },
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
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uiState.catalogueViewMode == "LIST") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { onViewModeChanged("LIST") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewList,
                            contentDescription = "Vue liste",
                            tint = if (uiState.catalogueViewMode == "LIST") CyanPrimary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uiState.catalogueViewMode == "GRID") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { onViewModeChanged("GRID") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Vue grille",
                            tint = if (uiState.catalogueViewMode == "GRID") CyanPrimary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Articles List or Grid
        if (uiState.visibleCatalogueOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aucun article dans cette sélection",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            if (uiState.catalogueViewMode == "GRID") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 70.dp)
                ) {
                    items(uiState.visibleCatalogueOrders, key = { it.id }) { order ->
                        if (order.isHidden) {
                            HiddenCatalogueCard(
                                order = order,
                                onRestoreClick = { onToggleHideArticle(order) },
                                modifier = Modifier.clickable { onArticleClick(order) }
                            )
                        } else {
                            ActiveCatalogueCard(
                                order = order,
                                onArticleClick = { onArticleClick(order) },
                                onToggleHide = { onToggleHideArticle(order) }
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 70.dp)
                ) {
                    items(uiState.visibleCatalogueOrders, key = { it.id }) { order ->
                        if (order.isHidden) {
                            HiddenCatalogueCard(
                                order = order,
                                onRestoreClick = { onToggleHideArticle(order) },
                                modifier = Modifier.clickable { onArticleClick(order) }
                            )
                        } else {
                            ActiveCatalogueCard(
                                order = order,
                                onArticleClick = { onArticleClick(order) },
                                onToggleHide = { onToggleHideArticle(order) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ACTIVE CATALOGUE ARTICLE CARD
// ---------------------------------------------------------------------------
@Composable
private fun ActiveCatalogueCard(
    order: BusinessOrderEntity,
    onArticleClick: () -> Unit,
    onToggleHide: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // Family color
    val isMode = order.family.equals("Mode", ignoreCase = true)
    val isMaison = order.family.equals("Maison", ignoreCase = true)
    val familyColor = when {
        isMode -> Color(0xFF712AE2)
        isMaison -> Color(0xFFEA580C)
        else -> CyanPrimary
    }

    val isTop = order.unitsSold > 10

    val lastCostAr = if (order.unitCostAr > 0L) {
        order.unitCostAr
    } else if (order.priceYuan > 0.0) {
        (order.priceYuan * order.exchangeRate).toLong() + (if (order.quantityBought > 0) order.freightCostAr / order.quantityBought else 0L)
    } else {
        0L
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable { onArticleClick() }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Thumbnail + Info + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product Thumbnail
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    familyColor.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVector = when {
                        order.imageUrl == "earbuds" || order.product.contains("Écouteurs", ignoreCase = true) -> Icons.Default.Headphones
                        order.imageUrl == "smartwatch" || order.product.contains("Montre", ignoreCase = true) -> Icons.Default.Watch
                        order.imageUrl == "bag" || order.product.contains("Sacoche", ignoreCase = true) -> Icons.Default.ShoppingBag
                        else -> Icons.Default.Inventory2
                    }

                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = familyColor,
                        modifier = Modifier.size(34.dp)
                    )

                    // Top badge in top-left
                    if (isTop) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF59E0B))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(9.dp)
                                )
                                Text(
                                    text = "Top",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Variants count in bottom-end
                    if (order.variants.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF131B2E).copy(alpha = 0.85f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            val varsCount = order.variants.split("·", ",").size
                            Text(
                                text = "$varsCount vars",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Middle Column: Category, Name, SKU
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(familyColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = order.family.ifBlank { "Catalogue" },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = familyColor
                            )
                        }

                        // Context Menu
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Modifier l'article", fontFamily = PlusJakartaSans) },
                                    onClick = {
                                        showMenu = false
                                        onArticleClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Masquer l'article", fontFamily = PlusJakartaSans) },
                                    onClick = {
                                        showMenu = false
                                        onToggleHide()
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = order.product,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = order.referenceCode.ifBlank { "REF-${order.id}" },
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Variants & Logistics Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (order.variants.isNotBlank()) order.variants else "${order.quantityBought} pcs",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (order.weightKg != null && order.weightKg > 0.0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${(order.weightKg * 1000).toInt()} g",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            )

            // Bottom Row: Stock Badge & Last Cost (Ar & ¥)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A))
                    )
                    Text(
                        text = "Stock: ",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${order.stockCount} pcs",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Last Cost
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Dernier coût",
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = CurrencyFormatter.formatAriary(lastCostAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CyanPrimary
                        )
                        Text(
                            text = "(¥ ${order.priceYuan.toInt()})",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// HIDDEN CATALOGUE ARTICLE CARD (Masked state)
// ---------------------------------------------------------------------------
@Composable
private fun HiddenCatalogueCard(
    order: BusinessOrderEntity,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Grayscale / Masked Icon Box
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge Masqué
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Masqué",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        // Family
                        Text(
                            text = order.family.ifBlank { "Accessoires" },
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Text(
                        text = order.product,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = order.referenceCode.ifBlank { "CAB-65W-USB" },
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Warning and Restore Button Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "N'apparaît plus aux achats",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Button(
                    onClick = onRestoreClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary.copy(alpha = 0.12f),
                        contentColor = CyanPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Réafficher",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SUB-TAB: FAMILLES CONTENT
// ---------------------------------------------------------------------------
@Composable
private fun FamillesSubTabContent(
    families: List<CatalogueFamilySummaryUi>,
    onNewFamilyClick: () -> Unit,
    onCreateFamily: (name: String, emoji: String, colorHex: String) -> Unit,
    onFamilyClick: (CatalogueFamilySummaryUi) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var newFamilyName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("✨") }
    var selectedColorHex by remember { mutableStateOf("#67E8F9") }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("catalogue_families_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Summary Bar: X Familles actives / Y articles répertoriés + Pill button "+ Nouvelle famille"
        item(key = "header_summary") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${families.size} Familles actives",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val totalArticles = families.sumOf { it.articleCount }
                    Text(
                        text = "$totalArticles articles répertoriés",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Pill Button: "+ Nouvelle famille"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFFE0F2FE))
                        .clickable {
                            // Scroll down to the creation card and trigger onNewFamilyClick
                            onNewFamilyClick()
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("btn_new_family_pill"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFF0369A1), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF0369A1),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = "Nouvelle famille",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0369A1)
                        )
                    }
                }
            }
        }

        // 2. Family Cards
        items(families, key = { "family_${it.id}_${it.name}" }) { family ->
            val parsedColor = remember(family.colorHex) {
                try {
                    Color(android.graphics.Color.parseColor(family.colorHex))
                } catch (e: Exception) {
                    when {
                        family.name.contains("Mode", ignoreCase = true) -> Color(0xFFEC4899)
                        family.name.contains("Maison", ignoreCase = true) -> Color(0xFFF97316)
                        family.name.contains("Accessoires", ignoreCase = true) -> Color(0xFF8B5CF6)
                        else -> CyanPrimary
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .clickable { onFamilyClick(family) }
                    .padding(14.dp)
                    .testTag("family_card_${family.name}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Emoji Container
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(parsedColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = family.emoji.ifBlank { "🎧" },
                                fontSize = 24.sp
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = family.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Subtitle: "14 articles (12 actifs) · 2 masqués"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "${family.articleCount} articles ",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "(${family.activeCount} actifs)",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = when {
                                        family.name.contains("Mode", ignoreCase = true) -> Color(0xFF7C3AED)
                                        family.name.contains("Maison", ignoreCase = true) -> Color(0xFF2563EB)
                                        else -> Color(0xFF0D9488)
                                    }
                                )
                                if (family.hiddenCount > 0) {
                                    Text(
                                        text = " · ${family.hiddenCount} masqués",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            // Valeur stock : 1 845 000 Ar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Valeur stock :",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = CurrencyFormatter.formatAriary(family.stockValueAr),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Drag indicator & Chevron
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragIndicator,
                            contentDescription = "Réorganiser",
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 3. Inline Card "Créer une famille"
        item(key = "card_create_family") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
                    .testTag("card_create_family")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Header Row: Icon Squircle + Title + Badge "Étape 1/2"
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
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Créer une famille",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(Color(0xFFEDE9FE))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Étape 1/2",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF6D28D9)
                            )
                        }
                    }

                    // Nom de la famille
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Nom de la famille",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = newFamilyName,
                            onValueChange = { newFamilyName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_family_name"),
                            placeholder = {
                                Text(
                                    text = "Beauté & Bien-être",
                                    fontFamily = Inter,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            },
                            trailingIcon = {
                                if (newFamilyName.isNotEmpty()) {
                                    IconButton(
                                        onClick = { newFamilyName = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Effacer",
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }

                    // Icône symbolique
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Icône symbolique",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val emojiChoices = listOf("💄", "✨", "🧴", "💅", "🌿")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            emojiChoices.forEach { emoji ->
                                val isSelected = selectedEmoji == emoji
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) Color(0xFFBAE6FD)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) CyanPrimary else Color.Transparent,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { selectedEmoji = emoji }
                                        .testTag("emoji_btn_$emoji"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emoji,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }
                    }

                    // Teinte d'identification
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Teinte d'identification",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val colorChoices = listOf(
                            "#67E8F9", // Cyan / Mint
                            "#F3E8FF", // Pastel lavender
                            "#DBEAFE", // Pastel sky
                            "#E0E7FF", // Pastel blue
                            "#22D3EE", // Vivid Cyan
                            "#C084FC", // Vivid Purple
                            "#93C5FD", // Medium Blue
                            "#E2E8F0"  // Soft Slate
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            colorChoices.forEach { colorHex ->
                                val isSelected = selectedColorHex.equals(colorHex, ignoreCase = true)
                                val swatchColor = try {
                                    Color(android.graphics.Color.parseColor(colorHex))
                                } catch (e: Exception) {
                                    CyanPrimary
                                }

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.5.dp,
                                            color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = colorHex }
                                        .testTag("color_swatch_$colorHex"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Sélectionné",
                                            tint = if (colorHex in listOf("#F3E8FF", "#DBEAFE", "#E0E7FF", "#E2E8F0")) Color(0xFF1E293B) else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Button "+ Créer la famille"
                    Button(
                        onClick = {
                            if (newFamilyName.isNotBlank()) {
                                onCreateFamily(newFamilyName.trim(), selectedEmoji, selectedColorHex)
                                newFamilyName = ""
                            }
                        },
                        enabled = newFamilyName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_submit_create_family")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Créer la famille",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. "Protection des données" Notice Card
        item(key = "card_data_protection") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFEFF6FF))
                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(18.dp))
                    .padding(14.dp)
                    .testTag("card_data_protection")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Protection des données",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "En cas de suppression d'une famille, les articles associés sont automatiquement transférés vers une famille de destination pour éviter toute perte de stock ou de chiffre d'affaires.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SUB-TAB: PLATEFORMES CONTENT
// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------
// SUB-TAB: PLATEFORMES CONTENT (100% Hors-ligne / Room persistant)
// ---------------------------------------------------------------------------
@Composable
private fun PlateformesSubTabContent(
    platforms: List<CataloguePlatformSummaryUi>,
    onTogglePlatformActive: (Long, Boolean) -> Unit,
    onCreatePlatform: (name: String, subtitle: String, shippingTag: String, secondaryTag: String, iconType: String, colorHex: String) -> Unit,
    onDeletePlatform: (Long) -> Unit
) {
    var showAddPlatformDialog by remember { mutableStateOf(false) }
    var selectedPlatformDetails by remember { mutableStateOf<CataloguePlatformSummaryUi?>(null) }

    val activeCount = platforms.count { it.isActive }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("plateformes_subtab_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Summary Banner Card
        item(key = "card_platform_summary") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
                    .testTag("card_platform_summary")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TravelExplore,
                                    contentDescription = null,
                                    tint = Color(0xFF712AE2),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "SOURCING CHINE",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp,
                                    color = Color(0xFF712AE2)
                                )
                            }

                            Text(
                                text = "Plateformes Chinoises d'Achat",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Suivi des fournisseurs, frais de port intérieurs et dépenses cumulées.",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF712AE2).copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color(0xFF712AE2),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary)
                            )
                            Text(
                                text = "$activeCount actives répertoriées",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showAddPlatformDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF712AE2),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("btn_new_platform_banner")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Nouvelle plateforme",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Platforms Cards Stack (Persistant & Dynamique)
        items(platforms, key = { it.id.toString() + "_" + it.name }) { platform ->
            PlatformCardItem(
                platform = platform,
                onToggleActive = { isActive -> onTogglePlatformActive(platform.id, isActive) },
                onDetailsClick = { selectedPlatformDetails = platform }
            )
        }

        // 3. Quick Reference Info Card ("Frais de port en Chine")
        item(key = "card_freight_info") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEADDFF).copy(alpha = 0.5f))
                    .padding(16.dp)
                    .testTag("card_freight_info")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF712AE2),
                        modifier = Modifier.size(24.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Frais de port en Chine (运费)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF25005A)
                        )
                        Text(
                            text = "Si le port est payant sur la plateforme, il est imputé sur le coût de revient du lot avant l'envoi vers le transitaire SinoExpress ou MadaAir.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFF5A00C6)
                        )
                    }
                }
            }
        }

        // 4. Bottom Floating Action CTA
        item(key = "card_bottom_add_button") {
            Button(
                onClick = { showAddPlatformDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF712AE2),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_bottom_add_platform")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Ajouter une plateforme",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // Modal: Ajouter une plateforme
    if (showAddPlatformDialog) {
        AddPlatformDialog(
            onDismiss = { showAddPlatformDialog = false },
            onConfirm = { name, subtitle, shipping, secondary ->
                val iconType = when {
                    name.contains("1688", ignoreCase = true) -> "TEXT_1688"
                    name.contains("taobao", ignoreCase = true) -> "CART"
                    name.contains("pinduoduo", ignoreCase = true) -> "PRICE"
                    name.contains("wechat", ignoreCase = true) -> "WECHAT"
                    else -> "STORE"
                }
                onCreatePlatform(name, subtitle, shipping, secondary, iconType, "#712ae2")
                showAddPlatformDialog = false
            }
        )
    }

    // Modal: Détails & Tarifs transit
    selectedPlatformDetails?.let { plat ->
        PlatformTransitDetailsDialog(
            platform = plat,
            onDismiss = { selectedPlatformDetails = null },
            onDelete = {
                onDeletePlatform(plat.id)
                selectedPlatformDetails = null
            }
        )
    }
}

// ---------------------------------------------------------------------------
// COMPOSABLE: PLATFORM CARD ITEM
// ---------------------------------------------------------------------------
@Composable
private fun PlatformCardItem(
    platform: CataloguePlatformSummaryUi,
    onToggleActive: (Boolean) -> Unit,
    onDetailsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("platform_card_${platform.name}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row: Icon + Names + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Badge logo icon box
                    PlatformBadgeIcon(
                        iconType = platform.iconType,
                        name = platform.name,
                        colorHex = platform.colorHex
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = platform.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (platform.isVerified) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Certifié",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (platform.badgeText.isNotBlank()) {
                                val isUsine = platform.badgeText.contains("Usine", ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isUsine) Color(0xFFFFDAD6) else MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = platform.badgeText,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = if (isUsine) Color(0xFF93000A) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = platform.subtitle,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = platform.isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF712AE2),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("switch_platform_${platform.name}")
                )
            }

            // Tags row (Port Chine + Condition)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isFreeShipping = platform.shippingTag.contains("gratuit", ignoreCase = true) || platform.shippingTag.contains("包邮")
                val isDevis = platform.shippingTag.contains("devis", ignoreCase = true)

                // Shipping pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (isFreeShipping) Color(0xFFACEDFF) else Color(0xFFEADDFF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isFreeShipping -> Icons.Default.CheckCircle
                                isDevis -> Icons.Default.RequestQuote
                                else -> Icons.Default.LocalShipping
                            },
                            contentDescription = null,
                            tint = if (isFreeShipping) Color(0xFF004E5C) else Color(0xFF5A00C6),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = platform.shippingTag,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isFreeShipping) Color(0xFF004E5C) else Color(0xFF5A00C6)
                        )
                    }
                }

                // Secondary tag pill
                if (platform.secondaryTag.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = platform.secondaryTag,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Metrics Box (Dépenses totales + Activité récente)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF2F3FF))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: Dépenses totales
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Dépenses totales",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "¥ ${formatYuanAmount(platform.totalSpentYuan)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "~ ${CurrencyFormatter.formatAriary(platform.totalSpentAr)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color(0xFF712AE2)
                        )
                    }

                    // Column 2: Activité récente
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (platform.totalLots > 0) "Activité récente" else "Commandes passées",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (platform.totalLots > 0) Icons.Default.ShoppingBag else Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${platform.totalLots} commandes",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (platform.lastOrderDate.startsWith("Dernière") || platform.lastOrderDate.startsWith("Suivi") || platform.lastOrderDate.startsWith("Aucune") || platform.lastOrderDate.startsWith("Nouvelle")) platform.lastOrderDate else "Dernière : ${platform.lastOrderDate}",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action button for details
            if (platform.hasTransitDetails) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E7FF))
                        .clickable { onDetailsClick() }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Détails & Tarifs transit",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF712AE2)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF712AE2),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// BADGE LOGO ICON CONTAINER
// ---------------------------------------------------------------------------
@Composable
private fun PlatformBadgeIcon(
    iconType: String,
    name: String,
    colorHex: String
) {
    val bgModifier = Modifier
        .size(44.dp)
        .clip(RoundedCornerShape(14.dp))

    when {
        iconType == "TEXT_1688" || name.contains("1688", ignoreCase = true) -> {
            Box(
                modifier = bgModifier.background(Color(0xFF712AE2).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "1688",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color(0xFF712AE2)
                )
            }
        }
        iconType == "CART" || name.contains("taobao", ignoreCase = true) -> {
            Box(
                modifier = bgModifier.background(Color(0xFF8A4CFC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        iconType == "PRICE" || name.contains("pinduoduo", ignoreCase = true) -> {
            Box(
                modifier = bgModifier.background(Color(0xFFFFDAD6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PriceChange,
                    contentDescription = null,
                    tint = Color(0xFF93000A),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        iconType == "WECHAT" || name.contains("wechat", ignoreCase = true) -> {
            Box(
                modifier = bgModifier.background(Color(0xFFACEDFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = Color(0xFF004E5C),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        else -> {
            val fallbackColor = try {
                Color(android.graphics.Color.parseColor(colorHex))
            } catch (e: Exception) {
                Color(0xFF712AE2)
            }
            Box(
                modifier = bgModifier.background(fallbackColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = fallbackColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// DIALOG: AJOUTER UNE PLATEFORME (ROOM PERSISTANT)
// ---------------------------------------------------------------------------
@Composable
private fun AddPlatformDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, subtitle: String, shippingTag: String, secondaryTag: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var shippingChoice by remember { mutableStateOf("Port Gratuit (包邮)") }
    var secondaryTag by remember { mutableStateOf("") }

    val shippingOptions = listOf(
        "Port Gratuit (包邮)",
        "Port Payant (¥ 10 - ¥ 25)",
        "Port Payant (Sur devis)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nouvelle plateforme sourcing",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la plateforme") },
                    placeholder = { Text("ex: AliExpress, Weidian, JD.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Description / Spécialité") },
                    placeholder = { Text("ex: Grossiste électronique Shenzhen") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Mode de port intérieur en Chine",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    shippingOptions.forEach { opt ->
                        val isSelected = shippingChoice == opt
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF712AE2).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(1.dp, if (isSelected) Color(0xFF712AE2) else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { shippingChoice = opt }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = opt,
                                fontFamily = PlusJakartaSans,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color(0xFF712AE2) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = secondaryTag,
                    onValueChange = { secondaryTag = it },
                    label = { Text("Condition / Tag") },
                    placeholder = { Text("ex: Livraison rapide état côtier") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            if (subtitle.isBlank()) "Fournisseur Chine" else subtitle.trim(),
                            shippingChoice,
                            secondaryTag.trim()
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF712AE2),
                    contentColor = Color.White
                )
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

// ---------------------------------------------------------------------------
// DIALOG: DÉTAILS ET TARIFS TRANSIT
// ---------------------------------------------------------------------------
@Composable
private fun PlatformTransitDetailsDialog(
    platform: CataloguePlatformSummaryUi,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = platform.name,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (platform.isVerified) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Certifié",
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = platform.subtitle,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF2F3FF))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Logistique intérieure",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF712AE2)
                        )
                        Text(
                            text = platform.transitNotes.ifBlank { "Les frais de port payants sont imputés au coût de revient du lot avant l'envoi vers le transitaire SinoExpress ou MadaAir." },
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Volume total dépensé", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            "¥ ${formatYuanAmount(platform.totalSpentYuan)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Équivalent Ariary", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            CurrencyFormatter.formatAriary(platform.totalSpentAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF712AE2)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF712AE2),
                    contentColor = Color.White
                )
            ) {
                Text("Fermer")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFBA1A1A))
            ) {
                Text("Supprimer la plateforme")
            }
        }
    )
}

// ---------------------------------------------------------------------------
// HELPER: FORMAT YUAN
// ---------------------------------------------------------------------------
private fun formatYuanAmount(amount: Double): String {
    val rounded = amount.toLong()
    return String.format(Locale.FRENCH, "%,d", rounded).replace('\u00A0', ' ')
}

