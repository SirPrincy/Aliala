package com.example.ui.business.transitaires

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun TransitaireCard(
    trans: CatalogueTransitaireSummaryUi,
    onClick: () -> Unit
) {
    if (trans.isArchived) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.65f)
                .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF2F3FF).copy(alpha = 0.7f),
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFBCC9CD).copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = trans.initials,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = trans.name,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFFBCC9CD).copy(alpha = 0.5f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Archivé",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = trans.routeHub,
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Verrouillé",
                        tint = Color(0xFF6D797D),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF6D797D),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Non sélectionnable pour les nouveaux achats",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .testTag("transitaire_card_${trans.name}"),
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when (trans.avatarType) {
                                        "GRADIENT" -> Brush.linearGradient(
                                            listOf(Color(0xFF712AE2), Color(0xFF06B6D4))
                                        )
                                        "CYAN" -> Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF06B6D4)))
                                        "VIOLET" -> Brush.linearGradient(listOf(Color(0xFF8A4CFC), Color(0xFF8A4CFC)))
                                        else -> Brush.linearGradient(listOf(Color(0xFF00687A), Color(0xFF00687A)))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = trans.initials,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (trans.avatarType == "CYAN") Color(0xFF00424F) else Color.White
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = trans.name,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFF00687A).copy(alpha = 0.10f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Actif",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF00687A)
                                    )
                                }
                            }
                            Text(
                                text = trans.routeHub,
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Détails",
                            tint = Color(0xFF3D494C),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flight,
                                contentDescription = null,
                                tint = if (trans.airRateAr > 0L) Color(0xFF00687A) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (trans.airRateAr > 0L) "${CurrencyFormatter.formatAriary(trans.airRateAr)}/kg" else "Aérien : Non défini",
                                fontFamily = PlusJakartaSans,
                                fontWeight = if (trans.airRateAr > 0L) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (trans.airRateAr > 0L) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (trans.isAirCheapest) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF06B6D4).copy(alpha = 0.3f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("Moins cher", fontFamily = PlusJakartaSans, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00424F))
                                }
                            } else if (trans.airRateTrend.isNotBlank()) {
                                val airDown = trans.airRateTrend.startsWith("-")
                                MiniSparkline(
                                    isUp = !airDown,
                                    color = if (airDown) Color(0xFF06B6D4) else Color(0xFFBA1A1A)
                                )
                                Text(
                                    text = trans.airRateTrend,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (airDown) Color(0xFF00687A) else Color(0xFFBA1A1A)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBoat,
                                contentDescription = null,
                                tint = if (trans.seaRateAr > 0L) Color(0xFF005AC2) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (trans.seaRateAr > 0L) "${CurrencyFormatter.formatAriary(trans.seaRateAr)}/m³" else "Maritime : Non défini",
                                fontFamily = PlusJakartaSans,
                                fontWeight = if (trans.seaRateAr > 0L) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (trans.seaRateAr > 0L) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (trans.seaRateTrend.isNotBlank()) {
                                val seaDown = trans.seaRateTrend.startsWith("-")
                                MiniSparkline(
                                    isUp = !seaDown,
                                    color = if (seaDown) Color(0xFF06B6D4) else Color(0xFFBA1A1A)
                                )
                                Text(
                                    text = trans.seaRateTrend,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (seaDown) Color(0xFF00687A) else Color(0xFFBA1A1A)
                                )
                            }
                        }
                    }
                }

                if (trans.rateApplicationDate.isNotBlank() && (trans.airRateAr > 0L || trans.airFastRateAr > 0L || trans.seaRateAr > 0L)) {
                    Text(
                        text = "En vigueur depuis le ${trans.rateApplicationDate}",
                        fontFamily = Inter,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val realStatsParts = buildList {
                    trans.avgAirTransitDays?.let { add("Aérien ${com.example.util.TransitStatsCalculator.formatDaysAvg(it)}") }
                    trans.avgSeaTransitDays?.let { add("Maritime ${com.example.util.TransitStatsCalculator.formatDaysAvg(it)}") }
                    trans.avgTotalDays?.let { add("Total ${com.example.util.TransitStatsCalculator.formatDaysAvg(it)}") }
                }
                if (realStatsParts.isNotEmpty()) {
                    Text(
                        text = "Délai réel moy. : ${realStatsParts.joinToString(" · ")}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (trans.balanceDueAr > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Solde dû :", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = CurrencyFormatter.formatAriary(trans.balanceDueAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFBA1A1A)
                            )
                            if (trans.balanceDueDate.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFFDAD6))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = trans.balanceDueDate,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF93000A)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Solde :", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "À jour ✓",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF00687A)
                            )
                        }
                    }

                    Text(
                        text = if (trans.activeShipmentsCount > 0) "${trans.activeShipmentsCount} expéditions en cours"
                        else if (trans.lastShipmentDate.isNotBlank()) "0 en cours · ${trans.lastShipmentDate}"
                        else "0 expédition en cours",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniSparkline(
    isUp: Boolean,
    color: Color,
    modifier: Modifier = Modifier.size(width = 24.dp, height = 10.dp)
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            if (isUp) {
                moveTo(size.width * 0.05f, size.height * 0.9f)
                lineTo(size.width * 0.35f, size.height * 0.7f)
                lineTo(size.width * 0.65f, size.height * 0.8f)
                lineTo(size.width * 0.95f, size.height * 0.15f)
            } else {
                moveTo(size.width * 0.05f, size.height * 0.2f)
                lineTo(size.width * 0.35f, size.height * 0.4f)
                lineTo(size.width * 0.65f, size.height * 0.3f)
                lineTo(size.width * 0.95f, size.height * 0.9f)
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 1.8f.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
