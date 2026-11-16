package com.example.ui.business.purchases

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun PurchasesView(
    uiState: BusinessUiState,
    onStatusFilterSelected: (String) -> Unit,
    onCategoryFilterSelected: (String) -> Unit,
    onSortFilterSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit = {},
    onNewPurchaseClick: () -> Unit,
    onBuyYuansClick: () -> Unit,
    onAdvanceQualityControl: (BusinessOrderEntity) -> Unit,
    onOrderClick: (BusinessOrderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberTransitaireColors()
    val statusScrollState = rememberScrollState()
    val categoryScrollState = rememberScrollState()
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    val activeYuanNames = remember(uiState.businessAccounts) {
        uiState.businessAccounts.filter { it.isYuanAccount && it.isActive }.map { it.name.trim() }.filter { it.isNotBlank() }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Achats",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.warningSoftBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Chine · Tana",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = colors.warningAccent
                        )
                    }
                }
                Text(
                    text = "Suivi du sourcing et logistique fret",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSearchExpanded || uiState.purchasesSearchQuery.isNotBlank()) colors.cyanSoftBg
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable {
                            if (isSearchExpanded && uiState.purchasesSearchQuery.isNotBlank()) {
                                onSearchQueryChanged("")
                            }
                            isSearchExpanded = !isSearchExpanded
                        }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Recherche",
                        tint = if (isSearchExpanded || uiState.purchasesSearchQuery.isNotBlank()) colors.cyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box {
                    val hasActiveFilter = uiState.purchasesStatusFilter != "Tous" || uiState.purchasesCategoryFilter != "Toutes catégories"
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (hasActiveFilter) colors.violetSoftBg else MaterialTheme.colorScheme.surface)
                            .clickable { filterMenuExpanded = true }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filtres avancés",
                            tint = if (hasActiveFilter) colors.violetAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false }
                    ) {
                        listOf("Tous", "Brouillon", "Commandé", "Vers Mada", "Contrôle qualité", "Terminé").forEach { st ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (uiState.purchasesStatusFilter == st) "✓ Statut : $st" else "Statut : $st",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp
                                    )
                                },
                                onClick = {
                                    onStatusFilterSelected(st)
                                    filterMenuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Réinitialiser les filtres",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = colors.cyanAccent
                                )
                            },
                            onClick = {
                                onStatusFilterSelected("Tous")
                                onCategoryFilterSelected("Toutes catégories")
                                onSortFilterSelected("Date récent")
                                onSearchQueryChanged("")
                                filterMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (isSearchExpanded || uiState.purchasesSearchQuery.isNotBlank()) {
            OutlinedTextField(
                value = uiState.purchasesSearchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Rechercher produit, SKU, lot, transitaire...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (uiState.purchasesSearchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Effacer", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = colors.border
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("purchases_search_input")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "En cours",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0EA5E9))
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${uiState.activeLotsCount}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "lots",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Brouillons",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF94A3B8))
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${uiState.draftActionsCount}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.draftActionsCount > 1) "actions" else "action",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = colors.warningAccent
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "En route",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Column {
                        Text(
                            text = CurrencyFormatter.formatCompactAriary(uiState.inRouteAmountAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "≈ ¥ ${uiState.inRouteYuans.toInt()}",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(statusScrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val countDraft = uiState.orders.count { it.status == "DRAFT" }
            val countOrdered = uiState.orders.count { it.status == "ORDERED" }
            val countWarehouse = uiState.orders.count { it.status == "WAREHOUSE" }
            val countGuangzhou = uiState.orders.count { it.status == "GUANGZHOU" }
            val countFlight = uiState.orders.count { it.status == "FLIGHT" }
            val countCustoms = uiState.orders.count { it.status == "QUALITY_CONTROL" || it.status == "CUSTOMS" }
            val countDone = uiState.orders.count { it.status in listOf("DELIVERY_READY", "IN_STOCK", "COMPLETED", "SOLD") }

            val statusItems = listOf(
                "Tous" to uiState.orders.size.toString() to Color(0xFFF97316),
                "Brouillon" to (if (countDraft > 0) countDraft.toString() else null) to Color(0xFF64748B),
                "Commandé" to (if (countOrdered > 0) countOrdered.toString() else null) to Color(0xFF2563EB),
                "En livraison" to (if (countWarehouse > 0) countWarehouse.toString() else null) to Color(0xFF06B6D4),
                "Arrivé entrepôt" to (if (countGuangzhou > 0) countGuangzhou.toString() else null) to Color(0xFF6366F1),
                "Vers Mada" to (if (countFlight > 0) countFlight.toString() else null) to Color(0xFF9333EA),
                "Contrôle qualité" to (if (countCustoms > 0) countCustoms.toString() else null) to Color(0xFFEC4899),
                "Terminé" to (if (countDone > 0) countDone.toString() else null) to Color(0xFF10B981)
            )

            statusItems.forEach { (labelCount, dotColor) ->
                val (label, count) = labelCount
                val isSelected = uiState.purchasesStatusFilter == label

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFFF97316)
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onStatusFilterSelected(label) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (!isSelected && label != "Tous") {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }

                        Text(
                            text = label,
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )

                        count?.let {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color.White.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = it,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White else dotColor
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(categoryScrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Toutes catégories",
                    "Business",
                    "Pour moi",
                    "Famille & amis"
                ).forEach { cat ->
                    val isSelected = uiState.purchasesCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { onCategoryFilterSelected(cat) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { sortMenuExpanded = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.purchasesSortFilter,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    listOf("Date récent", "Prix ¥ décroissant", "Prix Ar décroissant").forEach { sortOption ->
                        DropdownMenuItem(
                            text = { Text(sortOption, fontFamily = PlusJakartaSans, fontSize = 12.sp) },
                            onClick = {
                                onSortFilterSelected(sortOption)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.filteredPurchases.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun achat ne correspond aux filtres",
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
            } else {
                uiState.filteredPurchases.forEach { order ->
                    PurchaseDetailedCard(
                        order = order,
                        availableYuans = uiState.availableYuans,
                        activeYuanNames = activeYuanNames,
                        onOrderClick = { onOrderClick(order) },
                        onBuyYuansClick = onBuyYuansClick,
                        onQualityControlClick = { onAdvanceQualityControl(order) }
                    )
                }
            }
        }
    }
}
