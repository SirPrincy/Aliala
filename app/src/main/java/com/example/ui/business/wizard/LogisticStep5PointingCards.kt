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
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.DateUtils

@Composable
internal fun Step5StepperCard() {
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
                Step5DoneCircle("Sourcing")
                Box(modifier = Modifier.width(12.dp).height(2.dp).background(Color(0xFF00687A)))
                Step5DoneCircle("Chine")
                Box(modifier = Modifier.width(12.dp).height(2.dp).background(Color(0xFF00687A)))
                Step5DoneCircle("Fret")
                Box(modifier = Modifier.width(12.dp).height(2.dp).background(Color(0xFF00687A)))
                Step5DoneCircle("Transit")
                Box(modifier = Modifier.width(12.dp).height(2.dp).background(Color(0xFF00687A)))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8A4CFC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("5", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                    Text("Mada", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF712AE2), modifier = Modifier.padding(top = 4.dp))
                }

                Box(modifier = Modifier.width(12.dp).height(2.dp).background(Color(0xFFE2E7FF)))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("6", fontFamily = PlusJakartaSans, fontSize = 12.sp, color = Color(0xFF3D494C).copy(alpha = 0.5f))
                    }
                    Text("Stock", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C).copy(alpha = 0.5f), modifier = Modifier.padding(top = 4.dp))
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF2F3FF)
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
                            imageVector = Icons.Default.FlightLand,
                            contentDescription = null,
                            tint = Color(0xFF712AE2),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Étape 5 sur 6",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF712AE2)
                            )
                            Text(
                                text = "Arrivé à Madagascar & Dédouanement",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF131B2E)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFFDAE2FD)
                    ) {
                        Text(
                            text = "Actif",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF712AE2),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step5DoneCircle(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF00687A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Text(label, fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C), modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
internal fun Step5IvatoPointingCard(
    order: BusinessOrderEntity,
    receivedParcelsCount: Int,
    expectedParcels: Int,
    isPartialReceptionChecked: Boolean,
    onDecrementParcels: () -> Unit,
    onIncrementParcels: () -> Unit,
    onTogglePartialReception: (Boolean) -> Unit
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
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = Color(0xFF00687A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Pointage à Ivato",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF131B2E)
                        )
                        Text(
                            text = "Hub ${order.carrier.ifBlank { "Transitaire partenaire" }} Cargo",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = Color(0xFFF2F3FF)
                ) {
                    Text(
                        text = DateUtils.formatShortDate(order.estimatedArrival),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = Color(0xFF3D494C),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF00687A),
                        modifier = Modifier.size(18.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Lieu de mise à disposition",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF131B2E)
                        )
                        Text(
                            text = "Hub ${order.carrier.ifBlank { "Transitaire" }} Ivato Cargo, Zone Fret International, Antananarivo",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFAF8FF)
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
                            color = Color(0xFF131B2E)
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
                                color = Color(0xFF00687A)
                            )
                            Text(
                                text = "sur $expectedParcels attendus",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
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
                                    tint = Color(0xFF3D494C),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = "$receivedParcelsCount",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF131B2E),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            IconButton(
                                onClick = onIncrementParcels,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Ajouter un colis",
                                    tint = Color(0xFF00687A),
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
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00687A))
                )
                Text(
                    text = "Réception partielle : continuer avec les colis arrivés",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = Color(0xFF3D494C)
                )
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF00687A),
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Dédouané sans surtaxe",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF131B2E)
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00687A))
                            )
                        }
                        Text(
                            text = "Taxe douanière comprise dans le fret transitaire international.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }
            }
        }
    }
}
