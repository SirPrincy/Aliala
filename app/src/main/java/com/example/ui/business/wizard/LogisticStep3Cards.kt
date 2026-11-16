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
import androidx.compose.material.icons.filled.AddAPhoto
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
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.util.Locale

@Composable
internal fun Step3StepperHeaderCard() {
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
                    color = Color(0xFF712AE2),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Contrôle Réception",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF3D494C)
                )
            }

            Text(
                text = "Arrivé à l'entrepôt du transitaire",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF131B2E)
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
                            .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(16.dp))
                    }
                    Text("Cmd", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C), modifier = Modifier.padding(top = 4.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(16.dp))
                    }
                    Text("Transit", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C), modifier = Modifier.padding(top = 4.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF712AE2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warehouse, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                    }
                    Text("Entrepôt", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF712AE2), modifier = Modifier.padding(top = 4.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("4", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF3D494C).copy(alpha = 0.6f))
                    }
                    Text("Vers Ma...", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C).copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("5", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF3D494C).copy(alpha = 0.6f))
                    }
                    Text("Arrivé M...", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C).copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("6", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF3D494C).copy(alpha = 0.6f))
                    }
                    Text("Stock", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C).copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
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
                            .background(Color(0xFF712AE2).copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF712AE2), modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("TRANSITAIRE PARTENAIRE", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C), letterSpacing = 0.5.sp)
                        Text(selectedForwarder, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF131B2E))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEADDFF).copy(alpha = 0.30f),
                    modifier = Modifier.clickable(onClick = onOpenChangeForwarder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color(0xFF712AE2), modifier = Modifier.size(16.dp))
                        Text("Changer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF712AE2))
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF2F3FF)
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(16.dp))
                        Text(warehouseLocation, fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFFEADDFF).copy(alpha = 0.40f)
                    ) {
                        Text(
                            text = "${CurrencyFormatter.formatAriary(airRatePerKg)}/kg · ${CurrencyFormatter.formatAriary(seaRatePerM3)}/m³",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = Color(0xFF712AE2),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Mode de Fret International", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF131B2E))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF2F3FF)
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
                            color = if (isAirFreight) Color.White else Color.Transparent,
                            shadowElevation = if (isAirFreight) 1.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = if (isAirFreight) Color(0xFF712AE2) else Color(0xFF3D494C), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("✈ Aérien (Express)", fontFamily = PlusJakartaSans, fontWeight = if (isAirFreight) FontWeight.Bold else FontWeight.Medium, fontSize = 12.sp, color = if (isAirFreight) Color(0xFF712AE2) else Color(0xFF3D494C))
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable { onSelectAirMode(false) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isAirFreight) Color.White else Color.Transparent,
                            shadowElevation = if (!isAirFreight) 1.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.DirectionsBoat, contentDescription = null, tint = if (!isAirFreight) Color(0xFF712AE2) else Color(0xFF3D494C), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🚢 Maritime (Groupé)", fontFamily = PlusJakartaSans, fontWeight = if (!isAirFreight) FontWeight.Bold else FontWeight.Medium, fontSize = 12.sp, color = if (!isAirFreight) Color(0xFF712AE2) else Color(0xFF3D494C))
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
    orderDate: Long,
    isAirFreight: Boolean,
    weightInput: String,
    onWeightChange: (String) -> Unit,
    volumeInput: String,
    onVolumeChange: (String) -> Unit,
    weightDouble: Double,
    volumeDouble: Double,
    upstreamCarrierLabel: String,
    agentNotes: String,
    onAgentNotesChange: (String) -> Unit,
    onOpenAddPhoto: () -> Unit
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFACEDFF).copy(alpha = 0.25f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Pesée & Cubage Certifiés", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF00424F))
                        Text(
                            text = "Le poids et le volume réels ne sont certifiés qu'à l'arrivée physique à l'entrepôt du transitaire.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF004E5C),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF2F3FF)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.EventAvailable, contentDescription = null, tint = Color(0xFF3D494C), modifier = Modifier.size(18.dp))
                        Text("Arrivée certifiée :", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                    }
                    Text(DateUtils.formatShortDate(orderDate), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Poids réel mesuré", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(0xFF131B2E))
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = Color(0xFFF2F3FF)) {
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent),
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF131B2E))
                            )
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEADDFF).copy(alpha = 0.50f)) {
                                Text("kg", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF712AE2), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Volume certifié", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(0xFF131B2E))
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = Color(0xFFF2F3FF)) {
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent),
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF131B2E))
                            )
                            Text("m³", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF3D494C), modifier = Modifier.padding(end = 4.dp))
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(20.dp))
                        Column {
                            Text(
                                text = if (isAirFreight) "Lot pesé : ${String.format(Locale.US, "%.1f", weightDouble)} kg" else "Lot mesuré : ${String.format(Locale.US, "%.3f", volumeDouble)} m³",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text("Transporteur amont : $upstreamCarrierLabel", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                        }
                    }

                    Surface(shape = RoundedCornerShape(9999.dp), color = Color(0xFFACEDFF).copy(alpha = 0.40f)) {
                        Text("Scellé OK", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF00687A), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Preuves visuelles (Pesée & Étiquette)", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF131B2E))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE2E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(24.dp))
                            Text("Balance", fontFamily = Inter, fontSize = 9.sp, color = Color(0xFF3D494C))
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF131B2E).copy(alpha = 0.75f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("${weightDouble}kg", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEADDFF).copy(alpha = 0.50f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF712AE2), modifier = Modifier.size(24.dp))
                            Text("Bordereau", fontFamily = Inter, fontSize = 9.sp, color = Color(0xFF3D494C))
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF131B2E).copy(alpha = 0.75f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(upstreamCarrierLabel.take(8), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(onClick = onOpenAddPhoto),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF2F3FF)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color(0xFF712AE2), modifier = Modifier.size(24.dp))
                            Text("+ Photo", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF712AE2), modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Notes de l'agent de réception", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(0xFF131B2E))
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = Color(0xFFF2F3FF)) {
                    OutlinedTextField(
                        value = agentNotes,
                        onValueChange = onAgentNotesChange,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent),
                        textStyle = TextStyle(fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF131B2E))
                    )
                }
            }
        }
    }
}
