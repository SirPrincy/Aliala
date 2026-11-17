package com.example.ui.business.purchases

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.BordereauGroupUi
import com.example.util.DateUtils
import java.util.Locale

@Composable
internal fun BordereauGroupCard(
    group: BordereauGroupUi,
    onOrderClick: (BusinessOrderEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    var isExpanded by rememberSaveable(group.code) { mutableStateOf(group.isInTransit) }
    val modeBg = if (group.isMaritime) colors.cyanSoftBg else colors.violetSoftBg
    val modeAccent = if (group.isMaritime) colors.cyanAccent else colors.violetAccent
    val statusColors = when (group.statusLabel) {
        "En transit" -> colors.violetSoftBg to colors.violetAccent
        "Dédouanement" -> colors.warningSoftBg to colors.warningAccent
        "Contrôle qualité" -> colors.dangerSoftBg to colors.dangerAccent
        else -> colors.successSoftBg to colors.successAccent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bordereau_card_${group.code}"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(modeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (group.isMaritime) Icons.Default.LocalShipping else Icons.Default.Flight,
                            contentDescription = if (group.isMaritime) "Maritime" else "Aérien",
                            tint = modeAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = group.code,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = modeAccent
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(modeBg)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (group.isMaritime) "MARITIME" else "AÉRIEN",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = modeAccent
                                )
                            }
                        }

                        Text(
                            text = group.carrier,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColors.first)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = group.statusLabel,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = statusColors.second
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Réduire" else "Déplier",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.innerCardBg)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Départ : " + (group.departureMillis?.let { DateUtils.formatShortDate(it) } ?: "Non daté"),
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightLand,
                            contentDescription = null,
                            tint = modeAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Arrivée prévue : " + (group.estimatedArrivalMillis?.let { DateUtils.formatShortDate(it) } ?: "Selon délai"),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = modeAccent
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${group.orders.size} art. · ${group.totalQuantity} pcs",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.textPrimary
                    )
                    val measureText = if (group.isMaritime) {
                        if (group.totalVolumeM3 > 0.0) {
                            "${String.format(Locale.FRENCH, "%.5f", group.totalVolumeM3)} m³"
                        } else {
                            "Volume à saisir"
                        }
                    } else {
                        if (group.totalWeightKg > 0.0) {
                            "${String.format(Locale.US, "%.1f", group.totalWeightKg)} kg"
                        } else {
                            "Poids à saisir"
                        }
                    }
                    Text(
                        text = measureText,
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            if (isExpanded) {
                HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    group.orders.forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .clickable { onOrderClick(order) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = order.product,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val detailMeasure = if (group.isMaritime && (order.volumeM3 ?: 0.0) > 0.0) {
                                    " · ${String.format(Locale.FRENCH, "%.5f", order.volumeM3 ?: 0.0)} m³"
                                } else if (!group.isMaritime && (order.weightKg ?: 0.0) > 0.0) {
                                    " · ${String.format(Locale.US, "%.1f", order.weightKg ?: 0.0)} kg"
                                } else ""
                                Text(
                                    text = "${order.quantityBought} pcs$detailMeasure · ${OrderStatus.fromCode(order.status).label}",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Ouvrir le suivi",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
