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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
internal fun Step5StepperCard(isMaritime: Boolean) {
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
                Step5DoneCircle("Sourcing", colors)
                Box(modifier = Modifier.width(12.dp).height(2.dp).background(colors.cyanAccent))
                Step5DoneCircle("Chine", colors)
                Box(modifier = Modifier.width(12.dp).height(2.dp).background(colors.cyanAccent))
                Step5DoneCircle("Fret", colors)
                Box(modifier = Modifier.width(12.dp).height(2.dp).background(colors.cyanAccent))
                Step5DoneCircle("Transit", colors)
                Box(modifier = Modifier.width(12.dp).height(2.dp).background(colors.cyanAccent))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.violetSolidBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("5", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                    Text("Mada", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = colors.violetAccent, modifier = Modifier.padding(top = 4.dp))
                }

                Box(modifier = Modifier.width(12.dp).height(2.dp).background(colors.badgeNeutralBg))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.badgeNeutralBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("6", fontFamily = PlusJakartaSans, fontSize = 12.sp, color = colors.textSecondary)
                    }
                    Text("Stock", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary, modifier = Modifier.padding(top = 4.dp))
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.innerCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isMaritime) Icons.Default.DirectionsBoat else Icons.Default.FlightLand,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Étape 5 sur 6",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = colors.violetAccent
                            )
                            Text(
                                text = if (isMaritime) "Arrivée Port de Toamasina & Dédouanement" else "Arrivée à Ivato & Dédouanement",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.violetSoftBg
                    ) {
                        Text(
                            text = if (isMaritime) "Maritime" else "Aérien",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = colors.violetAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step5DoneCircle(label: String, colors: TransitaireColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(colors.cyanSolidBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Text(label, fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
internal fun Step5ArrivalPointingCard(
    order: BusinessOrderEntity,
    isMaritime: Boolean,
    arrivalShortName: String,
    arrivalPortName: String,
    madaArrivalDateMillis: Long?,
    receivedParcelsCount: Int,
    expectedParcels: Int,
    isPartialReceptionChecked: Boolean,
    onDecrementParcels: () -> Unit,
    onIncrementParcels: () -> Unit,
    onTogglePartialReception: (Boolean) -> Unit
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMaritime) Icons.Default.DirectionsBoat else Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Pointage à $arrivalShortName",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (order.carrier.isNotBlank()) {
                                "${order.carrier} · ${if (isMaritime) "Fret Maritime" else "Fret Aérien"}"
                            } else {
                                if (isMaritime) "Fret Maritime" else "Fret Aérien"
                            },
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (madaArrivalDateMillis != null) colors.cyanSoftBg else colors.innerCardBg
                ) {
                    Text(
                        text = if (madaArrivalDateMillis != null) {
                            "Arrivé : ${DateUtils.formatShortDate(madaArrivalDateMillis)}"
                        } else if (order.estimatedArrival > 0L) {
                            "Prévu : ${DateUtils.formatShortDate(order.estimatedArrival)}"
                        } else {
                            "Date non définie"
                        },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (madaArrivalDateMillis != null) colors.cyanAccent else colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = colors.innerCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Lieu d'arrivée Madagascar",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isMaritime) {
                                "$arrivalPortName — Débarquement maritime"
                            } else {
                                "$arrivalPortName (Antananarivo) — Fret aérien"
                            } + if (order.carrier.isNotBlank()) " · ${order.carrier}" else "",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.subtleBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Colis réceptionnés",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "$receivedParcelsCount",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                color = colors.cyanAccent
                            )
                            Text(
                                text = "sur $expectedParcels attendus",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.cardBg,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = onDecrementParcels,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Retirer un colis",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = "$receivedParcelsCount",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            IconButton(
                                onClick = onIncrementParcels,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Ajouter un colis",
                                    tint = colors.cyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTogglePartialReception(!isPartialReceptionChecked) }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = isPartialReceptionChecked,
                    onCheckedChange = onTogglePartialReception,
                    colors = CheckboxDefaults.colors(checkedColor = colors.cyanAccent)
                )
                Text(
                    text = "Réception partielle : continuer avec les colis arrivés",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            if (order.customsTaxAr > 0L) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = colors.innerCardBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Frais de douane saisis : ${CurrencyFormatter.formatAriary(order.customsTaxAr)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
