package com.example.ui.business.wizard

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.FreightMode
import com.example.data.model.OrderStatus
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.DomesticParcelStorage
import com.example.ui.business.shared.MilestoneDateCard
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.util.LotCostCalculator
import com.example.util.TransitStatsCalculator
import java.util.Locale

@Composable
fun LogisticStep3View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    onUpdateOrder: (updatedOrder: BusinessOrderEntity) -> Unit = {},
    onAdvanceToFlight: (updatedOrder: BusinessOrderEntity) -> Unit,
    onPreviousStep: () -> Unit
) {
    val context = LocalContext.current

    val parcels = remember(order.trackingNumbers, order.parcelDetailsJson) {
        DomesticParcelStorage.parse(order)
    }
    val upstreamCarrierLabel = parcels.firstOrNull()?.carrierName?.takeIf { it.isNotBlank() }
        ?: "Transporteur domestique Chine"

    val defaultCarrierName = transitaires.firstOrNull()?.name ?: ""
    val initialCarrier = order.carrier.ifBlank { defaultCarrierName }
    var selectedForwarder by rememberSaveable(order.id) { mutableStateOf(initialCarrier) }
    var isAirFreight by rememberSaveable(order.id) {
        mutableStateOf(
            !order.freightMode.equals("MARITIME", ignoreCase = true) &&
                !order.freightMode.equals("SEA", ignoreCase = true)
        )
    }

    androidx.compose.runtime.LaunchedEffect(order.id, order.carrier, order.freightMode) {
        if (order.carrier.isNotBlank()) {
            selectedForwarder = order.carrier
        }
        isAirFreight = !order.freightMode.equals("MARITIME", ignoreCase = true) &&
            !order.freightMode.equals("SEA", ignoreCase = true)
    }

    val activeTransitaire = transitaires.find {
        selectedForwarder.isNotBlank() && it.name.equals(selectedForwarder, ignoreCase = true)
    } ?: transitaires.firstOrNull()

    val airRatePerKg = activeTransitaire?.airRateAr ?: 0L
    val seaRatePerM3 = activeTransitaire?.seaRateAr ?: 0L
    val warehouseLocation = activeTransitaire?.chinaWarehouseAddress?.takeIf { it.isNotBlank() }
        ?: activeTransitaire?.routeHub?.takeIf { it.isNotBlank() }
        ?: "Entrepôt non renseigné"

    var weightInput by rememberSaveable(order.id) {
        mutableStateOf(order.weightKg?.takeIf { it > 0.0 }?.let { String.format(Locale.US, "%.1f", it) } ?: "")
    }
    var volumeInput by rememberSaveable(order.id) {
        mutableStateOf(order.volumeM3?.takeIf { it > 0.0 }?.let { String.format(Locale.FRENCH, "%.5f", it) } ?: "")
    }
    var agentNotes by rememberSaveable(order.id) {
        mutableStateOf(order.receptionNotes)
    }

    val breakdown = remember(order, cmupRate, airRatePerKg, seaRatePerM3) {
        LotCostCalculator.calculate(
            order = order,
            cmupRate = cmupRate,
            transitaireAirRateAr = airRatePerKg,
            transitaireSeaRateAr = seaRatePerM3
        )
    }
    val totalQty = breakdown.quantity
    val unitPriceYuan = breakdown.unitPriceYuan
    val merchandiseTotalAr = breakdown.merchandiseTotalAr
    val domesticPortAr = breakdown.domesticPortAr

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
        String.format(Locale.US, "%.1f", margin)
    } else {
        "—"
    }

    var isBudgetExpanded by remember { mutableStateOf(true) }
    var showChangeForwarderDialog by remember { mutableStateOf(false) }
    val colors = rememberTransitaireColors()

    var warehouseArrivalDateMillis by rememberSaveable(order.id) {
        mutableStateOf(order.warehouseArrivalDate)
    }
    var madaDispatchDateMillis by rememberSaveable(order.id) {
        mutableStateOf(order.madaDispatchDate)
    }

    val chinaDurationLabel = remember(order.orderDate, warehouseArrivalDateMillis) {
        TransitStatsCalculator.daysBetween(order.orderDate, warehouseArrivalDateMillis)
            ?.let { "${TransitStatsCalculator.formatDaysExact(it)} en Chine" }
    }
    val hubWaitDurationLabel = remember(warehouseArrivalDateMillis, madaDispatchDateMillis) {
        TransitStatsCalculator.daysBetween(warehouseArrivalDateMillis, madaDispatchDateMillis)
            ?.let { "${TransitStatsCalculator.formatDaysExact(it)} au Hub" }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Step3StepperHeaderCard()

        Step3ForwarderModeCard(
            selectedForwarder = selectedForwarder.ifBlank { "Aucun transitaire" },
            warehouseLocation = warehouseLocation,
            airRatePerKg = airRatePerKg,
            seaRatePerM3 = seaRatePerM3,
            isAirFreight = isAirFreight,
            onOpenChangeForwarder = { showChangeForwarderDialog = true },
            onSelectAirMode = { isAirFreight = it }
        )

        Step3WeighingAndProofsCard(
            warehouseArrivalDate = warehouseArrivalDateMillis,
            isAirFreight = isAirFreight,
            weightInput = weightInput,
            onWeightChange = { weightInput = it },
            volumeInput = volumeInput,
            onVolumeChange = { volumeInput = it },
            weightDouble = weightDouble,
            volumeDouble = volumeDouble,
            upstreamCarrierLabel = upstreamCarrierLabel,
            agentNotes = agentNotes,
            onAgentNotesChange = { agentNotes = it }
        )

        MilestoneDateCard(
            title = "Date d'arrivée au dépôt Chine",
            subtitle = "Date réelle d'entrée à l'entrepôt transitaire",
            currentDateMillis = warehouseArrivalDateMillis,
            referenceEarlierMillis = order.orderDate,
            referenceLabel = "la date d'achat",
            durationLabel = chinaDurationLabel,
            colors = colors,
            testTagPrefix = "step3_warehouse_date",
            onDateSelected = { newDate ->
                warehouseArrivalDateMillis = newDate
                onUpdateOrder(order.copy(warehouseArrivalDate = newDate))
            }
        )

        MilestoneDateCard(
            title = "Date d'envoi vers Madagascar",
            subtitle = "Date de départ du Hub Chine (vol ou bateau)",
            currentDateMillis = madaDispatchDateMillis,
            referenceEarlierMillis = warehouseArrivalDateMillis ?: order.orderDate,
            referenceLabel = if (warehouseArrivalDateMillis != null) "l'arrivée au dépôt" else "la date d'achat",
            durationLabel = hubWaitDurationLabel,
            colors = colors,
            testTagPrefix = "step3_dispatch_date",
            onDateSelected = { newDate ->
                madaDispatchDateMillis = newDate
                onUpdateOrder(order.copy(madaDispatchDate = newDate))
            }
        )

        Step3FreightCalculationCard(
            isAirFreight = isAirFreight,
            airRatePerKg = airRatePerKg,
            seaRatePerM3 = seaRatePerM3,
            weightDouble = weightDouble,
            volumeDouble = volumeDouble,
            calculatedFreightCostAr = calculatedFreightCostAr
        )

        Step3MutualisationCard(
            order = order,
            totalQty = totalQty,
            isAirFreight = isAirFreight,
            weightDouble = weightDouble,
            volumeDouble = volumeDouble
        )

        Step3BudgetSynthesisCard(
            isBudgetExpanded = isBudgetExpanded,
            onToggleExpanded = { isBudgetExpanded = !isBudgetExpanded },
            totalCostOfGoodsAr = totalCostOfGoodsAr,
            totalQty = totalQty,
            unitPriceYuan = unitPriceYuan,
            merchandiseTotalAr = merchandiseTotalAr,
            domesticShippingYuan = order.domesticShippingYuan,
            domesticPortAr = domesticPortAr,
            isAirFreight = isAirFreight,
            selectedForwarder = selectedForwarder,
            weightDouble = weightDouble,
            volumeDouble = volumeDouble,
            calculatedFreightCostAr = calculatedFreightCostAr,
            unitCostAr = unitCostAr,
            projectedGrossMarginPercent = projectedGrossMarginPercent
        )

        Step3BottomActionsCard(
            isAirFreight = isAirFreight,
            isInputValid = isInputValid,
            onValidateArrivalClick = {
                if (!isInputValid) {
                    val msg = if (isAirFreight) "Veuillez saisir un poids valide (> 0 kg)" else "Veuillez saisir un volume valide (> 0 m³)"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    return@Step3BottomActionsCard
                }
                val resolvedDispatchDate = madaDispatchDateMillis ?: order.madaDispatchDate ?: System.currentTimeMillis()
                val updated = order.copy(
                    status = OrderStatus.FLIGHT.code,
                    weightKg = weightDouble.takeIf { it > 0.0 },
                    volumeM3 = volumeDouble.takeIf { it > 0.0 },
                    freightCostAr = calculatedFreightCostAr,
                    carrier = selectedForwarder,
                    freightMode = if (isAirFreight) FreightMode.AIR.name else FreightMode.SEA.name,
                    receptionNotes = agentNotes.trim(),
                    warehouseArrivalDate = warehouseArrivalDateMillis ?: order.warehouseArrivalDate,
                    madaDispatchDate = resolvedDispatchDate
                )
                onAdvanceToFlight(updated)
                Toast.makeText(context, "Pesée & réception validées ! Lot en transit.", Toast.LENGTH_SHORT).show()
            },
            onPreviousStep = onPreviousStep
        )
    }

    if (showChangeForwarderDialog) {
        Step3ChangeForwarderDialog(
            transitaires = transitaires,
            selectedForwarder = selectedForwarder,
            isAirFreight = isAirFreight,
            onDismiss = { showChangeForwarderDialog = false },
            onSelectForwarder = { fwd ->
                selectedForwarder = fwd.name
                val newCost = if (isAirFreight) {
                    (weightDouble * fwd.airRateAr).toLong()
                } else {
                    (volumeDouble * fwd.seaRateAr).toLong()
                }
                onUpdateOrder(order.copy(carrier = fwd.name, freightCostAr = newCost))
                showChangeForwarderDialog = false
                Toast.makeText(context, "Transitaire sélectionné : ${fwd.name}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
