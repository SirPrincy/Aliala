package com.example.ui.business.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessNavigationState
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.BusinessAdaptiveGrid
import com.example.ui.business.shared.HumanChipRow
import com.example.ui.business.shared.HumanEmptyState
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanIconBadge
import com.example.ui.business.shared.HumanSearchBar
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSegmentedSwitch
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.ProductVisualThumb
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.formatYuan
import com.example.ui.business.shared.isLotArrivedOrStocked
import com.example.ui.business.shared.isLotInTransit
import com.example.ui.business.shared.lotStatusLabel
import com.example.ui.business.shared.lotStatusTone
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.BordereauGroupUi
import com.example.util.BordereauUtils
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.util.Locale

@Composable
internal fun PurchasesTab(
    uiState: BusinessUiState,
    navState: BusinessNavigationState,
    onSearchQueryChanged: (String) -> Unit
) {
    val colors = rememberTransitaireColors()
    var viewSegment by remember { mutableStateOf("LOTS") } // "LOTS" or "BORDEREAUX"
    var statusPill by remember { mutableStateOf("Tous") }

    val baseOrders = remember(uiState.orders, uiState.purchasesSearchQuery) {
        val actual = uiState.orders.filter { it.status != "CATALOGUE" }
        val q = uiState.purchasesSearchQuery.trim()
        if (q.isBlank()) {
            actual
        } else {
            actual.filter { order ->
                order.product.contains(q, ignoreCase = true) ||
                    order.referenceCode.contains(q, ignoreCase = true) ||
                    order.carrier.contains(q, ignoreCase = true) ||
                    order.platform.contains(q, ignoreCase = true) ||
                    order.lotNumber.contains(q, ignoreCase = true) ||
                    (BordereauUtils.resolveCodeOrNull(order)?.contains(q, ignoreCase = true) == true)
            }
        }.sortedByDescending { it.orderDate }
    }

    val displayedOrders = remember(baseOrders, statusPill) {
        when (statusPill) {
            "Commandé" -> baseOrders.filter { it.status in setOf("DRAFT", "ORDERED", "WAREHOUSE") }
            "En route" -> baseOrders.filter { isLotInTransit(it.status) && it.status !in setOf("ORDERED", "WAREHOUSE") }
            "Arrivé" -> baseOrders.filter { isLotArrivedOrStocked(it.status) }
            else -> baseOrders
        }
    }

    val bordereaux = remember(baseOrders) {
        BordereauUtils.groupOrders(baseOrders)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 2 KPIs calmes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HumanSectionCard(
                colors = colors,
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Lots en cours",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = "${uiState.activeLotsCount}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = colors.cyanAccent
                )
            }
            HumanSectionCard(
                colors = colors,
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "En route (Ar)",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = CurrencyFormatter.formatCompactAriary(uiState.inRouteAmountAr),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = colors.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = formatYuan(uiState.inRouteYuans),
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }

        HumanSearchBar(
            query = uiState.purchasesSearchQuery,
            onQueryChange = onSearchQueryChanged,
            placeholder = "Rechercher un lot, transitaire, AR/TC...",
            colors = colors,
            modifier = Modifier.testTag("purchases_search_bar")
        )

        HumanSegmentedSwitch(
            options = listOf("LOTS" to "Lots", "BORDEREAUX" to "Bordereaux (${bordereaux.size})"),
            selectedKey = viewSegment,
            onSelect = { viewSegment = it },
            colors = colors
        )

        if (viewSegment == "LOTS") {
            HumanChipRow(
                items = listOf(
                    "Tous" to "Tous (${baseOrders.size})",
                    "Commandé" to "Commandé",
                    "En route" to "En route",
                    "Arrivé" to "Arrivé"
                ),
                selectedKey = statusPill,
                onSelect = { statusPill = it },
                colors = colors
            )

            if (displayedOrders.isEmpty()) {
                HumanEmptyState(
                    icon = Icons.Default.ShoppingBag,
                    title = "Aucun lot trouvé",
                    subtitle = "Enregistrez votre premier achat fournisseur pour suivre son acheminement jusqu'à Madagascar.",
                    actionLabel = "Nouvel achat",
                    onActionClick = { navState.activeSheet = BusinessActiveSheet.OrderForm() },
                    colors = colors
                )
            } else {
                BusinessAdaptiveGrid(items = displayedOrders) { order ->
                    LotSummaryCard(
                        order = order,
                        colors = colors,
                        onClick = { navState.openLot(order.id) }
                    )
                }
            }
        } else {
            if (bordereaux.isEmpty()) {
                HumanEmptyState(
                    icon = Icons.Default.LocalShipping,
                    title = "Aucun bordereau d'expédition",
                    subtitle = "Les bordereaux TC (maritime) et AR (aérien) apparaîtront dès qu'un lot sera expédié.",
                    colors = colors
                )
            } else {
                BusinessAdaptiveGrid(items = bordereaux) { group ->
                    BordereauSummaryCard(
                        group = group,
                        colors = colors,
                        onOrderClick = { order -> navState.openLot(order.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LotSummaryCard(
    order: BusinessOrderEntity,
    colors: TransitaireColors,
    onClick: () -> Unit
) {
    val (statusBg, statusFg) = lotStatusTone(order.status, colors)
    val bordereau = BordereauUtils.resolveCodeOrNull(order)
    val totalYuan = (order.priceYuan * order.quantityBought) + order.domesticShippingYuan

    HumanSectionCard(
        colors = colors,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick,
        modifier = Modifier.testTag("lot_card_${order.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductVisualThumb(
                imageUrl = order.imageUrl,
                productName = order.product,
                family = order.family,
                colors = colors,
                size = 50.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.product,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    HumanStatusPill(
                        text = lotStatusLabel(order.status),
                        backgroundColor = statusBg,
                        contentColor = statusFg
                    )
                }

                Text(
                    text = buildString {
                        append("${order.quantityBought} pcs")
                        if (order.platform.isNotBlank()) append(" · ${order.platform}")
                        if (order.carrier.isNotBlank()) append(" · ${order.carrier}")
                        if (!bordereau.isNullOrBlank()) append(" · $bordereau")
                    },
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HumanHairline(colors)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (totalYuan > 0.0) formatYuan(totalYuan) else CurrencyFormatter.formatAriary(order.freightCostAr),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Text(
                    text = DateUtils.formatShortDate(order.orderDate) +
                        if (order.yuanDebited) " · Payé ¥" else " · À payer ¥",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = if (order.yuanDebited) colors.successAccent else colors.warningAccent
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Gérer",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colors.cyanAccent
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.cyanAccent
                )
            }
        }
    }
}

@Composable
private fun BordereauSummaryCard(
    group: BordereauGroupUi,
    colors: TransitaireColors,
    onOrderClick: (BusinessOrderEntity) -> Unit
) {
    HumanSectionCard(
        colors = colors,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                HumanIconBadge(
                    icon = if (group.isMaritime) Icons.Default.DirectionsBoat else Icons.Default.FlightTakeoff,
                    tint = if (group.isMaritime) colors.violetAccent else colors.cyanAccent,
                    backgroundColor = if (group.isMaritime) colors.violetSoftBg else colors.cyanSoftBg
                )
                Column {
                    Text(
                        text = group.code,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${group.carrier} · ${group.totalQuantity} pcs",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HumanStatusPill(
                text = group.statusLabel,
                backgroundColor = if (group.isInTransit) colors.expressSoftBg else colors.successSoftBg,
                contentColor = if (group.isInTransit) colors.expressAccent else colors.successAccent
            )
        }

        val measureStr = buildString {
            if (group.totalWeightKg > 0.0) append("${String.format(Locale.FRANCE, "%.1f", group.totalWeightKg)} kg")
            if (group.totalVolumeM3 > 0.0) {
                if (isNotEmpty()) append(" · ")
                append("${String.format(Locale.FRANCE, "%.3f", group.totalVolumeM3)} m³")
            }
            group.estimatedArrivalMillis?.let {
                if (isNotEmpty()) append(" · ")
                append("Arrivée ${DateUtils.formatShortDate(it)}")
            }
        }
        if (measureStr.isNotBlank()) {
            Text(
                text = measureStr,
                fontFamily = Inter,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }

        HumanHairline(colors)

        group.orders.forEachIndexed { idx, order ->
            if (idx > 0) Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.product} (${order.quantityBought})",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.TextButton(
                    onClick = { onOrderClick(order) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Ouvrir",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.cyanAccent
                    )
                }
            }
        }
    }
}
