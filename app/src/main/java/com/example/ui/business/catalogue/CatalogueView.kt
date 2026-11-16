package com.example.ui.business.catalogue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.ui.business.BusinessUiState
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.business.transitaires.AddTransitaireDialog
import com.example.ui.business.transitaires.TransitairesSubTabContent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

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
    onRecordTransitairePayment: (Long, Long, String) -> Unit = { _, _, _ -> },
    accounts: List<BusinessAccountEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
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
                        orders = uiState.orders,
                        accounts = accounts,
                        onCreateTransitaire = { name, hub, air, sea ->
                            val today = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
                            val fastDefault = if (air > 0L) round(air * 1.33 / 1000.0).toLong() * 1000L else 60000L
                            val defaultGrid = TariffGridParser.encode(
                                TariffGridParser.defaultTariffs(air, fastDefault, sea)
                            )
                            onCreateTransitaire(name, hub, air, sea, "", "", true, today, "7 à 10 jours", "30 à 45 jours", "", fastDefault, "3 à 5 jours", defaultGrid)
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
