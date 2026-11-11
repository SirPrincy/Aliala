package com.example.ui.business.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

import com.example.data.model.OrderStatus
import com.example.util.Calculations
import com.example.util.LotCostCalculator

data class DomesticParcel(
    val id: String,
    val carrierName: String,
    val carrierChinese: String,
    val trackingNumber: String,
    val statusPillText: String,
    val isPurplePill: Boolean = false,
    val isDelivered: Boolean = false,
    val checkpointTitle: String,
    val checkpointTime: String,
    val checkpointRoute: String? = null,
    val isHubCheckpoint: Boolean = false,
    val contentDescription: String
)

@Composable
fun LogisticStep2View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    onAdvanceToWeighing: (updatedOrder: BusinessOrderEntity) -> Unit,
    onPreviousStep: () -> Unit
) {
    val context = LocalContext.current

    val breakdown = remember(order, cmupRate) {
        LotCostCalculator.calculate(order, cmupRate)
    }
    val totalQty = breakdown.quantity
    val effectiveCmupRate = breakdown.effectiveCmupRate
    val unitPriceYuan = breakdown.unitPriceYuan
    val totalOrderAr = breakdown.merchandiseTotalAr + breakdown.domesticPortAr

    // Pulse animation for the active step capsule
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_step2")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Dynamic list of parcels initialized from persisted tracking numbers
    val initialParcels = remember(order.trackingNumbers, order.deliveredTrackingNumbers, totalQty) {
        val deliveredSet = order.deliveredTrackingNumbers.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val trackList = if (order.trackingNumbers.isNotBlank()) {
            order.trackingNumbers.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            val prefix = if (order.platform.isNotBlank()) order.platform.uppercase() else "SF"
            val ref = order.referenceCode.ifBlank { "${order.id}" }
            listOf("$prefix#TRK-$ref-1", "$prefix#TRK-$ref-2")
        }
        trackList.mapIndexed { idx, trk ->
            val isDelivered = deliveredSet.contains(trk)
            DomesticParcel(
                id = "p_$idx",
                carrierName = "SF Express",
                carrierChinese = "(顺丰速运)",
                trackingNumber = trk,
                statusPillText = if (isDelivered) "Reçu au hub" else "En acheminement",
                isPurplePill = idx % 2 == 1,
                isDelivered = isDelivered,
                checkpointTitle = if (isDelivered) "Arrivé au hub de transit" else "En cours d'acheminement",
                checkpointTime = "Aujourd'hui",
                checkpointRoute = null,
                isHubCheckpoint = isDelivered,
                contentDescription = "Colis ${idx + 1}"
            )
        }
    }

    val parcels = remember {
        mutableStateListOf<DomesticParcel>().apply { addAll(initialParcels) }
    }

    var showAddParcelDialog by remember { mutableStateOf(false) }
    var activeTrackingDialogParcel by remember { mutableStateOf<DomesticParcel?>(null) }

    fun copyToClipboard(label: String, text: String, toastMsg: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. STEPPER BREADCRUMB / WIZARD PROGRESS
        // ==========================================
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ÉTAPE 2 SUR 6",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF00687A),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Vers l'entrepôt transitaire",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF3D494C)
                )
            }

            // 6-Step Visual Indicator capsules
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Step 1: filled cyan
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFF06B6D4))
                )
                // Step 2: filled cyan with pulse
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFF06B6D4).copy(alpha = pulseAlpha))
                )
                // Steps 3 to 6: unfilled gray
                for (i in 3..6) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFFE2E7FF))
                    )
                }
            }

            // Active Step Status Pill
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = Color(0xFF06B6D4).copy(alpha = 0.15f),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF06B6D4).copy(alpha = pulseAlpha))
                    )
                    Text(
                        text = "En livraison vers l'entrepôt transitaire (Guangzhou)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color(0xFF00687A)
                    )
                }
            }
        }

        // ==========================================
        // 2. CARTE RÉCAPITULATIVE DU LOT
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
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF00687A).copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = order.product.ifEmpty { "Article #${order.id}" },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = "$totalQty unités commandées",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "¥ ${unitPriceYuan.toInt()} / u",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF00687A)
                        )
                        Text(
                            text = "${CurrencyFormatter.formatAriary(totalOrderAr)} total",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }

                // Sub-card: Destination & ETA
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF2F3FF)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(18.dp)
                            )
                            Row {
                                Text(
                                    text = "Destination : ",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF3D494C)
                                )
                                Text(
                                    text = "Entrepôt SinoExpress Guangzhou",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.size(18.dp)
                            )
                            Row {
                                Text(
                                    text = "Arrivée prévue : ",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF3D494C)
                                )
                                Text(
                                    text = com.example.util.DateUtils.formatShortDate(order.estimatedArrival),
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF00687A)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. SUIVI MULTI-COLIS (DOMESTIC LOGISTICS)
        // ==========================================
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
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = Color(0xFF00687A),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Colis en transit intérieur",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF131B2E)
                )
            }

            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = Color(0xFF00687A).copy(alpha = 0.10f)
            ) {
                Text(
                    text = "${parcels.size} colis expédiés",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF00687A),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }

        // List of parcels
        parcels.forEachIndexed { index, parcel ->
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
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = parcel.carrierName,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF131B2E)
                                )
                                Text(
                                    text = parcel.carrierChinese,
                                    fontFamily = Inter,
                                    fontSize = 13.sp,
                                    color = Color(0xFF3D494C)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = parcel.trackingNumber,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = Color(0xFF3D494C)
                                )
                                IconButton(
                                    onClick = {
                                        copyToClipboard(
                                            label = "Tracking Number",
                                            text = parcel.trackingNumber,
                                            toastMsg = "Numéro ${parcel.trackingNumber} copié !"
                                        )
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copier le numéro",
                                        tint = Color(0xFF00687A),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        // Status pill badge
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = if (parcel.isDelivered) {
                                Color(0xFFE8F5E9)
                            } else if (parcel.isPurplePill) {
                                Color(0xFF8A4CFC).copy(alpha = 0.15f)
                            } else {
                                Color(0xFF06B6D4).copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = if (parcel.isDelivered) "Réceptionné" else parcel.statusPillText,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (parcel.isDelivered) {
                                    Color(0xFF2E7D32)
                                } else if (parcel.isPurplePill) {
                                    Color(0xFF712AE2)
                                } else {
                                    Color(0xFF00687A)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Timeline Tracking Step inside parcel
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2F3FF).copy(alpha = 0.6f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (parcel.isHubCheckpoint) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hub,
                                        contentDescription = null,
                                        tint = Color(0xFF712AE2),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = parcel.checkpointTitle,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Vertical dot - line - dot
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF06B6D4))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(28.dp)
                                                .background(Color(0xFFDAE2FD))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFDAE2FD))
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = parcel.checkpointTitle,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                        Text(
                                            text = parcel.checkpointTime,
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                        parcel.checkpointRoute?.let { route ->
                                            Text(
                                                text = route,
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = Color(0xFF3D494C),
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Content item line
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = Color(0xFF3D494C),
                                    modifier = Modifier.size(16.dp)
                                )
                                Row {
                                    Text(
                                        text = "Contenu : ",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                    Text(
                                        text = parcel.contentDescription,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }
                            }
                        }
                    }

                    // Quick Action Buttons (Grid 2 columns)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable {
                                    activeTrackingDialogParcel = parcel
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE2E7FF).copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Tracer le colis",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF131B2E)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = Color(0xFF131B2E),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable {
                                    val updated = parcel.copy(isDelivered = !parcel.isDelivered)
                                    parcels[index] = updated
                                    val statusMsg = if (updated.isDelivered) {
                                        "Colis ${parcel.trackingNumber} marqué comme réceptionné au hub !"
                                    } else {
                                        "Colis ${parcel.trackingNumber} remis en cours d'acheminement"
                                    }
                                    Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (parcel.isDelivered) Color(0xFFE8F5E9) else Color(0xFF00687A).copy(alpha = 0.10f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    tint = if (parcel.isDelivered) Color(0xFF2E7D32) else Color(0xFF00687A),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (parcel.isDelivered) "Reçu à l'entrepôt" else "Réceptionné",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (parcel.isDelivered) Color(0xFF2E7D32) else Color(0xFF00687A)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Parcel Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { showAddParcelDialog = true },
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = Color(0xFF00687A),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ajouter un colis / numéro de suivi",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF00687A)
                )
            }
        }

        // ==========================================
        // 4. POINT DE CHUTE CHINE (FORWARDER ADDRESS)
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
                                .background(Color(0xFF712AE2).copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF712AE2),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Point de chute Chine",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFF712AE2).copy(alpha = 0.10f)
                    ) {
                        Text(
                            text = "Transitaire",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF712AE2),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                    }
                }

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
                            text = "Transitaire : ${order.carrier.ifBlank { "À sélectionner à l'étape 3" }}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF131B2E)
                        )
                        Text(
                            text = "Renseignez dans votre commande fournisseur l'adresse de livraison et votre identifiant client fournis par votre transitaire.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF6D797D)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 5. NOTICE / ALERTE DE LIVRAISON
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFE2E7FF).copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF06B6D4),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Dès que tous les colis sont notés comme livrés par le transporteur chinois, le transitaire effectuera la pesée volumétrique et émettra la fiche d'arrivée officielle.",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = Color(0xFF3D494C),
                    lineHeight = 18.sp
                )
            }
        }

        // ==========================================
        // 6. STICKY BOTTOM ACTION STRIP
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
                val deliveredParcelsCount = parcels.count { it.isDelivered }
                val hasDeliveredParcels = deliveredParcelsCount > 0

                Button(
                    onClick = {
                        val nextStatus = OrderStatus.GUANGZHOU.code
                        val deliveredString = parcels.filter { it.isDelivered }.joinToString(",") { it.trackingNumber }
                        val allParcelsString = parcels.joinToString(",") { it.trackingNumber }
                        onAdvanceToWeighing(order.copy(
                            status = nextStatus,
                            deliveredTrackingNumbers = deliveredString,
                            trackingNumbers = allParcelsString
                        ))
                        Toast.makeText(context, "$deliveredParcelsCount colis réceptionnés ! Passage à la pesée.", Toast.LENGTH_SHORT).show()
                    },
                    enabled = hasDeliveredParcels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_all_parcels_arrived"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00687A),
                        disabledContainerColor = Color(0xFFBCC9CD)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (hasDeliveredParcels) "Passer à la pesée ($deliveredParcelsCount/${parcels.size} reçus)" else "Réceptionnez au moins 1 colis pour continuer",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
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

                Text(
                    text = "Modifier les informations de commande",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF3D494C),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { onPreviousStep() }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }

    // Modal Dialog: Add Parcel
    if (showAddParcelDialog) {
        var carrierInput by remember { mutableStateOf("SF Express") }
        var trackingInput by remember { mutableStateOf("") }
        var contentInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddParcelDialog = false },
            title = {
                Text("Ajouter un colis", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = carrierInput,
                        onValueChange = { carrierInput = it },
                        label = { Text("Transporteur intérieur") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = trackingInput,
                        onValueChange = { trackingInput = it },
                        label = { Text("Numéro de tracking (ex: SF...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("Contenu (ex: 5 pièces Rouge)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = trackingInput.trim().ifEmpty { "SF" + System.currentTimeMillis().toString().takeLast(9) }
                        val desc = contentInput.trim().ifEmpty { "Lot de marchandises" }
                        parcels.add(
                            DomesticParcel(
                                id = "p_${System.currentTimeMillis()}",
                                carrierName = carrierInput.trim().ifEmpty { "SF Express" },
                                carrierChinese = "(顺丰速运)",
                                trackingNumber = num,
                                statusPillText = "En acheminement",
                                isPurplePill = false,
                                isDelivered = false,
                                checkpointTitle = "Pris en charge transporteur",
                                checkpointTime = "Aujourd'hui",
                                checkpointRoute = "Expédié depuis l'usine",
                                isHubCheckpoint = false,
                                contentDescription = desc
                            )
                        )
                        showAddParcelDialog = false
                        Toast.makeText(context, "Colis $num ajouté !", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00687A))
                ) {
                    Text("Ajouter", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddParcelDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Modal Dialog: Live Tracking Details
    activeTrackingDialogParcel?.let { parcel ->
        AlertDialog(
            onDismissRequest = { activeTrackingDialogParcel = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF00687A))
                    Text("Suivi ${parcel.carrierName}", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Numéro de suivi : ${parcel.trackingNumber}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF00687A))
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF2F3FF), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Dernier statut : ${parcel.checkpointTitle}", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                            Text("Horodatage : ${parcel.checkpointTime}", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                            parcel.checkpointRoute?.let {
                                Text("Acheminement : $it", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                            }
                        }
                    }
                    Text("Contenu associé : ${parcel.contentDescription}", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeTrackingDialogParcel = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00687A))
                ) {
                    Text("Fermer", color = Color.White)
                }
            }
        )
    }
}
