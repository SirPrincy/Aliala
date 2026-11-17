package com.example.ui.business.wizard

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.DomesticParcelStorage
import com.example.ui.business.shared.MilestoneDateCard
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.util.LotCostCalculator
import com.example.util.TransitStatsCalculator

@Composable
fun LogisticStep5View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    onUpdateOrder: (updatedOrder: BusinessOrderEntity) -> Unit = {},
    onAdvanceToQualityControl: (updatedOrder: BusinessOrderEntity) -> Unit,
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
    val resolvedAirRate = if (order.freightMode.equals("EXPRESS", ignoreCase = true) && (selectedTransitaire?.airFastRateAr ?: 0L) > 0L) {
        selectedTransitaire?.airFastRateAr
    } else {
        selectedTransitaire?.airRateAr
    }

    var customsTaxInput by rememberSaveable(order.id) {
        mutableStateOf(if (order.customsTaxAr > 0L) order.customsTaxAr.toString() else "")
    }
    LaunchedEffect(order.id, order.customsTaxAr) {
        if (order.customsTaxAr > 0L && customsTaxInput.isBlank()) {
            customsTaxInput = order.customsTaxAr.toString()
        }
    }
    val customsTaxAr = customsTaxInput.filter { it.isDigit() }.toLongOrNull() ?: 0L

    val breakdown = remember(order, cmupRate, customsTaxAr, selectedTransitaire) {
        LotCostCalculator.calculate(
            order = order,
            cmupRate = cmupRate,
            customsTaxArInput = customsTaxAr,
            transitaireAirRateAr = resolvedAirRate,
            transitaireSeaRateAr = selectedTransitaire?.seaRateAr
        )
    }
    val isMaritime = breakdown.isMaritime
    val arrivalPortName = if (isMaritime) "Port de Toamasina" else "Aéroport d'Ivato"
    val arrivalShortName = if (isMaritime) "Toamasina" else "Ivato"
    val totalQty = breakdown.quantity
    val merchandiseTotalAr = breakdown.merchandiseTotalAr
    val domesticPortAr = breakdown.domesticPortAr
    val freightCostAr = breakdown.freightCostAr
    val totalDisbursedAr = breakdown.totalDisbursedAr
    val weightKg = order.weightKg ?: 0.0

    val parcels = remember(order.trackingNumbers, order.parcelDetailsJson) {
        DomesticParcelStorage.parse(order)
    }
    val expectedParcels = if (parcels.isNotEmpty()) parcels.size else 1
    var receivedParcelsCount by rememberSaveable(order.id) {
        mutableIntStateOf(if (order.receivedParcelsCount > 0) order.receivedParcelsCount else expectedParcels)
    }
    var isPartialReceptionChecked by rememberSaveable(order.id) {
        mutableStateOf(receivedParcelsCount < expectedParcels)
    }

    var showIncidentDialog by remember { mutableStateOf(false) }
    var incidentDescription by rememberSaveable(order.id) { mutableStateOf(order.incidentReport) }
    var showColisDetailsDialog by remember { mutableStateOf(false) }

    var madaArrivalDateMillis by rememberSaveable(order.id) {
        mutableStateOf(order.madaArrivalDate)
    }

    val transitDurationLabel = remember(order.orderDate, order.madaDispatchDate, madaArrivalDateMillis) {
        val fretDays = TransitStatsCalculator.daysBetween(order.madaDispatchDate, madaArrivalDateMillis)
        val totalDays = TransitStatsCalculator.daysBetween(order.orderDate, madaArrivalDateMillis)
        when {
            fretDays != null && totalDays != null -> "${TransitStatsCalculator.formatDaysExact(fretDays)} fret · ${TransitStatsCalculator.formatDaysExact(totalDays)} total"
            fretDays != null -> "${TransitStatsCalculator.formatDaysExact(fretDays)} de trajet"
            totalDays != null -> "${TransitStatsCalculator.formatDaysExact(totalDays)} total"
            else -> null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Step5StepperCard(isMaritime = isMaritime)

        Step5ArrivalPointingCard(
            order = order,
            isMaritime = isMaritime,
            arrivalShortName = arrivalShortName,
            arrivalPortName = arrivalPortName,
            madaArrivalDateMillis = madaArrivalDateMillis,
            receivedParcelsCount = receivedParcelsCount,
            expectedParcels = expectedParcels,
            isPartialReceptionChecked = isPartialReceptionChecked,
            onDecrementParcels = {
                if (receivedParcelsCount > 0) {
                    receivedParcelsCount--
                    isPartialReceptionChecked = (receivedParcelsCount < expectedParcels)
                }
            },
            onIncrementParcels = {
                if (receivedParcelsCount < expectedParcels) {
                    receivedParcelsCount++
                    isPartialReceptionChecked = (receivedParcelsCount < expectedParcels)
                }
            },
            onTogglePartialReception = { checked ->
                isPartialReceptionChecked = checked
                if (checked && receivedParcelsCount == expectedParcels) {
                    receivedParcelsCount = 1
                } else if (!checked && receivedParcelsCount < expectedParcels) {
                    receivedParcelsCount = expectedParcels
                }
            }
        )

        MilestoneDateCard(
            title = "Date d'arrivée à Madagascar",
            subtitle = "Date réelle de réception ($arrivalShortName)",
            currentDateMillis = madaArrivalDateMillis,
            referenceEarlierMillis = order.madaDispatchDate ?: order.warehouseArrivalDate ?: order.orderDate,
            referenceLabel = when {
                order.madaDispatchDate != null -> "l'envoi vers Madagascar"
                order.warehouseArrivalDate != null -> "l'arrivée au dépôt Chine"
                else -> "la date d'achat"
            },
            durationLabel = transitDurationLabel,
            colors = colors,
            testTagPrefix = "step5_mada_arrival_date",
            onDateSelected = { newDate ->
                madaArrivalDateMillis = newDate
                onUpdateOrder(order.copy(madaArrivalDate = newDate))
            }
        )

        Step5UnloadingStateCard(
            order = order,
            isMaritime = isMaritime,
            parcels = parcels,
            receivedParcelsCount = receivedParcelsCount,
            expectedParcels = expectedParcels,
            totalQty = totalQty,
            weightKg = weightKg,
            onShowColisDetails = { showColisDetailsDialog = true }
        )

        Step5SecurityAndInvoiceCard(
            carrier = order.carrier,
            freightCostAr = freightCostAr,
            isFreightPaid = order.isFreightPaid
        )

        Step5CustomsTaxCard(
            customsTaxInput = customsTaxInput,
            onCustomsTaxInputChange = { customsTaxInput = it },
            customsTaxAr = customsTaxAr,
            savedCustomsTaxAr = order.customsTaxAr,
            onSaveCustomsTax = {
                onUpdateOrder(order.copy(customsTaxAr = customsTaxAr))
                Toast.makeText(context, "Frais de douane enregistrés.", Toast.LENGTH_SHORT).show()
            }
        )

        Step5LiveCostCard(
            arrivalShortName = arrivalShortName,
            totalDisbursedAr = totalDisbursedAr,
            merchandiseTotalAr = merchandiseTotalAr,
            domesticPortAr = domesticPortAr,
            freightCostAr = freightCostAr,
            customsTaxAr = customsTaxAr
        )

        Step5BottomActionsCard(
            receivedParcelsCount = receivedParcelsCount,
            expectedParcels = expectedParcels,
            onValidateArrival = {
                val unitsReceived = if (isPartialReceptionChecked && receivedParcelsCount < expectedParcels) {
                    (totalQty * receivedParcelsCount / expectedParcels).coerceAtLeast(1)
                } else {
                    totalQty
                }
                val resolvedMadaArrival = madaArrivalDateMillis ?: order.madaArrivalDate ?: System.currentTimeMillis()
                val updated = order.copy(
                    status = OrderStatus.QUALITY_CONTROL.code,
                    receivedParcelsCount = receivedParcelsCount,
                    receivedUnitsCount = unitsReceived,
                    freightCostAr = freightCostAr,
                    customsTaxAr = customsTaxAr,
                    madaArrivalDate = resolvedMadaArrival,
                    incidentReport = if (incidentDescription.isNotBlank()) incidentDescription else order.incidentReport
                )
                onAdvanceToQualityControl(updated)
                Toast.makeText(
                    context,
                    "Arrivée validée ($unitsReceived/$totalQty unités) ! Passage au contrôle qualité.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onOpenIncidentDialog = { showIncidentDialog = true },
            onPreviousStep = onPreviousStep
        )
    }

    Step5Dialogs(
        order = order,
        isMaritime = isMaritime,
        arrivalPortName = arrivalPortName,
        expectedParcels = expectedParcels,
        weightKg = weightKg,
        showIncidentDialog = showIncidentDialog,
        incidentDescription = incidentDescription,
        showColisDetailsDialog = showColisDetailsDialog,
        onDismissIncident = { showIncidentDialog = false },
        onIncidentDescriptionChange = { incidentDescription = it },
        onConfirmIncident = {
            val updated = order.copy(
                incidentReport = incidentDescription.trim()
            )
            onUpdateOrder(updated)
            showIncidentDialog = false
            Toast.makeText(context, "Signalement enregistré ($arrivalShortName).", Toast.LENGTH_SHORT).show()
        },
        onDismissColisDetails = { showColisDetailsDialog = false }
    )
}
