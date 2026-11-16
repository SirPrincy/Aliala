package com.example.ui.business.catalogue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.CatalogueArticleSummaryUi
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun ActiveCatalogueListCard(
    article: CatalogueArticleSummaryUi,
    onArticleClick: () -> Unit,
    onEditClick: () -> Unit,
    onQuickRestock: () -> Unit,
    onToggleHide: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val colors = rememberTransitaireColors()
    val order = article.primaryOrder
    val familyColor = remember(article.familyColorHex, colors) {
        parseColorSafe(article.familyColorHex, colors.cyanAccent)
    }
    val isTop = article.totalSoldCount >= 10
    val unitProfitAr = if (article.resalePriceAr > 0L && article.latestUnitCostAr > 0L) {
        article.resalePriceAr - article.latestUnitCostAr
    } else 0L

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onArticleClick() },
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    ArticleVisualThumbnail(
                        imageUrl = order.imageUrl,
                        fallbackEmoji = article.familyEmoji,
                        familyColor = familyColor,
                        productName = order.product,
                        modifier = Modifier.fillMaxSize(),
                        emojiFontSize = 32
                    )

                    if (isTop) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF59E0B))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(9.dp))
                                Text("Top", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }

                    if (article.allVariants.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.72f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${article.allVariants.size} var.",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = familyColor.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = order.family.ifBlank { "Catalogue" },
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = familyColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            if (article.lotsCount > 1) {
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = colors.badgeNeutralBg
                                ) {
                                    Text(
                                        text = "${article.lotsCount} lots",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.MoreVert, "Options", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                            }
                            ArticleActionDropdown(
                                expanded = showMenu,
                                isHidden = article.isHidden,
                                onDismiss = { showMenu = false },
                                onOpenDetail = { showMenu = false; onArticleClick() },
                                onEdit = { showMenu = false; onEditClick() },
                                onRestock = { showMenu = false; onQuickRestock() },
                                onToggleHide = { showMenu = false; onToggleHide() },
                                onDelete = { showMenu = false; onDeleteRequest() }
                            )
                        }
                    }

                    Text(
                        text = order.product.ifBlank { "Article #${order.id}" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.innerCardBg
                        ) {
                            Text(
                                text = order.referenceCode.ifBlank { "REF-${order.id}" },
                                fontFamily = Inter,
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (order.platform.isNotBlank()) {
                            Text(
                                text = "• ${order.platform}",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.innerCardBg)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Coût revient", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (article.latestUnitCostAr > 0L) CurrencyFormatter.formatAriary(article.latestUnitCostAr) else "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = formatYuanPrecise(article.latestPriceYuan),
                            fontFamily = Inter,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Prix vente", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                    Text(
                        text = if (article.resalePriceAr > 0L) CurrencyFormatter.formatAriary(article.resalePriceAr) else "À définir",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.cyanAccent
                    )
                }

                val marginColor = when {
                    article.marginPercent >= 25 -> colors.successAccent
                    article.marginPercent > 0 -> colors.violetAccent
                    article.resalePriceAr > 0L && unitProfitAr <= 0L -> colors.dangerAccent
                    else -> colors.textSecondary
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = marginColor.copy(alpha = 0.14f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (article.resalePriceAr > 0L && article.latestUnitCostAr > 0L) "${article.marginPercent}%" else "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = marginColor
                        )
                        if (unitProfitAr != 0L) {
                            Text(
                                text = (if (unitProfitAr > 0) "+" else "") + CurrencyFormatter.formatAriary(unitProfitAr),
                                fontFamily = Inter,
                                fontSize = 9.sp,
                                color = marginColor
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArticleStatusPill(
                    statusTag = article.statusTag,
                    stockCount = article.totalStockCount,
                    transitCount = article.totalInTransitCount,
                    colors = colors
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (article.totalSoldCount > 0) {
                        Text(
                            text = "${article.totalSoldCount} vendus",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = colors.violetAccent
                        )
                    }
                    if (order.weightKg != null && order.weightKg > 0.0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.Scale, null, tint = colors.textSecondary, modifier = Modifier.size(12.dp))
                            Text(
                                text = "${(order.weightKg * 1000).toInt()} g",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ActiveCatalogueGridCard(
    article: CatalogueArticleSummaryUi,
    onArticleClick: () -> Unit,
    onEditClick: () -> Unit,
    onQuickRestock: () -> Unit,
    onToggleHide: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val colors = rememberTransitaireColors()
    val order = article.primaryOrder
    val familyColor = remember(article.familyColorHex, colors) {
        parseColorSafe(article.familyColorHex, colors.cyanAccent)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onArticleClick() },
        shape = RoundedCornerShape(18.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                ArticleVisualThumbnail(
                    imageUrl = order.imageUrl,
                    fallbackEmoji = article.familyEmoji,
                    familyColor = familyColor,
                    productName = order.product,
                    modifier = Modifier.fillMaxSize(),
                    emojiFontSize = 40
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    ArticleStatusPill(
                        statusTag = article.statusTag,
                        stockCount = article.totalStockCount,
                        transitCount = article.totalInTransitCount,
                        colors = colors
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(colors.cardBg.copy(alpha = 0.88f))
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MoreVert, "Options", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                    ArticleActionDropdown(
                        expanded = showMenu,
                        isHidden = article.isHidden,
                        onDismiss = { showMenu = false },
                        onOpenDetail = { showMenu = false; onArticleClick() },
                        onEdit = { showMenu = false; onEditClick() },
                        onRestock = { showMenu = false; onQuickRestock() },
                        onToggleHide = { showMenu = false; onToggleHide() },
                        onDelete = { showMenu = false; onDeleteRequest() }
                    )
                }

                if (article.lotsCount > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.72f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${article.lotsCount} lots",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = order.family.ifBlank { "Catalogue" },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = familyColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = order.product.ifBlank { "Article #${order.id}" },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = if (article.resalePriceAr > 0L) CurrencyFormatter.formatAriary(article.resalePriceAr) else "À définir",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = colors.cyanAccent
                        )
                        Text(
                            text = formatYuanPrecise(article.latestPriceYuan),
                            fontFamily = Inter,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                    if (article.resalePriceAr > 0L && article.latestUnitCostAr > 0L) {
                        val mColor = if (article.marginPercent >= 20) colors.successAccent else colors.violetAccent
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = mColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "${article.marginPercent}%",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = mColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
