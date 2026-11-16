package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.model.DomesticParcelItem
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import java.util.Locale

@Composable
internal fun Step5UnloadingStateCard(
    order: BusinessOrderEntity,
    parcels: List<DomesticParcelItem>,
    receivedParcelsCount: Int,
    expectedParcels: Int,
    totalQty: Int,
    weightKg: Double,
    onShowColisDetails: () -> Unit,
    onAddPhoto: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEAEDFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFF00687A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "État au déchargement",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF131B2E)
                        )
                        Text(
                            text = "Inspection physique initiale",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }

                Text(
                    text = "$receivedParcelsCount/$expectedParcels inspectés",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF00687A)
                )
            }

            val parcelCountForDisplay = expectedParcels.coerceAtLeast(1)
            if (parcels.isNotEmpty()) {
                parcels.forEachIndexed { idx, parcel ->
                    val isDeliveredOrArrived = idx < receivedParcelsCount
                    Step5ParcelRow(
                        title = "Colis ${idx + 1} · ${parcel.trackingNumber}",
                        measure = if (order.freightMode == "MARITIME") {
                            "${String.format(Locale.US, "%.3f", (order.volumeM3 ?: 0.014) / parcelCountForDisplay)} m³"
                        } else {
                            "${String.format(Locale.US, "%.1f", weightKg / parcelCountForDisplay)} kg"
                        },
                        subtitle = if (isDeliveredOrArrived) {
                            "État intact scellé · ${parcel.contentDescription}"
                        } else {
                            "En attente de pointage · ${parcel.contentDescription}"
                        },
                        isReady = isDeliveredOrArrived,
                        onClick = onShowColisDetails
                    )
                }
            } else {
                Step5ParcelRow(
                    title = "Colis 1 · Lot #${order.id}",
                    measure = if (order.freightMode == "MARITIME") {
                        "${String.format(Locale.US, "%.3f", order.volumeM3 ?: 0.014)} m³"
                    } else {
                        "${String.format(Locale.US, "%.1f", weightKg)} kg"
                    },
                    subtitle = "État intact scellé · $totalQty unités",
                    isReady = true,
                    onClick = onShowColisDetails
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Preuves visuelles d'arrivée",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF131B2E)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(112.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE2E7FF)),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Colis scellé ${order.carrier.ifBlank { "Transitaire" }}",
                                fontFamily = Inter,
                                fontSize = 10.sp,
                                color = Color(0xFF3D494C)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF131B2E).copy(alpha = 0.65f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Ivato Hub #1",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(112.dp)
                            .clickable(onClick = onAddPhoto),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF2F3FF)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = Color(0xFF00687A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "+ Prendre photo",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF00687A),
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step5ParcelRow(
    title: String,
    measure: String,
    subtitle: String,
    isReady: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF2F3FF)
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
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isReady) Color(0xFF00687A) else Color(0xFF712AE2),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF131B2E)
                        )
                        Text(
                            text = measure,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                    Text(
                        text = subtitle,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (isReady) Color(0xFF00687A) else Color(0xFF3D494C)
                    )
                }
            }

            Icon(
                imageVector = if (isReady) Icons.Default.Done else Icons.Default.Visibility,
                contentDescription = "Voir détail",
                tint = if (isReady) Color(0xFF00687A) else Color(0xFF3D494C),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
