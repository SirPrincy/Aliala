package com.example.ui.business.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransitaireRateHistoryEntity
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessNavigationState
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.model.TariffItem
import com.example.ui.business.shared.BusinessResponsiveContainer
import com.example.ui.business.shared.HumanAvatarCircle
import com.example.ui.business.shared.HumanEmptyState
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanHeroCard
import com.example.ui.business.shared.HumanIconBadge
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSectionHeader
import com.example.ui.business.shared.HumanSegmentedSwitch
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.HumanTextField
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.parseDoubleInput
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.TransitStatsCalculator
import java.util.Locale

internal data class SyncedTransitaireGridUpdate(
    val airRateAr: Long,
    val airFastRateAr: Long,
    val seaRateAr: Long,
    val airLeadDays: String,
    val airFastLeadDays: String,
    val seaLeadDays: String,
    val rateApplicationDate: String,
    val encodedGrid: String
)

internal fun syncTransitaireGridValues(
    transitaire: CatalogueTransitaireSummaryUi,
    updatedGrid: List<TariffItem>,
    preferredEffectiveDate: String = ""
): SyncedTransitaireGridUpdate {
    val airItems = updatedGrid.filter { it.mode == "AÉRIEN" && it.priceAr > 0L }
    val fastAirItem = airItems.firstOrNull {
        it.shipmentType.contains("Express", ignoreCase = true) ||
            it.shipmentType.contains("Rapide", ignoreCase = true) ||
            it.shipmentType.contains("Spécial", ignoreCase = true)
    }
    val stdAirItem = airItems.firstOrNull { it != fastAirItem } ?: airItems.firstOrNull()
    val seaItem = updatedGrid.firstOrNull { it.mode == "MARITIME" && it.priceAr > 0L }

    val resolvedDate = preferredEffectiveDate.trim().ifBlank {
        updatedGrid.firstOrNull { it.effectiveDate.isNotBlank() }?.effectiveDate
            ?: transitaire.rateApplicationDate.ifBlank { DateUtils.getTodayFrenchDate() }
    }

    return SyncedTransitaireGridUpdate(
        airRateAr = stdAirItem?.priceAr ?: if (updatedGrid.none { it.mode == "AÉRIEN" }) 0L else transitaire.airRateAr,
        airFastRateAr = fastAirItem?.priceAr ?: if (updatedGrid.none { it.mode == "AÉRIEN" }) 0L else transitaire.airFastRateAr,
        seaRateAr = seaItem?.priceAr ?: if (updatedGrid.none { it.mode == "MARITIME" }) 0L else transitaire.seaRateAr,
        airLeadDays = stdAirItem?.leadDays?.ifBlank { transitaire.airLeadDays } ?: transitaire.airLeadDays,
        airFastLeadDays = fastAirItem?.leadDays?.ifBlank { transitaire.airFastLeadDays } ?: transitaire.airFastLeadDays,
        seaLeadDays = seaItem?.leadDays?.ifBlank { transitaire.seaLeadDays } ?: transitaire.seaLeadDays,
        rateApplicationDate = resolvedDate,
        encodedGrid = TariffGridParser.encode(updatedGrid)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransitaireDetailScreen(
    transitaire: CatalogueTransitaireSummaryUi,
    navState: BusinessNavigationState,
    onBack: () -> Unit,
    onSaveGrid: (SyncedTransitaireGridUpdate) -> Unit,
    onResetHistory: () -> Unit,
    onOpenLot: (Long) -> Unit
) {
    val colors = rememberTransitaireColors()
    val tariffs = remember(
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
        )
    }

    var simMode by remember { mutableStateOf("AÉRIEN") }
    var simMeasureText by remember { mutableStateOf("2") }
    var showResetHistoryConfirm by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = transitaire.name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_transitaire")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navState.activeSheet = BusinessActiveSheet.TransitaireForm(transitaire) },
                        modifier = Modifier.testTag("btn_edit_transitaire_detail")
                    ) {
                        Icon(Icons.Default.Edit, "Modifier le partenaire")
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
                    // 1. Carte Héro Transitaire
                    HumanHeroCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HumanAvatarCircle(
                                initials = transitaire.initials,
                                tint = Color.White,
                                backgroundColor = Color.White.copy(alpha = 0.22f),
                                size = 54.dp
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = transitaire.name,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = transitaire.routeHub.ifBlank { "Chine -> Madagascar" },
                                    fontFamily = Inter,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            if (transitaire.isAirCheapest) {
                                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.22f)) {
                                    Text(
                                        text = "Meilleur tarif",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HeroMetricPill(
                                label = "Aérien base",
                                value = if (transitaire.airRateAr > 0L) "${CurrencyFormatter.formatCompactAriary(transitaire.airRateAr)}/kg" else "—",
                                modifier = Modifier.weight(1f)
                            )
                            HeroMetricPill(
                                label = "Maritime base",
                                value = if (transitaire.seaRateAr > 0L) "${CurrencyFormatter.formatCompactAriary(transitaire.seaRateAr)}/m³" else "—",
                                modifier = Modifier.weight(1f)
                            )
                            HeroMetricPill(
                                label = "Solde dû",
                                value = CurrencyFormatter.formatCompactAriary(transitaire.balanceDueAr),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (transitaire.balanceDueAr > 0L) {
                        HumanTonalButton(
                            text = "Régler le solde fret (${CurrencyFormatter.formatAriary(transitaire.balanceDueAr)})",
                            icon = Icons.Default.Payments,
                            onClick = { navState.activeSheet = BusinessActiveSheet.PayTransitaire(transitaire) },
                            colors = colors,
                            containerColor = colors.warningSoftBg,
                            contentColor = colors.warningAccent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_pay_transitaire_due")
                        )
                    }

                    // 2. Grille tarifaire complète (Aérien & Maritime + conditions)
                    HumanSectionHeader(
                        title = "Grille des tarifs",
                        subtitle = "${tariffs.size} tarif${if (tariffs.size > 1) "s" else ""} configuré${if (tariffs.size > 1) "s" else ""}",
                        actionText = "+ Ajouter",
                        onActionClick = {
                            navState.activeSheet = BusinessActiveSheet.TransitaireTariffLine(transitaire, null)
                        },
                        colors = colors
                    )

                    if (tariffs.isEmpty()) {
                        HumanEmptyState(
                            icon = Icons.Default.FlightTakeoff,
                            title = "Aucune ligne tarifaire",
                            subtitle = "Ajoutez vos prix au kilo (Aérien) ou au mètre cube (Maritime), par type de marchandise ou palier.",
                            actionLabel = "Charger la grille type",
                            onActionClick = {
                                val defaults = TariffGridParser.defaultTariffs(
                                    airRateAr = transitaire.airRateAr,
                                    airFastRateAr = transitaire.airFastRateAr,
                                    seaRateAr = transitaire.seaRateAr
                                )
                                onSaveGrid(syncTransitaireGridValues(transitaire, defaults))
                            },
                            colors = colors
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            tariffs.forEach { item ->
                                TariffLineCard(
                                    item = item,
                                    colors = colors,
                                    onEdit = {
                                        navState.activeSheet = BusinessActiveSheet.TransitaireTariffLine(transitaire, item)
                                    },
                                    onDelete = {
                                        val remaining = tariffs.filterNot { it.id == item.id }
                                        onSaveGrid(syncTransitaireGridValues(transitaire, remaining))
                                    }
                                )
                            }
                        }
                    }

                    // 3. Simulateur de coût fret
                    if (tariffs.isNotEmpty()) {
                        HumanSectionCard(colors = colors) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HumanIconBadge(
                                    icon = Icons.Default.Calculate,
                                    tint = colors.violetAccent,
                                    backgroundColor = colors.violetSoftBg
                                )
                                Column {
                                    Text(
                                        text = "Simulateur de fret",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Estimez le prix selon la grille et le seuil",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            HumanSegmentedSwitch(
                                options = listOf("AÉRIEN" to "Aérien (kg)", "MARITIME" to "Maritime (m³)"),
                                selectedKey = simMode,
                                onSelect = { simMode = it },
                                colors = colors
                            )

                            HumanTextField(
                                value = simMeasureText,
                                onValueChange = { simMeasureText = it },
                                label = if (simMode == "MARITIME") "Volume estimé (m³)" else "Poids estimé (kg)",
                                keyboardType = KeyboardType.Decimal,
                                suffixText = if (simMode == "MARITIME") "m³" else "kg",
                                colors = colors
                            )

                            val measure = parseDoubleInput(simMeasureText) ?: 0.0
                            val matched = remember(tariffs, simMode, measure) {
                                TariffGridParser.matchTariffForMeasure(tariffs, simMode, measure)
                            }
                            if (matched != null && measure > 0.0) {
                                val estimatedCost = (measure * matched.priceAr).toLong()
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = colors.cyanSoftBg,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${matched.shipmentType} · ${matched.goodsCategory}",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "${CurrencyFormatter.formatAriary(matched.priceAr)}/${if (simMode == "MARITIME") "m³" else "kg"}" +
                                                    if (matched.leadDays.isNotBlank()) " · ${matched.leadDays}" else "",
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                        Text(
                                            text = CurrencyFormatter.formatAriary(estimatedCost),
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = colors.cyanAccent
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Aucun tarif ${if (simMode == "MARITIME") "maritime" else "aérien"} configuré pour ce mode.",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    // 4. Historique des 2 prix (Aérien & Maritime par périodes)
                    val twoPricesHistory = remember(transitaire.rateHistory) {
                        transitaire.rateHistory.filter { it.mode == "AERIEN" || it.mode == "MARITIME" }
                    }
                    HumanSectionHeader(
                        title = "Historique des 2 prix",
                        subtitle = if (twoPricesHistory.isEmpty()) "Aucune période enregistrée" else "Aérien (/kg) & Maritime (/m³) · ${twoPricesHistory.size} période(s)",
                        actionText = if (transitaire.rateHistory.isNotEmpty()) "Réinitialiser" else null,
                        onActionClick = { showResetHistoryConfirm = true },
                        colors = colors
                    )

                    if (twoPricesHistory.isEmpty()) {
                        HumanSectionCard(colors = colors, contentPadding = PaddingValues(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HumanIconBadge(
                                    icon = Icons.Default.History,
                                    tint = colors.textSecondary,
                                    backgroundColor = colors.badgeNeutralBg
                                )
                                Text(
                                    text = "Saisis un tarif Aérien ou Maritime : il restera actif à partir du jour choisi jusqu'au prochain changement.",
                                    fontFamily = Inter,
                                    fontSize = 13.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    } else {
                        HumanSectionCard(colors = colors, contentPadding = PaddingValues(16.dp)) {
                            twoPricesHistory.forEachIndexed { idx, entry ->
                                if (idx > 0) HumanHairline(colors)
                                RateHistoryRow(entry = entry, colors = colors)
                            }
                        }
                    }

                    // 5. Entrepôt Chine & Contact
                    if (transitaire.chinaWarehouseAddress.isNotBlank() || transitaire.contact.isNotBlank() || transitaire.internalNotes.isNotBlank()) {
                        HumanSectionCard(colors = colors) {
                            HumanSectionHeader(
                                title = "Entrepôt & Contact",
                                colors = colors
                            )
                            if (transitaire.contact.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.Phone, null, tint = colors.cyanAccent, modifier = Modifier.size(18.dp))
                                    Text(transitaire.contact, fontFamily = Inter, fontSize = 13.sp, color = colors.textPrimary)
                                }
                            }
                            if (transitaire.chinaWarehouseAddress.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, null, tint = colors.violetAccent, modifier = Modifier.size(18.dp))
                                    Text(transitaire.chinaWarehouseAddress, fontFamily = Inter, fontSize = 13.sp, color = colors.textPrimary)
                                }
                            }
                            if (transitaire.internalNotes.isNotBlank()) {
                                Text(transitaire.internalNotes, fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                            }
                        }
                    }

                    // 6. Colis récents confiés
                    if (transitaire.recentShipments.isNotEmpty()) {
                        HumanSectionHeader(
                            title = "Colis confiés",
                            subtitle = "${transitaire.recentShipments.size} derniers lots",
                            colors = colors
                        )
                        HumanSectionCard(colors = colors, contentPadding = PaddingValues(16.dp)) {
                            transitaire.recentShipments.forEachIndexed { idx, ship ->
                                if (idx > 0) HumanHairline(colors)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onOpenLot(ship.id) }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ship.title,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${ship.dateLabel} · ${ship.statusLabel}" +
                                                if (ship.totalTransitDays != null) " · ${ship.totalTransitDays} j" else "",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                    Text(
                                        text = if (ship.freightCostAr > 0L) CurrencyFormatter.formatCompactAriary(ship.freightCostAr) else "—",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.cyanAccent
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showResetHistoryConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetHistoryConfirm = false },
            title = {
                Text(
                    text = "Réinitialiser l'historique ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Tous les anciens historiques seront supprimés et remplacés uniquement par les prix actuels Aérien et Maritime en cours.",
                    fontFamily = Inter
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showResetHistoryConfirm = false
                        onResetHistory()
                    }
                ) {
                    Text("Réinitialiser", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = colors.dangerAccent)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showResetHistoryConfirm = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }
}

@Composable
private fun HeroMetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1
            )
            Text(
                text = value,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TariffLineCard(
    item: TariffItem,
    colors: TransitaireColors,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isMaritime = item.mode == "MARITIME"
    val accent = if (isMaritime) colors.violetAccent else colors.cyanAccent
    val softBg = if (isMaritime) colors.violetSoftBg else colors.cyanSoftBg
    val unit = if (isMaritime) "m³" else "kg"
    val condLabel = remember(item) { TariffGridParser.formatCondition(item) }

    HumanSectionCard(
        colors = colors,
        contentPadding = PaddingValues(14.dp),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HumanIconBadge(
                icon = if (isMaritime) Icons.Default.DirectionsBoat else Icons.Default.FlightTakeoff,
                tint = accent,
                backgroundColor = softBg
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.shipmentType.ifBlank { if (isMaritime) "Maritime" else "Aérien" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (condLabel.isNotBlank()) {
                        HumanStatusPill(
                            text = condLabel,
                            backgroundColor = softBg,
                            contentColor = accent
                        )
                    }
                }
                Text(
                    text = buildString {
                        append(item.goodsCategory.ifBlank { "Marchandise Générale" })
                        if (item.leadDays.isNotBlank()) append(" · ${item.leadDays}")
                        if (item.effectiveDate.isNotBlank()) append(" · maj ${item.effectiveDate}")
                    },
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${CurrencyFormatter.formatAriary(item.priceAr)}/$unit",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = accent
                )
                if (item.usdPrice != null && item.usdPrice > 0.0) {
                    Text(
                        text = String.format(Locale.US, "$%.0f/%s", item.usdPrice, unit),
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.DeleteOutline, "Supprimer ce tarif", tint = colors.dangerAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun RateHistoryRow(
    entry: TransitaireRateHistoryEntity,
    colors: TransitaireColors
) {
    val isMaritime = entry.mode == "MARITIME"
    val modeLabel = if (isMaritime) "Maritime" else "Aérien"
    val unitLabel = if (isMaritime) "m³" else "kg"
    val accent = if (isMaritime) colors.violetAccent else colors.cyanAccent
    val isCurrent = entry.endDateLabel.isBlank()

    val periodLabel = if (isCurrent) {
        "À partir du ${entry.effectiveDateLabel}"
    } else {
        "Du ${entry.effectiveDateLabel} au ${entry.endDateLabel}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = modeLabel,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
                HumanStatusPill(
                    text = if (isCurrent) "En cours" else "Ancien",
                    backgroundColor = if (isCurrent) colors.successSoftBg else colors.badgeNeutralBg,
                    contentColor = if (isCurrent) colors.successAccent else colors.textSecondary
                )
            }
            Text(
                text = periodLabel,
                fontFamily = Inter,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }
        Text(
            text = "${CurrencyFormatter.formatAriary(entry.newRateAr)}/$unitLabel",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = accent
        )
    }
}
