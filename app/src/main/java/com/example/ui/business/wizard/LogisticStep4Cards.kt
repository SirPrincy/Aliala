package com.example.ui.business.wizard

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.model.DomesticParcelItem
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.DateUtils

@Composable
internal fun Step4StepperHeaderCard(
    modeTitle: String,
    modeSubtitle: String
) {
    val colors = rememberTransitaireColors()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_step4")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )

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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(colors.violetSolidBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("4", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                    }
                    Text("ÉTAPE 4 SUR 6", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.violetAccent, letterSpacing = 0.5.sp)
                }

                Text(modeSubtitle, fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = colors.textSecondary)
            }

            Text(modeTitle, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.textPrimary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(9999.dp)).background(colors.cyanAccent))
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(15.dp))
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(9999.dp)).background(colors.violetSolidBg))
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = colors.violetAccent.copy(alpha = pulseAlpha), modifier = Modifier.size(15.dp))
                }

                repeat(2) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(9999.dp)).background(colors.badgeNeutralBg))
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cmd", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = colors.cyanAccent, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Chine", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = colors.cyanAccent, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Hub", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = colors.cyanAccent, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Mada", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = colors.violetAccent, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("TNR", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Stock", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
internal fun Step4FlightTrajectoryCard(
    order: BusinessOrderEntity,
    isMaritime: Boolean,
    modeBadge: String,
    originHubLabel: String,
    leadDaysLabel: String,
    parcelsCount: Int,
    arrivalPortName: String,
    arrivalPortCity: String
) {
    val colors = rememberTransitaireColors()
    val hasIncident = order.incidentReport.isNotBlank()
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
                Surface(shape = RoundedCornerShape(9999.dp), color = colors.violetSoftBg) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isMaritime) Icons.Default.LocalShipping else Icons.Default.Flight,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(modeBadge, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.violetAccent)
                    }
                }

                Surface(shape = RoundedCornerShape(9999.dp), color = colors.badgeNeutralBg) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(14.dp))
                        Text(
                            text = when {
                                order.estimatedArrival > 0L -> "ETA : ${DateUtils.formatShortDate(order.estimatedArrival)}"
                                leadDaysLabel.isNotBlank() -> "Délai : $leadDaysLabel"
                                else -> "Date non définie"
                            },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = colors.innerCardBg
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val hubDepartureMillis = order.madaDispatchDate ?: order.warehouseArrivalDate
                            Text("CH", fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = colors.textPrimary)
                            Text(originHubLabel, fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                            Text(
                                text = if (hubDepartureMillis != null && hubDepartureMillis > 0L) DateUtils.formatShortDate(hubDepartureMillis) else "Non daté",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = colors.cyanAccent
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(28.dp), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(9999.dp)).background(colors.badgeNeutralBg))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(modifier = Modifier.fillMaxWidth(0.6f).height(4.dp).clip(RoundedCornerShape(9999.dp)).background(colors.violetSolidBg))
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Spacer(modifier = Modifier.fillMaxWidth(0.58f))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(colors.violetSolidBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isMaritime) Icons.Default.LocalShipping else Icons.Default.Flight,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp).rotate(if (isMaritime) 0f else 90f)
                                        )
                                    }
                                }
                            }

                            Surface(shape = RoundedCornerShape(9999.dp), color = colors.cardBg, shadowElevation = 1.dp) {
                                Text("En acheminement", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = colors.violetAccent, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(arrivalPortName, fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = colors.textPrimary)
                            Text(arrivalPortCity, fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                            Text(
                                text = if (order.estimatedArrival > 0L) DateUtils.formatShortDate(order.estimatedArrival) else "Selon délai",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = colors.violetAccent
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (hasIncident) colors.dangerSoftBg else colors.innerCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = if (hasIncident) colors.dangerAccent else colors.cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (hasIncident) {
                                "Anomalie signalée sur le lot"
                            } else {
                                "Suivi actif · $parcelsCount colis expédié(s)"
                            },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (hasIncident) colors.dangerAccent else colors.textPrimary
                        )
                        Text(
                            text = when {
                                hasIncident -> order.incidentReport
                                leadDaysLabel.isNotBlank() -> "Délai annoncé par le transitaire : $leadDaysLabel."
                                else -> "Lot #${order.id} en cours d'acheminement vers $arrivalPortCity."
                            },
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

@Composable
internal fun Step4ShipmentDetailsCard(
    order: BusinessOrderEntity,
    isMaritime: Boolean,
    carrierHeader: String,
    carrierName: String,
    awbNumber: String,
    arrivalPortCity: String,
    leadDaysLabel: String,
    onCopyAwb: () -> Unit,
    onSelectDelayDays: (Int) -> Unit = {}
) {
    val colors = rememberTransitaireColors()
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(carrierHeader, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                        Text(carrierName, fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                    }
                }

                Surface(shape = RoundedCornerShape(9999.dp), color = colors.badgeNeutralBg) {
                    Text(
                        text = if (isMaritime) "FRET MARITIME" else "FRET AÉRIEN",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            val estimatedDays = com.example.util.TransitStatsCalculator.daysBetween(
                order.madaDispatchDate ?: order.warehouseArrivalDate ?: order.orderDate,
                order.estimatedArrival
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onCopyAwb),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.innerCardBg
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                        Text(
                            text = if (isMaritime) "BORDEREAU TC" else "BORDEREAU AR",
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.sp,
                            color = colors.textSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(awbNumber, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.textPrimary)
                    }
                }

                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = colors.innerCardBg) {
                    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                        Text("DÉLAI ESTIMÉ", fontFamily = PlusJakartaSans, fontSize = 9.sp, color = colors.textSecondary, letterSpacing = 0.5.sp)
                        Text(
                            text = when {
                                estimatedDays != null -> "${com.example.util.TransitStatsCalculator.formatDaysExact(estimatedDays)} estimés"
                                leadDaysLabel.isNotBlank() -> leadDaysLabel
                                else -> "Non défini"
                            },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Délai :",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                listOf(30, 60, 90).forEach { delayDays ->
                    val isSelected = estimatedDays == delayDays.toLong()
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) colors.violetSolidBg else colors.innerCardBg,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectDelayDays(delayDays) }
                            .testTag("btn_step4_delay_$delayDays")
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = "+${delayDays}j",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else colors.violetAccent
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val hubDepartureMillis = order.madaDispatchDate ?: order.warehouseArrivalDate
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(15.dp))
                    Text(
                        text = "Envoi Hub Chine : " + (if (hubDepartureMillis != null && hubDepartureMillis > 0L) DateUtils.formatShortDate(hubDepartureMillis) else "Non daté"),
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.FlightLand, contentDescription = null, tint = colors.violetAccent, modifier = Modifier.size(15.dp))
                    Text(
                        text = "Arrivée $arrivalPortCity : " + (if (order.estimatedArrival > 0L) DateUtils.formatShortDate(order.estimatedArrival) else "Selon délai"),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.violetAccent
                    )
                }
            }
        }
    }
}

@Composable
internal fun Step4HoldParcelsCard(
    order: BusinessOrderEntity,
    parcels: List<DomesticParcelItem>,
    isMaritime: Boolean,
    weightKg: Double,
    volumeM3: Double,
    totalQty: Int,
    onOpenRadar: () -> Unit
) {
    val colors = rememberTransitaireColors()
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
                Column {
                    Text("Colis groupés en soute", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                    val totalParcelsCount = if (parcels.isNotEmpty()) parcels.size else 1
                    val measureSummary = if (isMaritime) {
                        if (volumeM3 > 0.0) "Volume : ${String.format(java.util.Locale.FRENCH, "%.5f", volumeM3)} m³" else "Volume non saisi"
                    } else {
                        if (weightKg > 0.0) "Poids total : ${String.format(java.util.Locale.US, "%.1f", weightKg)} kg" else "Poids non saisi"
                    }
                    Text(
                        text = "$totalParcelsCount colis expédié(s) · $measureSummary",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.cyanSoftBg,
                    modifier = Modifier.clickable(onClick = onOpenRadar)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Radar ↗", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.cyanAccent)
                    }
                }
            }

            if (parcels.isNotEmpty()) {
                parcels.forEachIndexed { idx, parcel ->
                    Step4HoldParcelRow(
                        title = "Colis ${idx + 1} · ${parcel.trackingNumber}",
                        subtitle = parcel.contentDescription.ifBlank { "$totalQty unités · ${order.product}" }
                    )
                }
            } else {
                Step4HoldParcelRow(
                    title = "Colis 1 · Lot #${order.id}",
                    subtitle = "$totalQty unités (${order.variants.ifBlank { order.product.ifBlank { "Articles" } }})"
                )
            }
        }
    }
}

@Composable
private fun Step4HoldParcelRow(title: String, subtitle: String) {
    val colors = rememberTransitaireColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.innerCardBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.cardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = colors.violetAccent, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(title, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
                    Text(subtitle, fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                }
            }

            Surface(shape = RoundedCornerShape(9999.dp), color = colors.cardBg) {
                Text("Soute cargo", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
            }
        }
    }
}
