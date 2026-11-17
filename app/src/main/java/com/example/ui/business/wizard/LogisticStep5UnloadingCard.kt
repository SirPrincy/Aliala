package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.model.DomesticParcelItem
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import java.util.Locale

@Composable
internal fun Step5UnloadingStateCard(
    order: BusinessOrderEntity,
    isMaritime: Boolean,
    parcels: List<DomesticParcelItem>,
    receivedParcelsCount: Int,
    expectedParcels: Int,
    totalQty: Int,
    weightKg: Double,
    onShowColisDetails: () -> Unit
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "État au déchargement",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Pointage des colis reçus",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Text(
                    text = "$receivedParcelsCount/$expectedParcels pointés",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = colors.cyanAccent
                )
            }

            val parcelCountForDisplay = expectedParcels.coerceAtLeast(1)
            val realMeasureText = if (isMaritime) {
                val vol = order.volumeM3
                if (vol != null && vol > 0.0) {
                    "${String.format(Locale.FRENCH, "%.5f", vol / parcelCountForDisplay)} m³"
                } else {
                    "Volume non renseigné"
                }
            } else {
                if (weightKg > 0.0) {
                    "${String.format(Locale.US, "%.1f", weightKg / parcelCountForDisplay)} kg"
                } else {
                    "Poids non renseigné"
                }
            }

            if (parcels.isNotEmpty()) {
                parcels.forEachIndexed { idx, parcel ->
                    val isDeliveredOrArrived = idx < receivedParcelsCount
                    Step5ParcelRow(
                        title = "Colis ${idx + 1} · ${parcel.trackingNumber}",
                        measure = realMeasureText,
                        subtitle = if (isDeliveredOrArrived) {
                            "Pointé à l'arrivée · ${parcel.contentDescription}"
                        } else {
                            "Non pointé · ${parcel.contentDescription}"
                        },
                        isReady = isDeliveredOrArrived,
                        colors = colors,
                        onClick = onShowColisDetails
                    )
                }
            } else {
                Step5ParcelRow(
                    title = "Lot #${order.id}",
                    measure = realMeasureText,
                    subtitle = "$totalQty unités commandées",
                    isReady = receivedParcelsCount > 0,
                    colors = colors,
                    onClick = onShowColisDetails
                )
            }

            if (order.incidentReport.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = colors.dangerSoftBg
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Anomalie signalée",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.dangerAccent
                        )
                        Text(
                            text = order.incidentReport,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
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
    colors: TransitaireColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
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
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.cardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isReady) colors.cyanAccent else colors.violetAccent,
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
                            color = colors.textPrimary
                        )
                        Text(
                            text = measure,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                    Text(
                        text = subtitle,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (isReady) colors.cyanAccent else colors.textSecondary
                    )
                }
            }

            Icon(
                imageVector = if (isReady) Icons.Default.Done else Icons.Default.Visibility,
                contentDescription = "Voir détail",
                tint = if (isReady) colors.cyanAccent else colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
