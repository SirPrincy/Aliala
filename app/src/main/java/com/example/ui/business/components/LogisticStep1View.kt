package com.example.ui.business.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

import com.example.data.model.OrderStatus
import com.example.util.Calculations
import com.example.util.LotCostCalculator

@Composable
fun LogisticStep1View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    availableYuans: Double = 1840.0,
    onConfirmOrder: (updatedOrder: BusinessOrderEntity) -> Unit,
    onSaveAsDraft: () -> Unit,
    onSpendYuans: (amountYuan: Double, rateAr: Double, note: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    var isDraftCheckboxChecked by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val isAlreadyDebited = order.yuanDebited

    val breakdown = remember(order, cmupRate) {
        LotCostCalculator.calculate(order, cmupRate)
    }
    val isOrderValid = breakdown.isOrderDataComplete
    val effectiveCmupRate = breakdown.effectiveCmupRate
    val unitPriceYuan = breakdown.unitPriceYuan
    val totalQty = breakdown.quantity
    val goodsTotalYuan = if (isOrderValid) totalQty * unitPriceYuan else 0.0
    val shippingNationalYuan = Calculations.DOMESTIC_PORT_YUAN
    val totalOrderYuan = if (isOrderValid) goodsTotalYuan + shippingNationalYuan else 0.0
    val totalOrderAr = if (isOrderValid) (totalOrderYuan * effectiveCmupRate).toLong() else 0L

    val availableWalletYuans = availableYuans
    val remainingWalletYuans = availableWalletYuans - totalOrderYuan
    val unitPriceAr = if (isOrderValid) (unitPriceYuan * effectiveCmupRate).toLong() else 0L
    val shippingNationalAr = breakdown.domesticPortAr

    var showAddTrackingDialog by remember { mutableStateOf(false) }
    var newTrackingInput by remember { mutableStateOf("") }
    var newTrackingLabel by remember { mutableStateOf("") }

    val initialTrackings = remember(order.trackingNumbers, totalQty) {
        if (order.trackingNumbers.isNotBlank()) {
            order.trackingNumbers.split(",").map { it.trim() }.filter { it.isNotEmpty() }.mapIndexed { idx, tr ->
                tr to "Colis ${idx + 1}"
            }
        } else {
            val prefix = if (order.platform.isNotBlank()) order.platform.uppercase() else "SF"
            val ref = order.referenceCode.ifBlank { "${order.id}" }
            val q1 = if (totalQty > 0) (totalQty * 0.75).toInt().coerceAtLeast(1) else 0
            val q2 = if (totalQty > 0) (totalQty - q1).coerceAtLeast(1) else 0
            listOf(
                "$prefix #TRK-$ref-1" to "Colis 1 · ${if (q1 > 0) "$q1 unités" else "—"}",
                "$prefix #TRK-$ref-2" to "Colis 2 · ${if (q2 > 0) "$q2 unités" else "—"}"
            )
        }
    }
    val trackingList = remember {
        mutableStateListOf<Pair<String, String>>().apply { addAll(initialTrackings) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (!isOrderValid) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFDAD6),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFBA1A1A),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Commande incomplète : prix, quantité ou taux manquant",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFBA1A1A)
                        )
                        Text(
                            text = "Renseignez le prix usine (¥) et la quantité achetée dans la commande avant de valider l'étape 1.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = Color(0xFF410002)
                        )
                    }
                }
            }
        }
        // ==========================================
        // 1. STEPPER PROGRESSION BAR CARD (Étape 1 sur 6)
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
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00687A).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF00687A)
                            )
                        }
                        Text(
                            text = "ÉTAPE 1 SUR 6",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C),
                            letterSpacing = 0.8.sp
                        )
                    }

                    Text(
                        text = "Validation & Débit",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color(0xFF00687A)
                    )
                }

                // Progress Bar (16.6%)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFFE2E7FF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.166f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFF06B6D4))
                    )
                }

                // Status Banner: Commandé chez le fournisseur
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF005AC2).copy(alpha = 0.10f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                tint = Color(0xFF005AC2),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Statut : Commandé chez le fournisseur",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF005AC2)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF005AC2))
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. RÉCAPITULATIF COMMANDE CHINE
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header card
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Récapitulatif Commande Chine",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFFACEDFF)
                    ) {
                        Text(
                            text = "${order.platform.ifBlank { "1688" }} Direct Usine",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF00424F),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Product details row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEAEDFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (order.family.contains("MODE", ignoreCase = true)) "🎒" else "🎧",
                            fontSize = 36.sp
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = order.product.ifBlank { "Article #${order.id}" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF131B2E),
                            maxLines = 1
                        )
                        Text(
                            text = "Réf: ${order.referenceCode.ifBlank { "RÉF-${order.id}" }}",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = Color(0xFF6D797D)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = Color(0xFF3D494C),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = order.supplier.ifBlank { "Fournisseur Chine" },
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF3D494C),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Quantités commandées (Pills)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF2F3FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quantités commandées",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = if (totalQty > 0) "$totalQty unités" else "—",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (totalQty > 0) Color(0xFF00687A) else Color(0xFFBA1A1A)
                            )
                        }

                        val parsedVariants = remember(order.variants, totalQty) {
                            if (order.variants.isNotBlank()) {
                                order.variants.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            } else {
                                listOf(
                                    "${(totalQty * 0.5).toInt().coerceAtLeast(1)} Noir Pro",
                                    "${(totalQty * 0.25).toInt().coerceAtLeast(1)} Blanc Pur",
                                    "${(totalQty - (totalQty * 0.5).toInt().coerceAtLeast(1) - (totalQty * 0.25).toInt().coerceAtLeast(1)).coerceAtLeast(1)} Rouge Sport"
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            parsedVariants.forEachIndexed { idx, variantText ->
                                val dotColor = when (idx % 3) {
                                    0 -> Color(0xFF131B2E)
                                    1 -> Color(0xFFBCC9CD)
                                    else -> Color(0xFFBA1A1A)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                                        Text(variantText, fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF131B2E))
                                    }
                                }
                            }
                        }
                    }
                }

                // Cost rows
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Prix unitaire usine", fontFamily = Inter, fontSize = 13.sp, color = Color(0xFF3D494C))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (unitPriceYuan > 0) "¥ ${String.format(java.util.Locale.US, "%.2f", unitPriceYuan)}" else "—",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = if (isOrderValid) "~ ${CurrencyFormatter.formatAriary(unitPriceAr)} (CMUP)" else "—",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color(0xFF6D797D)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Frais de port intérieurs (Chine)", fontFamily = Inter, fontSize = 13.sp, color = Color(0xFF3D494C))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("¥ ${String.format(java.util.Locale.US, "%.2f", shippingNationalYuan)}", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF131B2E))
                            Text(if (effectiveCmupRate > 0) CurrencyFormatter.formatAriary(shippingNationalAr) else "—", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF6D797D))
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E7FF)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("Total commande", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF131B2E))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isOrderValid) "¥ ${String.format(java.util.Locale.US, "%.2f", totalOrderYuan)}" else "—",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = Color(0xFF00687A)
                            )
                            Text(
                                text = if (isOrderValid) CurrencyFormatter.formatAriary(totalOrderAr) else "—",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. PORTEFEUILLE ALIPAY (¥)
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF00687A).copy(alpha = 0.12f)),
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
                                text = "Portefeuille Alipay (¥)",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = "Débit immédiat pour réservation",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color(0xFF6D797D)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEAEDFF)
                    ) {
                        Text(
                            text = "CMUP : $effectiveCmupRate Ar/¥",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Two Columns: Solde disponible / Montant à débiter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Solde disponible", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                            Text(
                                text = "¥ ${String.format(java.util.Locale.US, "%.2f", availableWalletYuans)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(CurrencyFormatter.formatAriary((availableWalletYuans * effectiveCmupRate).toLong()), fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Montant à débiter", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                            Text(
                                text = "- ¥ ${String.format(java.util.Locale.US, "%.2f", totalOrderYuan)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFFBA1A1A)
                            )
                            Text(CurrencyFormatter.formatAriary(totalOrderAr), fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                        }
                    }
                }

                if (isAlreadyDebited) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF00687A).copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Paiement Alipay déjà effectué",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF00687A)
                                )
                                Text(
                                    text = "Un montant de ¥ ${String.format(java.util.Locale.US, "%.2f", if (order.yuanDebitedAmount > 0.0) order.yuanDebitedAmount else totalOrderYuan)} a déjà été déduit de votre solde pour ce lot.",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF3D494C)
                                )
                            }
                        }
                    }
                } else {
                    // Solde de yuans suffisant / insuffisant pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (remainingWalletYuans >= 0) Color(0xFF00687A).copy(alpha = 0.10f) else Color(0xFFFFDAD6)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (remainingWalletYuans >= 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (remainingWalletYuans >= 0) Color(0xFF00687A) else Color(0xFFBA1A1A),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (remainingWalletYuans >= 0) "Solde de yuans suffisant" else "Solde de yuans insuffisant",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (remainingWalletYuans >= 0) Color(0xFF00687A) else Color(0xFFBA1A1A)
                                )
                                Text(
                                    text = if (remainingWalletYuans >= 0) {
                                        "Reste prévisionnel après confirmation : ¥ ${String.format(java.util.Locale.US, "%.2f", remainingWalletYuans)} (${CurrencyFormatter.formatAriary((remainingWalletYuans * effectiveCmupRate).toLong())})"
                                    } else {
                                        "Manque ¥ ${String.format(java.util.Locale.US, "%.2f", -remainingWalletYuans)}. Cochez la case ci-dessous pour valider sans débit du solde Yuan."
                                    },
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF3D494C)
                                )
                            }
                        }
                    }

                    // Checkbox: Conserver en commande active sans débiter le portefeuille
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDraftCheckboxChecked = !isDraftCheckboxChecked },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isDraftCheckboxChecked,
                            onCheckedChange = { isDraftCheckboxChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00687A))
                        )
                        Text(
                            text = "Conserver en commande active sans débiter le portefeuille immédiatement",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 4. SUIVI LOGISTIQUE USINE
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF8A4CFC).copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AltRoute,
                            contentDescription = null,
                            tint = Color(0xFF712AE2),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Suivi Logistique Usine",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF131B2E)
                    )
                }

                // Numéro de commande fournisseur (1688)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Numéro de commande fournisseur (1688)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFF131B2E)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2F3FF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Tag, contentDescription = null, tint = Color(0xFF3D494C), modifier = Modifier.size(18.dp))
                            Text(
                                text = if (order.referenceCode.isNotBlank()) "1688-${order.referenceCode}" else "1688-ORD-${order.id}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF131B2E)
                            )
                        }
                    }
                }

                // Date validation & Expédition usine
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Date validation", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                            Text(com.example.util.DateUtils.formatShortDate(order.orderDate), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF131B2E))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Expédition usine", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                            Text("2 - 3 j ouvrés", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF131B2E))
                        }
                    }
                }

                // Entrepôt de transit en Chine
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Entrepôt de transit en Chine",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFF131B2E)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2F3FF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warehouse, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(20.dp))
                            Column {
                                Text("SinoExpress Guangzhou Hub", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF131B2E))
                                Text("Baiyun District, Gate 4 · Transit Maritime & Aérien", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF6D797D))
                            }
                        }
                    }
                }

                // Numéros de suivi intérieur (Tracking)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Numéros de suivi intérieur (Tracking)", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(0xFF131B2E))
                        Text("${trackingList.size} colis", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF3D494C))
                    }

                    trackingList.forEach { (trackNo, trackDesc) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF2F3FF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF8A4CFC).copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFF712AE2), modifier = Modifier.size(14.dp))
                                    }
                                    Column {
                                        Text(trackNo, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF131B2E))
                                        Text(trackDesc, fontFamily = Inter, fontSize = 10.sp, color = Color(0xFF6D797D))
                                    }
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFDAE2FD)) {
                                    Text("En attente", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF3D494C), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }

                    // Bouton Ajouter un autre numéro de suivi
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEAEDFF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                newTrackingInput = "SF Express #SF${(100000000..999999999).random()}"
                                newTrackingLabel = "Colis ${trackingList.size + 1}"
                                showAddTrackingDialog = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ajouter un autre numéro de suivi", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF00687A))
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. PINNED BOTTOM ACTION STRIP
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
                val isBalanceSufficient = remainingWalletYuans >= 0.0
                val canConfirm = isOrderValid && !isSubmitting && (isAlreadyDebited || isDraftCheckboxChecked || isBalanceSufficient)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAlreadyDebited) "Paiement Yuan réglé :" else if (isDraftCheckboxChecked) "Montant commande (sans débit) :" else "Montant à débiter :",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color(0xFF3D494C)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isOrderValid) "¥ ${String.format(java.util.Locale.US, "%.2f", totalOrderYuan)}" else "—",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (isAlreadyDebited) Color(0xFF00687A) else Color(0xFF712AE2)
                        )
                        Text(
                            text = if (isOrderValid) "(${CurrencyFormatter.formatAriary(totalOrderAr)})" else "—",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (!canConfirm || isSubmitting) return@Button
                        isSubmitting = true
                        val nextStatus = OrderStatus.WAREHOUSE.code
                        val shouldDebitNow = !isAlreadyDebited && !isDraftCheckboxChecked && totalOrderYuan > 0

                        if (shouldDebitNow) {
                            onSpendYuans(totalOrderYuan, effectiveCmupRate, "Paiement 1688: ${order.product} (Lot #${order.id})")
                        }
                        val serializedTrackings = trackingList.joinToString(",") { it.first }
                        val finalExchangeRate = if (order.exchangeRate > 0.0) order.exchangeRate else effectiveCmupRate

                        val updatedOrder = order.copy(
                            status = nextStatus,
                            priceYuan = order.priceYuan,
                            quantityBought = order.quantityBought,
                            exchangeRate = finalExchangeRate,
                            trackingNumbers = serializedTrackings,
                            yuanDebited = isAlreadyDebited || shouldDebitNow,
                            yuanDebitedAmount = if (shouldDebitNow) totalOrderYuan else if (isAlreadyDebited) order.yuanDebitedAmount else 0.0
                        )
                        onConfirmOrder(updatedOrder)

                        val msg = when {
                            isAlreadyDebited -> "Commande mise à jour (Débit déjà effectué)"
                            shouldDebitNow -> "Commande confirmée et payée en Yuans !"
                            else -> "Commande validée sans débit immédiat"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    enabled = canConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_confirm_pay_order"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAlreadyDebited) Color(0xFF00687A) else if (isDraftCheckboxChecked) Color(0xFF712AE2) else Color(0xFF00687A),
                        disabledContainerColor = Color(0xFFBCC9CD)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when {
                                !isOrderValid -> "Validation bloquée (quantité ou prix non défini)"
                                isSubmitting -> "Traitement en cours..."
                                isAlreadyDebited -> "Valider & Poursuivre (Déjà Payé)"
                                !isBalanceSufficient && !isDraftCheckboxChecked -> "Solde Yuan insuffisant (Cochez 'Sans débit')"
                                isDraftCheckboxChecked -> "Enregistrer Commande (Sans Débit)"
                                else -> "Confirmer et Payer la Commande"
                            },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Icon(
                            imageVector = if (isDraftCheckboxChecked) Icons.Default.Bookmark else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "Enregistrer comme brouillon sans finaliser",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Color(0xFF3D494C),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { onSaveAsDraft() }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }

    if (showAddTrackingDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddTrackingDialog = false },
            title = {
                Text("Ajouter un colis de tracking", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Transporteur & Numéro :", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                    androidx.compose.material3.OutlinedTextField(
                        value = newTrackingInput,
                        onValueChange = { newTrackingInput = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Description contenu :", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                    androidx.compose.material3.OutlinedTextField(
                        value = newTrackingLabel,
                        onValueChange = { newTrackingLabel = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTrackingInput.isNotBlank()) {
                            trackingList.add(newTrackingInput to newTrackingLabel.ifBlank { "Nouveau colis" })
                        }
                        showAddTrackingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00687A))
                ) {
                    Text("Ajouter", color = Color.White)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showAddTrackingDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
