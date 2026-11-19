package com.example.ui.business.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessNavigationState
import com.example.ui.business.BusinessUiState
import com.example.ui.business.CatalogueFamilySummaryUi
import com.example.ui.business.CataloguePlatformSummaryUi
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.shared.BusinessAdaptiveGrid
import com.example.ui.business.shared.BusinessResponsiveContainer
import com.example.ui.business.shared.HumanAvatarCircle
import com.example.ui.business.shared.HumanEmptyState
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanIconBadge
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSegmentedSwitch
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.formatYuan
import com.example.ui.business.shared.pastelToneForKey
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.TransitStatsCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReferentielScreen(
    uiState: BusinessUiState,
    initialTab: String,
    navState: BusinessNavigationState,
    onBack: () -> Unit,
    onTogglePlatformActive: (Long, Boolean) -> Unit,
    onDeletePlatform: (Long) -> Unit,
    onDeleteFamily: (CatalogueFamilySummaryUi) -> Unit,
    onToggleTransitaireArchived: (Long) -> Unit,
    onDeleteTransitaire: (Long) -> Unit
) {
    val colors = rememberTransitaireColors()
    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }

    BackHandler(onBack = onBack)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Référentiel",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_referentiel")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navState.activeSheet = when (activeTab) {
                        "Familles" -> BusinessActiveSheet.AddFamily
                        "Plateformes" -> BusinessActiveSheet.AddPlatform
                        else -> BusinessActiveSheet.TransitaireForm(null)
                    }
                },
                containerColor = CyanPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_referentiel_add")
            ) {
                Icon(Icons.Default.Add, "Ajouter")
            }
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
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    HumanSegmentedSwitch(
                        options = listOf(
                            "Transitaires" to "Transitaires",
                            "Familles" to "Familles",
                            "Plateformes" to "Plateformes"
                        ),
                        selectedKey = activeTab,
                        onSelect = { activeTab = it },
                        colors = colors
                    )

                    when (activeTab) {
                        "Familles" -> {
                            if (uiState.catalogueFamilies.isEmpty()) {
                                HumanEmptyState(
                                    icon = Icons.Default.Category,
                                    title = "Aucune famille d'articles",
                                    subtitle = "Créez des familles (Électronique, Mode, Maison...) pour trier votre stock.",
                                    actionLabel = "Nouvelle famille",
                                    onActionClick = { navState.activeSheet = BusinessActiveSheet.AddFamily },
                                    colors = colors
                                )
                            } else {
                                BusinessAdaptiveGrid(items = uiState.catalogueFamilies) { fam ->
                                    FamilyCard(
                                        family = fam,
                                        colors = colors,
                                        onDelete = { onDeleteFamily(fam) }
                                    )
                                }
                            }
                        }

                        "Plateformes" -> {
                            if (uiState.cataloguePlatforms.isEmpty()) {
                                HumanEmptyState(
                                    icon = Icons.Default.Storefront,
                                    title = "Aucune plateforme d'achat",
                                    subtitle = "Ajoutez 1688, Taobao ou vos fournisseurs directs.",
                                    actionLabel = "Nouvelle plateforme",
                                    onActionClick = { navState.activeSheet = BusinessActiveSheet.AddPlatform },
                                    colors = colors
                                )
                            } else {
                                BusinessAdaptiveGrid(items = uiState.cataloguePlatforms) { plat ->
                                    PlatformCard(
                                        platform = plat,
                                        colors = colors,
                                        onToggleActive = { active -> onTogglePlatformActive(plat.id, active) },
                                        onDelete = { onDeletePlatform(plat.id) }
                                    )
                                }
                            }
                        }

                        else -> {
                            if (uiState.catalogueTransitaires.isEmpty()) {
                                HumanEmptyState(
                                    icon = Icons.Default.LocalShipping,
                                    title = "Aucun transitaire",
                                    subtitle = "Enregistrez vos partenaires aériens et maritimes pour calculer vos coûts et délais.",
                                    actionLabel = "Nouveau transitaire",
                                    onActionClick = { navState.activeSheet = BusinessActiveSheet.TransitaireForm(null) },
                                    colors = colors
                                )
                            } else {
                                BusinessAdaptiveGrid(items = uiState.catalogueTransitaires) { trans ->
                                    TransitaireCard(
                                        transitaire = trans,
                                        colors = colors,
                                        onOpenDetail = { navState.openTransitaire(trans.id, fromReferentiel = true) },
                                        onEdit = { navState.activeSheet = BusinessActiveSheet.TransitaireForm(trans) },
                                        onToggleArchive = { onToggleTransitaireArchived(trans.id) },
                                        onDelete = { onDeleteTransitaire(trans.id) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.size(72.dp))
                }
            }
        }
    }
}

@Composable
private fun TransitaireCard(
    transitaire: CatalogueTransitaireSummaryUi,
    colors: TransitaireColors,
    onOpenDetail: () -> Unit,
    onEdit: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val (bg, fg) = pastelToneForKey(transitaire.name, colors)
    val tariffCount = remember(
        transitaire.goodsCategoryDelays,
        transitaire.airRateAr,
        transitaire.airFastRateAr,
        transitaire.seaRateAr
    ) {
        TariffGridParser.parse(
            raw = transitaire.goodsCategoryDelays,
            airRateAr = transitaire.airRateAr,
            airFastRateAr = transitaire.airFastRateAr,
            seaRateAr = transitaire.seaRateAr
        ).size
    }

    HumanSectionCard(
        colors = colors,
        contentPadding = PaddingValues(16.dp),
        onClick = onOpenDetail
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HumanAvatarCircle(
                initials = transitaire.initials,
                tint = fg,
                backgroundColor = bg,
                size = 46.dp
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transitaire.name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (transitaire.isArchived) {
                        HumanStatusPill("Archivé", colors.badgeNeutralBg, colors.textSecondary)
                    } else if (transitaire.balanceDueAr > 0L) {
                        HumanStatusPill(
                            "Dû ${CurrencyFormatter.formatCompactAriary(transitaire.balanceDueAr)}",
                            colors.warningSoftBg,
                            colors.warningAccent
                        )
                    } else if (transitaire.isAirCheapest) {
                        HumanStatusPill(
                            "Meilleur tarif",
                            colors.successSoftBg,
                            colors.successAccent
                        )
                    }
                }
                Text(
                    text = buildString {
                        append(transitaire.routeHub.ifBlank { "Chine -> Mada" })
                        if (tariffCount > 0) append(" · $tariffCount tarifs")
                        if (transitaire.avgTotalDays != null) {
                            append(" · Moy. ${TransitStatsCalculator.formatDaysAvg(transitaire.avgTotalDays)}")
                        }
                    },
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (transitaire.airRateAr > 0L) {
                val trend = if (transitaire.airRateTrend.isNotBlank()) " (${transitaire.airRateTrend})" else ""
                Text(
                    text = "Aérien : ${CurrencyFormatter.formatAriary(transitaire.airRateAr)}/kg$trend",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.cyanAccent
                )
            }
            if (transitaire.seaRateAr > 0L) {
                Text(
                    text = "Maritime : ${CurrencyFormatter.formatAriary(transitaire.seaRateAr)}/m³",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.violetAccent
                )
            }
        }

        HumanHairline(colors)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HumanTonalButton(
                text = "Grille & Tarifs",
                icon = Icons.Default.Tune,
                onClick = onOpenDetail,
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Edit, "Modifier", tint = colors.textSecondary, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onToggleArchive, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (transitaire.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                    contentDescription = if (transitaire.isArchived) "Réactiver" else "Archiver",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.DeleteOutline, "Supprimer", tint = colors.dangerAccent, modifier = Modifier.size(19.dp))
            }
        }
    }
}

@Composable
private fun FamilyCard(
    family: CatalogueFamilySummaryUi,
    colors: TransitaireColors,
    onDelete: () -> Unit
) {
    val (bg, _) = pastelToneForKey(family.name, colors)
    HumanSectionCard(colors = colors, contentPadding = PaddingValues(16.dp)) {
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
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = family.emoji, fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = family.name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${family.articleCount} articles · ${family.totalStock} pcs en stock",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    if (family.stockValueAr > 0L) {
                        Text(
                            text = "Valeur ${CurrencyFormatter.formatCompactAriary(family.stockValueAr)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.cyanAccent
                        )
                    }
                }
            }
            if (!family.name.equals("Divers", ignoreCase = true)) {
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.DeleteOutline, "Supprimer", tint = colors.dangerAccent, modifier = Modifier.size(19.dp))
                }
            }
        }
    }
}

@Composable
private fun PlatformCard(
    platform: CataloguePlatformSummaryUi,
    colors: TransitaireColors,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val (bg, fg) = pastelToneForKey(platform.name, colors)
    HumanSectionCard(colors = colors, contentPadding = PaddingValues(16.dp)) {
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
                HumanIconBadge(icon = Icons.Default.Storefront, tint = fg, backgroundColor = bg)
                Column {
                    Text(
                        text = platform.name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = platform.subtitle.ifBlank { platform.shippingTag },
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Switch(
                checked = platform.isActive,
                onCheckedChange = onToggleActive
            )
        }
        HumanHairline(colors)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${platform.totalLots} achats · ${formatYuan(platform.totalSpentYuan)}",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = colors.textPrimary
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.DeleteOutline, "Supprimer", tint = colors.dangerAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}
