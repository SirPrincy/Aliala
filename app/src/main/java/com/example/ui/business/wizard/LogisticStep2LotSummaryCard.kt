package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.VariantColorUtils
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step2LotSummaryCard(
    order: BusinessOrderEntity,
    familyEmoji: String,
    totalQty: Int,
    unitPriceYuan: Double,
    totalOrderYuan: Double,
    totalOrderAr: Long,
    isMaritime: Boolean,
    selectedTransitaire: CatalogueTransitaireSummaryUi?,
    colors: TransitaireColors,
    onSelectTransitaire: () -> Unit,
    onEditEta: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Récapitulatif du Lot",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.cyanSoftBg
                    ) {
                        Text(
                            text = order.platform.ifBlank { "Achat Chine" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = colors.cyanAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (order.yuanDebited) {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = colors.successSoftBg
                        ) {
                            Text(
                                text = "Payé ¥",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = colors.successAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.badgeNeutralBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = familyEmoji, fontSize = 30.sp)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = order.product.ifBlank { "Article #${order.id}" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (order.referenceCode.isNotBlank()) "Réf : ${order.referenceCode}" else "$totalQty unités commandées",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = buildString {
                                append(order.supplier.ifBlank { "Fournisseur Chine" })
                                if (order.supplierDispatchDelay.isNotBlank()) {
                                    append(" · ")
                                    append(order.supplierDispatchDelay)
                                }
                            },
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val parsedVariants = remember(order.variants) {
                VariantColorUtils.parseVariants(order.variants)
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.innerCardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$totalQty unités · ¥ ${formatYuanCompact(unitPriceYuan)}/u",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                            if (order.domesticShippingYuan > 0.0) {
                                Text(
                                    text = "Port Chine inclus : ¥ ${formatYuanCompact(order.domesticShippingYuan)}",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "¥ ${formatYuanCompact(totalOrderYuan)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = colors.cyanAccent
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(totalOrderAr),
                                fontFamily = Inter,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    if (parsedVariants.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            parsedVariants.forEachIndexed { idx, variantText ->
                                val dotColor = VariantColorUtils.getVariantColor(variantText, idx)
                                val isLight = VariantColorUtils.isLightColor(dotColor)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.cardBg,
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                                .then(
                                                    if (isLight) Modifier.border(0.5.dp, colors.border, CircleShape)
                                                    else Modifier
                                                )
                                        )
                                        Text(
                                            text = variantText,
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.innerCardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onSelectTransitaire)
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Destination transitaire",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                                val destTitle = when {
                                    selectedTransitaire != null -> selectedTransitaire.name
                                    order.carrier.isNotBlank() -> order.carrier
                                    else -> "Non défini — toucher pour choisir"
                                }
                                Text(
                                    text = destTitle,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (selectedTransitaire != null || order.carrier.isNotBlank()) {
                                        colors.textPrimary
                                    } else {
                                        colors.warningAccent
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isMaritime) colors.violetSoftBg else colors.cyanSoftBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMaritime) Icons.Default.DirectionsBoat else Icons.Default.Flight,
                                        contentDescription = null,
                                        tint = if (isMaritime) colors.violetAccent else colors.cyanAccent,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = if (isMaritime) "MARITIME" else "AÉRIEN",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = if (isMaritime) colors.violetAccent else colors.cyanAccent
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier le transitaire",
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onEditEta)
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = colors.cyanSolidBg,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Arrivée prévue : ${DateUtils.formatShortDate(order.estimatedArrival)} (${DateUtils.formatRelativeDays(order.estimatedArrival)})",
                                fontFamily = Inter,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = colors.cyanAccent,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier la date d'arrivée",
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
