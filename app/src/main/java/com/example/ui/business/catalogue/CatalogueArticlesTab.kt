package com.example.ui.business.catalogue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessUiState
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun ArticlesSubTabContent(
    uiState: BusinessUiState,
    onSearchQueryChanged: (String) -> Unit,
    onFamilyFilterSelected: (String) -> Unit,
    onStockFilterSelected: (String) -> Unit,
    onToggleShowHidden: () -> Unit,
    onSortChanged: (String) -> Unit,
    onViewModeChanged: (String) -> Unit,
    onArticleClick: (CatalogueArticleSummaryUi) -> Unit,
    onEditArticleClick: (CatalogueArticleSummaryUi) -> Unit,
    onQuickRestockArticle: (CatalogueArticleSummaryUi) -> Unit,
    onToggleHideArticle: (CatalogueArticleSummaryUi) -> Unit,
    onDeleteArticle: (CatalogueArticleSummaryUi) -> Unit,
    onNewFamilyClick: () -> Unit,
    onNewArticleClick: () -> Unit,
    showSortMenu: Boolean,
    onDismissSortMenu: () -> Unit,
    onOpenSortMenu: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val colors = rememberTransitaireColors()
    var articlePendingDelete by remember { mutableStateOf<CatalogueArticleSummaryUi?>(null) }

    val activeArticles = remember(uiState.allCatalogueArticles) {
        uiState.allCatalogueArticles.filter { !it.isHidden }
    }
    val totalStockPieces = remember(activeArticles) { activeArticles.sumOf { it.totalStockCount } }
    val lowOrRuptureCount = remember(activeArticles) {
        activeArticles.count { !it.isReferenceOnly && it.totalStockCount <= 5 && it.totalInTransitCount == 0 }
    }
    val totalStockValueAr = remember(activeArticles) {
        activeArticles.sumOf { it.totalStockCount.toLong() * it.latestUnitCostAr }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ArticleKpiChip(
                label = "Actifs",
                value = "${activeArticles.size}",
                accent = colors.cyanAccent,
                bg = colors.cyanSoftBg
            )
            ArticleKpiChip(
                label = "Stock rayon",
                value = "$totalStockPieces pcs",
                accent = colors.successAccent,
                bg = colors.successSoftBg
            )
            ArticleKpiChip(
                label = "Alerte / Rupture",
                value = "$lowOrRuptureCount",
                accent = if (lowOrRuptureCount > 0) colors.dangerAccent else colors.textSecondary,
                bg = if (lowOrRuptureCount > 0) colors.dangerSoftBg else colors.badgeNeutralBg
            )
            ArticleKpiChip(
                label = "Valeur stock",
                value = CurrencyFormatter.formatAriary(totalStockValueAr),
                accent = colors.violetAccent,
                bg = colors.violetSoftBg
            )
        }

        OutlinedTextField(
            value = uiState.catalogueSearchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("catalogue_search_input"),
            placeholder = {
                Text(
                    text = "Rechercher nom, SKU, variante, plateforme...",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (uiState.catalogueSearchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChanged("") },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Effacer la recherche",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.cardBg,
                unfocusedContainerColor = colors.cardBg,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = colors.border
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            uiState.catalogueFamilyPills.forEach { pill ->
                val isSelected = uiState.catalogueFamilyFilter.equals(pill.name, ignoreCase = true)
                val famEmoji = uiState.catalogueFamilies.firstOrNull {
                    it.name.equals(pill.name, ignoreCase = true)
                }?.emoji ?: ""
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (isSelected) CyanPrimary else colors.cardBg,
                    border = if (isSelected) null else BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .height(36.dp)
                        .clickable { onFamilyFilterSelected(pill.name) }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (famEmoji.isNotBlank()) "$famEmoji ${pill.name} (${pill.count})" else "${pill.name} (${pill.count})",
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.Black else colors.textSecondary
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = colors.innerCardBg,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .height(36.dp)
                    .clickable { onNewFamilyClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = colors.cyanAccent, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Famille",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.cyanAccent
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Tous", "En stock", "En transit", "Rupture", "Référentiel").forEach { filterOption ->
                val isSelected = uiState.catalogueStockFilter == filterOption
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) colors.violetSoftBg else colors.innerCardBg,
                    border = if (isSelected) BorderStroke(1.dp, colors.violetAccent) else null,
                    modifier = Modifier.clickable { onStockFilterSelected(filterOption) }
                ) {
                    Text(
                        text = filterOption,
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (isSelected) colors.violetAccent else colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (uiState.catalogueShowHidden) colors.cyanSoftBg else colors.innerCardBg,
                modifier = Modifier.clickable { onToggleShowHidden() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.catalogueShowHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (uiState.catalogueShowHidden) colors.cyanAccent else colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Masqués (${uiState.hiddenCatalogueCount})",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (uiState.catalogueShowHidden) colors.cyanAccent else colors.textSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.innerCardBg,
                        modifier = Modifier.clickable { onOpenSortMenu() }
                    ) {
                        Text(
                            text = "Tri : ${uiState.catalogueSort}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = onDismissSortMenu
                    ) {
                        listOf("Plus vendus", "Marge élevée", "Stock bas", "Dernier coût", "Nom", "Plus récents").forEach { sortOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = sortOption,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (uiState.catalogueSort == sortOption) FontWeight.Bold else FontWeight.Normal,
                                        color = if (uiState.catalogueSort == sortOption) colors.cyanAccent else colors.textPrimary
                                    )
                                },
                                onClick = {
                                    onSortChanged(sortOption)
                                    onDismissSortMenu()
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.innerCardBg)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uiState.catalogueViewMode == "LIST") colors.cardBg else Color.Transparent)
                            .clickable { onViewModeChanged("LIST") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewList,
                            contentDescription = "Vue liste",
                            tint = if (uiState.catalogueViewMode == "LIST") colors.cyanAccent else colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uiState.catalogueViewMode == "GRID") colors.cardBg else Color.Transparent)
                            .clickable { onViewModeChanged("GRID") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Vue grille",
                            tint = if (uiState.catalogueViewMode == "GRID") colors.cyanAccent else colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (uiState.visibleCatalogueArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Text(
                        text = "Aucun article trouvé",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Ajustez vos filtres ou ajoutez une nouvelle fiche produit.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Button(
                        onClick = onNewArticleClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Créer un article", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val isTablet = maxWidth >= 600.dp

                if (uiState.catalogueViewMode == "GRID") {
                    val columns = if (isTablet) GridCells.Adaptive(185.dp) else GridCells.Fixed(2)
                    LazyVerticalGrid(
                        columns = columns,
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 84.dp)
                    ) {
                        items(uiState.visibleCatalogueArticles, key = { it.articleKey }) { article ->
                            if (article.isHidden) {
                                HiddenCatalogueCard(
                                    article = article,
                                    onRestoreClick = { onToggleHideArticle(article) },
                                    onClick = { onArticleClick(article) }
                                )
                            } else {
                                ActiveCatalogueGridCard(
                                    article = article,
                                    onArticleClick = { onArticleClick(article) },
                                    onEditClick = { onEditArticleClick(article) },
                                    onQuickRestock = { onQuickRestockArticle(article) },
                                    onToggleHide = { onToggleHideArticle(article) },
                                    onDeleteRequest = { articlePendingDelete = article }
                                )
                            }
                        }
                    }
                } else if (isTablet) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 84.dp)
                    ) {
                        items(uiState.visibleCatalogueArticles, key = { it.articleKey }) { article ->
                            if (article.isHidden) {
                                HiddenCatalogueCard(
                                    article = article,
                                    onRestoreClick = { onToggleHideArticle(article) },
                                    onClick = { onArticleClick(article) }
                                )
                            } else {
                                ActiveCatalogueListCard(
                                    article = article,
                                    onArticleClick = { onArticleClick(article) },
                                    onEditClick = { onEditArticleClick(article) },
                                    onQuickRestock = { onQuickRestockArticle(article) },
                                    onToggleHide = { onToggleHideArticle(article) },
                                    onDeleteRequest = { articlePendingDelete = article }
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 84.dp)
                    ) {
                        items(uiState.visibleCatalogueArticles, key = { it.articleKey }) { article ->
                            if (article.isHidden) {
                                HiddenCatalogueCard(
                                    article = article,
                                    onRestoreClick = { onToggleHideArticle(article) },
                                    onClick = { onArticleClick(article) }
                                )
                            } else {
                                ActiveCatalogueListCard(
                                    article = article,
                                    onArticleClick = { onArticleClick(article) },
                                    onEditClick = { onEditArticleClick(article) },
                                    onQuickRestock = { onQuickRestockArticle(article) },
                                    onToggleHide = { onToggleHideArticle(article) },
                                    onDeleteRequest = { articlePendingDelete = article }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    articlePendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { articlePendingDelete = null },
            title = {
                Text(
                    text = "Supprimer cet article ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = colors.dangerAccent
                )
            },
            text = {
                val lotsSuffix = if (pending.lotsCount > 1) " et ses ${pending.lotsCount} lots liés" else ""
                Text(
                    text = "Voulez-vous supprimer définitivement « ${pending.primaryOrder.product} »$lotsSuffix ? Cette action est irréversible. Astuce : vous pouvez plutôt masquer l'article.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteArticle(pending)
                        articlePendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent, contentColor = Color.White)
                ) {
                    Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { articlePendingDelete = null }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }
}

@Composable
private fun ArticleKpiChip(
    label: String,
    value: String,
    accent: Color,
    bg: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 10.sp,
                color = accent.copy(alpha = 0.85f)
            )
            Text(
                text = value,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = accent
            )
        }
    }
}
