package com.example.ui.business.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material.icons.filled.Sell
import com.example.ui.business.BusinessUiState
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun StockView(
    uiState: BusinessUiState,
    onSegmentSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onFamilyFilterSelected: (String) -> Unit,
    onToggleShowHidden: () -> Unit,
    onAdjustStockClick: (BusinessOrderEntity) -> Unit,
    onToggleHideItem: (BusinessOrderEntity) -> Unit,
    onDeleteItem: (BusinessOrderEntity) -> Unit,
    onCorrectStockFabClick: () -> Unit,
    onArticleClick: (BusinessOrderEntity) -> Unit = {},
    onOpenCatalogue: () -> Unit = {},
    onOpenCatalogueWithHidden: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }

    // Pulsing animation for the "Inventaire en temps réel" indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // -------------------------------------------------------------
        // 1. HEADER UTILITY BAR (Search & Filter triggers)
        // -------------------------------------------------------------
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
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(CyanPrimary)
                        .alpha(pulseAlpha)
                )
                Text(
                    text = "Inventaire en temps réel",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .clickable {
                            isSearchFocused = !isSearchFocused
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Recherche rapide",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .clickable {
                            Toast.makeText(context, "Filtres stock actifs", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filtrer la vue",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 2. HORIZONTAL SCROLL SUMMARY KPI CARDS
        // -------------------------------------------------------------
        val kpiScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(kpiScrollState),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // KPI 1: Valeur totale du stock
            StockKpiCard(
                title = "VALEUR DU STOCK",
                badgeText = "FIFO valorisé",
                badgeBg = CyanPrimary.copy(alpha = 0.12f),
                badgeColor = CyanPrimary,
                mainValue = CurrencyFormatter.formatAriary(uiState.totalStockValueAr).replace(" Ar", ""),
                subValue = "Ariary malgache (Ar)",
                footerIcon = Icons.Default.AccountBalance,
                footerIconTint = CyanPrimary,
                footerText = "${uiState.stockFamiliesCount} familles d'articles",
                cardWidth = 200
            )

            // KPI 2: Articles disponibles
            StockKpiCard(
                title = "DISPONIBLES",
                badgeText = "En rayon",
                badgeBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                badgeColor = MaterialTheme.colorScheme.onSurface,
                mainValue = "${uiState.availableStockPieces}",
                subValue = "pièces prêtes",
                footerIcon = Icons.Default.LocalMall,
                footerIconTint = Color(0xFF7C3AED),
                footerText = "${uiState.reservedClientsPieces} réservés clients",
                cardWidth = 175
            )

            // KPI 3: En route
            StockKpiCard(
                title = "EN ROUTE",
                badgeText = "SinoExpress",
                badgeBg = Color(0xFFEADDFF),
                badgeColor = Color(0xFF25005A),
                mainValue = "${uiState.inRouteStockUnits}",
                subValue = "unités vers Mada",
                footerIcon = Icons.Default.FlightLand,
                footerIconTint = Color(0xFF8A4CFC),
                footerText = if (uiState.inRouteStockUnits > 0) "Expéditions SinoExpress" else "Aucun colis en vol",
                cardWidth = 185
            )

            // KPI 4: Défectueux / SAV
            StockKpiCard(
                title = "DÉFECTUEUX",
                badgeText = "SAV",
                badgeBg = MaterialTheme.colorScheme.surfaceVariant,
                badgeColor = MaterialTheme.colorScheme.onSurfaceVariant,
                mainValue = "${uiState.defectiveStockUnits}",
                subValue = "non commercialisables",
                footerIcon = Icons.Default.AssignmentLate,
                footerIconTint = MaterialTheme.colorScheme.outline,
                footerText = "En diagnostic",
                cardWidth = 170
            )
        }

        // -------------------------------------------------------------
        // 2b. GÉRER LE CATALOGUE BUTTON-CARD (Soft orange with chevron)
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFF7ED))
                .border(1.dp, Color(0xFFFDBA74).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable { onOpenCatalogue() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("btn_manage_catalogue")
        ) {
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
                            .background(Color(0xFFF97316).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sell,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Gérer le catalogue",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF9A3412)
                        )
                        Text(
                            text = "Articles, familles & plateformes fournisseurs",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0xFFC2410C)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFEA580C),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // -------------------------------------------------------------
        // 3. SEGMENTED CONTROL (Disponible, En route, Défectueux)
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Disponible
                val isDisponible = uiState.stockSegment == "Disponible"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDisponible) MaterialTheme.colorScheme.surface
                            else Color.Transparent
                        )
                        .clickable { onSegmentSelected("Disponible") },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Disponible",
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (isDisponible) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isDisponible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isDisponible) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary)
                            )
                        }
                    }
                }

                // En route
                val isEnRoute = uiState.stockSegment == "En route"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isEnRoute) MaterialTheme.colorScheme.surface
                            else Color.Transparent
                        )
                        .clickable { onSegmentSelected("En route") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "En route (${uiState.inRouteStockUnits})",
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (isEnRoute) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (isEnRoute) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Défectueux
                val isDefectueux = uiState.stockSegment == "Défectueux"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDefectueux) MaterialTheme.colorScheme.surface
                            else Color.Transparent
                        )
                        .clickable { onSegmentSelected("Défectueux") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Défectueux (${uiState.defectiveStockUnits})",
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (isDefectueux) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (isDefectueux) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 4. SEARCH BAR + FAMILY FILTERS + HIDDEN ITEMS LINK
        // -------------------------------------------------------------
        OutlinedTextField(
            value = uiState.stockSearchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("stock_search_input"),
            placeholder = {
                Text(
                    text = "Rechercher un article en stock...",
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
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable {
                            Toast.makeText(context, "Scanner code-barres prêt", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scanner",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
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

        // Family Filter Pills
        val familiesScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(familiesScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.stockFamilyPills.forEach { pill ->
                val isSelected = uiState.stockFamilyFilter.equals(pill.name, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onFamilyFilterSelected(pill.name) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${pill.name} (${pill.count})",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Link: Articles masqués (N) -> Vers le Catalogue
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenCatalogueWithHidden() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("btn_stock_hidden_items"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Articles masqués (${uiState.hiddenStockCount})",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // -------------------------------------------------------------
        // 5. ARTICLES LIST (Disponibles / Filtrés)
        // -------------------------------------------------------------
        if (uiState.visibleStockOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LocalMall,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aucun article trouvé dans cette sélection",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.visibleStockOrders.forEach { order ->
                    StockItemCard(
                        order = order,
                        onArticleClick = { onArticleClick(order) },
                        onAdjustClick = { onAdjustStockClick(order) },
                        onToggleHide = { onToggleHideItem(order) },
                        onDelete = { onDeleteItem(order) }
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 6. EN COURS D'ACHEMINEMENT PREVIEW SECTION
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header
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
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF8A4CFC).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF712AE2),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "En cours d'acheminement",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.inRouteStockUnits} unités programmées",
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                Toast.makeText(context, "Suivi logistique SinoExpress ouvert", Toast.LENGTH_SHORT).show()
                            }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Suivi Sino",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF712AE2)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF712AE2),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // In-route dynamic rows (or top 2 in transit)
                val inRoutePreview = uiState.inRouteStockOrders.take(2)
                if (inRoutePreview.isEmpty()) {
                    Text(
                        text = "Aucun colis en vol actuellement",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    inRoutePreview.forEach { inRouteOrder ->
                        InRouteShipmentRow(order = inRouteOrder)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// KPI CARD COMPONENT
// ---------------------------------------------------------------------------
@Composable
private fun StockKpiCard(
    title: String,
    badgeText: String,
    badgeBg: Color,
    badgeColor: Color,
    mainValue: String,
    subValue: String,
    footerIcon: androidx.compose.ui.graphics.vector.ImageVector,
    footerIconTint: Color,
    footerText: String,
    cardWidth: Int
) {
    Box(
        modifier = Modifier
            .width(cardWidth.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(badgeBg)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Value
            Column {
                Text(
                    text = mainValue,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 28.sp
                )
                Text(
                    text = subValue,
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = footerIcon,
                    contentDescription = null,
                    tint = footerIconTint,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = footerText,
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STOCK ITEM CARD COMPONENT
// ---------------------------------------------------------------------------
@Composable
private fun StockItemCard(
    order: BusinessOrderEntity,
    onArticleClick: () -> Unit = {},
    onAdjustClick: () -> Unit,
    onToggleHide: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // FIFO unitaire calculation
    val unitCostAr = if (order.unitCostAr > 0L) {
        order.unitCostAr
    } else if (order.priceYuan > 0.0) {
        (order.priceYuan * order.exchangeRate).toLong() + (if (order.quantityBought > 0) order.freightCostAr / order.quantityBought else 0L)
    } else {
        0L
    }
    val totalItemValueAr = unitCostAr * order.stockCount

    // Category styling color
    val isMode = order.family.equals("Mode", ignoreCase = true)
    val familyColor = if (isMode) Color(0xFF712AE2) else CyanPrimary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable { onArticleClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Upper row: Image + Details + Menu
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
                        else -> Icons.Default.LocalMall
                    }

                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = familyColor,
                        modifier = Modifier.size(34.dp)
                    )

                    // Tag badge in corner if PRO or special
                    if (order.product.contains("Pro", ignoreCase = true)) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF131B2E).copy(alpha = 0.85f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "PRO",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Details column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = order.family.uppercase(),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = familyColor,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = order.product,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "SKU: ${order.referenceCode.ifBlank { "REF-${order.id}" }}",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        // Context Menu Button
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
                                    text = {
                                        Text(
                                            if (order.isHidden) "Réafficher" else "Masquer l'article",
                                            fontFamily = PlusJakartaSans
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onToggleHide()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (order.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Ajuster le stock", fontFamily = PlusJakartaSans) },
                                    onClick = {
                                        showMenu = false
                                        onAdjustClick()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Tune, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Supprimer", fontFamily = PlusJakartaSans, color = Color(0xFFBA1A1A)) },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFBA1A1A))
                                    }
                                )
                            }
                        }
                    }

                    // Stock count & Reservation Tag
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${order.stockCount}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "pcs",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 1.dp)
                            )
                        }

                        if (order.reservedCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(Color(0xFFEADDFF))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = Color(0xFF25005A),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Réservé ${order.reservedCount}" + if (order.assignedClient.isNotBlank()) " (${order.assignedClient})" else "",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF25005A)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Libre",
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

            // Color / variants breakdown pills
            if (order.variants.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    val variantPills = order.variants.split("·", ",").map { it.trim() }.filter { it.isNotBlank() }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        variantPills.forEachIndexed { index, pillText ->
                            val dotColor = when {
                                pillText.contains("Noir", ignoreCase = true) -> Color(0xFF0F172A)
                                pillText.contains("Blanc", ignoreCase = true) -> Color(0xFFFFFFFF)
                                pillText.contains("Rouge", ignoreCase = true) -> Color(0xFFF43F5E)
                                pillText.contains("Bleu", ignoreCase = true) -> Color(0xFF3B82F6)
                                pillText.contains("Stellaire", ignoreCase = true) || pillText.contains("Gris", ignoreCase = true) -> Color(0xFF94A3B8)
                                else -> CyanPrimary
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                                )
                                Text(
                                    text = pillText,
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (index < variantPills.size - 1) {
                                Text(
                                    text = "·",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // Financial Valuation Footer & Adjust Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Coût FIFO unitaire",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${CurrencyFormatter.formatAriary(unitCostAr)} / u",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Valeur totale",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = CurrencyFormatter.formatAriary(totalItemValueAr),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CyanPrimary
                    )
                }

                Button(
                    onClick = onAdjustClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Ajuster",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// IN-ROUTE SHIPMENT ROW COMPONENT
// ---------------------------------------------------------------------------
@Composable
private fun InRouteShipmentRow(order: BusinessOrderEntity) {
    val isWarehouse = order.status == "WAREHOUSE"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = order.product,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${order.quantityBought} pcs",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Status badge
                if (isWarehouse) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFFD8E2FF))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = Color(0xFF001A42),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Arrivé entrepôt",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF001A42)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFFEADDFF))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = Color(0xFF25005A),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Vers Mada · J+6",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF25005A)
                            )
                        }
                    }
                }
            }

            // Variants / breakdown
            if (order.variants.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isWarehouse) Color(0xFF005AC2) else Color(0xFF8A4CFC))
                    )
                    Text(
                        text = order.variants,
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Logistics note footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (isWarehouse) {
                        if (order.weightKg != null && order.weightKg > 0.0) "Pesée SinoExpress validée : ${order.weightKg} kg" else "Arrivé à l'entrepôt Chine"
                    } else "Disponible après le contrôle qualité au hub Tana",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
