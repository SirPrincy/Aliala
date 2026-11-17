package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.DateUtils
import com.example.util.TransitStatsCalculator

@Composable
internal fun SuccessMilestoneTimelineCard(order: BusinessOrderEntity) {
    val colors = rememberTransitaireColors()
    val stats = remember(order) { TransitStatsCalculator.calculateForOrder(order) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        shadowElevation = 2.dp
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Chronologie & Délais réels",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Alimente les statistiques transitaire",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                stats.totalDays?.let { total ->
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.violetSoftBg
                    ) {
                        Text(
                            text = "Total : ${TransitStatsCalculator.formatDaysExact(total)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.violetAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TimelineMilestoneRow(
                    label = "1. Commande / Achat",
                    dateStr = if (order.orderDate > 0L) DateUtils.formatShortDate(order.orderDate) else "Non datée",
                    durationStr = null,
                    colors = colors
                )
                TimelineMilestoneRow(
                    label = "2. Arrivée dépôt Chine",
                    dateStr = order.warehouseArrivalDate?.let { DateUtils.formatShortDate(it) } ?: "Non renseignée",
                    durationStr = stats.chinaDays?.let { "+${TransitStatsCalculator.formatDaysExact(it)}" },
                    colors = colors
                )
                TimelineMilestoneRow(
                    label = "3. Envoi vers Madagascar",
                    dateStr = order.madaDispatchDate?.let { DateUtils.formatShortDate(it) } ?: "Non renseignée",
                    durationStr = stats.hubWaitDays?.let { "+${TransitStatsCalculator.formatDaysExact(it)} hub" },
                    colors = colors
                )
                TimelineMilestoneRow(
                    label = "4. Arrivée à Madagascar",
                    dateStr = order.madaArrivalDate?.let { DateUtils.formatShortDate(it) } ?: "Non renseignée",
                    durationStr = stats.transitDays?.let { "+${TransitStatsCalculator.formatDaysExact(it)} fret" },
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun TimelineMilestoneRow(
    label: String,
    dateStr: String,
    durationStr: String?,
    colors: com.example.ui.business.shared.TransitaireColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.innerCardBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontSize = 12.sp,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!durationStr.isNullOrBlank()) {
                Text(
                    text = durationStr,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.cyanAccent
                )
            }
            Text(
                text = dateStr,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = colors.textPrimary
            )
        }
    }
}
