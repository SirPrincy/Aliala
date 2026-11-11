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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.LotCostCalculator

@Composable
fun LogisticStep6View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    onCompleteWizard: (updatedOrder: BusinessOrderEntity) -> Unit,
    onPreviousStep: () -> Unit,
    onRefundYuans: (amount: Double, rateAr: Double, note: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current

    // Refund input state (in Yuan)
    var refundYuanInput by rememberSaveable { mutableStateOf(if (order.refundYuan > 0.0) String.format(java.util.Locale.US, "%.1f", order.refundYuan) else "0") }
    val refundYuan = refundYuanInput.replace(',', '.').toDoubleOrNull() ?: 0.0

    // Customs & Import Tax input state (in Ariary)
    var customsTaxInput by rememberSaveable { mutableStateOf(if (order.customsTaxAr > 0) order.customsTaxAr.toString() else "0") }
    val customsTaxAr = customsTaxInput.toLongOrNull() ?: 0L

    val breakdown = remember(order, cmupRate, refundYuan, customsTaxAr) {
        LotCostCalculator.calculate(order, cmupRate, customRefundYuan = refundYuan, customsTaxArInput = customsTaxAr)
    }
    val totalQty = breakdown.quantity
    val effectiveCmupRate = breakdown.effectiveCmupRate
    val merchandiseTotalAr = breakdown.merchandiseTotalAr
    val domesticPortAr = breakdown.domesticPortAr
    val freightCostAr = breakdown.freightCostAr
    val refundAr = breakdown.refundAr

    val unitsReceived = if (order.receivedUnitsCount > 0) order.receivedUnitsCount else totalQty

    val parsedVariants = remember(order.variants) {
        val list = order.variants.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (list.isNotEmpty()) list else listOf("Noir Pro", "Blanc Pur", "Rouge Sport")
    }
    val v1Name = parsedVariants.getOrElse(0) { "Variante A" }
    val v2Name = parsedVariants.getOrElse(1) { "Variante B" }
    val v3Name = parsedVariants.getOrElse(2) { "Variante C" }

    // Proportions initiales dynamiques basées sur unitsReceived
    val v1Ordered = (unitsReceived * 0.5).toInt().coerceAtLeast(1)
    val v2Ordered = (unitsReceived * 0.25).toInt().coerceAtLeast(1)
    val v3Ordered = (unitsReceived - v1Ordered - v2Ordered).coerceAtLeast(0)

    // Variant 1: v1Name (v1Ordered units)
    var noirOk by rememberSaveable(v1Ordered) { mutableIntStateOf(v1Ordered) }
    var noirDefautMineur by rememberSaveable { mutableIntStateOf(0) }
    var noirNonVendable by rememberSaveable { mutableIntStateOf(0) }
    var noirPerdu by rememberSaveable { mutableIntStateOf(0) }

    // Variant 2: Blanc Pur (v2Ordered units)
    var blancOk by rememberSaveable(v2Ordered) { mutableIntStateOf(v2Ordered) }
    var blancDefautMineur by rememberSaveable { mutableIntStateOf(0) }
    var blancNonVendable by rememberSaveable { mutableIntStateOf(0) }
    var blancPerdu by rememberSaveable { mutableIntStateOf(0) }

    // Variant 3: Rouge Sport (v3Ordered units)
    var rougeOk by rememberSaveable(v3Ordered) { mutableIntStateOf(v3Ordered) }
    var rougeDefautVendable by rememberSaveable { mutableIntStateOf(0) }
    var rougeNonVendable by rememberSaveable { mutableIntStateOf(0) }
    var rougePerdu by rememberSaveable { mutableIntStateOf(0) }

    // Variant sums
    val v1Sum = noirOk + noirDefautMineur + noirNonVendable + noirPerdu
    val v2Sum = blancOk + blancDefautMineur + blancNonVendable + blancPerdu
    val v3Sum = rougeOk + rougeDefautVendable + rougeNonVendable + rougePerdu

    // Calculated totals
    val totalConforme = noirOk + blancOk + rougeOk
    val totalDefautVendable = noirDefautMineur + blancDefautMineur + rougeDefautVendable
    val totalVendable = totalConforme + totalDefautVendable
    val totalNonVendable = noirNonVendable + blancNonVendable + rougeNonVendable
    val totalPerdu = noirPerdu + blancPerdu + rougePerdu
    val totalDistributed = totalVendable + totalNonVendable + totalPerdu

    val isDistributedValid = (v1Sum == v1Ordered) && (v2Sum == v2Ordered) && (v3Sum == v3Ordered) && (totalDistributed == unitsReceived)

    val netCostAr = breakdown.netCostAr
    val unitFifoCostAr = if (totalVendable > 0) netCostAr / totalVendable else 0L

    var isInvoicePaid by rememberSaveable { mutableStateOf(order.isFreightPaid) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. STEPPER PROGRESSION HORIZONTAL
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
                // Steps 1 to 5 done (green/cyan checks) + Step 6 active (violet)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step 1 Done
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(16.dp))
                    }

                    Box(modifier = Modifier.weight(1f).height(4.dp).padding(horizontal = 2.dp).clip(RoundedCornerShape(9999.dp)).background(Color(0xFF06B6D4).copy(alpha = 0.30f)))

                    // Step 2 Done
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(16.dp))
                    }

                    Box(modifier = Modifier.weight(1f).height(4.dp).padding(horizontal = 2.dp).clip(RoundedCornerShape(9999.dp)).background(Color(0xFF06B6D4).copy(alpha = 0.30f)))

                    // Step 3 Done
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(16.dp))
                    }

                    Box(modifier = Modifier.weight(1f).height(4.dp).padding(horizontal = 2.dp).clip(RoundedCornerShape(9999.dp)).background(Color(0xFF06B6D4).copy(alpha = 0.30f)))

                    // Step 4 Done
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(16.dp))
                    }

                    Box(modifier = Modifier.weight(1f).height(4.dp).padding(horizontal = 2.dp).clip(RoundedCornerShape(9999.dp)).background(Color(0xFF06B6D4).copy(alpha = 0.30f)))

                    // Step 5 Done
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(16.dp))
                    }

                    Box(modifier = Modifier.weight(1f).height(4.dp).padding(horizontal = 2.dp).clip(RoundedCornerShape(9999.dp)).background(Color(0xFF712AE2)))

                    // Step 6 Active (Violet)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF712AE2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("6", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }

                // Header title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0xFFEADDFF)
                        ) {
                            Text(
                                text = "Étape 6 sur 6",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFF712AE2),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "Contrôle Qualité & Répartition",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    Text(
                        text = "$unitsReceived / $totalQty reçues",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF00687A)
                    )
                }
            }
        }

        // ==========================================
        // 2. INSPECTION OVERVIEW SUMMARY PILL
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF2F3FF)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEAEDFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF00687A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "${order.lotNumber.ifBlank { "Lot #${order.id}" }} · ${order.product.ifBlank { "Article #${order.id}" }}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF131B2E)
                        )
                        Text(
                            text = "Fournisseur ${order.supplier.ifBlank { "Chine" }}",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$totalQty",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color(0xFF00687A)
                    )
                    Text(
                        text = "commandées",
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = Color(0xFF6D797D)
                    )
                }
            }
        }

        // ==========================================
        // 3. DÉCLINAISONS & ÉTATS (4 STATUTS NORMALISÉS)
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Déclinaisons & États",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF131B2E)
            )
            Text(
                text = "4 statuts normalisés",
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = Color(0xFF3D494C)
            )
        }

        // Carte 1: Noir Titane (10 unités)
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
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF283044)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFAF8FF)))
                        }
                        Column {
                            Text(v1Name, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF131B2E))
                            Text("$v1Ordered unités commandées", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (v1Sum == v1Ordered) Color(0xFFACEDFF) else Color(0xFFFFDAD6)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(if (v1Sum == v1Ordered) Icons.Default.CheckCircle else Icons.Default.ReportProblem, contentDescription = null, tint = if (v1Sum == v1Ordered) Color(0xFF001F26) else Color(0xFFBA1A1A), modifier = Modifier.size(13.dp))
                            Text("$v1Sum/$v1Ordered réparties", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (v1Sum == v1Ordered) Color(0xFF001F26) else Color(0xFFBA1A1A))
                        }
                    }
                }

                // Grid 2x2: Conforme, Défaut mineur, Non vendable, Manquant/Perdu
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Conforme (OK)
                    StatusCounterBox(
                        title = "Conforme (OK)",
                        count = noirOk,
                        titleColor = Color(0xFF00687A),
                        onDecrement = { if (noirOk > 0) noirOk-- },
                        onIncrement = { if (v1Sum < v1Ordered) noirOk++ },
                        modifier = Modifier.weight(1f)
                    )
                    // Défaut mineur
                    StatusCounterBox(
                        title = "Défaut mineur",
                        count = noirDefautMineur,
                        titleColor = Color(0xFF3D494C),
                        onDecrement = { if (noirDefautMineur > 0) noirDefautMineur-- },
                        onIncrement = { if (v1Sum < v1Ordered) noirDefautMineur++ },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Non vendable
                    StatusCounterBox(
                        title = "Non vendable",
                        count = noirNonVendable,
                        titleColor = Color(0xFFBA1A1A),
                        onDecrement = { if (noirNonVendable > 0) noirNonVendable-- },
                        onIncrement = { if (v1Sum < v1Ordered) noirNonVendable++ },
                        modifier = Modifier.weight(1f)
                    )
                    // Manquant/Perdu
                    StatusCounterBox(
                        title = "Manquant/Perdu",
                        count = noirPerdu,
                        titleColor = Color(0xFF6D797D),
                        onDecrement = { if (noirPerdu > 0) noirPerdu-- },
                        onIncrement = { if (v1Sum < v1Ordered) noirPerdu++ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Carte 2: v2Name (v2Ordered unités)
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
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDAE2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.White))
                        }
                        Column {
                            Text(v2Name, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF131B2E))
                            Text("$v2Ordered unités commandées", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (v2Sum == v2Ordered) Color(0xFFACEDFF) else Color(0xFFFFDAD6)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(if (v2Sum == v2Ordered) Icons.Default.CheckCircle else Icons.Default.ReportProblem, contentDescription = null, tint = if (v2Sum == v2Ordered) Color(0xFF001F26) else Color(0xFFBA1A1A), modifier = Modifier.size(13.dp))
                            Text("$v2Sum/$v2Ordered réparties", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (v2Sum == v2Ordered) Color(0xFF001F26) else Color(0xFFBA1A1A))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusCounterBox(
                        title = "Conforme (OK)",
                        count = blancOk,
                        titleColor = Color(0xFF00687A),
                        onDecrement = { if (blancOk > 0) blancOk-- },
                        onIncrement = { if (v2Sum < v2Ordered) blancOk++ },
                        modifier = Modifier.weight(1f)
                    )
                    StatusCounterBox(
                        title = "Défaut mineur",
                        count = blancDefautMineur,
                        titleColor = Color(0xFF3D494C),
                        onDecrement = { if (blancDefautMineur > 0) blancDefautMineur-- },
                        onIncrement = { if (v2Sum < v2Ordered) blancDefautMineur++ },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusCounterBox(
                        title = "Non vendable",
                        count = blancNonVendable,
                        titleColor = Color(0xFFBA1A1A),
                        onDecrement = { if (blancNonVendable > 0) blancNonVendable-- },
                        onIncrement = { if (v2Sum < v2Ordered) blancNonVendable++ },
                        modifier = Modifier.weight(1f)
                    )
                    StatusCounterBox(
                        title = "Manquant/Perdu",
                        count = blancPerdu,
                        titleColor = Color(0xFF6D797D),
                        onDecrement = { if (blancPerdu > 0) blancPerdu-- },
                        onIncrement = { if (v2Sum < v2Ordered) blancPerdu++ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Carte 3: v3Name (v3Ordered unités)
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
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFDAD6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFBA1A1A)))
                        }
                        Column {
                            Text(v3Name, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF131B2E))
                            Text("$v3Ordered unités commandées", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (v3Sum == v3Ordered) Color(0xFFACEDFF) else Color(0xFFFFDAD6)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(if (v3Sum == v3Ordered) Icons.Default.CheckCircle else Icons.Default.ReportProblem, contentDescription = null, tint = if (v3Sum == v3Ordered) Color(0xFF001F26) else Color(0xFFBA1A1A), modifier = Modifier.size(13.dp))
                            Text("$v3Sum/$v3Ordered réparties", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (v3Sum == v3Ordered) Color(0xFF001F26) else Color(0xFFBA1A1A))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Conforme
                    StatusCounterBox(
                        title = "Conforme (OK)",
                        count = rougeOk,
                        titleColor = Color(0xFF00687A),
                        onDecrement = { if (rougeOk > 0) rougeOk-- },
                        onIncrement = { if (v3Sum < v3Ordered) rougeOk++ },
                        modifier = Modifier.weight(1f)
                    )
                    // Défaut vendable
                    StatusCounterBox(
                        title = "Défaut vendable",
                        count = rougeDefautVendable,
                        titleColor = Color(0xFF712AE2),
                        containerColor = Color(0xFFEADDFF).copy(alpha = 0.35f),
                        onDecrement = { if (rougeDefautVendable > 0) rougeDefautVendable-- },
                        onIncrement = { if (v3Sum < v3Ordered) rougeDefautVendable++ },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Non vendable
                    StatusCounterBox(
                        title = "Non vendable",
                        count = rougeNonVendable,
                        titleColor = Color(0xFFBA1A1A),
                        onDecrement = { if (rougeNonVendable > 0) rougeNonVendable-- },
                        onIncrement = { if (v3Sum < v3Ordered) rougeNonVendable++ },
                        modifier = Modifier.weight(1f)
                    )
                    // Perdu / Volé
                    StatusCounterBox(
                        title = "Perdu / Volé",
                        count = rougePerdu,
                        titleColor = Color(0xFFBA1A1A),
                        containerColor = Color(0xFFFFDAD6).copy(alpha = 0.50f),
                        onDecrement = { if (rougePerdu > 0) rougePerdu-- },
                        onIncrement = { if (v3Sum < v3Ordered) rougePerdu++ },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Detail pill alert
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
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF6D797D), modifier = Modifier.size(15.dp))
                            Text(
                                text = "Bilan : $totalConforme OK · $totalDefautVendable défectueux vendable · $totalPerdu perdu",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                        Text(
                            text = "Total $totalDistributed",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF00687A)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 4. BLOC REMBOURSEMENT FOURNISSEUR USINE (LITIGE CHINE)
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEADDFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = null,
                            tint = Color(0xFF25005A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Remboursement Usine Chine",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF131B2E)
                        )
                        Text(
                            text = "Litige Taobao / 1688 ouvert",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }

                // Champ Yuans avec calcul automatique
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF2F3FF)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "AVOIR OU REMBOURSEMENT NÉGOCIÉ",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = Color(0xFF3D494C),
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "¥ ",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color(0xFF712AE2)
                                )
                                OutlinedTextField(
                                    value = refundYuanInput,
                                    onValueChange = { refundYuanInput = it },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(90.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+ ${CurrencyFormatter.formatAriary(refundAr)}",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF00687A)
                                )
                                Text(
                                    text = "Taux CMUP $effectiveCmupRate Ar/¥",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = Color(0xFF3D494C)
                                )
                            }
                        }
                    }
                }

                // Explication pédagogique
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE2E7FF)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF712AE2),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Les yuans sont immédiatement recrédités dans ton solde de yuans Alipay pour amortir le coût d'achat global.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // ==========================================
        // 5. CARTE RÉSULTAT EN DIRECT: COÛT UNITAIRE FIFO CERTIFIÉ
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
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = Color(0xFF00687A),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Coût Unitaire FIFO Certifié",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFFACEDFF)
                    ) {
                        Text(
                            text = "$totalVendable vendables",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF001F26),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Callout Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF2F3FF)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "COÛT DE REVIENT FINAL NET",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = Color(0xFF3D494C),
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = CurrencyFormatter.formatAriary(unitFifoCostAr).replace(" Ar", ""),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp,
                                color = Color(0xFF00687A)
                            )
                            Text(
                                text = "Ar / unité",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color(0xFF131B2E)
                            )
                        }
                        Text(
                            text = "$totalConforme conformes + $totalDefautVendable vendable à perte",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF00687A),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Formule mathématique transparente
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEAEDFF)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "FORMULE MATHÉMATIQUE TRANSPARENTE",
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.sp,
                            color = Color(0xFF6D797D),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "(${CurrencyFormatter.formatAriary(merchandiseTotalAr)} [Marchandise] + ${CurrencyFormatter.formatAriary(domesticPortAr)} [Port CH] + ${CurrencyFormatter.formatAriary(freightCostAr)} [Fret] − ${CurrencyFormatter.formatAriary(refundAr)} [Remb.]) ÷ $totalVendable unités",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF6D797D),
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Text(
                        text = "Les unités non vendables ou perdues ne sont pas comptées dans le stock disponible et augmentent le coût unitaire des pièces saines.",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = Color(0xFF3D494C),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // ==========================================
        // 6. BLOC RÈGLEMENTS TRANSITAIRE SINOEXPRESS
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE2E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = Color(0xFF712AE2),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Transitaire SinoExpress",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = "Facture Fret Aérien #${order.referenceCode.ifBlank { "SINO-${order.id}" }}",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (isInvoicePaid) Color(0xFFE8F5E9) else Color(0xFFFFDAD6)
                    ) {
                        Text(
                            text = if (isInvoicePaid) "RÉGLÉ" else "À PAYER",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (isInvoicePaid) Color(0xFF2E7D32) else Color(0xFF93000A),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Montant TTC
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF2F3FF)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Montant TTC de la facture",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color(0xFF131B2E)
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(freightCostAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF131B2E)
                        )
                    }
                }

                // 2 Actions: Enregistrer facture / Payer (MVola)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable {
                                Toast.makeText(context, "Facture SinoExpress enregistrée dans vos documents.", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEAEDFF)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color(0xFF131B2E), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Enregistrer facture",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF131B2E)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable { showPaymentDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isInvoicePaid) Color(0xFF2E7D32) else Color(0xFF712AE2)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isInvoicePaid) "Payé ✓" else "Payer (MVola)",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 7. STICKY BOTTOM VALIDATION ACTION
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isDistributedValid) Icons.Default.TaskAlt else Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = if (isDistributedValid) Color(0xFF00687A) else Color(0xFFBA1A1A),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isDistributedValid) "Tous les compteurs sont validés ($totalDistributed/$unitsReceived)" else "Répartition incomplète ($totalDistributed/$unitsReceived réparties)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isDistributedValid) Color(0xFF00687A) else Color(0xFFBA1A1A)
                    )
                }

                Button(
                    onClick = {
                        if (!isDistributedValid) return@Button
                        val inspectionSummary = "$totalConforme conformes, $totalDefautVendable défauts mineurs, $totalNonVendable non vendables, $totalPerdu perdus"
                        val updated = order.copy(
                            status = OrderStatus.IN_STOCK.code,
                            stockCount = totalVendable,
                            defectiveCount = totalNonVendable,
                            lostCount = totalPerdu,
                            customsTaxAr = customsTaxAr,
                            refundYuan = refundYuan,
                            unitCostAr = unitFifoCostAr,
                            inspectionResults = inspectionSummary,
                            variants = "$v1Name ($noirOk ok), $v2Name ($blancOk ok), $v3Name ($rougeOk ok)",
                            freightCostAr = freightCostAr,
                            isFreightPaid = isInvoicePaid,
                            soldDate = System.currentTimeMillis()
                        )
                        if (refundYuan > 0.0) {
                            onRefundYuans(refundYuan, effectiveCmupRate, "Remboursement usine 1688 : ${order.product} (Lot #${order.id})")
                        }
                        onCompleteWizard(updated)
                        Toast.makeText(context, "$totalVendable unités injectées dans le stock avec succès !", Toast.LENGTH_SHORT).show()
                    },
                    enabled = isDistributedValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_validate_stock_injection"),
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
                            text = "Valider et ajouter les $totalVendable unités au stock",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal: Confirmation de paiement MVola
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF712AE2))
                    Text("Paiement Fret SinoExpress", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Règlement sécurisé de ${CurrencyFormatter.formatAriary(freightCostAr)} vers le compte marchand SinoExpress Cargo Mada.",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color(0xFF131B2E)
                    )
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF2F3FF), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Référence : SINO-9941 · Mode : Mobile Money (MVola/Orange Money)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isInvoicePaid = true
                        showPaymentDialog = false
                        Toast.makeText(context, "Facture de ${CurrencyFormatter.formatAriary(freightCostAr)} réglée avec succès !", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF712AE2))
                ) {
                    Text("Confirmer le paiement", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun StatusCounterBox(
    title: String,
    count: Int,
    titleColor: Color,
    containerColor: Color = Color(0xFFF2F3FF),
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = titleColor
                )
                Text(
                    text = "$count",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF131B2E)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Surface(
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onDecrement() },
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    shadowElevation = 0.5.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, contentDescription = "Diminuer", tint = Color(0xFF131B2E), modifier = Modifier.size(14.dp))
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onIncrement() },
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    shadowElevation = 0.5.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Augmenter", tint = Color(0xFF131B2E), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
