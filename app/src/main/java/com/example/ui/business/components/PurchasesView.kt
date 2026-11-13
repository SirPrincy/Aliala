package com.example.ui.business.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.BusinessUiState
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun PurchasesView(
    uiState: BusinessUiState,
    onStatusFilterSelected: (String) -> Unit,
    onCategoryFilterSelected: (String) -> Unit,
    onSortFilterSelected: (String) -> Unit,
    onNewPurchaseClick: () -> Unit,
    onBuyYuansClick: () -> Unit,
    onAdvanceQualityControl: (BusinessOrderEntity) -> Unit,
    onOrderClick: (BusinessOrderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusScrollState = rememberScrollState()
    val categoryScrollState = rememberScrollState()
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. EN-TÊTE DE SECTION (Titre & Actions Rapides)
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
                            .background(Color(0xFFFFEDD5))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Chine · Tana",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color(0xFFC2410C)
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
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Recherche",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filtres avancés",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. BANDEAU RÉCAPITULATIF (3 COLONNES)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Col 1: En cours
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
                                .background(Color(0xFF0EA5E9)) // sky-500
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

            // Col 2: Brouillons
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
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "action",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color(0xFFD97706)
                        )
                    }
                }
            }

            // Col 3: En route
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

        // 3. FILTRES HORIZONTAUX (LIGNE 1 : STATUTS)
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
            val countDone = uiState.orders.count { it.status in listOf("DELIVERY_READY", "SOLD") }

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

        // 4. FILTRES HORIZONTAUX (LIGNE 2 : CATÉGORIES & TRI)
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

        // 5. LISTE DES CARTES D'ACHATS
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.filteredPurchases.forEach { order ->
                PurchaseDetailedCard(
                    order = order,
                    availableYuans = uiState.availableYuans,
                    onOrderClick = { onOrderClick(order) },
                    onBuyYuansClick = onBuyYuansClick,
                    onQualityControlClick = { onAdvanceQualityControl(order) }
                )
            }
        }
    }
}

@Composable
private fun PurchaseDetailedCard(
    order: BusinessOrderEntity,
    availableYuans: Double,
    onOrderClick: () -> Unit,
    onBuyYuansClick: () -> Unit,
    onQualityControlClick: () -> Unit
) {
    val isDraft = order.status == "DRAFT"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .then(
                if (isDraft) Modifier.border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable { onOrderClick() }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top row: Thumbnail + Title + Ref + Category badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vignette with Platform overlay
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (order.imageUrl) {
                            "smartwatch" -> Icons.Default.Watch
                            "bag" -> Icons.Default.ShoppingBag
                            "cable" -> Icons.Default.Cable
                            "case" -> Icons.Default.PhoneAndroid
                            "bottle" -> Icons.Default.WaterDrop
                            else -> Icons.Default.Headphones
                        },
                        contentDescription = null,
                        tint = when (order.platform) {
                            "Taobao" -> Color(0xFFEA580C)
                            "Pinduoduo" -> Color(0xFFE11D48)
                            else -> Color(0xFFDC2626)
                        },
                        modifier = Modifier.size(30.dp)
                    )

                    // Platform badge overlay at the bottom
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                when (order.platform) {
                                    "Taobao" -> Color(0xFFEA580C)
                                    "Pinduoduo" -> Color(0xFFE11D48)
                                    else -> Color(0xFFDC2626)
                                }
                            )
                            .padding(vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = order.platform,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color.White
                        )
                    }
                }

                // Title + Ref + Color dots
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = order.product,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Category Pill
                        val (catBg, catText, catLabel) = when (order.category) {
                            "PERSONAL" -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "Pour moi")
                            "FAMILY_FRIENDS" -> Triple(Color(0xFFF3E8FF), Color(0xFF7E22CE), "Famille & amis")
                            else -> Triple(Color(0xFFECFEFF), Color(0xFF0E7490), "Business")
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(catBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = catLabel,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = catText
                            )
                        }
                    }

                    Text(
                        text = "Réf: ${order.referenceCode.ifBlank { "LOT-${order.id}" }}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Color variants row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = order.variants.ifBlank { "Quantité : ${order.quantityBought}" },
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Total: ${order.quantityBought} pcs",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Price & Conversion Strip
            val unitAr = (order.priceYuan * order.exchangeRate).toLong()
            val totalAr = if (order.unitCostAr > 0L) {
                order.unitCostAr * order.quantityBought
            } else {
                (unitAr * order.quantityBought + order.freightCostAr + order.customsTaxAr)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "¥ ${order.priceYuan.toInt()} × ${order.exchangeRate.toInt()} Ar = ${CurrencyFormatter.formatAriary(unitAr)}/u",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = CurrencyFormatter.formatAriary(totalAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isDraft) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(¥ ${(order.priceYuan * order.quantityBought).toInt()})",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Status Pill & delay subtext
                    Column(horizontalAlignment = Alignment.End) {
                        when (order.status) {
                            "DRAFT" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Brouillon",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                            "ORDERED" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFDBEAFE))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Commandé",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF1D4ED8)
                                    )
                                }
                                Text(
                                    text = "Fournisseur validé",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    color = Color(0xFF2563EB)
                                )
                            }
                            "GUANGZHOU" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE0E7FF))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Arrivé Guangzhou",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF4338CA)
                                    )
                                }
                                Text(
                                    text = "Départ vol jeudi",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    color = Color(0xFF4F46E5)
                                )
                            }
                            "FLIGHT" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF3E8FF))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Vers Mada (Étape 4/6)",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF7E22CE)
                                    )
                                }
                                Text(
                                    text = "Arrive dans 6 j",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    color = Color(0xFF9333EA)
                                )
                            }
                            "QUALITY_CONTROL" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFCE7F3))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Contrôle qualité",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFFBE185D)
                                    )
                                }
                                Text(
                                    text = "Arrivé Tana",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    color = Color(0xFFDB2777)
                                )
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Terminé",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Mini progress stepper for orders in transit
            if (order.status in listOf("ORDERED", "WAREHOUSE", "GUANGZHOU", "FLIGHT")) {
                val filledSteps = when (order.status) {
                    "ORDERED" -> 1
                    "WAREHOUSE" -> 2
                    "GUANGZHOU" -> 3
                    "FLIGHT" -> 4
                    else -> 2
                }
                val stepColor = if (order.status == "FLIGHT") Color(0xFF9333EA) else Color(0xFF4F46E5)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..6) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(if (i <= filledSteps) stepColor else Color(0xFFE2E8F0))
                        )
                    }
                }
            }

            // Quality control highlight button
            if (order.status == "QUALITY_CONTROL") {
                Button(
                    onClick = onQualityControlClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFCE7F3),
                        contentColor = Color(0xFFBE185D)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "À contrôler (${order.quantityBought} unités restantes)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Draft Warning & Buy Yuans button
            if (isDraft) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFFBEB))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                        val shortage = (order.priceYuan * order.quantityBought - availableYuans).coerceAtLeast(0.0).toInt()
                        Text(
                            text = if (shortage > 0) "Yuans insuffisants : il manque ¥ $shortage sur le compte." else "Brouillon prêt à payer avec le solde dispo.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }

                Button(
                    onClick = onBuyYuansClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF97316),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "+ Acheter des yuans (Alipay)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Logistics Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (order.status == "FLIGHT") {
                        Icon(
                            imageVector = Icons.Default.Flight,
                            contentDescription = null,
                            tint = Color(0xFF9333EA),
                            modifier = Modifier.size(14.dp)
                        )
                    } else if (order.status == "GUANGZHOU") {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(14.dp)
                        )
                    } else if (order.status == "ORDERED") {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = order.carrier.ifBlank { "Non attribué" },
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (order.assignedClient.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFECFDF5))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = order.assignedClient,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                } else if (order.weightKg != null) {
                    Text(
                        text = if (order.status == "QUALITY_CONTROL") "Poids réel: ${order.weightKg} kg" else "Poids pesé: ${order.weightKg} kg",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (order.lotNumber.isNotBlank()) {
                    Text(
                        text = order.lotNumber,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
