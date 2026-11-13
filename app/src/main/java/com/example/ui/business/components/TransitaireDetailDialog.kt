package com.example.ui.business.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
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
import com.example.ui.business.model.TariffGridParser
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransitaireDetailDialog(
    transitaire: CatalogueTransitaireSummaryUi,
    onDismiss: () -> Unit,
    onToggleArchived: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onUpdateRates: (id: Long, airRate: Long, seaRate: Long) -> Unit = { _, _, _ -> },
    onRecordPayment: (id: Long, amountAr: Long) -> Unit = { _, _ -> },
    onUpdateTransitaire: (
        id: Long,
        name: String,
        hub: String,
        airRate: Long,
        seaRate: Long,
        contact: String,
        chinaWarehouseAddress: String,
        isActive: Boolean,
        rateApplicationDate: String,
        airLeadDays: String,
        seaLeadDays: String,
        internalNotes: String,
        airFastRate: Long,
        airFastLeadDays: String,
        goodsCategoryDelays: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val colors = rememberTransitaireColors()
    var showMenu by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEditTransitaireDialog by remember { mutableStateOf(false) }

    fun copyToClipboard(label: String, text: String, feedback: String) {
        if (text.isBlank()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
    }

    val warehouseAddress = transitaire.chinaWarehouseAddress.trim()
    val contactStr = transitaire.contact.trim()
    val wechatId = transitaire.wechat.trim()
    val whatsappNum = transitaire.whatsapp.trim()

    val parsedTariffs = remember(
        transitaire.goodsCategoryDelays,
        transitaire.airRateAr,
        transitaire.airFastRateAr,
        transitaire.seaRateAr
    ) {
        TariffGridParser.parse(
            raw = transitaire.goodsCategoryDelays,
            airRateAr = transitaire.airRateAr,
            airFastRateAr = transitaire.airFastRateAr,
            seaRateAr = transitaire.seaRateAr
        )
    }

    val currentAirRate = transitaire.airRateAr
    val currentSeaRate = transitaire.seaRateAr

    val totalInvoiced = transitaire.totalInvoicedAr
    val balanceDue = transitaire.balanceDueAr
    val paidAmount = if (transitaire.paidAmountAr > 0L) transitaire.paidAmountAr else (totalInvoiced - balanceDue).coerceAtLeast(0L)
    val settlementPercent = if (totalInvoiced > 0L) {
        ((paidAmount.toDouble() / totalInvoiced.toDouble()) * 100.0).toInt().coerceIn(0, 100)
    } else {
        100
    }

    val airTariffs = parsedTariffs.filter { it.mode == "AÉRIEN" }
    val seaTariffs = parsedTariffs.filter { it.mode == "MARITIME" }
    val airMinPrice = airTariffs.minOfOrNull { it.priceAr } ?: currentAirRate
    val airMaxPrice = airTariffs.maxOfOrNull { it.priceAr } ?: currentAirRate
    val seaMinPrice = seaTariffs.minOfOrNull { it.priceAr } ?: currentSeaRate
    val seaMaxPrice = seaTariffs.maxOfOrNull { it.priceAr } ?: currentSeaRate

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // 1. TOP HEADER
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.cardBg,
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
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Fiche Transitaire",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = colors.textPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { showEditTransitaireDialog = true },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("btn_edit_transitaire_detail")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modifier le transitaire",
                                    tint = colors.cyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("btn_transitaire_menu")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Modifier la fiche complète", fontFamily = PlusJakartaSans) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = colors.cyanAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            showEditTransitaireDialog = true
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
                                                tint = colors.textSecondary,
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
                                                color = colors.dangerAccent
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = colors.dangerAccent,
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
                }

                // 2. SCROLLABLE CONTENT CARDS
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ENTITY IDENTITY CARD
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = colors.cardBg,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(colors.cyanSoftBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlightTakeoff,
                                        contentDescription = null,
                                        tint = colors.cyanAccent,
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
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(
                                                    if (!transitaire.isArchived) colors.cyanSoftBg
                                                    else colors.badgeNeutralBg
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (!transitaire.isArchived) "Actif" else "Archivé",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (!transitaire.isArchived) colors.cyanAccent else colors.textSecondary
                                            )
                                        }
                                    }
                                    Text(
                                        text = transitaire.routeHub.ifBlank { "Fret Chine → Madagascar" },
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // 1. GRILLE TARIFAIRE INTERACTIVE ("TARIFS EN VIGUEUR")
                    TransitaireTariffGridSection(
                        tariffItems = parsedTariffs,
                        headerTitle = "Tarifs en vigueur",
                        headerBadge = if (transitaire.rateApplicationDate.isNotBlank()) "Au ${transitaire.rateApplicationDate}" else "Grille active",
                        onTariffItemsChanged = { updatedItems ->
                            val newAirRate = TariffGridParser.matchTariffForMeasure(updatedItems, "AÉRIEN", 1.0)?.priceAr
                                ?: updatedItems.firstOrNull { it.mode == "AÉRIEN" }?.priceAr
                                ?: transitaire.airRateAr
                            val newAirFastRate = updatedItems.filter { it.mode == "AÉRIEN" }.getOrNull(1)?.priceAr
                                ?: newAirRate
                            val newSeaRate = TariffGridParser.matchTariffForMeasure(updatedItems, "MARITIME", 1.0)?.priceAr
                                ?: updatedItems.firstOrNull { it.mode == "MARITIME" }?.priceAr
                                ?: transitaire.seaRateAr
                            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())

                            onUpdateTransitaire(
                                transitaire.id,
                                transitaire.name,
                                transitaire.routeHub,
                                newAirRate,
                                newSeaRate,
                                transitaire.contact,
                                transitaire.chinaWarehouseAddress,
                                !transitaire.isArchived,
                                todayStr,
                                transitaire.airLeadDays,
                                transitaire.seaLeadDays,
                                transitaire.internalNotes,
                                newAirFastRate,
                                transitaire.airFastLeadDays,
                                TariffGridParser.encode(updatedItems)
                            )
                            Toast.makeText(context, "Grille tarifaire mise à jour", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // 2. POLITIQUE & DÉLAIS CONFIGURÉS
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.cyanSoftBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = colors.cyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "Politique & Délais",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = colors.textPrimary
                                    )
                                }

                                TextButton(onClick = { showEditTransitaireDialog = true }) {
                                    Text(
                                        text = "Modifier",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = colors.cyanAccent
                                    )
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
                                        .background(colors.innerCardBg)
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Aérien Std", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                                        Text(
                                            text = transitaire.airLeadDays.ifBlank { "Non défini" },
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.cyanAccent
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.innerCardBg)
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Aérien Fast", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                                        Text(
                                            text = transitaire.airFastLeadDays.ifBlank { "Non défini" },
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.expressAccent
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.innerCardBg)
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Maritime", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                                        Text(
                                            text = transitaire.seaLeadDays.ifBlank { "Non défini" },
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.violetAccent
                                        )
                                    }
                                }
                            }

                            if (transitaire.internalNotes.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.subtleBg)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = transitaire.internalNotes,
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = colors.textPrimary,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }

                    // 3. SYNTHÈSE FOURCHETTES & COÛT CONSTATÉ
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.violetSoftBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueryStats,
                                        contentDescription = null,
                                        tint = colors.violetAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Synthèse tarifaire & Réel constaté",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = colors.textPrimary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.innerCardBg)
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Fourchette Aérien", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                                        Text(
                                            text = if (airTariffs.isEmpty()) "Aucun tarif"
                                            else if (airMinPrice == airMaxPrice) "${CurrencyFormatter.formatAriary(airMinPrice)}/kg"
                                            else "${CurrencyFormatter.formatAriary(airMinPrice)} – ${CurrencyFormatter.formatAriary(airMaxPrice)}",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = colors.cyanAccent
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.innerCardBg)
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Fourchette Maritime", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                                        Text(
                                            text = if (seaTariffs.isEmpty()) "Aucun tarif"
                                            else if (seaMinPrice == seaMaxPrice) "${CurrencyFormatter.formatAriary(seaMinPrice)}/m³"
                                            else "${CurrencyFormatter.formatAriary(seaMinPrice)} – ${CurrencyFormatter.formatAriary(seaMaxPrice)}",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = colors.violetAccent
                                        )
                                    }
                                }
                            }

                            if (transitaire.effectiveRateAr > 0L) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.badgeNeutralBg)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Coût moyen réel constaté",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "Basé sur vos expéditions enregistrées",
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                        Text(
                                            text = "${CurrencyFormatter.formatAriary(transitaire.effectiveRateAr)} / kg",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = colors.violetAccent
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.subtleBg)
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = colors.cyanAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Le coût réel moyen sera calculé automatiquement dès votre première expédition pesée avec ce transitaire.",
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. CARD FACTURES & RÈGLEMENTS
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.cyanSoftBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = colors.cyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "Factures & Règlements",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = colors.textPrimary
                                    )
                                }

                                if (balanceDue > 0L) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(colors.dangerSoftBg)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Reste : ${CurrencyFormatter.formatAriary(balanceDue)}",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = colors.dangerAccent
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
                                        .background(colors.innerCardBg)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total facturé", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                                        Text(
                                            text = CurrencyFormatter.formatAriary(totalInvoiced),
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.innerCardBg)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Réglé", fontFamily = Inter, fontSize = 10.sp, color = colors.textSecondary)
                                        Text(
                                            text = CurrencyFormatter.formatAriary(paidAmount),
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.cyanAccent
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (balanceDue > 0L) colors.dangerSoftBg else colors.successSoftBg)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Dû",
                                            fontFamily = Inter,
                                            fontSize = 10.sp,
                                            color = if (balanceDue > 0L) colors.dangerAccent else colors.successAccent
                                        )
                                        Text(
                                            text = CurrencyFormatter.formatAriary(balanceDue),
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (balanceDue > 0L) colors.dangerAccent else colors.successAccent
                                        )
                                    }
                                }
                            }

                            if (totalInvoiced > 0L) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Taux de règlement",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                        Text(
                                            text = "$settlementPercent% réglé",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = colors.cyanAccent
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(colors.badgeNeutralBg)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(settlementPercent / 100f)
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(colors.cyanSolidBg)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { showPaymentDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.cyanSolidBg,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
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
                            } else {
                                Text(
                                    text = "Aucune facture de fret enregistrée pour ce transitaire.",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    // 5. EXPÉDITIONS RÉCENTES
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.violetSoftBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = colors.violetAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "Expéditions récentes",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = colors.textPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = colors.badgeNeutralBg
                                ) {
                                    Text(
                                        text = "${transitaire.recentShipments.size} lot(s)",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = colors.textPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (transitaire.recentShipments.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.subtleBg)
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = "Aucune expédition associée à ${transitaire.name} pour le moment.",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            } else {
                                transitaire.recentShipments.forEach { ship ->
                                    val isSea = ship.transportMode == "SEA"
                                    val measureStr = if (isSea) {
                                        if (ship.volumeCbm > 0.0) "${String.format(Locale.FRENCH, "%.2f", ship.volumeCbm)} m³" else "Maritime"
                                    } else {
                                        if (ship.weightKg > 0.0) "${String.format(Locale.FRENCH, "%.1f", ship.weightKg)} kg" else "Aérien"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.innerCardBg)
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
                                                        .size(34.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isSea) colors.violetSoftBg else colors.cyanSoftBg),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSea) Icons.Default.DirectionsBoat else Icons.Default.Flight,
                                                        contentDescription = null,
                                                        tint = if (isSea) colors.violetAccent else colors.cyanAccent,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = "${ship.code} · ${ship.title}",
                                                        fontFamily = PlusJakartaSans,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = colors.textPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "$measureStr · ${CurrencyFormatter.formatAriary(ship.freightCostAr)} · ${ship.dateLabel}",
                                                        fontFamily = Inter,
                                                        fontSize = 11.sp,
                                                        color = colors.textSecondary
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(9999.dp))
                                                    .background(
                                                        if (ship.isDelivered) colors.cyanSoftBg
                                                        else colors.badgeNeutralBg
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = ship.statusLabel,
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = if (ship.isDelivered) colors.cyanAccent else colors.textPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. ENTREPÔT & CONTACTS CHINE
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.cyanSoftBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warehouse,
                                        contentDescription = null,
                                        tint = colors.cyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Entrepôt & Contacts Chine",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = colors.textPrimary
                                )
                            }

                            if (warehouseAddress.isBlank()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = colors.subtleBg,
                                    border = BorderStroke(1.dp, colors.border)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Aucune adresse d'entrepôt enregistrée",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedButton(
                                            onClick = { showEditTransitaireDialog = true },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.cyanAccent),
                                            border = BorderStroke(1.dp, colors.cyanAccent.copy(alpha = 0.5f))
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Ajouter", fontFamily = PlusJakartaSans, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.innerCardBg)
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
                                                tint = colors.cyanAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Adresse entrepôt Chine",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = colors.textPrimary
                                            )
                                        }

                                        Text(
                                            text = warehouseAddress,
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            lineHeight = 17.sp,
                                            modifier = Modifier.padding(start = 24.dp)
                                        )

                                        Box(modifier = Modifier.padding(start = 24.dp, top = 2.dp)) {
                                            Button(
                                                onClick = {
                                                    copyToClipboard(
                                                        label = "Adresse Entrepôt Chine",
                                                        text = warehouseAddress,
                                                        feedback = "Adresse copiée dans le presse-papiers"
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colors.cyanSoftBg,
                                                    contentColor = colors.cyanAccent
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
                                                        text = "Copier l'adresse",
                                                        fontFamily = PlusJakartaSans,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (contactStr.isBlank() && wechatId.isBlank() && whatsappNum.isBlank()) {
                                Text(
                                    text = "Aucun contact renseigné",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (contactStr.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(colors.innerCardBg)
                                                .clickable {
                                                    copyToClipboard("Contact transitaire", contactStr, "Contact copié : $contactStr")
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(colors.cyanSoftBg),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ChatBubbleOutline,
                                                            contentDescription = null,
                                                            tint = colors.cyanAccent,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    Column {
                                                        Text(
                                                            text = "Contact / Téléphone / WeChat",
                                                            fontFamily = PlusJakartaSans,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 12.sp,
                                                            color = colors.textSecondary
                                                        )
                                                        Text(
                                                            text = contactStr,
                                                            fontFamily = PlusJakartaSans,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = colors.textPrimary
                                                        )
                                                    }
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copier le contact",
                                                    tint = colors.cyanAccent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (wechatId.isNotBlank() || whatsappNum.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (wechatId.isNotBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(colors.innerCardBg)
                                                        .clickable {
                                                            copyToClipboard("WeChat ID", wechatId, "WeChat ID copié : $wechatId")
                                                        }
                                                        .padding(10.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(Icons.Default.ChatBubbleOutline, null, tint = colors.cyanAccent, modifier = Modifier.size(18.dp))
                                                        Column {
                                                            Text("WeChat", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                                                            Text(wechatId, fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        }
                                                    }
                                                }
                                            }
                                            if (whatsappNum.isNotBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(colors.innerCardBg)
                                                        .clickable {
                                                            copyToClipboard("WhatsApp", whatsappNum, "WhatsApp copié : $whatsappNum")
                                                        }
                                                        .padding(10.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(Icons.Default.Call, null, tint = colors.cyanAccent, modifier = Modifier.size(18.dp))
                                                        Column {
                                                            Text("WhatsApp", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                                                            Text(whatsappNum, fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        }
                                                    }
                                                }
                                            }
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

    // MODAL: ENREGISTRER UN PAIEMENT
    if (showPaymentDialog) {
        var paymentAmountInput by remember { mutableStateOf(balanceDue.toString()) }

        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            containerColor = colors.cardBg,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            title = {
                Text(
                    text = "Enregistrer un règlement",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Solde restant dû : ${CurrencyFormatter.formatAriary(balanceDue)}",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )

                    OutlinedTextField(
                        value = paymentAmountInput,
                        onValueChange = { paymentAmountInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Montant payé (Ariary)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.inputBg,
                            unfocusedContainerColor = colors.inputBg,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.cyanAccent,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = colors.cyanAccent,
                            unfocusedLabelColor = colors.textSecondary,
                            cursorColor = colors.cyanAccent
                        ),
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
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cyanSolidBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans, color = colors.textSecondary)
                }
            }
        )
    }

    // MODAL: CONFIRMATION SUPPRESSION
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = colors.cardBg,
            titleContentColor = colors.dangerAccent,
            textContentColor = colors.textSecondary,
            title = {
                Text(
                    text = "Supprimer le transitaire ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.dangerAccent
                )
            },
            text = {
                Text(
                    text = "Voulez-vous supprimer définitivement ${transitaire.name} du catalogue ? Les achats passés associés conserveront leurs données d'historique.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete(transitaire.id)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Supprimer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans, color = colors.textSecondary)
                }
            }
        )
    }

    if (showEditTransitaireDialog) {
        AddTransitaireDialog(
            initialTransitaire = transitaire,
            onDismiss = { showEditTransitaireDialog = false },
            onConfirm = { name, hub, airRate, seaRate, contact, address, isActive, dateApp, airLead, seaLead, notes, airFastRate, airFastLead, goodsDelays ->
                onUpdateTransitaire(
                    transitaire.id,
                    name,
                    hub,
                    airRate,
                    seaRate,
                    contact,
                    address,
                    isActive,
                    dateApp,
                    airLead,
                    seaLead,
                    notes,
                    airFastRate,
                    airFastLead,
                    goodsDelays
                )
                showEditTransitaireDialog = false
            }
        )
    }
}
