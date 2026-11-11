package com.example.ui.business.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun TransitaireDetailDialog(
    transitaire: CatalogueTransitaireSummaryUi,
    onDismiss: () -> Unit,
    onToggleArchived: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onUpdateRates: (id: Long, airRate: Long, seaRate: Long) -> Unit = { _, _, _ -> },
    onRecordPayment: (id: Long, amountAr: Long) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showRateEditDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Segmented toggle: Aérien vs Maritime
    var selectedFretType by remember { mutableStateOf("AERIEN") } // "AERIEN" or "MARITIME"
    var selectedPeriod by remember { mutableStateOf("6 mois") } // "3 mois", "6 mois", "1 an", "Tout"

    // Helper function for clipboard
    fun copyToClipboard(label: String, text: String, feedback: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
    }

    val warehouseAddress = if (transitaire.chinaWarehouseAddress.isNotBlank()) {
        transitaire.chinaWarehouseAddress
    } else {
        "Guangzhou Baiyun District, Warehouse #4B, Logistics Park South Rd"
    }

    val wechatId = if (transitaire.wechat.isNotBlank()) transitaire.wechat else "sino_gz_agent"
    val whatsappNum = if (transitaire.whatsapp.isNotBlank()) transitaire.whatsapp else "+86 138 2901"

    val currentAirRate = transitaire.airRateAr
    val currentSeaRate = transitaire.seaRateAr

    val totalInvoiced = if (transitaire.totalInvoicedAr > 0) transitaire.totalInvoicedAr else 420_000L
    val balanceDue = transitaire.balanceDueAr
    val paidAmount = if (transitaire.paidAmountAr > 0) transitaire.paidAmountAr else (totalInvoiced - balanceDue).coerceAtLeast(0L)
    val settlementPercent = if (totalInvoiced > 0) ((paidAmount.toDouble() / totalInvoiced) * 100).toInt().coerceIn(0, 100) else 100

    val effectiveRateCalculated = if (transitaire.effectiveRateAr > 0) transitaire.effectiveRateAr else 38_570L

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF8FF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // ---------------------------------------------------------
                // 1. TOP HEADER (Back arrow, Title, Bell, Profile)
                // ---------------------------------------------------------
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFAF8FF).copy(alpha = 0.96f),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_back_transitaire_detail")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = Color(0xFF131B2E),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Transitaire Détail",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF131B2E)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color(0xFF3D494C),
                                    modifier = Modifier.size(24.dp)
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

                // ---------------------------------------------------------
                // 2. SCROLLABLE CONTENT CARDS
                // ---------------------------------------------------------
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // -----------------------------------------------------
                    // SUB-HEADER BAR / ENTITY STATUS & ACTIONS
                    // -----------------------------------------------------
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlightTakeoff,
                                        contentDescription = null,
                                        tint = Color(0xFF00687A),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = transitaire.name,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = Color(0xFF131B2E),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(
                                                    if (!transitaire.isArchived) Color(0xFF00687A).copy(alpha = 0.10f)
                                                    else Color(0xFF6D797D).copy(alpha = 0.15f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (!transitaire.isArchived) "Actif" else "Archivé",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (!transitaire.isArchived) Color(0xFF00687A) else Color(0xFF3D494C)
                                            )
                                        }
                                    }
                                    Text(
                                        text = transitaire.routeHub.ifBlank { "Fret Chine → Madagascar · Hub Baiyun" },
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = Color(0xFF3D494C),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Menu options 3-dots
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("btn_transitaire_menu")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = Color(0xFF3D494C),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Modifier le tarif", fontFamily = PlusJakartaSans) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = Color(0xFF00687A),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            showRateEditDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (transitaire.isArchived) "Réactiver" else "Archiver",
                                                fontFamily = PlusJakartaSans
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Archive,
                                                contentDescription = null,
                                                tint = Color(0xFF3D494C),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            onToggleArchived(transitaire.id)
                                            Toast.makeText(
                                                context,
                                                if (transitaire.isArchived) "Transitaire réactivé" else "Transitaire archivé",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Supprimer",
                                                fontFamily = PlusJakartaSans,
                                                color = Color(0xFFBA1A1A)
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color(0xFFBA1A1A),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            showDeleteConfirmDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // 1. TARIFS EN VIGUEUR CARD
                    // -----------------------------------------------------
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
                                            imageVector = Icons.Default.PriceChange,
                                            contentDescription = null,
                                            tint = Color(0xFF00687A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "Tarifs en vigueur",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFFEAEDFF))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Actualisé juil. 26",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                }
                            }

                            // Row 1: Aérien
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
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(36.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White,
                                            shadowElevation = 1.dp
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Flight,
                                                    contentDescription = null,
                                                    tint = Color(0xFF00687A),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "Aérien",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF131B2E)
                                                )
                                                Text(
                                                    text = CurrencyFormatter.formatAriary(currentAirRate),
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp,
                                                    color = Color(0xFF00687A)
                                                )
                                                Text(
                                                    text = "/ kg",
                                                    fontFamily = Inter,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF3D494C)
                                                )
                                            }
                                            Text(
                                                text = "Depuis le 1er juil. 2026",
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = Color(0xFF3D494C)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFFDAD6))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                tint = Color(0xFFBA1A1A),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "+2k (+5,6%)",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFFBA1A1A)
                                            )
                                        }
                                    }
                                }
                            }

                            // Row 2: Maritime
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
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(36.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White,
                                            shadowElevation = 1.dp
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsBoat,
                                                    contentDescription = null,
                                                    tint = Color(0xFF712AE2),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "Maritime",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF131B2E)
                                                )
                                                Text(
                                                    text = CurrencyFormatter.formatAriary(currentSeaRate),
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp,
                                                    color = Color(0xFF712AE2)
                                                )
                                                Text(
                                                    text = "/ m³",
                                                    fontFamily = Inter,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF3D494C)
                                                )
                                            }
                                            Text(
                                                text = "Depuis le 15 août 2026",
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = Color(0xFF3D494C)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFACEDFF))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = Color(0xFF004E5C),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "-40k (-3,2%)",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF004E5C)
                                            )
                                        }
                                    }
                                }
                            }

                            // Modifier le tarif CTA
                            Button(
                                onClick = { showRateEditDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEAEDFF),
                                    contentColor = Color(0xFF00687A)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("btn_modifier_tarif")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Modifier le tarif",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // 2. ÉVOLUTION DES TARIFS CARD
                    // -----------------------------------------------------
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF00687A).copy(alpha = 0.10f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFF00687A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Évolution des tarifs",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }

                            // Segmented toggle Aérien vs Maritime
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF2F3FF))
                                    .padding(4.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selectedFretType == "AERIEN") Color.White else Color.Transparent)
                                            .clickable { selectedFretType = "AERIEN" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Aérien (Ar/kg)",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = if (selectedFretType == "AERIEN") FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = if (selectedFretType == "AERIEN") Color(0xFF00687A) else Color(0xFF3D494C)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selectedFretType == "MARITIME") Color.White else Color.Transparent)
                                            .clickable { selectedFretType = "MARITIME" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Maritime (Ar/m³)",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = if (selectedFretType == "MARITIME") FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = if (selectedFretType == "MARITIME") Color(0xFF712AE2) else Color(0xFF3D494C)
                                        )
                                    }
                                }
                            }

                            // Period chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("3 mois", "6 mois", "1 an", "Tout").forEach { period ->
                                    val isSelected = selectedPeriod == period
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(if (isSelected) Color(0xFF00687A) else Color(0xFFEAEDFF))
                                            .clickable { selectedPeriod = period }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = period,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White else Color(0xFF3D494C)
                                        )
                                    }
                                }
                            }

                            // Stepped Line Chart Canvas Container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF2F3FF))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Tooltip Bubble
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF131B2E))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF06B6D4))
                                                )
                                                Text(
                                                    text = if (selectedFretType == "AERIEN") "1 juil. 2026 : 36k → 38k (+5,6%)" else "15 août 2026 : 1240k → 1200k (-3,2%)",
                                                    fontFamily = PlusJakartaSans,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    // Stepped Chart Canvas
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                    ) {
                                        val w = size.width
                                        val h = size.height

                                        val leftPadding = 36f
                                        val rightPadding = 12f
                                        val bottomPadding = 24f
                                        val chartW = w - leftPadding - rightPadding
                                        val chartH = h - bottomPadding

                                        val yTop = 16f
                                        val yMid = yTop + chartH * 0.45f
                                        val yLow = yTop + chartH * 0.90f

                                        // Dashed guideline stroke
                                        val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                                        // 3 Guide Lines
                                        drawLine(
                                            color = Color(0xFFDAE2FD),
                                            start = Offset(leftPadding, yTop),
                                            end = Offset(w - rightPadding, yTop),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = dashedEffect
                                        )
                                        drawLine(
                                            color = Color(0xFFDAE2FD),
                                            start = Offset(leftPadding, yMid),
                                            end = Offset(w - rightPadding, yMid),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = dashedEffect
                                        )
                                        drawLine(
                                            color = Color(0xFFDAE2FD),
                                            start = Offset(leftPadding, yLow),
                                            end = Offset(w - rightPadding, yLow),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = dashedEffect
                                        )

                                        // Stepped points
                                        val x1 = leftPadding
                                        val x2 = leftPadding + chartW * 0.35f
                                        val x3 = leftPadding + chartW * 0.70f
                                        val x4 = w - rightPadding

                                        val strokeColor = if (selectedFretType == "AERIEN") Color(0xFF00687A) else Color(0xFF712AE2)
                                        val fillColor = strokeColor.copy(alpha = 0.12f)

                                        // Shaded step polygon
                                        val fillPath = Path().apply {
                                            moveTo(x1, yLow)
                                            lineTo(x2, yLow)
                                            lineTo(x2, yMid)
                                            lineTo(x3, yMid)
                                            lineTo(x3, yTop)
                                            lineTo(x4, yTop)
                                            lineTo(x4, yLow + 8f)
                                            lineTo(x1, yLow + 8f)
                                            close()
                                        }
                                        drawPath(path = fillPath, color = fillColor)

                                        // Stepped Line Path
                                        val linePath = Path().apply {
                                            moveTo(x1, yLow)
                                            lineTo(x2, yLow)
                                            lineTo(x2, yMid)
                                            lineTo(x3, yMid)
                                            lineTo(x3, yTop)
                                            lineTo(x4, yTop)
                                        }
                                        drawPath(
                                            path = linePath,
                                            color = strokeColor,
                                            style = Stroke(
                                                width = 2.5.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )

                                        // Markers
                                        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(x1, yLow))
                                        drawCircle(color = strokeColor, radius = 4.dp.toPx(), center = Offset(x1, yLow), style = Stroke(2.dp.toPx()))

                                        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(x2, yMid))
                                        drawCircle(color = strokeColor, radius = 4.dp.toPx(), center = Offset(x2, yMid), style = Stroke(2.dp.toPx()))

                                        drawCircle(color = Color(0xFF06B6D4), radius = 5.dp.toPx(), center = Offset(x3, yTop))
                                        drawCircle(color = strokeColor, radius = 5.dp.toPx(), center = Offset(x3, yTop), style = Stroke(2.dp.toPx()))

                                        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(x4, yTop))
                                        drawCircle(color = strokeColor, radius = 4.dp.toPx(), center = Offset(x4, yTop), style = Stroke(2.dp.toPx()))
                                    }

                                    // X-Axis labels
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 28.dp, end = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Jan", fontFamily = Inter, fontSize = 10.sp, color = Color(0xFF3D494C))
                                        Text("Avr", fontFamily = Inter, fontSize = 10.sp, color = Color(0xFF3D494C))
                                        Text("Juil", fontFamily = Inter, fontSize = 10.sp, color = Color(0xFF3D494C))
                                        Text(
                                            text = "Actuel",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color(0xFF00687A)
                                        )
                                    }
                                }
                            }

                            // 2x2 Mini stats tiles
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
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Prix actuel", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                                        Text(
                                            text = if (selectedFretType == "AERIEN") CurrencyFormatter.formatAriary(currentAirRate) else CurrencyFormatter.formatAriary(currentSeaRate),
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Variation période", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                                        Text(
                                            text = if (selectedFretType == "AERIEN") "+11,8%" else "-3,2%",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (selectedFretType == "AERIEN") Color(0xFFBA1A1A) else Color(0xFF00687A)
                                        )
                                    }
                                }
                            }

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
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Plus bas", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                                        Text(
                                            text = if (selectedFretType == "AERIEN") "34 000 Ar" else "1 200 000 Ar",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Plus haut", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                                        Text(
                                            text = CurrencyFormatter.formatAriary(if (selectedFretType == "AERIEN") transitaire.airRateAr else transitaire.seaRateAr),
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }
                                }
                            }

                            // Timeline list of rate changes
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "HISTORIQUE DES PALIERS",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF3D494C),
                                    letterSpacing = 0.5.sp
                                )

                                // Palier 1
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = Color(0xFFBA1A1A),
                                            modifier = Modifier
                                                .size(18.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "1 juil. 2026",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF131B2E)
                                                )
                                                Text(
                                                    text = "+2 000 Ar (+5,6%)",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFBA1A1A)
                                                )
                                            }
                                            Text(
                                                text = "Tarif actuel : ${CurrencyFormatter.formatAriary(transitaire.airRateAr)} / kg",
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFDAE2FD))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Hausse carburant kérosène",
                                                    fontFamily = Inter,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF3D494C)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Palier 2
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = Color(0xFFBA1A1A),
                                            modifier = Modifier
                                                .size(18.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "1 avr. 2026",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF131B2E)
                                                )
                                                Text(
                                                    text = "+2 000 Ar (+5,9%)",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFBA1A1A)
                                                )
                                            }
                                            Text(
                                                text = "34 000 Ar → 36 000 Ar / kg",
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFDAE2FD))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Ajustement saisonnier",
                                                    fontFamily = Inter,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF3D494C)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Note info
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF00687A).copy(alpha = 0.10f))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF00687A),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 1.dp)
                                    )
                                    Text(
                                        text = "Le tarif utilisé est figé à chaque achat : un changement ultérieur n'affecte pas les achats passés.",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // 3. CARD PRIX PAYÉS VS TARIF
                    // -----------------------------------------------------
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
                                            .background(Color(0xFF712AE2).copy(alpha = 0.10f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QueryStats,
                                            contentDescription = null,
                                            tint = Color(0xFF712AE2),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "Prix payés vs Tarif",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFFEADDFF))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Écart moy. +3%",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF25005A)
                                    )
                                }
                            }

                            // Visual Comparison Progress Card
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
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Tarif théorique (${CurrencyFormatter.formatAriary(currentAirRate)})",
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                        Text(
                                            text = "Réel constaté",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }

                                    // Comparison visual bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEAEDFF))
                                            .padding(horizontal = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        // Filled actual bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.81f)
                                                .height(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF8A4CFC).copy(alpha = 0.35f))
                                        )

                                        // Benchmark line
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.78f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .width(2.5.dp)
                                                    .height(28.dp)
                                                    .background(Color(0xFF00687A))
                                            )
                                        }

                                        // Labels inside bar
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Taux effectif calculé",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Text(
                                                text = "${CurrencyFormatter.formatAriary(effectiveRateCalculated)} / kg",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF712AE2)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "*Léger surcoût lié aux frais de manutention et pesées volumétriques de douane.",
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = Color(0xFF3D494C)
                                    )
                                }
                            }

                            // Callout on latest shipment
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEAEDFF))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF712AE2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Lot #EXP-88 · 4,2 kg",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                        Text(
                                            text = "162 000 Ar payé (soit 38 570 Ar/kg)",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // 4. CARD FACTURES & RÈGLEMENTS
                    // -----------------------------------------------------
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
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = Color(0xFF00687A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "Factures & Règlements",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }

                                if (balanceDue > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFFFFDAD6))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Reste : ${CurrencyFormatter.formatAriary(balanceDue)}",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFFBA1A1A)
                                        )
                                    }
                                }
                            }

                            // 3 Summary Metrics Tiles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                                        Text(
                                            text = "420k Ar",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Réglé", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                                        Text(
                                            text = "335k Ar",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF00687A)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFDAD6).copy(alpha = 0.5f))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Dû", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFFBA1A1A))
                                        Text(
                                            text = if (balanceDue > 0) "${balanceDue / 1000}k Ar" else "0 Ar",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFFBA1A1A)
                                        )
                                    }
                                }
                            }

                            // Progress bar (80% réglé)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Taux de règlement",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                    Text(
                                        text = "$settlementPercent% réglé",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF00687A)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFFEAEDFF))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(settlementPercent / 100f)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFF00687A))
                                    )
                                }
                            }

                            // Latest invoice item
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF2F3FF))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Text(
                                                text = "Facture #INV-SE-88",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Text(
                                                text = "Lot #EXP-88 · 201 600 Ar",
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = Color(0xFF3D494C)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(Color(0xFFFFDAD6))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Partielle (Reste 85 000 Ar)",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFFBA1A1A)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { showPaymentDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF06B6D4),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .testTag("btn_enregistrer_paiement")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Enregistrer un paiement",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // 5. EXPÉDITIONS RÉCENTES CARD
                    // -----------------------------------------------------
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
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = Color(0xFF712AE2),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "Expéditions récentes",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }
                                TextButton(onClick = { }) {
                                    Text(
                                        text = "Voir tout",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF00687A)
                                    )
                                }
                            }

                            // Lot #EXP-88
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
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Flight,
                                                contentDescription = null,
                                                tint = Color(0xFF00687A),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "Lot #EXP-88",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Text(
                                                text = "Aérien · 4,2 kg · 2 achats inclus",
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = Color(0xFF3D494C)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFF00687A).copy(alpha = 0.10f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Arrivé Mada",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF00687A)
                                        )
                                    }
                                }
                            }

                            // Lot #EXP-79
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
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF712AE2).copy(alpha = 0.20f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsBoat,
                                                contentDescription = null,
                                                tint = Color(0xFF712AE2),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "Lot #EXP-79",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Text(
                                                text = "Maritime · 0,85 m³ · 5 achats",
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = Color(0xFF3D494C)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFFEAEDFF))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Réglé 100%",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // 6. ENTREPÔT & CONTACTS CHINE CARD
                    // -----------------------------------------------------
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
                                        imageVector = Icons.Default.Warehouse,
                                        contentDescription = null,
                                        tint = Color(0xFF00687A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Entrepôt & Contacts Chine",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }

                            // Location info
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF2F3FF))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PinDrop,
                                            contentDescription = null,
                                            tint = Color(0xFF00687A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Adresse Guangzhou (Chine)",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }

                                    Text(
                                        text = warehouseAddress,
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = Color(0xFF3D494C),
                                        lineHeight = 17.sp,
                                        modifier = Modifier.padding(start = 24.dp)
                                    )

                                    Box(modifier = Modifier.padding(start = 24.dp, top = 2.dp)) {
                                        Button(
                                            onClick = {
                                                copyToClipboard(
                                                    label = "Adresse Entrepôt Chine",
                                                    text = warehouseAddress,
                                                    feedback = "Adresse chinoise copiée dans le presse-papiers"
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFEAEDFF),
                                                contentColor = Color(0xFF00687A)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Copier l'adresse chinoise",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Contact channels (2 columns)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // WeChat Card
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .clickable {
                                            copyToClipboard("WeChat ID", wechatId, "WeChat ID copié : $wechatId")
                                        }
                                        .padding(10.dp)
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
                                                imageVector = Icons.Default.ChatBubbleOutline,
                                                contentDescription = null,
                                                tint = Color(0xFF00687A),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "WeChat",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Text(
                                                text = wechatId,
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = Color(0xFF3D494C),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                // WhatsApp Card
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .clickable {
                                            copyToClipboard("WhatsApp", whatsappNum, "Numéro WhatsApp copié : $whatsappNum")
                                        }
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = null,
                                                tint = Color(0xFF00687A),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "WhatsApp",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Text(
                                                text = whatsappNum,
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = Color(0xFF3D494C),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // -------------------------------------------------------------
    // MODAL: MODIFIER LE TARIF
    // -------------------------------------------------------------
    if (showRateEditDialog) {
        var newAirRateInput by remember { mutableStateOf(currentAirRate.toString()) }
        var newSeaRateInput by remember { mutableStateOf(currentSeaRate.toString()) }

        AlertDialog(
            onDismissRequest = { showRateEditDialog = false },
            title = {
                Text(
                    text = "Modifier les tarifs",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF131B2E)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Ajustez les tarifs applicables pour les futurs lots de ${transitaire.name}.",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color(0xFF3D494C)
                    )

                    OutlinedTextField(
                        value = newAirRateInput,
                        onValueChange = { newAirRateInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Tarif fret aérien (Ar / kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newSeaRateInput,
                        onValueChange = { newSeaRateInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Tarif fret maritime (Ar / m³)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val air = newAirRateInput.toLongOrNull() ?: currentAirRate
                        val sea = newSeaRateInput.toLongOrNull() ?: currentSeaRate
                        onUpdateRates(transitaire.id, air, sea)
                        showRateEditDialog = false
                        Toast.makeText(context, "Tarifs mis à jour", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00687A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Valider", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateEditDialog = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }

    // -------------------------------------------------------------
    // MODAL: ENREGISTRER UN PAIEMENT
    // -------------------------------------------------------------
    if (showPaymentDialog) {
        var paymentAmountInput by remember { mutableStateOf(balanceDue.toString()) }

        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = {
                Text(
                    text = "Enregistrer un règlement",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF131B2E)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Solde restant dû : ${CurrencyFormatter.formatAriary(balanceDue)}",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color(0xFF3D494C)
                    )

                    OutlinedTextField(
                        value = paymentAmountInput,
                        onValueChange = { paymentAmountInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Montant payé (Ariary)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountInput.toLongOrNull() ?: 0L
                        if (amount > 0) {
                            onRecordPayment(transitaire.id, amount)
                            showPaymentDialog = false
                            Toast.makeText(context, "Paiement de ${CurrencyFormatter.formatAriary(amount)} enregistré", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }

    // -------------------------------------------------------------
    // MODAL: CONFIRMATION SUPPRESSION
    // -------------------------------------------------------------
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Supprimer le transitaire ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFBA1A1A)
                )
            },
            text = {
                Text(
                    text = "Voulez-vous supprimer définitivement ${transitaire.name} du catalogue ? Les achats passés associés conserveront leurs données d'historique.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = Color(0xFF3D494C)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete(transitaire.id)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        )
    }
}
