package com.example.ui.business.wizard

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.util.Locale

@Composable
internal fun Step3StepperHeaderCard() {
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ÉTAPE 3 SUR 6",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.violetAccent,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Contrôle Réception",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }

            Text(
                text = "Arrivé à l'entrepôt du transitaire",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colors.textPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(16.dp))
                    }
                    Text("Cmd", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary, modifier = Modifier.padding(top = 4.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(16.dp))
                    }
                    Text("Transit", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary, modifier = Modifier.padding(top = 4.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.violetSolidBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warehouse, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                    }
                    Text("Entrepôt", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = colors.violetAccent, modifier = Modifier.padding(top = 4.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.badgeNeutralBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("4", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = colors.textSecondary)
                    }
                    Text("Vers Ma...", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary, modifier = Modifier.padding(top = 4.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.badgeNeutralBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("5", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = colors.textSecondary)
                    }
                    Text("Arrivé M...", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary, modifier = Modifier.padding(top = 4.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.badgeNeutralBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("6", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = colors.textSecondary)
                    }
                    Text("Stock", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
internal fun Step3ForwarderModeCard(
    selectedForwarder: String,
    warehouseLocation: String,
    airRatePerKg: Long,
    seaRatePerM3: Long,
    isAirFreight: Boolean,
    onOpenChangeForwarder: () -> Unit,
    onSelectAirMode: (Boolean) -> Unit
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
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = colors.violetAccent, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("TRANSITAIRE PARTENAIRE", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = colors.textSecondary, letterSpacing = 0.5.sp)
                        Text(selectedForwarder, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.violetSoftBg,
                    modifier = Modifier.clickable(onClick = onOpenChangeForwarder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = colors.violetAccent, modifier = Modifier.size(16.dp))
                        Text("Changer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.violetAccent)
                    }
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
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(16.dp))
                        Text(warehouseLocation, fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.violetSoftBg
                    ) {
                        val airBadge = if (airRatePerKg > 0L) "${CurrencyFormatter.formatAriary(airRatePerKg)}/kg" else "Aérien non défini"
                        val seaBadge = if (seaRatePerM3 > 0L) "${CurrencyFormatter.formatAriary(seaRatePerM3)}/m³" else "Maritime non défini"
                        Text(
                            text = "$airBadge · $seaBadge",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = colors.violetAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Mode de Fret International", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.toggleTrackBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable { onSelectAirMode(true) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAirFreight) colors.cardBg else Color.Transparent,
                            shadowElevation = if (isAirFreight) 1.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = if (isAirFreight) colors.violetAccent else colors.textSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("✈ Aérien (Express)", fontFamily = PlusJakartaSans, fontWeight = if (isAirFreight) FontWeight.Bold else FontWeight.Medium, fontSize = 12.sp, color = if (isAirFreight) colors.violetAccent else colors.textSecondary)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable { onSelectAirMode(false) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isAirFreight) colors.cardBg else Color.Transparent,
                            shadowElevation = if (!isAirFreight) 1.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.DirectionsBoat, contentDescription = null, tint = if (!isAirFreight) colors.violetAccent else colors.textSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🚢 Maritime (Groupé)", fontFamily = PlusJakartaSans, fontWeight = if (!isAirFreight) FontWeight.Bold else FontWeight.Medium, fontSize = 12.sp, color = if (!isAirFreight) colors.violetAccent else colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun Step3WeighingAndProofsCard(
    warehouseArrivalDate: Long?,
    isAirFreight: Boolean,
    weightInput: String,
    onWeightChange: (String) -> Unit,
    volumeInput: String,
    onVolumeChange: (String) -> Unit,
    weightDouble: Double,
    volumeDouble: Double,
    upstreamCarrierLabel: String,
    agentNotes: String,
    onAgentNotesChange: (String) -> Unit
) {
    val colors = rememberTransitaireColors()
    val hasValidMeasure = if (isAirFreight) weightDouble > 0.0 else volumeDouble > 0.0

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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = colors.cyanSoftBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Pesée & Cubage Certifiés", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.cyanAccent)
                        Text(
                            text = "Renseignez le poids ou le volume réel mesuré à l'arrivée physique à l'entrepôt.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
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
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.EventAvailable, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                        Text("Arrivée dépôt Chine :", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                    }
                    Text(
                        text = warehouseArrivalDate?.let { DateUtils.formatShortDate(it) } ?: "Non renseignée",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (warehouseArrivalDate != null) colors.textPrimary else colors.textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Poids réel mesuré", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = colors.textPrimary)
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = colors.inputBg) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = onWeightChange,
                                singleLine = true,
                                placeholder = {
                                    Text("0.0", fontFamily = PlusJakartaSans, fontSize = 15.sp, color = colors.textSecondary.copy(alpha = 0.5f))
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    cursorColor = colors.cyanAccent
                                ),
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                            )
                            Surface(shape = RoundedCornerShape(6.dp), color = colors.violetSoftBg) {
                                Text("kg", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.violetAccent, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Volume certifié", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = colors.textPrimary)
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = colors.inputBg) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = volumeInput,
                                onValueChange = onVolumeChange,
                                singleLine = true,
                                placeholder = {
                                    Text("0,00000", fontFamily = PlusJakartaSans, fontSize = 15.sp, color = colors.textSecondary.copy(alpha = 0.5f))
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    cursorColor = colors.cyanAccent
                                ),
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                            )
                            Text("m³", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.textSecondary, modifier = Modifier.padding(end = 4.dp))
                        }
                    }
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(20.dp))
                        Column {
                            Text(
                                text = when {
                                    isAirFreight && weightDouble > 0.0 -> "Lot pesé : ${String.format(Locale.US, "%.1f", weightDouble)} kg"
                                    !isAirFreight && volumeDouble > 0.0 -> "Lot mesuré : ${String.format(Locale.FRENCH, "%.5f", volumeDouble)} m³"
                                    else -> "Mesure en attente de saisie"
                                },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                            Text("Transporteur amont : $upstreamCarrierLabel", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (hasValidMeasure) colors.cyanSoftBg else colors.badgeNeutralBg
                    ) {
                        Text(
                            text = if (hasValidMeasure) "Mesuré" else "À saisir",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (hasValidMeasure) colors.cyanAccent else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Notes de réception entrepôt", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = colors.textPrimary)
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = colors.inputBg) {
                    OutlinedTextField(
                        value = agentNotes,
                        onValueChange = onAgentNotesChange,
                        placeholder = {
                            Text(
                                text = "Saisir une note réelle de réception (optionnel)",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textSecondary.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            cursorColor = colors.cyanAccent
                        ),
                        textStyle = TextStyle(fontFamily = Inter, fontSize = 12.sp, color = colors.textPrimary)
                    )
                }
            }
        }
    }
}
