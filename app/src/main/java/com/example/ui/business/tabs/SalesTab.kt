package com.example.ui.business.tabs

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessSaleEntity
import com.example.ui.business.BusinessActiveSheet
import com.example.ui.business.BusinessNavigationState
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.BusinessAdaptiveGrid
import com.example.ui.business.shared.HumanChipRow
import com.example.ui.business.shared.HumanEmptyState
import com.example.ui.business.shared.HumanHairline
import com.example.ui.business.shared.HumanHeroCard
import com.example.ui.business.shared.HumanProgressBar
import com.example.ui.business.shared.HumanSearchBar
import com.example.ui.business.shared.HumanSectionCard
import com.example.ui.business.shared.HumanSectionHeader
import com.example.ui.business.shared.HumanSegmentedSwitch
import com.example.ui.business.shared.HumanStatusPill
import com.example.ui.business.shared.HumanTonalButton
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
internal fun SalesTab(
    uiState: BusinessUiState,
    navState: BusinessNavigationState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDomainSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onDeleteSale: (BusinessSaleEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    var showSearch by remember { mutableStateOf(false) }
    var statusFilter by remember { mutableStateOf("Toutes") }

    val displayedSales = remember(uiState.visibleSales, statusFilter) {
        when (statusFilter) {
            "À encaisser" -> uiState.visibleSales.filter { !it.isFullyPaid && it.totalPriceAr > it.paidAmountAr }
            "Réglées" -> uiState.visibleSales.filter { it.isFullyPaid || it.paidAmountAr >= it.totalPriceAr }
            else -> uiState.visibleSales
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Barre mois + bouton recherche
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = colors.cardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    IconButton(onClick = onPrevMonth, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Mois précédent", tint = colors.textPrimary)
                    }
                    Text(
                        text = uiState.selectedMonthLabel.ifBlank { "Ce mois" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Mois suivant", tint = colors.textPrimary)
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = if (showSearch || uiState.salesSearchQuery.isNotBlank()) colors.cyanSoftBg else colors.cardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { showSearch = !showSearch }
                    .testTag("btn_toggle_sales_search")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher",
                        tint = if (showSearch) colors.cyanAccent else colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Rechercher",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (showSearch) colors.cyanAccent else colors.textPrimary
                    )
                }
            }
        }

        AnimatedVisibility(visible = showSearch || uiState.salesSearchQuery.isNotBlank()) {
            HumanSearchBar(
                query = uiState.salesSearchQuery,
                onQueryChange = onSearchQueryChanged,
                placeholder = "Client, article ou facture...",
                colors = colors,
                modifier = Modifier.testTag("sales_search_bar")
            )
        }

        // Hero Ventes
        HumanHeroCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (uiState.salesDomain == "BUSINESS") "Encaissé · Business Pro" else "Encaissé · Famille & Amis",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = CurrencyFormatter.formatAriary(uiState.salesTotalCollectedAr),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                }
                val trend = uiState.salesGrowthVsPrevMonthPercent
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.18f)) {
                    Text(
                        text = "${if (trend >= 0) "+" else ""}$trend% vs ${uiState.prevMonthShortLabel.ifBlank { "mois préc." }}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Bénéfice net", fontFamily = Inter, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            CurrencyFormatter.formatCompactAriary(uiState.salesNetProfitAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("À recevoir (${uiState.salesOpenInvoicesCount})", fontFamily = Inter, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            CurrencyFormatter.formatCompactAriary(uiState.salesRemainingToCollectAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Switch Domaine
        HumanSegmentedSwitch(
            options = listOf("BUSINESS" to "Business Pro", "FAMILY_FRIENDS" to "Famille & Amis"),
            selectedKey = uiState.salesDomain,
            onSelect = onDomainSelected,
            colors = colors
        )

        // 3 filtres simplifiés
        HumanChipRow(
            items = listOf(
                "Toutes" to "Toutes (${uiState.visibleSales.size})",
                "À encaisser" to "À encaisser (${uiState.visibleSales.count { !it.isFullyPaid && it.totalPriceAr > it.paidAmountAr }})",
                "Réglées" to "Réglées"
            ),
            selectedKey = statusFilter,
            onSelect = { statusFilter = it },
            colors = colors
        )

        // Commandes client réservées
        if (uiState.pendingCustomerOrders.isNotEmpty()) {
            HumanSectionCard(
                colors = colors,
                contentPadding = PaddingValues(14.dp)
            ) {
                HumanSectionHeader(
                    title = "Commandes réservées (${uiState.pendingCustomerOrders.size})",
                    subtitle = "Lots en transit attribués à un client",
                    colors = colors
                )
                uiState.pendingCustomerOrders.take(3).forEachIndexed { idx, ord ->
                    if (idx > 0) HumanHairline(colors)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navState.openLot(ord.id) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${ord.assignedClient} · ${ord.product}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${ord.reservedCount.coerceAtLeast(ord.quantityBought)} pcs",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                        HumanStatusPill(
                            text = "Voir le lot",
                            backgroundColor = colors.cyanSoftBg,
                            contentColor = colors.cyanAccent
                        )
                    }
                }
            }
        }

        if (displayedSales.isEmpty()) {
            HumanEmptyState(
                icon = Icons.Default.PointOfSale,
                title = "Aucune facture dans cette vue",
                subtitle = "Créez une nouvelle vente pour générer automatiquement une facture et déduire le stock.",
                actionLabel = "Nouvelle vente",
                onActionClick = { navState.activeSheet = BusinessActiveSheet.AddSale() },
                colors = colors
            )
        } else {
            BusinessAdaptiveGrid(items = displayedSales) { sale ->
                SaleInvoiceCard(
                    sale = sale,
                    colors = colors,
                    onRecordPayment = { navState.activeSheet = BusinessActiveSheet.RecordSalePayment(sale) },
                    onRelance = {
                        val rem = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
                        navState.openClientRelanceByName(
                            clients = uiState.allClients,
                            name = sale.clientName,
                            fallbackDue = rem,
                            fallbackPhone = sale.clientPhone
                        )
                    },
                    onDelete = { onDeleteSale(sale) }
                )
            }
        }
    }
}

@Composable
private fun SaleInvoiceCard(
    sale: BusinessSaleEntity,
    colors: TransitaireColors,
    onRecordPayment: () -> Unit,
    onRelance: () -> Unit,
    onDelete: () -> Unit
) {
    val remaining = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
    val isPaid = sale.isFullyPaid || remaining == 0L
    val progress = if (sale.totalPriceAr > 0L) (sale.paidAmountAr.toFloat() / sale.totalPriceAr.toFloat()).coerceIn(0f, 1f) else 1f
    val profit = sale.totalPriceAr - (sale.unitCostAr * sale.quantity)

    HumanSectionCard(
        colors = colors,
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.testTag("sale_card_${sale.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = sale.clientName.ifBlank { "Client" },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${sale.productName} · ${sale.quantity} pcs · ${DateUtils.formatShortDate(sale.saleDate)}",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            HumanStatusPill(
                text = if (isPaid) "Réglée" else "Reste ${CurrencyFormatter.formatCompactAriary(remaining)}",
                backgroundColor = if (isPaid) colors.successSoftBg else colors.warningSoftBg,
                contentColor = if (isPaid) colors.successAccent else colors.warningAccent
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${CurrencyFormatter.formatAriary(sale.paidAmountAr)} / ${CurrencyFormatter.formatAriary(sale.totalPriceAr)}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
                if (sale.unitCostAr > 0L) {
                    Text(
                        text = "Bénéf. ${CurrencyFormatter.formatCompactAriary(profit)}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = if (profit >= 0L) colors.successAccent else colors.dangerAccent
                    )
                }
            }
            HumanProgressBar(
                progress = progress,
                trackColor = colors.innerCardBg,
                fillColor = if (isPaid) colors.successAccent else colors.cyanAccent
            )
        }

        HumanHairline(colors)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isPaid) {
                HumanTonalButton(
                    text = "Encaisser",
                    icon = Icons.Default.Payments,
                    onClick = onRecordPayment,
                    colors = colors,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_collect_sale_${sale.id}")
                )
                HumanTonalButton(
                    text = "Relancer",
                    icon = Icons.AutoMirrored.Filled.Send,
                    onClick = onRelance,
                    colors = colors,
                    containerColor = colors.violetSoftBg,
                    contentColor = colors.violetAccent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_relance_sale_${sale.id}")
                )
            } else {
                Text(
                    text = sale.invoiceNumber.ifBlank { "Facture réglée" },
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("btn_delete_sale_${sale.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Annuler la facture",
                    tint = colors.dangerAccent,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
