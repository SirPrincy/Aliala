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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.model.DomesticParcelItem
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.DateUtils

@Composable
internal fun Step4StepperHeaderCard(
    modeTitle: String,
    modeSubtitle: String
) {
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
        color = MaterialTheme.colorScheme.surface,
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
                            .background(Color(0xFF712AE2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("4", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                    }
                    Text("ÉTAPE 4 SUR 6", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF712AE2), letterSpacing = 0.5.sp)
                }

                Text(modeSubtitle, fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(modeTitle, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(9999.dp)).background(MaterialTheme.colorScheme.primary))
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(9999.dp)).background(Color(0xFF712AE2)))
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = Color(0xFF712AE2).copy(alpha = pulseAlpha), modifier = Modifier.size(15.dp))
                }

                repeat(2) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(9999.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cmd", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Chine", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Hub", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Mada", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF712AE2), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("TNR", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Stock", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
internal fun Step4FlightTrajectoryCard(
    order: BusinessOrderEntity,
    isMaritime: Boolean,
    modeBadge: String,
    arrivalPortName: String,
    arrivalPortCity: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
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
                Surface(shape = RoundedCornerShape(9999.dp), color = Color(0xFFEADDFF).copy(alpha = 0.60f)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Flight, contentDescription = null, tint = Color(0xFF712AE2), modifier = Modifier.size(16.dp))
                        Text(modeBadge, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF712AE2))
                    }
                }

                Surface(shape = RoundedCornerShape(9999.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Text(
                            text = if (order.estimatedArrival > 0) "ETA : ${DateUtils.formatShortDate(order.estimatedArrival)}" else "Selon délai transitaire",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isMaritime) "SZX" else "CAN", fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(if (isMaritime) "Shenzhen Port" else "Guangzhou", fontFamily = Inter, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (order.orderDate > 0) DateUtils.formatShortDate(order.orderDate) else "—", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(28.dp), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(9999.dp)).background(MaterialTheme.colorScheme.outlineVariant))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(modifier = Modifier.fillMaxWidth(0.6f).height(4.dp).clip(RoundedCornerShape(9999.dp)).background(Color(0xFF712AE2)))
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Spacer(modifier = Modifier.fillMaxWidth(0.58f))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF712AE2)),
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

                            Surface(shape = RoundedCornerShape(9999.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                                Text("En acheminement", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF712AE2), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(arrivalPortName, fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(arrivalPortCity, fontFamily = Inter, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DateUtils.formatShortDate(order.estimatedArrival), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF712AE2))
                        }
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (isMaritime) "Escale Port Louis (Maurice) effectuée" else "Correspondance cargo Nairobi (NBO) à l'heure",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isMaritime) "Conteneur scellé intact · Trajet maritime fluide." else "Transfert palettes validé · Aucun retard météo détecté sur la ligne.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    onCopyAwb: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
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
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(carrierHeader, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(carrierName, fontFamily = Inter, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(shape = RoundedCornerShape(9999.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        text = if (isMaritime) "FRET MARITIME" else "FRET AÉRIEN",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onCopyAwb),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                        Text("BORDEREAU LTA / AWB", fontFamily = PlusJakartaSans, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
                        Text(awbNumber, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                        Text("DÉLAI ESTIMÉ", fontFamily = PlusJakartaSans, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
                        Text("6 jours au total", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                    Text(
                        text = "Départ Hub Chine : " + (if (order.orderDate > 0) DateUtils.formatShortDate(order.orderDate) else "Selon départ"),
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.FlightLand, contentDescription = null, tint = Color(0xFF712AE2), modifier = Modifier.size(15.dp))
                    Text(
                        text = "Arrivée $arrivalPortCity : " + (if (order.estimatedArrival > 0) DateUtils.formatShortDate(order.estimatedArrival) else "Selon délai"),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF712AE2)
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
    weightKg: Double,
    totalQty: Int,
    onOpenRadar: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
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
                    Text("Colis groupés en soute", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    val totalParcelsCount = if (parcels.isNotEmpty()) parcels.size else 1
                    Text(
                        text = "$totalParcelsCount colis sous scellé · Poids total : ${if (weightKg > 0.0) "$weightKg kg" else "En cours"}",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.clickable(onClick = onOpenRadar)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Radar ↗", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
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
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFF712AE2), modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(title, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, fontFamily = Inter, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Surface(shape = RoundedCornerShape(9999.dp), color = MaterialTheme.colorScheme.surface) {
                Text("Soute cargo", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
            }
        }
    }
}
