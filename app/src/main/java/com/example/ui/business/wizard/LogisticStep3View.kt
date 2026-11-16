package com.example.ui.business.wizard

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.util.LotCostCalculator
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

    val defaultCarrierName = transitaires.firstOrNull()?.name ?: "Transitaire partenaire"
    val initialCarrier = order.carrier.ifBlank { defaultCarrierName }
    var selectedForwarder by rememberSaveable { mutableStateOf(initialCarrier) }
    var isAirFreight by rememberSaveable {
        mutableStateOf(
            !order.freightMode.equals("MARITIME", ignoreCase = true) &&
                !order.freightMode.equals("SEA", ignoreCase = true)
        )
    }

    val activeTransitaire = transitaires.find { it.name.equals(selectedForwarder, ignoreCase = true) }
        ?: transitaires.firstOrNull()

    val airRatePerKg = activeTransitaire?.airRateAr ?: 38_000L
    val seaRatePerM3 = activeTransitaire?.seaRateAr ?: 1_200_000L
    val warehouseLocation = activeTransitaire?.chinaWarehouseAddress?.takeIf { it.isNotBlank() }
        ?: activeTransitaire?.routeHub?.takeIf { it.isNotBlank() }
        ?: "Guangzhou, Hub International"

    var weightInput by rememberSaveable { mutableStateOf(order.weightKg?.let { String.format(Locale.US, "%.1f", it) } ?: "3.2") }
    var volumeInput by rememberSaveable { mutableStateOf(order.volumeM3?.let { String.format(Locale.US, "%.3f", it) } ?: "0.014") }
    var agentNotes by rememberSaveable {
        mutableStateOf(order.receptionNotes.ifBlank { "Cartons intacts et propres. Pesée conforme au bordereau d'expédition fournisseur." })
    }

    val breakdown = remember(order, cmupRate) {
        LotCostCalculator.calculate(order, cmupRate)
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

    val photoProofList = remember {
        mutableStateListOf(
            "Pesée certifiée (${String.format(Locale.US, "%.1f", weightDouble)} kg)" to "${String.format(Locale.US, "%.1f", weightDouble)}kg",
            "Bordereau scellé" to "OK"
        )
    }
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var showChangeForwarderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Step3StepperHeaderCard()

        Step3ForwarderModeCard(
            selectedForwarder = selectedForwarder,
            warehouseLocation = warehouseLocation,
            airRatePerKg = airRatePerKg,
            seaRatePerM3 = seaRatePerM3,
            isAirFreight = isAirFreight,
            onOpenChangeForwarder = { showChangeForwarderDialog = true },
            onSelectAirMode = { isAirFreight = it }
        )

        Step3WeighingAndProofsCard(
            orderDate = order.orderDate,
            isAirFreight = isAirFreight,
            weightInput = weightInput,
            onWeightChange = { weightInput = it },
            volumeInput = volumeInput,
            onVolumeChange = { volumeInput = it },
            weightDouble = weightDouble,
            volumeDouble = volumeDouble,
            upstreamCarrierLabel = upstreamCarrierLabel,
            agentNotes = agentNotes,
            onAgentNotesChange = { agentNotes = it },
            onOpenAddPhoto = { showAddPhotoDialog = true }
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
            weightDouble = weightDouble
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
                val updated = order.copy(
                    status = OrderStatus.FLIGHT.code,
                    weightKg = weightDouble,
                    volumeM3 = volumeDouble,
                    freightCostAr = calculatedFreightCostAr,
                    carrier = selectedForwarder,
                    freightMode = if (isAirFreight) FreightMode.AIR.name else FreightMode.SEA.name,
                    receptionNotes = agentNotes
                )
                onAdvanceToFlight(updated)
                Toast.makeText(context, "Pesée & réception validées ! Lot en transit.", Toast.LENGTH_SHORT).show()
            },
            onPreviousStep = onPreviousStep
        )
    }

    if (showAddPhotoDialog) {
        Step3AddPhotoDialog(
            onDismiss = { showAddPhotoDialog = false },
            onConfirmPhoto = {
                photoProofList.add("Preuve ajoutée" to "OK")
                showAddPhotoDialog = false
                Toast.makeText(context, "Photo de réception enregistrée !", Toast.LENGTH_SHORT).show()
            }
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
