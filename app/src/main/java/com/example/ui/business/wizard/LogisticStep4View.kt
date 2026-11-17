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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.DomesticParcelStorage
import com.example.util.BordereauUtils
import com.example.util.LotCostCalculator

@Composable
fun LogisticStep4View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    onUpdateOrder: (updatedOrder: BusinessOrderEntity) -> Unit = {},
    onAdvanceToCustoms: (updatedOrder: BusinessOrderEntity) -> Unit,
    onPreviousStep: () -> Unit
) {
    val context = LocalContext.current

    val selectedTransitaire = remember(transitaires, order.carrier) {
        if (order.carrier.isBlank()) null
        else transitaires.firstOrNull {
            it.name.equals(order.carrier, ignoreCase = true) ||
                order.carrier.contains(it.name, ignoreCase = true)
        }
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
    val merchandiseTotalAr = breakdown.merchandiseTotalAr
    val domesticPortAr = breakdown.domesticPortAr
    val isMaritime = breakdown.isMaritime
    val weightKg = order.weightKg ?: 0.0
    val volumeM3 = order.volumeM3 ?: 0.0
    val freightCostAr = breakdown.freightCostAr
    val totalCostOfGoodsAr = breakdown.totalDisbursedAr
    val unitCostAr = breakdown.unitCostAr

    val modeTitle = if (isMaritime) "En navire cargo vers Madagascar" else "En vol cargo vers Madagascar"
    val modeSubtitle = if (isMaritime) "Fret maritime en transit" else "Vol cargo en transit"
    val modeBadge = if (isMaritime) "En transit maritime" else "En transit aérien"
    val arrivalPortName = if (isMaritime) "TOA" else "TNR"
    val arrivalPortCity = if (isMaritime) "Toamasina" else "Antananarivo"
    val carrierHeader = if (isMaritime) "Fret Maritime Groupage" else "Fret Aérien Express"

    val originHubLabel = selectedTransitaire?.routeHub?.takeIf { it.isNotBlank() } ?: "Hub Chine"
    val leadDaysLabel = if (isMaritime) {
        selectedTransitaire?.seaLeadDays?.trim().orEmpty()
    } else {
        selectedTransitaire?.airLeadDays?.trim().orEmpty()
    }

    val carrierName = order.carrier.ifBlank { "Transitaire non défini" }
    val bordereauPrefix = BordereauUtils.prefix(order)
    val departureMillis = BordereauUtils.departureMillis(order)
    val awbNumber = BordereauUtils.formatCode(order)
    val parcels = remember(order.trackingNumbers, order.parcelDetailsJson) {
        DomesticParcelStorage.parse(order)
    }

    var isCostDetailsExpanded by remember { mutableStateOf(false) }
    var showRadarDialog by remember { mutableStateOf(false) }
    var showIncidentDialog by remember { mutableStateOf(false) }
    var incidentText by rememberSaveable(order.id) { mutableStateOf(order.incidentReport) }

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
            originHubLabel = originHubLabel,
            leadDaysLabel = leadDaysLabel,
            parcelsCount = parcels.size.coerceAtLeast(1),
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
            leadDaysLabel = leadDaysLabel,
            onCopyAwb = { copyToClipboard("Bordereau $bordereauPrefix", awbNumber, "Bordereau $awbNumber copié !") },
            onSelectDelayDays = { days ->
                val baseMillis = departureMillis ?: System.currentTimeMillis()
                val newEta = baseMillis + days * 86_400_000L
                onUpdateOrder(order.copy(estimatedArrival = newEta))
                Toast.makeText(context, "Délai fixé à +${days}j", Toast.LENGTH_SHORT).show()
            }
        )

        Step4HoldParcelsCard(
            order = order,
            parcels = parcels,
            isMaritime = isMaritime,
            weightKg = weightKg,
            volumeM3 = volumeM3,
            totalQty = totalQty,
            onOpenRadar = { showRadarDialog = true }
        )

        Step4TransitCostCard(
            order = order,
            isMaritime = isMaritime,
            isExpanded = isCostDetailsExpanded,
            onToggleExpanded = { isCostDetailsExpanded = !isCostDetailsExpanded },
            totalCostOfGoodsAr = totalCostOfGoodsAr,
            unitCostAr = unitCostAr,
            totalQty = totalQty,
            unitPriceYuan = unitPriceYuan,
            merchandiseTotalAr = merchandiseTotalAr,
            domesticPortAr = domesticPortAr,
            weightKg = weightKg,
            volumeM3 = volumeM3,
            freightCostAr = freightCostAr
        )

        Step4ReportIssueBanner(onClick = { showIncidentDialog = true })

        Step4BottomActionsCard(
            onValidateCustoms = {
                val updated = order.copy(
                    status = OrderStatus.CUSTOMS.code,
                    freightCostAr = freightCostAr,
                    incidentReport = if (incidentText.isNotBlank()) incidentText.trim() else order.incidentReport
                )
                onAdvanceToCustoms(updated)
                Toast.makeText(
                    context,
                    if (isMaritime) "Navire arrivé au port de Toamasina ! Passage en douane." else "Vol arrivé à Ivato ! Passage en douane.",
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
            originHubLabel = originHubLabel,
            arrivalPortCity = arrivalPortCity,
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
                    incidentReport = incidentText.trim()
                )
                onUpdateOrder(updated)
                showIncidentDialog = false
                Toast.makeText(context, "Signalement enregistré dans l'historique du lot.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
