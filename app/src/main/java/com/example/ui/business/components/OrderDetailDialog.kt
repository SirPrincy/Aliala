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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DomainVerification
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun OrderDetailDialog(
    order: BusinessOrderEntity,
    cmupRate: Long,
    onDismiss: () -> Unit,
    onAdvanceStatus: (BusinessOrderEntity) -> Unit,
    onDeleteOrder: (BusinessOrderEntity) -> Unit,
    onDuplicateOrder: (BusinessOrderEntity) -> Unit = {},
    onUpdateOrder: (BusinessOrderEntity) -> Unit = {}
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val effectiveCmupRate = if (order.exchangeRate > 0) order.exchangeRate.toLong() else if (cmupRate > 0) cmupRate else 650L
    val unitPriceYuan = if (order.priceYuan > 0) order.priceYuan else 28.0
    val totalQty = if (order.quantityBought > 0) order.quantityBought else 20
    val unitPriceAr = (unitPriceYuan * effectiveCmupRate).toLong()
    val goodsTotalYuan = totalQty * unitPriceYuan
    val shippingNationalYuan = 15.0
    val shippingNationalAr = (shippingNationalYuan * effectiveCmupRate).toLong()
    val totalOrderYuan = goodsTotalYuan + shippingNationalYuan
    val totalOrderAr = (totalOrderYuan * effectiveCmupRate).toLong()

    // 6-step logistic process
    val stepsList = listOf(
        LogisticStep(
            index = 0,
            title = "Commandé",
            subtitle = "12 oct. · Paiement Alipay ¥ ${String.format(java.util.Locale.US, "%.0f", totalOrderYuan)}",
            statusCode = "ORDERED"
        ),
        LogisticStep(
            index = 1,
            title = "En livraison Chine",
            subtitle = "14 oct. · Expédié via SF Express",
            statusCode = "WAREHOUSE"
        ),
        LogisticStep(
            index = 2,
            title = "Arrivé entrepôt Guangzhou",
            subtitle = "16 oct. · SinoExpress pesée 4,2 kg",
            statusCode = "GUANGZHOU"
        ),
        LogisticStep(
            index = 3,
            title = "En livraison vers Mada",
            subtitle = "19 oct. · Vol Cargo SinoExpress #AF882",
            statusCode = "FLIGHT"
        ),
        LogisticStep(
            index = 4,
            title = "Arrivée & Dédouanement",
            subtitle = "Estimée le 26 oct. · Ivato Cargo",
            statusCode = "QUALITY_CONTROL"
        ),
        LogisticStep(
            index = 5,
            title = "Contrôle qualité & Stock",
            subtitle = "Entrée au stock magasin Antananarivo",
            statusCode = "DELIVERY_READY"
        )
    )

    // Current step resolution (defaults to step 3 "En livraison vers Mada" if matching mock)
    val currentStepIndex = when (order.status) {
        "DRAFT", "ORDERED" -> 0
        "WAREHOUSE" -> 1
        "GUANGZHOU" -> 2
        "FLIGHT" -> 3
        "QUALITY_CONTROL" -> 4
        "DELIVERY_READY", "SOLD" -> 5
        else -> 3 // Step 4 in mock (0-indexed 3)
    }

    val currentActiveStep = stepsList.getOrElse(currentStepIndex) { stepsList[3] }
    val nextStep = stepsList.getOrNull(currentStepIndex + 1)
    val nextStepLabel = nextStep?.title ?: "Clôturé"

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFFAF8FF)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 76.dp)
                ) {
                    // TOP APP HEADER (Fixed prototype shell: "✕ Nouvelle Habitude   Enregistrer   [avatar]")
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFAF8FF).copy(alpha = 0.95f),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable { onDismiss() }
                                        .testTag("btn_close_order_detail"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fermer",
                                        tint = Color(0xFF131B2E),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = "Nouvelle Habitude",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.testTag("top_save_detail_action")
                                ) {
                                    Text(
                                        text = "Enregistrer",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF6D797D)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00687A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profil",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // SUB-BAR NAVIGATOR: (← ACHAT BUSINESS #AC-2026-042   ⋮)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { onDismiss() }
                                    .testTag("btn_back_order_detail")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Retour",
                                        tint = Color(0xFF131B2E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "ACHAT BUSINESS",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFF6D797D),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (order.lotNumber.isNotBlank()) order.lotNumber else "#AC-2026-${order.id.toString().padStart(3, '0')}",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }
                        }

                        // Overflow Options Dropdown
                        Box {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { showMenu = true }
                                    .testTag("btn_menu_order_detail")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = Color(0xFF131B2E),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .background(Color.White)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF6D797D), modifier = Modifier.size(20.dp))
                                            Text("Modifier l'achat", fontFamily = PlusJakartaSans, fontSize = 14.sp)
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        Toast.makeText(context, "Mode modification actif", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF6D797D), modifier = Modifier.size(20.dp))
                                            Text("Dupliquer la fiche", fontFamily = PlusJakartaSans, fontSize = 14.sp)
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDuplicateOrder(order)
                                        Toast.makeText(context, "Fiche dupliquée !", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF6D797D), modifier = Modifier.size(20.dp))
                                            Text("Étiquette de colis", fontFamily = PlusJakartaSans, fontSize = 14.sp)
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        Toast.makeText(context, "Étiquette colis générée pour ${order.product}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(20.dp))
                                            Text("Annuler l'achat", fontFamily = PlusJakartaSans, fontSize = 14.sp, color = Color(0xFFBA1A1A))
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDeleteOrder(order)
                                        Toast.makeText(context, "Achat annulé", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }

                    // SCROLLABLE BODY (Cards from UI Specification)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. EN-TÊTE PRODUIT & STATUT
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
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Thumbnail with "20 pcs" overlay
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFFEAEDFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (order.family.contains("AUDIO", ignoreCase = true) || order.product.contains("Écouteurs", ignoreCase = true)) "🎧" else "📦",
                                            fontSize = 38.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.White.copy(alpha = 0.92f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$totalQty pcs",
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF131B2E)
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Badges row: [Business] [1688]
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(9999.dp))
                                                    .background(Color(0xFF8A4CFC).copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Business",
                                                    fontFamily = PlusJakartaSans,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF712AE2)
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(9999.dp))
                                                    .background(Color(0xFF06B6D4).copy(alpha = 0.20f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = order.platform.ifBlank { "1688" },
                                                    fontFamily = PlusJakartaSans,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF00424F)
                                                )
                                            }
                                        }

                                        Text(
                                            text = order.product.ifBlank { "Article #${order.id}" },
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color(0xFF131B2E),
                                            maxLines = 1
                                        )

                                        Text(
                                            text = "Réf : ${order.referenceCode.ifBlank { "RÉF-${order.id}" }}",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = Color(0xFF6D797D)
                                        )

                                        // Status badge with animated pulsing dot
                                        Surface(
                                            shape = RoundedCornerShape(9999.dp),
                                            color = Color(0xFF8A4CFC),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White)
                                                        .alpha(pulseAlpha)
                                                )
                                                Text(
                                                    text = currentActiveStep.title,
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }

                                // Action Step Transition Button: "Passer à l'étape suivante : Arrivé Mada →"
                                Button(
                                    onClick = {
                                        nextStep?.let { ns ->
                                            val updated = order.copy(status = ns.statusCode)
                                            onUpdateOrder(updated)
                                            onAdvanceStatus(updated)
                                            Toast.makeText(context, "Étape validée : ${ns.title}", Toast.LENGTH_SHORT).show()
                                        } ?: run {
                                            Toast.makeText(context, "Déjà à la dernière étape", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF712AE2)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_advance_step")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Passer à l'étape suivante : $nextStepLabel",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
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
                            }
                        }

                        // 2. PARCOURS LOGISTIQUE (Timeline à 6 étapes interactives)
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
                                                .background(Color(0xFF8A4CFC).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Timeline,
                                                contentDescription = null,
                                                tint = Color(0xFF712AE2),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = "Parcours logistique",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFFEADDFF).copy(alpha = 0.5f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Étape ${currentStepIndex + 1} sur 6",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color(0xFF712AE2)
                                        )
                                    }
                                }

                                // Interactive Timeline Steps
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 6.dp)
                                ) {
                                    stepsList.forEachIndexed { idx, step ->
                                        val isCompleted = idx < currentStepIndex
                                        val isActive = idx == currentStepIndex
                                        val isFuture = idx > currentStepIndex
                                        val isLast = idx == stepsList.lastIndex

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onUpdateOrder(order.copy(status = step.statusCode))
                                                },
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            // Vertical connector + node icon
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when {
                                                                isCompleted -> Color(0xFF712AE2)
                                                                isActive -> Color(0xFF8A4CFC)
                                                                else -> Color(0xFFEAEDFF)
                                                            }
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    when {
                                                        isCompleted -> {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                        isActive -> {
                                                            Icon(
                                                                imageVector = Icons.Default.FlightTakeoff,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                        idx == 4 -> {
                                                            Icon(
                                                                imageVector = Icons.Default.DomainVerification,
                                                                contentDescription = null,
                                                                tint = Color(0xFF6D797D),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                        else -> {
                                                            Icon(
                                                                imageVector = Icons.Default.Inventory2,
                                                                contentDescription = null,
                                                                tint = Color(0xFF6D797D),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                if (!isLast) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(2.dp)
                                                            .height(if (isActive) 56.dp else 40.dp)
                                                            .background(
                                                                if (isCompleted) Color(0xFF712AE2)
                                                                else Color(0xFFDAE2FD)
                                                            )
                                                    )
                                                }
                                            }

                                            // Content description
                                            if (isActive) {
                                                // Highlighted active box
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0xFFF2F3FF))
                                                        .padding(10.dp)
                                                ) {
                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = step.title,
                                                                fontFamily = PlusJakartaSans,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 14.sp,
                                                                color = Color(0xFF712AE2)
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(Color(0xFF712AE2))
                                                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                                                            ) {
                                                                Text(
                                                                    text = "En cours",
                                                                    fontFamily = PlusJakartaSans,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.White
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = step.subtitle,
                                                            fontFamily = Inter,
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF3D494C)
                                                        )
                                                    }
                                                }
                                            } else {
                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(bottom = if (!isLast) 18.dp else 0.dp),
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Text(
                                                        text = step.title,
                                                        fontFamily = PlusJakartaSans,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp,
                                                        color = if (isFuture) Color(0xFF6D797D) else Color(0xFF131B2E)
                                                    )
                                                    Text(
                                                        text = step.subtitle,
                                                        fontFamily = Inter,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF6D797D)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. PRIX & VALEUR D'ACHAT
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
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF06B6D4).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = null,
                                                tint = Color(0xFF00687A),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = "Prix & Valeur d'achat",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }
                                    Text(
                                        text = "${order.platform.ifBlank { "1688" }} Direct",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = Color(0xFF6D797D)
                                    )
                                }

                                // 2-column metrics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF2F3FF))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "Prix unitaire",
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = Color(0xFF6D797D)
                                            )
                                            Text(
                                                text = "¥ ${String.format(java.util.Locale.US, "%.0f", unitPriceYuan)}",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Text(
                                                text = "≈ ${CurrencyFormatter.formatAriary(unitPriceAr)}",
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 10.sp,
                                                color = Color(0xFF6D797D)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF2F3FF))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "Quantité totale",
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = Color(0xFF6D797D)
                                            )
                                            Text(
                                                text = "$totalQty unités",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Text(
                                                text = "3 déclinaisons",
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF712AE2)
                                            )
                                        }
                                    }
                                }

                                // Détail des couleurs
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "DÉTAIL DES COULEURS",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color(0xFF6D797D),
                                            letterSpacing = 0.5.sp
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF131B2E)))
                                                Text("10× Noir Pro", fontFamily = Inter, fontSize = 13.sp, color = Color(0xFF131B2E))
                                            }
                                            Text("¥ 280", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFBCC9CD)))
                                                Text("5× Blanc Pur", fontFamily = Inter, fontSize = 13.sp, color = Color(0xFF131B2E))
                                            }
                                            Text("¥ 140", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFBA1A1A)))
                                                Text("5× Rouge Sport", fontFamily = Inter, fontSize = 13.sp, color = Color(0xFF131B2E))
                                            }
                                            Text("¥ 140", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        }
                                    }
                                }

                                // Financial line items
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Frais de port national Chine", fontFamily = Inter, fontSize = 13.sp, color = Color(0xFF6D797D))
                                        Text("¥ 15 (${CurrencyFormatter.formatAriary(shippingNationalAr)})", fontFamily = Inter, fontSize = 13.sp, color = Color(0xFF131B2E))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Taux CMUP appliqué", fontFamily = Inter, fontSize = 13.sp, color = Color(0xFF6D797D))
                                        Text("$effectiveCmupRate Ar / ¥", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEAEDFF)))
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Coût total achat fournisseur",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "¥ ${String.format(java.util.Locale.US, "%.0f", totalOrderYuan)}",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = Color(0xFF00687A)
                                            )
                                            Text(
                                                text = CurrencyFormatter.formatAriary(totalOrderAr),
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = Color(0xFF6D797D)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. COLIS & NUMÉROS DE SUIVI (Tracking)
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
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF8A4CFC).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalShipping,
                                                contentDescription = null,
                                                tint = Color(0xFF712AE2),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = "Colis & Numéros de suivi",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFFEAEDFF))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "2 colis",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF131B2E)
                                        )
                                    }
                                }

                                // Colis 1
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Colis 1 · SF Express",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(9999.dp))
                                                    .background(Color(0xFFDAE2FD))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Livré entrepôt",
                                                    fontFamily = PlusJakartaSans,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF131B2E)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "SF18492049219",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 13.sp,
                                                color = Color(0xFF6D797D)
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color.White,
                                                    shadowElevation = 1.dp,
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clickable {
                                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                            clipboard.setPrimaryClip(ClipData.newPlainText("SF Tracking", "SF18492049219"))
                                                            Toast.makeText(context, "Numéro copié !", Toast.LENGTH_SHORT).show()
                                                        }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = Color(0xFF6D797D), modifier = Modifier.size(16.dp))
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color.White,
                                                    shadowElevation = 1.dp,
                                                    modifier = Modifier
                                                        .height(32.dp)
                                                        .clickable {
                                                            Toast.makeText(context, "Suivi SF Express ouvert", Toast.LENGTH_SHORT).show()
                                                        }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier.padding(horizontal = 8.dp)
                                                    ) {
                                                        Text("Tracer", fontFamily = PlusJakartaSans, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00687A))
                                                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Colis 2
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Colis 2 · SinoExpress Cargo",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(9999.dp))
                                                    .background(Color(0xFF8A4CFC))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "En vol",
                                                    fontFamily = PlusJakartaSans,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "SINO-MD-883920",
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF712AE2)
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color.White,
                                                    shadowElevation = 1.dp,
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clickable {
                                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                            clipboard.setPrimaryClip(ClipData.newPlainText("Sino Tracking", "SINO-MD-883920"))
                                                            Toast.makeText(context, "Numéro copié !", Toast.LENGTH_SHORT).show()
                                                        }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = Color(0xFF6D797D), modifier = Modifier.size(16.dp))
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color.White,
                                                    shadowElevation = 1.dp,
                                                    modifier = Modifier
                                                        .height(32.dp)
                                                        .clickable {
                                                            Toast.makeText(context, "Suivi SinoExpress Cargo ouvert", Toast.LENGTH_SHORT).show()
                                                        }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier.padding(horizontal = 8.dp)
                                                    ) {
                                                        Text("Tracer", fontFamily = PlusJakartaSans, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF712AE2))
                                                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF712AE2), modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Button Ajouter un autre tracking
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEAEDFF),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clickable {
                                            Toast.makeText(context, "Ajouter un numéro de suivi", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF131B2E), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Ajouter un autre tracking",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }
                                }
                            }
                        }

                        // 5. TRANSITAIRE & FRET
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
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF78A5FF).copy(alpha = 0.20f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Flight,
                                                contentDescription = null,
                                                tint = Color(0xFF005AC2),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = "Transitaire & Fret",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFF78A5FF).copy(alpha = 0.30f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Aérien Express",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00397F)
                                        )
                                    }
                                }

                                // Partner row
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Partenaire logistique", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF6D797D))
                                            Text(
                                                text = order.carrier.ifBlank { "SinoExpress Guangzhou" },
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text("48 000 Ar", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF005AC2))
                                            Text(" / kg", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF6D797D))
                                        }
                                    }
                                }

                                // 2-column metrics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF2F3FF))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text("Poids facturé", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF6D797D))
                                            Text(
                                                text = "${order.weightKg ?: 4.2} kg",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Text("Pesée certifiée", fontFamily = Inter, fontSize = 10.sp, color = Color(0xFF6D797D))
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF2F3FF))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text("Volume colis", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF6D797D))
                                            Text("0,018 m³", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF131B2E))
                                            Text("Ratio poids/vol OK", fontFamily = Inter, fontSize = 10.sp, color = Color(0xFF6D797D))
                                        }
                                    }
                                }

                                // Coût fret calculé
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Coût fret calculé", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                                        Column(horizontalAlignment = Alignment.End) {
                                            val calculatedFreight = if (order.freightCostAr > 0) order.freightCostAr else 201600L
                                            Text(CurrencyFormatter.formatAriary(calculatedFreight), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF131B2E))
                                            Text("(4,2 kg × 48 000 Ar)", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF6D797D))
                                        }
                                    }
                                }

                                // Tag fret groupé
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFEAEDFF))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.AccountTree, contentDescription = null, tint = Color(0xFF6D797D), modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "Fret groupé avec 2 autres achats de l'expédition #EXP-88.",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                    }
                                }
                            }
                        }

                        // 6. RÈGLEMENTS TRANSITAIRE
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
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF8A4CFC).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = Color(0xFF712AE2),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = "Règlements Transitaire",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }

                                    Text(
                                        text = "50% réglé",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF712AE2)
                                    )
                                }

                                // Progress bar
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFFEAEDFF))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.5f)
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(Color(0xFF712AE2))
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Payé : 100 000 Ar", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                                        Text("Total : 201 600 Ar", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF6D797D))
                                    }
                                }

                                // 2-column details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF2F3FF))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text("Acompte départ", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF6D797D))
                                            Text("100 000 Ar", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF712AE2))
                                            Text("16 oct. (Orange Money)", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF6D797D))
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
                                            Text("Reste à l'arrivée", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF6D797D))
                                            Text("101 600 Ar", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFBA1A1A))
                                            Text("À payer au retrait", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF6D797D))
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEAEDFF),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clickable {
                                            Toast.makeText(context, "Formulaire de règlement ouvert", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF712AE2), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "+ Enregistrer un paiement",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF712AE2)
                                        )
                                    }
                                }
                            }
                        }

                        // 7. RENTABILITÉ & COÛT DE REVIENT
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
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF06B6D4).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Calculate,
                                                contentDescription = null,
                                                tint = Color(0xFF00687A),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = "Rentabilité & Coût de revient",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFFEAEDFF))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Estimé",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF131B2E)
                                        )
                                    }
                                }

                                // Coût unitaire final
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Coût de revient unitaire", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF6D797D))
                                            Text("28 768 Ar", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF00687A))
                                        }
                                        Text(
                                            text = "Achat unitaire (18 688 Ar) + Fret imputé (10 080 Ar / pièce)",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = Color(0xFF6D797D)
                                        )
                                    }
                                }

                                // 2-column metrics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF2F3FF))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text("Prix revente conseillé", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF6D797D))
                                            Text(
                                                text = if (order.resalePriceAr > 0) CurrencyFormatter.formatAriary(order.resalePriceAr) else "55 000 Ar",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Text("Marché Tana", fontFamily = Inter, fontSize = 10.sp, color = Color(0xFF6D797D))
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF2F3FF))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text("Marge brute estimée", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF6D797D))
                                            Text("48 %", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF712AE2))
                                            Text("+26 232 Ar / pc", fontFamily = PlusJakartaSans, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF712AE2))
                                        }
                                    }
                                }

                                // Grille de contrôle à réception
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Grille de contrôle à réception", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                                        Text("En attente colis", fontFamily = Inter, fontSize = 10.sp, color = Color(0xFF6D797D))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            "Conformes OK" to (order.stockCount.takeIf { it > 0 }?.toString() ?: "--"),
                                            "Défectueux" to (order.defectiveCount.takeIf { it > 0 }?.toString() ?: "--"),
                                            "Manquants" to "--"
                                        ).forEach { (label, value) ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFFF2F3FF))
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(value, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF131B2E))
                                                    Text(label, fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF6D797D))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 8. PRÉCOMMANDES ASSOCIÉES
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
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF8A4CFC).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PersonPin,
                                                contentDescription = null,
                                                tint = Color(0xFF712AE2),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = "Précommandes associées",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFF8A4CFC).copy(alpha = 0.20f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "1 réservation",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF712AE2)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF712AE2)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val clientInit = order.assignedClient.trim().take(1).ifBlank { "R" }
                                                    Text(clientInit, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                                }

                                                Column {
                                                    Text(
                                                        text = "Client : ${order.assignedClient.ifBlank { "Rina" }}",
                                                        fontFamily = PlusJakartaSans,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF131B2E)
                                                    )
                                                    Text(
                                                        text = "5 unités Noir Pro réservées",
                                                        fontFamily = Inter,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF6D797D)
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color.White,
                                                shadowElevation = 1.dp
                                            ) {
                                                Text(
                                                    text = "Acompte 100k Ar",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF712AE2),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Prix convenu : 55 000 Ar / pc", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF6D797D))
                                            Text("Total : 275 000 Ar", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                                        }
                                    }
                                }
                            }
                        }

                        // 9. NOTES & REMARQUES
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
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFEAEDFF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NoteAlt,
                                                contentDescription = null,
                                                tint = Color(0xFF6D797D),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = "Notes & Remarques",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }
                                    Text(
                                        text = "12 oct. 14:32",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        color = Color(0xFF6D797D)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = Color(0xFF6D797D),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "\"Fournisseur fiable testé pour la 3ème fois. Emballage renforcé avec cornières bulle demandé pour sécuriser les boîtes individuelles.\"",
                                                fontFamily = Inter,
                                                fontSize = 13.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Text(
                                                text = "Enregistré par Administrateur",
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 10.sp,
                                                color = Color(0xFF6D797D)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // FIXED BOTTOM PRIMARY ACTION: ("Enregistrer l'habitude ✓" in cyan as designed)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_save_order_details_bottom")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Enregistrer l'habitude",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
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
        }
    }
}

private data class LogisticStep(
    val index: Int,
    val title: String,
    val subtitle: String,
    val statusCode: String
)
