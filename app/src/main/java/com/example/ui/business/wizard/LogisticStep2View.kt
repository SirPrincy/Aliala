package com.example.ui.business.wizard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.business.CatalogueFamilySummaryUi
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.DomesticParcelItem
import com.example.ui.business.model.DomesticParcelStorage
import com.example.ui.business.shared.MilestoneDateCard
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.util.LotCostCalculator
import com.example.util.TransitStatsCalculator

@Composable
fun LogisticStep2View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    families: List<CatalogueFamilySummaryUi> = emptyList(),
    onUpdateOrder: (BusinessOrderEntity) -> Unit = {},
    onAdvanceToWeighing: (updatedOrder: BusinessOrderEntity) -> Unit,
    onPreviousStep: () -> Unit
) {
    val context = LocalContext.current
    val colors = rememberTransitaireColors()

    val selectedTransitaire = remember(transitaires, order.carrier) {
        if (order.carrier.isBlank()) null
        else transitaires.firstOrNull {
            it.name.equals(order.carrier, ignoreCase = true) ||
                order.carrier.contains(it.name, ignoreCase = true)
        }
    }

    val matchedFamily = remember(families, order.familyId, order.family) {
        families.firstOrNull {
            (order.familyId > 0L && it.id == order.familyId) ||
                it.name.equals(order.family, ignoreCase = true)
        }
    }
    val familyEmoji = matchedFamily?.emoji?.takeIf { it.isNotBlank() } ?: when {
        order.family.contains("MODE", ignoreCase = true) -> "🎒"
        order.family.contains("MAISON", ignoreCase = true) -> "🏠"
        order.family.contains("MOBILE", ignoreCase = true) || order.family.contains("TÉLÉPHONE", ignoreCase = true) -> "📱"
        order.family.contains("ÉLECTRON", ignoreCase = true) || order.family.contains("AUDIO", ignoreCase = true) -> "🎧"
        else -> "📦"
    }

    val breakdown = remember(order, cmupRate, selectedTransitaire) {
        LotCostCalculator.calculate(
            order = order,
            cmupRate = cmupRate,
            transitaireAirRateAr = selectedTransitaire?.airRateAr,
            transitaireSeaRateAr = selectedTransitaire?.seaRateAr
        )
    }
    val totalQty = breakdown.quantity
    val unitPriceYuan = breakdown.unitPriceYuan
    val totalOrderYuan = (totalQty * unitPriceYuan) + order.domesticShippingYuan.coerceAtLeast(0.0)
    val totalOrderAr = breakdown.merchandiseTotalAr + breakdown.domesticPortAr
    val isMaritime = breakdown.isMaritime

    val parcels = remember(
        order.id,
        order.trackingNumbers,
        order.deliveredTrackingNumbers,
        order.parcelDetailsJson,
        order.variants,
        order.product,
        order.quantityBought,
        order.orderDate
    ) {
        DomesticParcelStorage.parse(order)
    }

    val deliveredParcelsCount = parcels.count { it.isDelivered }
    val allParcelsDelivered = parcels.isNotEmpty() && deliveredParcelsCount == parcels.size

    var showAddParcelDialog by remember { mutableStateOf(false) }
    var editingParcel by remember { mutableStateOf<DomesticParcelItem?>(null) }
    var parcelToDelete by remember { mutableStateOf<DomesticParcelItem?>(null) }
    var showSelectTransitaireDialog by remember { mutableStateOf(false) }
    var showEditEtaDialog by remember { mutableStateOf(false) }
    var showPartialProceedConfirm by remember { mutableStateOf(false) }
    var warehouseArrivalDateMillis by androidx.compose.runtime.saveable.rememberSaveable(order.id) {
        mutableStateOf(order.warehouseArrivalDate)
    }

    val chinaDurationLabel = remember(order.orderDate, warehouseArrivalDateMillis) {
        TransitStatsCalculator.daysBetween(order.orderDate, warehouseArrivalDateMillis)
            ?.let { "${TransitStatsCalculator.formatDaysExact(it)} depuis achat" }
    }

    fun copyToClipboard(label: String, text: String, toastMsg: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
    }

    fun persistParcels(updatedParcels: List<DomesticParcelItem>) {
        onUpdateOrder(DomesticParcelStorage.applyToOrder(order, updatedParcels))
    }

    val destinationLabel = when {
        selectedTransitaire != null && selectedTransitaire.routeHub.isNotBlank() ->
            "${selectedTransitaire.name} (${selectedTransitaire.routeHub})"
        selectedTransitaire != null -> selectedTransitaire.name
        order.carrier.isNotBlank() -> order.carrier
        else -> "l'entrepôt transitaire"
    }

    val statusBannerText = when {
        parcels.isEmpty() -> "En attente des numéros de suivi vers $destinationLabel"
        allParcelsDelivered -> "Tous les colis (${parcels.size}/${parcels.size}) réceptionnés chez $destinationLabel"
        deliveredParcelsCount > 0 -> "$deliveredParcelsCount/${parcels.size} colis reçus chez $destinationLabel"
        else -> "En livraison vers $destinationLabel"
    }

    val markAllDeliveredAction: () -> Unit = {
        val now = System.currentTimeMillis()
        val updated = parcels.map {
            it.copy(
                isDelivered = true,
                checkpointTitle = "Arrivé à l'entrepôt transitaire",
                updatedAtMillis = now
            )
        }
        persistParcels(updated)
        Toast.makeText(context, "Tous les colis marqués comme réceptionnés", Toast.LENGTH_SHORT).show()
    }

    val toggleParcelAction: (Int, DomesticParcelItem) -> Unit = { index, parcel ->
        val now = System.currentTimeMillis()
        val nextDelivered = !parcel.isDelivered
        val updatedList = parcels.toMutableList()
        updatedList[index] = parcel.copy(
            isDelivered = nextDelivered,
            checkpointTitle = if (nextDelivered) "Arrivé à l'entrepôt transitaire" else "En cours d'acheminement",
            updatedAtMillis = now
        )
        persistParcels(updatedList)
        val statusMsg = if (nextDelivered) {
            "Colis ${parcel.trackingNumber} marqué réceptionné !"
        } else {
            "Colis ${parcel.trackingNumber} remis en acheminement"
        }
        Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
    }

    val advanceAction: () -> Unit = {
        if (deliveredParcelsCount < parcels.size) {
            showPartialProceedConfirm = true
        } else {
            val resolvedWarehouseDate = warehouseArrivalDateMillis ?: order.warehouseArrivalDate ?: System.currentTimeMillis()
            val syncedOrder = DomesticParcelStorage.applyToOrder(order, parcels).copy(
                status = OrderStatus.GUANGZHOU.code,
                warehouseArrivalDate = resolvedWarehouseDate
            )
            onAdvanceToWeighing(syncedOrder)
            Toast.makeText(
                context,
                "$deliveredParcelsCount colis réceptionné(s) ! Passage à la pesée.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        val isWideLayout = maxWidth >= 680.dp
        val isCompactPhone = maxWidth < 340.dp

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WizardStepHeaderCard(
                stepNumber = 2,
                stepTitle = "Vers l'entrepôt transitaire",
                statusText = statusBannerText,
                statusIcon = Icons.Default.LocalShipping,
                statusAccentColor = if (allParcelsDelivered) colors.successAccent else colors.cyanAccent,
                statusContainerColor = if (allParcelsDelivered) colors.successSoftBg else colors.cyanSoftBg
            )

            if (isWideLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Step2LotSummaryCard(
                            order = order,
                            familyEmoji = familyEmoji,
                            totalQty = totalQty,
                            unitPriceYuan = unitPriceYuan,
                            totalOrderYuan = totalOrderYuan,
                            totalOrderAr = totalOrderAr,
                            isMaritime = isMaritime,
                            selectedTransitaire = selectedTransitaire,
                            colors = colors,
                            onSelectTransitaire = { showSelectTransitaireDialog = true },
                            onEditEta = { showEditEtaDialog = true }
                        )
                        Step2ChinaDropPointCard(
                            order = order,
                            selectedTransitaire = selectedTransitaire,
                            colors = colors,
                            onSelectTransitaire = { showSelectTransitaireDialog = true },
                            onCopyText = ::copyToClipboard
                        )
                        MilestoneDateCard(
                            title = "Date d'arrivée au dépôt Chine",
                            subtitle = "Date réelle de réception chez le transitaire",
                            currentDateMillis = warehouseArrivalDateMillis,
                            referenceEarlierMillis = order.orderDate,
                            referenceLabel = "la date d'achat",
                            durationLabel = chinaDurationLabel,
                            colors = colors,
                            testTagPrefix = "step2_warehouse_date",
                            onDateSelected = { newDate ->
                                warehouseArrivalDateMillis = newDate
                                onUpdateOrder(order.copy(warehouseArrivalDate = newDate))
                            }
                        )
                        Step2BottomActionsCard(
                            parcelsCount = parcels.size,
                            deliveredParcelsCount = deliveredParcelsCount,
                            colors = colors,
                            onAdvanceClick = advanceAction,
                            onPreviousStep = onPreviousStep
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Step2ParcelsTrackingSection(
                            parcels = parcels,
                            deliveredParcelsCount = deliveredParcelsCount,
                            allParcelsDelivered = allParcelsDelivered,
                            isCompactPhone = false,
                            colors = colors,
                            onMarkAllDelivered = markAllDeliveredAction,
                            onCopyTracking = { tr -> copyToClipboard("Numéro de suivi", tr, "Numéro $tr copié") },
                            onDeleteParcel = { parcelToDelete = it },
                            onEditParcel = { editingParcel = it },
                            onToggleParcelDelivered = toggleParcelAction,
                            onAddParcel = { showAddParcelDialog = true }
                        )
                    }
                }
            } else {
                Step2LotSummaryCard(
                    order = order,
                    familyEmoji = familyEmoji,
                    totalQty = totalQty,
                    unitPriceYuan = unitPriceYuan,
                    totalOrderYuan = totalOrderYuan,
                    totalOrderAr = totalOrderAr,
                    isMaritime = isMaritime,
                    selectedTransitaire = selectedTransitaire,
                    colors = colors,
                    onSelectTransitaire = { showSelectTransitaireDialog = true },
                    onEditEta = { showEditEtaDialog = true }
                )
                Step2ParcelsTrackingSection(
                    parcels = parcels,
                    deliveredParcelsCount = deliveredParcelsCount,
                    allParcelsDelivered = allParcelsDelivered,
                    isCompactPhone = isCompactPhone,
                    colors = colors,
                    onMarkAllDelivered = markAllDeliveredAction,
                    onCopyTracking = { tr -> copyToClipboard("Numéro de suivi", tr, "Numéro $tr copié") },
                    onDeleteParcel = { parcelToDelete = it },
                    onEditParcel = { editingParcel = it },
                    onToggleParcelDelivered = toggleParcelAction,
                    onAddParcel = { showAddParcelDialog = true }
                )
                Step2ChinaDropPointCard(
                    order = order,
                    selectedTransitaire = selectedTransitaire,
                    colors = colors,
                    onSelectTransitaire = { showSelectTransitaireDialog = true },
                    onCopyText = ::copyToClipboard
                )
                MilestoneDateCard(
                    title = "Date d'arrivée au dépôt Chine",
                    subtitle = "Date réelle de réception chez le transitaire",
                    currentDateMillis = warehouseArrivalDateMillis,
                    referenceEarlierMillis = order.orderDate,
                    referenceLabel = "la date d'achat",
                    durationLabel = chinaDurationLabel,
                    colors = colors,
                    testTagPrefix = "step2_warehouse_date",
                    onDateSelected = { newDate ->
                        warehouseArrivalDateMillis = newDate
                        onUpdateOrder(order.copy(warehouseArrivalDate = newDate))
                    }
                )
                Step2BottomActionsCard(
                    parcelsCount = parcels.size,
                    deliveredParcelsCount = deliveredParcelsCount,
                    colors = colors,
                    onAdvanceClick = advanceAction,
                    onPreviousStep = onPreviousStep
                )
            }
        }
    }

    if (showAddParcelDialog) {
        Step2AddParcelDialog(
            order = order,
            totalQty = totalQty,
            parcels = parcels,
            colors = colors,
            onDismiss = { showAddParcelDialog = false },
            onAdd = { newParcel ->
                persistParcels(parcels + newParcel)
                showAddParcelDialog = false
                Toast.makeText(context, "Colis ${newParcel.trackingNumber} ajouté", Toast.LENGTH_SHORT).show()
            }
        )
    }

    editingParcel?.let { parcel ->
        Step2EditParcelDialog(
            parcel = parcel,
            parcels = parcels,
            colors = colors,
            onDismiss = { editingParcel = null },
            onSave = { updatedItem ->
                val updatedParcels = parcels.map { if (it.trackingNumber == parcel.trackingNumber) updatedItem else it }
                persistParcels(updatedParcels)
                editingParcel = null
                Toast.makeText(context, "Suivi mis à jour", Toast.LENGTH_SHORT).show()
            }
        )
    }

    parcelToDelete?.let { target ->
        Step2DeleteParcelDialog(
            target = target,
            colors = colors,
            onDismiss = { parcelToDelete = null },
            onConfirmDelete = {
                persistParcels(parcels.filterNot { it.trackingNumber == target.trackingNumber })
                parcelToDelete = null
                Toast.makeText(context, "Colis supprimé", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showSelectTransitaireDialog) {
        WizardSelectTransitaireDialog(
            order = order,
            transitaires = transitaires,
            onDismiss = { showSelectTransitaireDialog = false },
            onApply = { updated ->
                onUpdateOrder(updated)
                showSelectTransitaireDialog = false
            }
        )
    }

    if (showEditEtaDialog) {
        Step2EditEtaDialog(
            currentEstimatedArrival = order.estimatedArrival,
            colors = colors,
            onDismiss = { showEditEtaDialog = false },
            onApplyNewEta = { newEta ->
                onUpdateOrder(order.copy(estimatedArrival = newEta))
                showEditEtaDialog = false
                Toast.makeText(context, "Date d'arrivée mise à jour", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showPartialProceedConfirm) {
        Step2PartialProceedDialog(
            deliveredParcelsCount = deliveredParcelsCount,
            totalParcelsCount = parcels.size,
            colors = colors,
            onDismiss = { showPartialProceedConfirm = false },
            onConfirmProceed = {
                showPartialProceedConfirm = false
                val resolvedWarehouseDate = warehouseArrivalDateMillis ?: order.warehouseArrivalDate ?: System.currentTimeMillis()
                val syncedOrder = DomesticParcelStorage.applyToOrder(order, parcels).copy(
                    status = OrderStatus.GUANGZHOU.code,
                    warehouseArrivalDate = resolvedWarehouseDate
                )
                onAdvanceToWeighing(syncedOrder)
                Toast.makeText(
                    context,
                    "$deliveredParcelsCount colis réceptionné(s) ! Passage à la pesée.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}
