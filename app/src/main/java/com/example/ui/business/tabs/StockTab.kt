package com.example.ui.business.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessNavigationState
import com.example.ui.business.BusinessUiState
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.shared.BusinessAdaptiveGrid
import com.example.ui.business.shared.HumanChipRow
import com.example.ui.business.shared.HumanEmptyState
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanSearchBar
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSegmentedSwitch
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.ProductVisualThumb
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.formatYuan
import com.example.ui.business.shared.lotStatusLabel
import com.example.ui.business.shared.lotStatusTone
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun StockTab(
    uiState: BusinessUiState,
    navState: BusinessNavigationState,
    onSegmentSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onFamilyFilterSelected: (String) -> Unit,
    onToggleShowHidden: () -> Unit
) {
    val colors = rememberTransitaireColors()

    // Harmonise segment mapping with BusinessOperationsMapper ("Disponible", "En route", plus "Catalogue")
    val activeSegment = when (uiState.stockSegment) {
        "En route" -> "En route"
        "Catalogue" -> "Catalogue"
        else -> "Disponible"
    }

    val filteredCatalogueArticles = remember(
        uiState.allCatalogueArticles,
        uiState.stockSearchQuery,
        uiState.stockFamilyFilter,
        uiState.showHiddenStock
    ) {
        val q = uiState.stockSearchQuery.trim()
        uiState.allCatalogueArticles.filter { art ->
            val matchHidden = !art.isHidden || uiState.showHiddenStock
            val matchSearch = if (q.isBlank()) true else {
                art.primaryOrder.product.contains(q, ignoreCase = true) ||
                    art.primaryOrder.referenceCode.contains(q, ignoreCase = true) ||
                    art.primaryOrder.family.contains(q, ignoreCase = true) ||
                    art.primaryOrder.variants.contains(q, ignoreCase = true)
            }
            val matchFamily = if (uiState.stockFamilyFilter == "Toutes") true else {
                art.primaryOrder.family.equals(uiState.stockFamilyFilter, ignoreCase = true)
            }
            matchHidden && matchSearch && matchFamily
        }
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
                    text = "Valeur du stock",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = CurrencyFormatter.formatCompactAriary(uiState.totalStockValueAr),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = colors.cyanAccent,
                    maxLines = 1
                )
            }
            HumanSectionCard(
                colors = colors,
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Pièces disponibles",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = "${uiState.availableStockPieces}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = colors.textPrimary
                )
                if (uiState.reservedClientsPieces > 0 || uiState.inRouteStockUnits > 0) {
                    Text(
                        text = buildString {
                            if (uiState.reservedClientsPieces > 0) append("${uiState.reservedClientsPieces} réservées")
                            if (uiState.inRouteStockUnits > 0) {
                                if (isNotEmpty()) append(" · ")
                                append("${uiState.inRouteStockUnits} en transit")
                            }
                        },
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Switch unifié Stock + Catalogue
        HumanSegmentedSwitch(
            options = listOf(
                "Disponible" to "En stock",
                "En route" to "En transit (${uiState.inRouteStockOrders.size})",
                "Catalogue" to "Catalogue"
            ),
            selectedKey = activeSegment,
            onSelect = onSegmentSelected,
            colors = colors
        )

        HumanSearchBar(
            query = uiState.stockSearchQuery,
            onQueryChange = onSearchQueryChanged,
            placeholder = if (activeSegment == "Catalogue") "Rechercher un article, SKU..." else "Rechercher dans le stock...",
            colors = colors,
            modifier = Modifier.testTag("stock_search_bar")
        )

        // Pilules familles
        val familyOptions = if (activeSegment == "Catalogue") {
            listOf("Toutes" to "Toutes") + uiState.catalogueFamilies.map { it.name to "${it.emoji} ${it.name}" }
        } else if (activeSegment == "Disponible" && uiState.stockFamilyPills.isNotEmpty()) {
            uiState.stockFamilyPills.map { it.name to "${it.name} (${it.count})" }
        } else {
            emptyList()
        }

        if (familyOptions.size > 1) {
            HumanChipRow(
                items = familyOptions,
                selectedKey = uiState.stockFamilyFilter,
                onSelect = onFamilyFilterSelected,
                colors = colors
            )
        }

        if (activeSegment == "Catalogue") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (uiState.showHiddenStock) colors.violetSoftBg else colors.cardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onToggleShowHidden() }
                        .testTag("btn_toggle_hidden_stock")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.showHiddenStock) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (uiState.showHiddenStock) colors.violetAccent else colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (uiState.showHiddenStock) "Masqués visibles" else "Masqués (${uiState.hiddenStockCount})",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                HumanTonalButton(
                    text = "Nouvel article",
                    icon = Icons.Default.Add,
                    onClick = { navState.activeSheet = BusinessActiveSheet.AddArticle },
                    colors = colors,
                    modifier = Modifier.testTag("btn_new_catalogue_article")
                )
            }
        }

        when (activeSegment) {
            "Disponible" -> {
                val items = uiState.visibleStockOrders
                if (items.isEmpty()) {
                    HumanEmptyState(
                        icon = Icons.Default.Inventory2,
                        title = "Aucune pièce en stock",
                        subtitle = "Clôturez un lot arrivé pour alimenter votre stock ou créez une fiche dans le Catalogue.",
                        actionLabel = "Voir le catalogue",
                        onActionClick = { onSegmentSelected("Catalogue") },
                        colors = colors
                    )
                } else {
                    BusinessAdaptiveGrid(items = items) { order ->
                        StockOrderCard(
                            order = order,
                            colors = colors,
                            onCardClick = {
                                val matchedArt = uiState.allCatalogueArticles.firstOrNull { art ->
                                    art.primaryOrder.id == order.id || art.lots.any { it.id == order.id }
                                }
                                if (matchedArt != null) navState.openProduct(matchedArt.articleKey)
                                else navState.openLot(order.id)
                            },
                            onSellClick = { navState.activeSheet = BusinessActiveSheet.AddSale(order) },
                            onAdjustClick = { navState.activeSheet = BusinessActiveSheet.AdjustStock(order) }
                        )
                    }
                }
            }

            "En route" -> {
                val items = uiState.visibleStockOrders
                if (items.isEmpty()) {
                    HumanEmptyState(
                        icon = Icons.Default.Inventory2,
                        title = "Aucun stock en transit",
                        subtitle = "Toutes vos commandes sont déjà arrivées ou n'ont pas encore été expédiées.",
                        colors = colors
                    )
                } else {
                    BusinessAdaptiveGrid(items = items) { order ->
                        InTransitStockCard(
                            order = order,
                            colors = colors,
                            onClick = { navState.openLot(order.id) }
                        )
                    }
                }
            }

            else -> {
                if (filteredCatalogueArticles.isEmpty()) {
                    HumanEmptyState(
                        icon = Icons.Default.Inventory2,
                        title = "Catalogue vide",
                        subtitle = "Ajoutez des fiches produits avec photo, variantes et prix cible pour commander et vendre plus vite.",
                        actionLabel = "Ajouter un article",
                        onActionClick = { navState.activeSheet = BusinessActiveSheet.AddArticle },
                        colors = colors
                    )
                } else {
                    BusinessAdaptiveGrid(items = filteredCatalogueArticles) { article ->
                        CatalogueArticleCard(
                            article = article,
                            colors = colors,
                            onClick = { navState.openProduct(article.articleKey) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StockOrderCard(
    order: BusinessOrderEntity,
    colors: TransitaireColors,
    onCardClick: () -> Unit,
    onSellClick: () -> Unit,
    onAdjustClick: () -> Unit
) {
    val unitCost = if (order.unitCostAr > 0L) order.unitCostAr
    else ((order.priceYuan * order.exchangeRate).toLong() + if (order.quantityBought > 0) order.freightCostAr / order.quantityBought else 0L)
    val marginPct = if (order.resalePriceAr > 0L && unitCost > 0L) {
        (((order.resalePriceAr - unitCost).toDouble() / order.resalePriceAr.toDouble()) * 100).toInt()
    } else order.marginPercent

    HumanSectionCard(
        colors = colors,
        contentPadding = PaddingValues(16.dp),
        onClick = onCardClick,
        modifier = Modifier.testTag("stock_card_${order.id}")
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
                size = 54.dp
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
                        text = "${order.stockCount} dispo",
                        backgroundColor = if (order.stockCount <= 2) colors.warningSoftBg else colors.successSoftBg,
                        contentColor = if (order.stockCount <= 2) colors.warningAccent else colors.successAccent
                    )
                }

                Text(
                    text = buildString {
                        append(order.family.ifBlank { "Article" })
                        if (order.variants.isNotBlank()) append(" · ${order.variants}")
                        if (order.reservedCount > 0) append(" · ${order.reservedCount} rés.")
                    },
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Revente ${CurrencyFormatter.formatAriary(order.resalePriceAr)}" +
                        if (marginPct > 0) " · Marge $marginPct%" else "",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colors.cyanAccent
                )
            }
        }

        HumanHairline(colors)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HumanTonalButton(
                text = "Vendre",
                icon = Icons.Default.PointOfSale,
                onClick = onSellClick,
                colors = colors,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_sell_stock_${order.id}")
            )
            HumanTonalButton(
                text = "Ajuster",
                icon = Icons.Default.Tune,
                onClick = onAdjustClick,
                colors = colors,
                containerColor = colors.innerCardBg,
                contentColor = colors.textPrimary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_adjust_stock_${order.id}")
            )
        }
    }
}

@Composable
private fun InTransitStockCard(
    order: BusinessOrderEntity,
    colors: TransitaireColors,
    onClick: () -> Unit
) {
    val (bg, fg) = lotStatusTone(order.status, colors)
    HumanSectionCard(
        colors = colors,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick
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
                size = 48.dp
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = order.product,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${order.quantityBought} pcs en route · ${order.carrier.ifBlank { "Transitaire" }}",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            HumanStatusPill(
                text = lotStatusLabel(order.status),
                backgroundColor = bg,
                contentColor = fg
            )
        }
    }
}

@Composable
private fun CatalogueArticleCard(
    article: CatalogueArticleSummaryUi,
    colors: TransitaireColors,
    onClick: () -> Unit
) {
    val order = article.primaryOrder
    HumanSectionCard(
        colors = colors,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick,
        modifier = Modifier.testTag("catalogue_article_${article.articleKey}")
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
                size = 54.dp
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
                        text = if (article.totalStockCount > 0) "${article.totalStockCount} en stock" else article.statusTag,
                        backgroundColor = if (article.totalStockCount > 0) colors.successSoftBg else colors.badgeNeutralBg,
                        contentColor = if (article.totalStockCount > 0) colors.successAccent else colors.textSecondary
                    )
                }

                Text(
                    text = buildString {
                        if (order.referenceCode.isNotBlank()) append("${order.referenceCode} · ")
                        append(order.family.ifBlank { "Divers" })
                        append(" · ${article.lotsCount} lot${if (article.lotsCount > 1) "s" else ""}")
                    },
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (article.resalePriceAr > 0L) CurrencyFormatter.formatAriary(article.resalePriceAr) else "Prix à définir",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.cyanAccent
                    )
                    if (article.latestPriceYuan > 0.0) {
                        Text(
                            text = "${formatYuan(article.latestPriceYuan)} · ${article.marginPercent}% marge",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
