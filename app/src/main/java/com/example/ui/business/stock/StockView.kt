package com.example.ui.business.stock

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans

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
    onInRouteOrderClick: (BusinessOrderEntity) -> Unit = {},
    onOpenLogistics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = rememberTransitaireColors()
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }
    var filterMenuExpanded by remember { mutableStateOf(false) }

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
                            searchFocusRequester.requestFocus()
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

                Box {
                    val hasFilter = uiState.showHiddenStock || uiState.stockFamilyFilter != "Toutes"
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (hasFilter) colors.violetSoftBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable { filterMenuExpanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filtrer la vue",
                            tint = if (hasFilter) colors.violetAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (uiState.showHiddenStock) "✓ Masqués affichés" else "Afficher les masqués (${uiState.hiddenStockCount})",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp
                                )
                            },
                            onClick = {
                                onToggleShowHidden()
                                filterMenuExpanded = false
                            }
                        )
                        listOf("Disponible", "En route", "Défectueux").forEach { seg ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (uiState.stockSegment == seg) "✓ Vue : $seg" else "Vue : $seg",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp
                                    )
                                },
                                onClick = {
                                    onSegmentSelected(seg)
                                    filterMenuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Toutes les familles",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = colors.cyanAccent
                                )
                            },
                            onClick = {
                                onFamilyFilterSelected("Toutes")
                                onSearchQueryChanged("")
                                filterMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        StockKpiHeaderStrip(uiState = uiState, colors = colors)

        StockCatalogueBannerCard(colors = colors, onOpenCatalogue = onOpenCatalogue)

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

        OutlinedTextField(
            value = uiState.stockSearchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(searchFocusRequester)
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
                if (uiState.stockSearchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Effacer la recherche",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            .clickable { onOpenLogistics() }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Suivi logistique",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.violetAccent
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

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
                        InRouteShipmentRow(
                            order = inRouteOrder,
                            onClick = { onInRouteOrderClick(inRouteOrder) }
                        )
                    }
                }
            }
        }
    }
}
