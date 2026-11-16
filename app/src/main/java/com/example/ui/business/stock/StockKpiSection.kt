package com.example.ui.business.stock

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessUiState
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun StockKpiHeaderStrip(
    uiState: BusinessUiState,
    colors: TransitaireColors
) {
    val kpiScrollState = rememberScrollState()
    val inRouteCarriers = remember(uiState.inRouteStockOrders) {
        uiState.inRouteStockOrders.mapNotNull { it.carrier.trim().takeIf { c -> c.isNotBlank() } }.distinct()
    }
    val inRouteBadgeLabel = when {
        inRouteCarriers.size == 1 -> inRouteCarriers.first()
        inRouteCarriers.size > 1 -> "${inRouteCarriers.size} transitaires"
        else -> "Transitaires"
    }
    val inRouteFooterLabel = when {
        uiState.inRouteStockUnits <= 0 -> "Aucun colis en vol"
        inRouteCarriers.isNotEmpty() -> "Via ${inRouteCarriers.take(2).joinToString(" · ")}"
        else -> "${uiState.inRouteStockOrders.size} lot(s) en transit"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(kpiScrollState),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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

        StockKpiCard(
            title = "DISPONIBLES",
            badgeText = "En rayon",
            badgeBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            badgeColor = MaterialTheme.colorScheme.onSurface,
            mainValue = "${uiState.availableStockPieces}",
            subValue = "pièces prêtes",
            footerIcon = Icons.Default.LocalMall,
            footerIconTint = colors.violetAccent,
            footerText = "${uiState.reservedClientsPieces} réservés clients",
            cardWidth = 175
        )

        StockKpiCard(
            title = "EN ROUTE",
            badgeText = inRouteBadgeLabel,
            badgeBg = colors.violetSoftBg,
            badgeColor = colors.violetAccent,
            mainValue = "${uiState.inRouteStockUnits}",
            subValue = "unités vers Mada",
            footerIcon = Icons.Default.FlightLand,
            footerIconTint = colors.violetAccent,
            footerText = inRouteFooterLabel,
            cardWidth = 185
        )

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
}

@Composable
internal fun StockCatalogueBannerCard(
    colors: TransitaireColors,
    onOpenCatalogue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.warningSoftBg)
            .border(1.dp, colors.warningAccent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
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
                        .background(colors.warningAccent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sell,
                        contentDescription = null,
                        tint = colors.warningAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Gérer le catalogue",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Articles, familles & plateformes fournisseurs",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = colors.warningAccent
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.warningAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StockKpiCard(
    title: String,
    badgeText: String,
    badgeBg: Color,
    badgeColor: Color,
    mainValue: String,
    subValue: String,
    footerIcon: ImageVector,
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
