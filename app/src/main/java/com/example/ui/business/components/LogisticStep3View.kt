package com.example.ui.business.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.FreightMode
import com.example.data.model.OrderStatus
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.Calculations
import com.example.util.CurrencyFormatter
import com.example.util.LotCostCalculator

@Composable
fun LogisticStep3View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    onAdvanceToFlight: (updatedOrder: BusinessOrderEntity) -> Unit,
    onPreviousStep: () -> Unit
) {
    val context = LocalContext.current

    val initialCarrier = order.carrier.ifBlank { "SinoExpress Hub Baiyun" }
    var selectedForwarder by rememberSaveable { mutableStateOf(initialCarrier) }
    var isAirFreight by rememberSaveable { mutableStateOf(!order.freightMode.equals("MARITIME", ignoreCase = true) && !order.freightMode.equals("SEA", ignoreCase = true)) }

    val airRatePerKg = when {
        selectedForwarder.contains("MadaAir", ignoreCase = true) -> 36_500L
        selectedForwarder.contains("QuickMada", ignoreCase = true) -> 42_000L
        selectedForwarder.contains("Oceanic", ignoreCase = true) -> 39_000L
        else -> 38_000L
    }
    val seaRatePerM3 = when {
        selectedForwarder.contains("MadaAir", ignoreCase = true) -> 1_280_000L
        selectedForwarder.contains("QuickMada", ignoreCase = true) -> 1_150_000L
        selectedForwarder.contains("Oceanic", ignoreCase = true) -> 1_100_000L
        else -> 1_200_000L
    }

    var weightInput by rememberSaveable { mutableStateOf(order.weightKg?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "3.2") }
    var volumeInput by rememberSaveable { mutableStateOf(order.volumeM3?.let { String.format(java.util.Locale.US, "%.3f", it) } ?: "0.014") }
    var agentNotes by rememberSaveable {
        mutableStateOf(order.receptionNotes.ifBlank { "Cartons intacts et propres. Pesée conforme au bordereau d'expédition fournisseur." })
    }

    val breakdown = remember(order, cmupRate) {
        LotCostCalculator.calculate(order, cmupRate)
    }
    val totalQty = breakdown.quantity
    val effectiveCmupRate = breakdown.effectiveCmupRate
    val unitPriceYuan = breakdown.unitPriceYuan
    val merchandiseTotalAr = breakdown.merchandiseTotalAr
    val domesticPortAr = breakdown.domesticPortAr

    // Dynamic calculations with decimal comma support
    fun String.toDecimalOrNull(): Double? = replace(',', '.').trim().toDoubleOrNull()
    val weightDouble = weightInput.toDecimalOrNull() ?: 0.0
    val volumeDouble = volumeInput.toDecimalOrNull() ?: 0.0
    val isInputValid = if (isAirFreight) weightDouble > 0.0 else volumeDouble > 0.0

    val calculatedFreightCostAr = if (isAirFreight) {
        (weightDouble * airRatePerKg).toLong()
    } else {
        (volumeDouble * seaRatePerM3).toLong()
    }

    val totalCostOfGoodsAr = merchandiseTotalAr + domesticPortAr + calculatedFreightCostAr
    val unitCostAr = totalCostOfGoodsAr / totalQty.coerceAtLeast(1)
    val projectedGrossMarginPercent = if (order.resalePriceAr > 0 && unitCostAr > 0) {
        val margin = ((order.resalePriceAr - unitCostAr).toDouble() / order.resalePriceAr.toDouble()) * 100.0
        String.format(java.util.Locale.US, "%.1f", margin)
    } else {
        "—"
    }

    // Accordion expand/collapse
    var isBudgetExpanded by remember { mutableStateOf(true) }
    val chevronRotation by animateFloatAsState(targetValue = if (isBudgetExpanded) 180f else 0f, label = "rot")

    // Photo proofs state
    val photoProofList = remember {
        mutableStateListOf(
            "Pesée certifiée (${String.format(java.util.Locale.US, "%.1f", weightDouble)} kg)" to "${String.format(java.util.Locale.US, "%.1f", weightDouble)}kg",
            "Bordereau scellé" to "OK"
        )
    }
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var showChangeForwarderDialog by remember { mutableStateOf(false) }

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

                // Visual Track of 6 steps
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step 1: Cmd (Validated)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Cmd",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = Color(0xFF3D494C),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Step 2: Transit (Validated)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Transit",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = Color(0xFF3D494C),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Step 3: Entrepôt (Active - Violet)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF712AE2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Text(
                            text = "Entrepôt",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF712AE2),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Step 4: Vers Mada
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
                        Text(
                            text = "Vers Ma...",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = Color(0xFF3D494C).copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Step 5: Arrivé Mada
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
                        Text(
                            text = "Arrivé M...",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = Color(0xFF3D494C).copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Step 6: Stock
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
                        Text(
                            text = "Stock",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = Color(0xFF3D494C).copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. CARTE 1: SÉLECTEUR DE TRANSITAIRE & MODE
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
                                .background(Color(0xFF712AE2).copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF712AE2),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "TRANSITAIRE PARTENAIRE",
                                fontFamily = PlusJakartaSans,
                                fontSize = 10.sp,
                                color = Color(0xFF3D494C),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = selectedForwarder,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF131B2E)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEADDFF).copy(alpha = 0.30f),
                        modifier = Modifier.clickable { showChangeForwarderDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = Color(0xFF712AE2),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Changer",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF712AE2)
                            )
                        }
                    }
                }

                // Quick rate info bar
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
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Guangzhou, Zone C4",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF3D494C)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0xFFEADDFF).copy(alpha = 0.40f)
                        ) {
                            Text(
                                text = "38 000 Ar/kg · 1 200 000 Ar/m³",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                color = Color(0xFF712AE2),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // International Freight Mode Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Mode de Fret International",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF131B2E)
                    )

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
                            // Air Freight Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clickable { isAirFreight = true },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isAirFreight) Color.White else Color.Transparent,
                                shadowElevation = if (isAirFreight) 1.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlightTakeoff,
                                        contentDescription = null,
                                        tint = if (isAirFreight) Color(0xFF712AE2) else Color(0xFF3D494C),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "✈ Aérien (Express)",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isAirFreight) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isAirFreight) Color(0xFF712AE2) else Color(0xFF3D494C)
                                    )
                                }
                            }

                            // Sea Freight Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clickable { isAirFreight = false },
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isAirFreight) Color.White else Color.Transparent,
                                shadowElevation = if (!isAirFreight) 1.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBoat,
                                        contentDescription = null,
                                        tint = if (!isAirFreight) Color(0xFF712AE2) else Color(0xFF3D494C),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🚢 Maritime (Groupé)",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (!isAirFreight) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (!isAirFreight) Color(0xFF712AE2) else Color(0xFF3D494C)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. CARTE 2: SAISIE PESÉE & MESURES OFFICIELLES
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
                // Bannière Pédagogique
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
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF00687A),
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Pesée & Cubage Certifiés",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF00424F)
                            )
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

                // Arrivée certifiée
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = Color(0xFF3D494C),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Arrivée certifiée :",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                        Text(
                            text = com.example.util.DateUtils.formatShortDate(order.orderDate),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF131B2E)
                        )
                    }
                }

                // Champs Poids réel & Volume certifié
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Poids réel mesuré
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Poids réel mesuré",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFF131B2E)
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF2F3FF)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedTextField(
                                    value = weightInput,
                                    onValueChange = { weightInput = it },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEADDFF).copy(alpha = 0.50f)
                                ) {
                                    Text(
                                        text = "kg",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF712AE2),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Volume certifié
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Volume certifié",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFF131B2E)
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF2F3FF)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedTextField(
                                    value = volumeInput,
                                    onValueChange = { volumeInput = it },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                )
                                Text(
                                    text = "m³",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF3D494C),
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Colis reçus scellés
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (isAirFreight) "Lot pesé : ${String.format(java.util.Locale.US, "%.1f", weightDouble)} kg" else "Lot mesuré : ${String.format(java.util.Locale.US, "%.3f", volumeDouble)} m³",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF131B2E)
                                )
                                Text(
                                    text = "Transporteur amont : SF Express Chine",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = Color(0xFF3D494C)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0xFFACEDFF).copy(alpha = 0.40f)
                        ) {
                            Text(
                                text = "Scellé OK",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color(0xFF00687A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Galerie Photos de Réception & Pesée
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Preuves visuelles (Pesée & Étiquette)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color(0xFF131B2E)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Photo 1: Scale 3.2kg
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE2E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = Color(0xFF00687A),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Balance",
                                    fontFamily = Inter,
                                    fontSize = 9.sp,
                                    color = Color(0xFF3D494C)
                                )
                            }
                            // Bottom right badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF131B2E).copy(alpha = 0.75f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${weightDouble}kg",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = Color.White
                                )
                            }
                        }

                        // Photo 2: SF Exp label
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEADDFF).copy(alpha = 0.50f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = Color(0xFF712AE2),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Bordereau",
                                    fontFamily = Inter,
                                    fontSize = 9.sp,
                                    color = Color(0xFF3D494C)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF131B2E).copy(alpha = 0.75f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "SF Exp",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = Color.White
                                )
                            }
                        }

                        // Add Photo Button
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .clickable { showAddPhotoDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF2F3FF)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = Color(0xFF712AE2),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "+ Photo",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF712AE2),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Notes de l'agent de réception
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Notes de l'agent de réception",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFF131B2E)
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF2F3FF)
                    ) {
                        OutlinedTextField(
                            value = agentNotes,
                            onValueChange = { agentNotes = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF131B2E)
                            )
                        )
                    }
                }
            }
        }

        // ==========================================
        // 4. CARTE 3: CALCUL DYNAMIQUE DU FRET
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF00687A).copy(alpha = 0.10f)),
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
                            text = if (isAirFreight) "Calcul du Fret Aérien" else "Calcul du Fret Maritime",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFFEAEDFF)
                    ) {
                        Text(
                            text = "Barème 01/07",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = Color(0xFF3D494C),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Ligne de calcul dynamique
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF2F3FF)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tarif unitaire applicable :",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF3D494C)
                            )
                            Text(
                                text = if (isAirFreight) "${CurrencyFormatter.formatAriary(airRatePerKg)} / kg" else "${CurrencyFormatter.formatAriary(seaRatePerM3)} / m³",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF131B2E)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAirFreight) "${String.format(java.util.Locale.US, "%.1f", weightDouble)} kg × ${CurrencyFormatter.formatAriary(airRatePerKg)}" else "${String.format(java.util.Locale.US, "%.3f", volumeDouble)} m³ × ${CurrencyFormatter.formatAriary(seaRatePerM3)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(calculatedFreightCostAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF712AE2)
                            )
                        }
                    }
                }

                // Montant final facturé
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Montant final facturé",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFF131B2E)
                        )
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0xFFACEDFF).copy(alpha = 0.40f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF00687A),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Conforme au barème",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFF00687A)
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
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = CurrencyFormatter.formatAriary(calculatedFreightCostAr).replace(" Ar", ""),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = "MGA",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF3D494C),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Le transitaire émet sa facture globale après pesée et contrôle.",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = Color(0xFF3D494C)
                    )
                }
            }
        }

        // ==========================================
        // 5. CARTE 4: GROUPAGE D'ACHATS (MUTUALISATION DE FRET)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEADDFF).copy(alpha = 0.50f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShareLocation,
                                contentDescription = null,
                                tint = Color(0xFF712AE2),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Mutualisation de Fret",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFF712AE2)
                    ) {
                        Text(
                            text = "Lot #EXP-88",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                // Item description row
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF2F3FF)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF00687A).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${order.product.ifEmpty { "Article #${order.id}" }} ($totalQty pcs)",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = "Poids attribué : ${weightDouble} kg (100% de ce colis)",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color(0xFF3D494C)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Ajuster la répartition",
                            tint = Color(0xFF3D494C),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clé de répartition au prorata :",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = Color(0xFF3D494C)
                    )
                    Text(
                        text = "Monocolis (100%)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF131B2E)
                    )
                }
            }
        }

        // ==========================================
        // 6. CARTE 5: SYNTHÈSE BUDGÉTAIRE (COLLAPSIBLE)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isBudgetExpanded = !isBudgetExpanded },
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
                                .background(Color(0xFFACEDFF).copy(alpha = 0.40f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "SYNTHÈSE BUDGÉTAIRE",
                                fontFamily = PlusJakartaSans,
                                fontSize = 10.sp,
                                color = Color(0xFF3D494C),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Coût de Revient Estimé",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF131B2E)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = CurrencyFormatter.formatAriary(totalCostOfGoodsAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF00687A)
                        )
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF00687A),
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(chevronRotation)
                        )
                    }
                }

                AnimatedVisibility(visible = isBudgetExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Marchandise ($totalQty pcs · ¥ ${(unitPriceYuan * totalQty).toInt()}) :",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF3D494C)
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(merchandiseTotalAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF131B2E)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Port Chine domestique (¥ ${String.format(java.util.Locale.US, "%.2f", order.domesticShippingYuan)}) :",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF3D494C)
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(domesticPortAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF131B2E)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isAirFreight) "Fret $selectedForwarder (${String.format(java.util.Locale.US, "%.1f", weightDouble)} kg) :" else "Fret $selectedForwarder (${String.format(java.util.Locale.US, "%.3f", volumeDouble)} m³) :",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF3D494C)
                            )
                            Text(
                                text = CurrencyFormatter.formatAriary(calculatedFreightCostAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF712AE2)
                            )
                        }

                        // Sub-box: Unit cost & projected margin
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
                                Column {
                                    Text(
                                        text = "COÛT UNITAIRE ESTIMÉ",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                    Text(
                                        text = "${CurrencyFormatter.formatAriary(unitCostAr)} / unité",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF00687A)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "MARGE BRUTE PROJETÉE",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                    Text(
                                        text = if (projectedGrossMarginPercent == "—") "Non défini" else "$projectedGrossMarginPercent%",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (projectedGrossMarginPercent.startsWith("-")) Color(0xFFBA1A1A) else Color(0xFF131B2E)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 7. ACTIONS FIXES INFÉRIEURES
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
                Button(
                    onClick = {
                        if (!isInputValid) {
                            val msg = if (isAirFreight) "Veuillez saisir un poids valide (> 0 kg)" else "Veuillez saisir un volume valide (> 0 m³)"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val nextStatus = OrderStatus.FLIGHT.code
                        val updated = order.copy(
                            status = nextStatus,
                            weightKg = weightDouble,
                            volumeM3 = volumeDouble,
                            freightCostAr = calculatedFreightCostAr,
                            carrier = selectedForwarder,
                            freightMode = if (isAirFreight) FreightMode.AIR.name else FreightMode.SEA.name,
                            receptionNotes = agentNotes
                        )
                        onAdvanceToFlight(updated)
                        Toast.makeText(context, "Pesée & réception validées ! Lot en transit.", Toast.LENGTH_SHORT).show()
                    },
                    enabled = isInputValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_validate_warehouse_arrival"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isAirFreight) "Valider l'arrivée à l'entrepôt (Aérien)" else "Valider l'arrivée à l'entrepôt (Maritime)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
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
                        .clickable { onPreviousStep() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color(0xFF3D494C),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Revenir à l'étape 2 (Livraison Chine)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFF3D494C)
                    )
                }
            }
        }
    }

    // Modal: Add Photo Proof
    if (showAddPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showAddPhotoDialog = false },
            title = {
                Text("Ajouter une photo de pesée", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Une photo du colis sur la balance numérique ou de l'étiquette scellée peut être ajoutée en tant que preuve de réception.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = Color(0xFF3D494C)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        photoProofList.add("Preuve ajoutée" to "OK")
                        showAddPhotoDialog = false
                        Toast.makeText(context, "Photo de réception enregistrée !", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00687A))
                ) {
                    Text("Prendre une photo", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPhotoDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Modal: Changer de transitaire
    if (showChangeForwarderDialog) {
        val forwarders = listOf("SinoExpress Hub Baiyun", "DHL Global Forwarding Guangzhou", "Bolloré Logistics Hub Huangpu", "Speedaf Express Hub")
        AlertDialog(
            onDismissRequest = { showChangeForwarderDialog = false },
            title = {
                Text("Choisir un transitaire", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    forwarders.forEach { fwd ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedForwarder = fwd
                                    showChangeForwarderDialog = false
                                    Toast.makeText(context, "Transitaire sélectionné : $fwd", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (fwd == selectedForwarder) Color(0xFFEADDFF).copy(alpha = 0.5f) else Color(0xFFF2F3FF)
                        ) {
                            Text(
                                text = fwd,
                                fontFamily = PlusJakartaSans,
                                fontWeight = if (fwd == selectedForwarder) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (fwd == selectedForwarder) Color(0xFF712AE2) else Color(0xFF131B2E),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showChangeForwarderDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}
