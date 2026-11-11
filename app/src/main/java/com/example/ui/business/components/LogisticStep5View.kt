package com.example.ui.business.components

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.LotCostCalculator

@Composable
fun LogisticStep5View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    onAdvanceToQualityControl: (updatedOrder: BusinessOrderEntity) -> Unit,
    onPreviousStep: () -> Unit
) {
    val context = LocalContext.current

    val breakdown = remember(order, cmupRate) {
        LotCostCalculator.calculate(order, cmupRate)
    }
    val totalQty = breakdown.quantity
    val effectiveCmupRate = breakdown.effectiveCmupRate
    val merchandiseTotalAr = breakdown.merchandiseTotalAr
    val domesticPortAr = breakdown.domesticPortAr
    val freightCostAr = breakdown.freightCostAr
    val totalDisbursedAr = breakdown.totalDisbursedAr
    val weightKg = order.weightKg ?: 0.0

    // Interactive reception counter based on tracking numbers
    val parsedTrackings = remember(order.trackingNumbers) {
        order.trackingNumbers.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
    val expectedParcels = parsedTrackings.size.coerceAtLeast(2)
    var receivedParcelsCount by rememberSaveable {
        mutableIntStateOf(if (order.receivedParcelsCount > 0) order.receivedParcelsCount else expectedParcels)
    }
    var isPartialReceptionChecked by rememberSaveable {
        mutableStateOf(receivedParcelsCount < expectedParcels)
    }

    // Dialog states
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var showIncidentDialog by remember { mutableStateOf(false) }
    var incidentDescription by rememberSaveable { mutableStateOf(order.incidentReport) }
    var showColis2DetailsDialog by remember { mutableStateOf(false) }

    val photoList = remember {
        mutableStateListOf("Ivato Hub #1" to "Reçu au tarmac cargo")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. STEPPER 6 ÉTAPES
        // ==========================================
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
                // Stepper horizontal: Sourcing, Chine, Fret, Transit, Mada, Stock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step 1: Sourcing (Check)
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
                        Text("Sourcing", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C), modifier = Modifier.padding(top = 4.dp))
                    }

                    Box(modifier = Modifier.width(12.dp).height(2.dp).background(Color(0xFF00687A)))

                    // Step 2: Chine (Check)
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
                        Text("Chine", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C), modifier = Modifier.padding(top = 4.dp))
                    }

                    Box(modifier = Modifier.width(12.dp).height(2.dp).background(Color(0xFF00687A)))

                    // Step 3: Fret (Check)
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
                        Text("Fret", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C), modifier = Modifier.padding(top = 4.dp))
                    }

                    Box(modifier = Modifier.width(12.dp).height(2.dp).background(Color(0xFF00687A)))

                    // Step 4: Transit (Check)
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
                        Text("Transit", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C), modifier = Modifier.padding(top = 4.dp))
                    }

                    Box(modifier = Modifier.width(12.dp).height(2.dp).background(Color(0xFF00687A)))

                    // Step 5: Mada (Active - Violet)
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

                    // Step 6: Stock (Inactive)
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

                // Active status pill card
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

        // ==========================================
        // 2. RÉCEPTION & POINTAGE IVATO
        // ==========================================
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
                                text = "Hub SinoExpress Cargo",
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
                            text = com.example.util.DateUtils.formatShortDate(order.estimatedArrival),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Location details
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
                                text = "Hub SinoExpress Ivato Cargo, Zone Fret International, Antananarivo",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }
                }

                // Stepper interactif de pointage
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

                        // Counter buttons: [-] count [+]
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
                                    onClick = {
                                        if (receivedParcelsCount > 0) {
                                            receivedParcelsCount--
                                            isPartialReceptionChecked = (receivedParcelsCount < expectedParcels)
                                        }
                                    },
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
                                    onClick = {
                                        if (receivedParcelsCount < expectedParcels) {
                                            receivedParcelsCount++
                                            isPartialReceptionChecked = (receivedParcelsCount < expectedParcels)
                                        }
                                    },
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

                // Option d'exception: Réception partielle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            isPartialReceptionChecked = !isPartialReceptionChecked
                            if (isPartialReceptionChecked && receivedParcelsCount == expectedParcels) {
                                receivedParcelsCount = 1
                            } else if (!isPartialReceptionChecked && receivedParcelsCount < expectedParcels) {
                                receivedParcelsCount = expectedParcels
                            }
                        }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isPartialReceptionChecked,
                        onCheckedChange = { checked ->
                            isPartialReceptionChecked = checked
                            if (checked && receivedParcelsCount == expectedParcels) {
                                receivedParcelsCount = 1
                            } else if (!checked && receivedParcelsCount < expectedParcels) {
                                receivedParcelsCount = expectedParcels
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00687A))
                    )
                    Text(
                        text = "Réception partielle : continuer avec les colis arrivés",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = Color(0xFF3D494C)
                    )
                }

                // Statut Dédouanement
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

        // ==========================================
        // 3. ÉTAT DES COLIS AU DÉCHARGEMENT
        // ==========================================
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
                        text = "2/2 inspectés",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF00687A)
                    )
                }

                // Liste des colis
                // Colis 1: Intact scellé
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF00687A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Colis 1",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Text(
                                        text = if (order.freightMode == "MARITIME") "${String.format(java.util.Locale.US, "%.3f", ((order.volumeM3 ?: 0.014) * 0.65))} m³" else "${String.format(java.util.Locale.US, "%.1f", (weightKg * 0.65))} kg",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                }
                                Text(
                                    text = "État intact scellé",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF00687A)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            tint = Color(0xFF00687A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Colis 2: Carton froissé / Ruban intact
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showColis2DetailsDialog = true },
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF712AE2),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Colis 2",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Text(
                                        text = if (order.freightMode == "MARITIME") "${String.format(java.util.Locale.US, "%.3f", ((order.volumeM3 ?: 0.014) * 0.35))} m³" else "${String.format(java.util.Locale.US, "%.1f", (weightKg * 0.35))} kg",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                }
                                Text(
                                    text = "Carton froissé · Ruban intact",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF3D494C)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Voir détail",
                            tint = Color(0xFF3D494C),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Preuves visuelles d'arrivée (Photos)
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
                        // Photo 1: Ivato Hub #1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(112.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE2E7FF)),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = Color(0xFF00687A),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Colis scellé SinoExpress",
                                    fontFamily = Inter,
                                    fontSize = 10.sp,
                                    color = Color(0xFF3D494C)
                                )
                            }

                            // Bottom label overlay
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

                        // Button "+ Prendre photo"
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(112.dp)
                                .clickable { showAddPhotoDialog = true },
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

        // ==========================================
        // 4. STOCK & FACTURE TRANSITAIRE
        // ==========================================
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
                // Notice sécurisation du stock
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
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF00687A),
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Règle de sécurisation du Stock",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = "Le stock ne sera incrémenté qu'après validation complète du contrôle qualité à l'Étape 6. Vos bilans d'inventaire restent protégés.",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color(0xFF3D494C),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Facture SinoExpress
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEAEDFF)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color(0xFF712AE2),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Facture SinoExpress",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF131B2E)
                                )
                                Text(
                                    text = "Solde fret à régler",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = Color(0xFF3D494C)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyFormatter.formatAriary(freightCostAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF712AE2)
                            )
                            Text(
                                text = "Règlement au retrait",
                                fontFamily = PlusJakartaSans,
                                fontSize = 10.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. MINI-CARTE COÛT DE REVIENT DIRECT
        // ==========================================
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEAEDFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Coût de revient en direct",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFFACEDFF).copy(alpha = 0.40f)
                    ) {
                        Text(
                            text = "Consolidé",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF00687A),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFAF8FF)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "Total déboursé réel",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF3D494C)
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(totalDisbursedAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Color(0xFF00687A)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Marchandise usine (Chine)", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                            Text(CurrencyFormatter.formatAriary(merchandiseTotalAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Port Chine & logistique locale", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                            Text(CurrencyFormatter.formatAriary(domesticPortAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fret & Dédouanement Ivato", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                            Text(CurrencyFormatter.formatAriary(freightCostAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. ACTIONS & CTA FIXES AU BAS
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val canAdvance = receivedParcelsCount > 0
                val unitsReceived = if (isPartialReceptionChecked && receivedParcelsCount < expectedParcels) {
                    (totalQty * receivedParcelsCount / expectedParcels).coerceAtLeast(1)
                } else {
                    totalQty
                }

                Button(
                    onClick = {
                        val nextStatus = OrderStatus.QUALITY_CONTROL.code
                        val updated = order.copy(
                            status = nextStatus,
                            receivedParcelsCount = receivedParcelsCount,
                            receivedUnitsCount = unitsReceived,
                            freightCostAr = freightCostAr,
                            incidentReport = if (incidentDescription.isNotBlank()) incidentDescription else order.incidentReport
                        )
                        onAdvanceToQualityControl(updated)
                        Toast.makeText(context, "Arrivée validée ($unitsReceived/$totalQty unités) ! Passage au contrôle qualité.", Toast.LENGTH_SHORT).show()
                    },
                    enabled = canAdvance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_validate_arrival_and_qc"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00687A),
                        disabledContainerColor = Color(0xFFBCC9CD)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (canAdvance) "Valider l'arrivée ($receivedParcelsCount/$expectedParcels colis) et lancer le contrôle" else "Sélectionnez au moins 1 colis reçu",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIncidentDialog = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = Color(0xFFBA1A1A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Signaler un carton endommagé ou manquant",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFFBA1A1A)
                    )
                }
            }
        }
    }

    // Modal: Prendre photo à Ivato
    if (showAddPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showAddPhotoDialog = false },
            title = {
                Text("Prendre une photo à l'arrivée", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Photographiez le déchargement des cartons sur le quai d'Ivato pour certifier l'intégrité des scellés.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = Color(0xFF3D494C)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        photoList.add("Ivato Hub #${photoList.size + 1}" to "Photo certifiée")
                        showAddPhotoDialog = false
                        Toast.makeText(context, "Photo enregistrée !", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00687A))
                ) {
                    Text("Capturer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPhotoDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Modal: Signaler carton endommagé
    if (showIncidentDialog) {
        AlertDialog(
            onDismissRequest = { showIncidentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFBA1A1A))
                    Text("Signaler une anomalie", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Précisez les dommages constatés sur les colis au hub Ivato (carton écrasé, bande décollée, manquant) :",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = Color(0xFF3D494C)
                    )
                    OutlinedTextField(
                        value = incidentDescription,
                        onValueChange = { incidentDescription = it },
                        placeholder = { Text("Ex: Colis 2 coin écrasé, ruban déchiré...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showIncidentDialog = false
                        Toast.makeText(context, "Signalement enregistré sur le bordereau d'Ivato.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showIncidentDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Modal: Détail Colis 2
    if (showColis2DetailsDialog) {
        AlertDialog(
            onDismissRequest = { showColis2DetailsDialog = false },
            title = {
                Text("Détail inspection Colis 2", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Poids estimé : ${if (expectedParcels > 0 && weightKg > 0) String.format(java.util.Locale.US, "%.1f", (weightKg / expectedParcels)) + " kg" else "—"}", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF131B2E))
                    Text("Contenu théorique : 5 unités Rouge Sport", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF2F3FF), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Observation déchargement : Léger froissement de la boîte extérieure pendant le transbordement, mais ruban adhésif SinoExpress intact. Aucun article exposé.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showColis2DetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00687A))
                ) {
                    Text("Compris", color = Color.White)
                }
            }
        )
    }
}
