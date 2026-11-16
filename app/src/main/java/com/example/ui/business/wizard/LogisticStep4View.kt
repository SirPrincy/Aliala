package com.example.ui.business.wizard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.business.model.DomesticParcelStorage
import com.example.util.LotCostCalculator

@Composable
fun LogisticStep4View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    onUpdateOrder: (updatedOrder: BusinessOrderEntity) -> Unit = {},
    onAdvanceToCustoms: (updatedOrder: BusinessOrderEntity) -> Unit,
    onPreviousStep: () -> Unit
) {
    val context = LocalContext.current

    val breakdown = remember(order, cmupRate) {
        LotCostCalculator.calculate(order, cmupRate)
    }
    val totalQty = breakdown.quantity
    val unitPriceYuan = breakdown.unitPriceYuan
    val merchandiseTotalAr = breakdown.merchandiseTotalAr
    val domesticPortAr = breakdown.domesticPortAr
    val isMaritime = breakdown.isMaritime
    val weightKg = order.weightKg ?: 0.0
    val freightCostAr = breakdown.freightCostAr
    val totalCostOfGoodsAr = breakdown.totalDisbursedAr
    val unitCostAr = breakdown.unitCostAr

    val modeTitle = if (isMaritime) "En navire cargo vers Madagascar" else "En vol cargo vers Madagascar"
    val modeSubtitle = if (isMaritime) "Fret maritime en transit" else "Vol cargo en transit"
    val modeBadge = if (isMaritime) "En transit maritime" else "En vol international"
    val arrivalPortName = if (isMaritime) "TOA" else "TNR"
    val arrivalPortCity = if (isMaritime) "Toamasina" else "Antananarivo"
    val carrierHeader = if (isMaritime) "Fret Maritime Groupage" else "Fret Aérien Express"

    val carrierName = order.carrier.ifBlank { "Transitaire partenaire" }
    val awbNumber = if (order.referenceCode.isNotBlank()) "AWB-${order.referenceCode}" else "AWB-LOT-${order.id}"
    val parcels = remember(order.trackingNumbers, order.parcelDetailsJson) {
        DomesticParcelStorage.parse(order)
    }

    var isCostDetailsExpanded by remember { mutableStateOf(false) }
    var showRadarDialog by remember { mutableStateOf(false) }
    var showIncidentDialog by remember { mutableStateOf(false) }
    var incidentText by remember { mutableStateOf("") }

    fun copyToClipboard(label: String, text: String, msg: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Step4StepperHeaderCard(modeTitle = modeTitle, modeSubtitle = modeSubtitle)

        Step4FlightTrajectoryCard(
            order = order,
            isMaritime = isMaritime,
            modeBadge = modeBadge,
            arrivalPortName = arrivalPortName,
            arrivalPortCity = arrivalPortCity
        )

        Step4ShipmentDetailsCard(
            order = order,
            isMaritime = isMaritime,
            carrierHeader = carrierHeader,
            carrierName = carrierName,
            awbNumber = awbNumber,
            arrivalPortCity = arrivalPortCity,
            onCopyAwb = { copyToClipboard("Bordereau LTA", awbNumber, "Bordereau LTA $awbNumber copié !") }
        )

        Step4HoldParcelsCard(
            order = order,
            parcels = parcels,
            weightKg = weightKg,
            totalQty = totalQty,
            onOpenRadar = { showRadarDialog = true }
        )

        Step4TransitCostCard(
            order = order,
            isExpanded = isCostDetailsExpanded,
            onToggleExpanded = { isCostDetailsExpanded = !isCostDetailsExpanded },
            totalCostOfGoodsAr = totalCostOfGoodsAr,
            unitCostAr = unitCostAr,
            totalQty = totalQty,
            unitPriceYuan = unitPriceYuan,
            merchandiseTotalAr = merchandiseTotalAr,
            domesticPortAr = domesticPortAr,
            weightKg = weightKg,
            freightCostAr = freightCostAr
        )

        Step4ReportIssueBanner(onClick = { showIncidentDialog = true })

        Step4BottomActionsCard(
            onValidateCustoms = {
                val updated = order.copy(
                    status = OrderStatus.CUSTOMS.code,
                    incidentReport = if (incidentText.isNotBlank()) incidentText else order.incidentReport
                )
                onAdvanceToCustoms(updated)
                Toast.makeText(
                    context,
                    if (isMaritime) "Navire arrivé au port de Toamasina ! Passage en douane." else "Vol atterri à Ivato ! Passage en douane.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onPreviousStep = onPreviousStep
        )
    }

    if (showRadarDialog) {
        Step4RadarDialog(
            isMaritime = isMaritime,
            carrierName = carrierName,
            awbNumber = awbNumber,
            estimatedArrival = order.estimatedArrival,
            onDismiss = { showRadarDialog = false }
        )
    }

    if (showIncidentDialog) {
        Step4IncidentDialog(
            incidentText = incidentText,
            onIncidentTextChange = { incidentText = it },
            onDismiss = { showIncidentDialog = false },
            onConfirm = {
                val updated = order.copy(
                    incidentReport = if (incidentText.isNotBlank()) incidentText else order.incidentReport
                )
                onUpdateOrder(updated)
                showIncidentDialog = false
                Toast.makeText(context, "Signalement enregistré dans l'historique du lot.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
