package com.example.ui.business.transitaires

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.BusinessAccountEntity
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.DateUtils

@Composable
fun TransitaireDetailDialog(
    transitaire: CatalogueTransitaireSummaryUi,
    accounts: List<BusinessAccountEntity> = emptyList(),
    onDismiss: () -> Unit,
    onToggleArchived: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onUpdateRates: (id: Long, airRate: Long, seaRate: Long) -> Unit = { _, _, _ -> },
    onRecordPayment: (id: Long, amountAr: Long, accountName: String) -> Unit = { _, _, _ -> },
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
    val airMinPrice = airTariffs.minOfOrNull { it.priceAr } ?: if (currentAirRate > 0L) currentAirRate else 0L
    val airMaxPrice = airTariffs.maxOfOrNull { it.priceAr } ?: if (currentAirRate > 0L) currentAirRate else 0L
    val seaMinPrice = seaTariffs.minOfOrNull { it.priceAr } ?: if (currentSeaRate > 0L) currentSeaRate else 0L
    val seaMaxPrice = seaTariffs.maxOfOrNull { it.priceAr } ?: if (currentSeaRate > 0L) currentSeaRate else 0L

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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
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

                    TransitaireTariffGridSection(
                        tariffItems = parsedTariffs,
                        headerTitle = "Tarifs en vigueur",
                        headerBadge = when {
                            transitaire.rateApplicationDate.isNotBlank() -> "Depuis le ${transitaire.rateApplicationDate}"
                            parsedTariffs.isEmpty() -> "Aucun tarif"
                            else -> "Grille active"
                        },
                        defaultEffectiveDate = transitaire.rateApplicationDate,
                        onTariffItemsChanged = { updatedItems ->
                            val airItems = updatedItems.filter { it.mode == "AÉRIEN" }
                            val nonExpressAir = airItems.filterNot { it.shipmentType.contains("Express", ignoreCase = true) }
                            val newAirRate = TariffGridParser.matchTariffForMeasure(nonExpressAir, "AÉRIEN", 1.0)?.priceAr
                                ?: nonExpressAir.firstOrNull()?.priceAr
                                ?: airItems.firstOrNull()?.priceAr
                                ?: 0L
                            val newAirFastRate = airItems.firstOrNull { it.shipmentType.contains("Express", ignoreCase = true) }?.priceAr
                                ?: airItems.getOrNull(1)?.priceAr
                                ?: 0L
                            val newSeaRate = TariffGridParser.matchTariffForMeasure(updatedItems, "MARITIME", 1.0)?.priceAr
                                ?: updatedItems.firstOrNull { it.mode == "MARITIME" }?.priceAr
                                ?: 0L
                            val hasAnyRate = newAirRate > 0L || newAirFastRate > 0L || newSeaRate > 0L
                            val changedItemDate = updatedItems
                                .firstOrNull { updated ->
                                    val prev = parsedTariffs.firstOrNull { it.id == updated.id }
                                    (prev == null || prev.effectiveDate != updated.effectiveDate || prev.priceAr != updated.priceAr) &&
                                        DateUtils.isValidFrenchDate(updated.effectiveDate)
                                }?.effectiveDate
                            val latestValidItemDate = updatedItems
                                .mapNotNull { it.effectiveDate.takeIf { d -> DateUtils.isValidFrenchDate(d) } }
                                .maxByOrNull { DateUtils.frenchDateToEpochMillis(it) }
                            val effectiveDateStr = when {
                                !hasAnyRate -> ""
                                !changedItemDate.isNullOrBlank() -> changedItemDate
                                !latestValidItemDate.isNullOrBlank() -> latestValidItemDate
                                transitaire.rateApplicationDate.isNotBlank() -> transitaire.rateApplicationDate
                                else -> DateUtils.getTodayFrenchDate()
                            }

                            onUpdateTransitaire(
                                transitaire.id,
                                transitaire.name,
                                transitaire.routeHub,
                                newAirRate,
                                newSeaRate,
                                transitaire.contact,
                                transitaire.chinaWarehouseAddress,
                                !transitaire.isArchived,
                                effectiveDateStr,
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

                    TransitaireRateHistoryCard(
                        rateHistory = transitaire.rateHistory,
                        rateApplicationDate = transitaire.rateApplicationDate,
                        colors = colors
                    )

                    TransitaireDelaysCard(
                        transitaire = transitaire,
                        colors = colors,
                        onEditClick = { showEditTransitaireDialog = true }
                    )

                    TransitaireRealTransitStatsCard(
                        transitaire = transitaire,
                        colors = colors
                    )

                    TransitaireSynthesisCard(
                        airTariffsEmpty = airTariffs.isEmpty() && currentAirRate <= 0L,
                        seaTariffsEmpty = seaTariffs.isEmpty() && currentSeaRate <= 0L,
                        airMinPrice = airMinPrice,
                        airMaxPrice = airMaxPrice,
                        seaMinPrice = seaMinPrice,
                        seaMaxPrice = seaMaxPrice,
                        effectiveRateAr = transitaire.effectiveRateAr,
                        colors = colors
                    )

                    TransitaireInvoicesCard(
                        totalInvoiced = totalInvoiced,
                        paidAmount = paidAmount,
                        balanceDue = balanceDue,
                        settlementPercent = settlementPercent,
                        colors = colors,
                        onRecordPaymentClick = { showPaymentDialog = true }
                    )

                    TransitaireRecentShipmentsCard(
                        transitaireName = transitaire.name,
                        recentShipments = transitaire.recentShipments,
                        colors = colors
                    )

                    TransitaireWarehouseContactsCard(
                        warehouseAddress = warehouseAddress,
                        contactStr = contactStr,
                        wechatId = wechatId,
                        whatsappNum = whatsappNum,
                        colors = colors,
                        onEditClick = { showEditTransitaireDialog = true },
                        onCopy = ::copyToClipboard
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showPaymentDialog) {
        TransitairePaymentDialog(
            transitaireId = transitaire.id,
            balanceDue = balanceDue,
            accounts = accounts,
            colors = colors,
            onDismiss = { showPaymentDialog = false },
            onConfirmPayment = { id, amount, accountName ->
                onRecordPayment(id, amount, accountName)
                showPaymentDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        TransitaireDeleteConfirmDialog(
            transitaireName = transitaire.name,
            colors = colors,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirmDelete = {
                showDeleteConfirmDialog = false
                onDelete(transitaire.id)
                onDismiss()
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
