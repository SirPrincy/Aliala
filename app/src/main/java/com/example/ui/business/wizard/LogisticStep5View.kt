package com.example.ui.business.wizard

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.business.model.DomesticParcelStorage
import com.example.util.LotCostCalculator

@Composable
fun LogisticStep5View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    onUpdateOrder: (updatedOrder: BusinessOrderEntity) -> Unit = {},
    onAdvanceToQualityControl: (updatedOrder: BusinessOrderEntity) -> Unit,
    onPreviousStep: () -> Unit
) {
    val context = LocalContext.current

    val breakdown = remember(order, cmupRate) {
        LotCostCalculator.calculate(order, cmupRate)
    }
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
    var receivedParcelsCount by rememberSaveable {
        mutableIntStateOf(if (order.receivedParcelsCount > 0) order.receivedParcelsCount else expectedParcels)
    }
    var isPartialReceptionChecked by rememberSaveable {
        mutableStateOf(receivedParcelsCount < expectedParcels)
    }

    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var showIncidentDialog by remember { mutableStateOf(false) }
    var incidentDescription by rememberSaveable { mutableStateOf(order.incidentReport) }
    var showColis2DetailsDialog by remember { mutableStateOf(false) }

    val photoList = remember {
        mutableStateListOf("Ivato Hub #1" to "Reçu au tarmac cargo")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Step5StepperCard()

        Step5IvatoPointingCard(
            order = order,
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

        Step5UnloadingStateCard(
            order = order,
            parcels = parcels,
            receivedParcelsCount = receivedParcelsCount,
            expectedParcels = expectedParcels,
            totalQty = totalQty,
            weightKg = weightKg,
            onShowColisDetails = { showColis2DetailsDialog = true },
            onAddPhoto = { showAddPhotoDialog = true }
        )

        Step5SecurityAndInvoiceCard(
            carrier = order.carrier,
            freightCostAr = freightCostAr
        )

        Step5LiveCostCard(
            totalDisbursedAr = totalDisbursedAr,
            merchandiseTotalAr = merchandiseTotalAr,
            domesticPortAr = domesticPortAr,
            freightCostAr = freightCostAr
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
                val updated = order.copy(
                    status = OrderStatus.QUALITY_CONTROL.code,
                    receivedParcelsCount = receivedParcelsCount,
                    receivedUnitsCount = unitsReceived,
                    freightCostAr = freightCostAr,
                    incidentReport = if (incidentDescription.isNotBlank()) incidentDescription else order.incidentReport
                )
                onAdvanceToQualityControl(updated)
                Toast.makeText(
                    context,
                    "Arrivée validée ($unitsReceived/$totalQty unités) ! Passage au contrôle qualité.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onOpenIncidentDialog = { showIncidentDialog = true }
        )
    }

    Step5Dialogs(
        order = order,
        expectedParcels = expectedParcels,
        weightKg = weightKg,
        showAddPhotoDialog = showAddPhotoDialog,
        showIncidentDialog = showIncidentDialog,
        incidentDescription = incidentDescription,
        showColisDetailsDialog = showColis2DetailsDialog,
        onDismissAddPhoto = { showAddPhotoDialog = false },
        onConfirmAddPhoto = {
            photoList.add("Ivato Hub #${photoList.size + 1}" to "Photo certifiée")
            showAddPhotoDialog = false
            Toast.makeText(context, "Photo enregistrée !", Toast.LENGTH_SHORT).show()
        },
        onDismissIncident = { showIncidentDialog = false },
        onIncidentDescriptionChange = { incidentDescription = it },
        onConfirmIncident = {
            val updated = order.copy(
                incidentReport = if (incidentDescription.isNotBlank()) incidentDescription else order.incidentReport
            )
            onUpdateOrder(updated)
            showIncidentDialog = false
            Toast.makeText(context, "Signalement enregistré sur le bordereau d'Ivato.", Toast.LENGTH_SHORT).show()
        },
        onDismissColisDetails = { showColis2DetailsDialog = false }
    )
}
