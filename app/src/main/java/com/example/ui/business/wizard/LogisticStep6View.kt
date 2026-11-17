package com.example.ui.business.wizard

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.VariantColorUtils
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.LotCostCalculator
import java.util.Locale

private data class SavedInspectionCounts(
    val ok: Int,
    val minorDefect: Int,
    val nonSellable: Int,
    val lost: Int
) {
    val total: Int get() = ok + minorDefect + nonSellable + lost
}

private fun parseInspectionResults(raw: String): SavedInspectionCounts? {
    if (raw.isBlank()) return null
    val match = Regex(
        """(\d+)\s*conformes?,\s*(\d+)\s*d[ée]fauts?\s*mineurs?,\s*(\d+)\s*non\s*vendables?,\s*(\d+)\s*perdus?""",
        RegexOption.IGNORE_CASE
    ).find(raw) ?: return null
    val ok = match.groupValues[1].toIntOrNull() ?: return null
    val minor = match.groupValues[2].toIntOrNull() ?: return null
    val nonSellable = match.groupValues[3].toIntOrNull() ?: return null
    val lost = match.groupValues[4].toIntOrNull() ?: return null
    return SavedInspectionCounts(ok, minor, nonSellable, lost)
}

private fun parseVariantWithCount(raw: String): Pair<String, Int?> {
    val timesMatch = Regex("""^(.*?)\s*[×xX]\s*(\d+)$""").find(raw.trim())
    val okMatch = Regex("""^(.*?)\s*\((\d+)\s*ok\)$""", RegexOption.IGNORE_CASE).find(raw.trim())
    return when {
        timesMatch != null -> timesMatch.groupValues[1].trim() to timesMatch.groupValues[2].toIntOrNull()
        okMatch != null -> okMatch.groupValues[1].trim() to okMatch.groupValues[2].toIntOrNull()
        else -> raw.trim() to null
    }
}

@Composable
fun LogisticStep6View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    accounts: List<BusinessAccountEntity> = emptyList(),
    onUpdateOrder: (updatedOrder: BusinessOrderEntity) -> Unit = {},
    onCompleteWizard: (updatedOrder: BusinessOrderEntity) -> Unit,
    onPreviousStep: () -> Unit,
    onRefundYuans: (amount: Double, rateAr: Double, yuanAccountName: String, note: String) -> Unit = { _, _, _, _ -> },
    onPayOrderFreight: (order: BusinessOrderEntity, amountAr: Long, accountName: String) -> Unit = { _, _, _ -> }
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

    val alreadyRefundedYuan = order.refundYuan.coerceAtLeast(0.0)
    val isRefundAlreadyCredited = alreadyRefundedYuan > 0.0

    var refundYuanInput by rememberSaveable(order.id) {
        mutableStateOf(if (alreadyRefundedYuan > 0.0) String.format(Locale.US, "%.1f", alreadyRefundedYuan) else "")
    }
    LaunchedEffect(order.id, order.refundYuan) {
        if (order.refundYuan > 0.0) {
            refundYuanInput = String.format(Locale.US, "%.1f", order.refundYuan)
        }
    }
    val refundYuan = if (isRefundAlreadyCredited) {
        alreadyRefundedYuan
    } else {
        (refundYuanInput.replace(',', '.').toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    }
    val customsTaxAr = order.customsTaxAr.coerceAtLeast(0L)

    val breakdown = remember(order, cmupRate, refundYuan, customsTaxAr, selectedTransitaire) {
        LotCostCalculator.calculate(
            order = order,
            cmupRate = cmupRate,
            customRefundYuan = refundYuan,
            customsTaxArInput = customsTaxAr,
            transitaireAirRateAr = resolvedAirRate,
            transitaireSeaRateAr = selectedTransitaire?.seaRateAr
        )
    }
    val totalQty = breakdown.quantity
    val effectiveCmupRate = breakdown.effectiveCmupRate
    val merchandiseTotalAr = breakdown.merchandiseTotalAr
    val domesticPortAr = breakdown.domesticPortAr
    val freightCostAr = breakdown.freightCostAr
    val refundAr = breakdown.refundAr

    val unitsReceived = if (order.receivedUnitsCount > 0) order.receivedUnitsCount else totalQty

    val parsedVariantEntries = remember(order.variants, order.product) {
        val rawList = VariantColorUtils.parseVariants(order.variants)
        if (rawList.isNotEmpty()) rawList.map { parseVariantWithCount(it) }
        else listOf("Standard" to null)
    }
    val parsedVariants = remember(parsedVariantEntries) {
        parsedVariantEntries.map { it.first.ifBlank { "Standard" } }
    }
    val v1Name = parsedVariants.getOrElse(0) { "Standard" }
    val v2Name = parsedVariants.getOrElse(1) { "Variante 2" }
    val v3Name = parsedVariants.getOrElse(2) { "Variante 3" }

    val savedInspection = remember(order.inspectionResults) {
        parseInspectionResults(order.inspectionResults)
    }

    val (v1Ordered, v2Ordered, v3Ordered) = remember(parsedVariantEntries, unitsReceived, savedInspection) {
        val visibleVariantCount = parsedVariants.size.coerceIn(1, 3)
        val explicitCounts = parsedVariantEntries.take(visibleVariantCount).map { it.second }
        val allHaveCounts = explicitCounts.all { it != null }
        val explicitSum = explicitCounts.sumOf { it ?: 0 }

        if (allHaveCounts && explicitSum == unitsReceived) {
            Triple(
                explicitCounts.getOrElse(0) { 0 } ?: 0,
                explicitCounts.getOrElse(1) { 0 } ?: 0,
                explicitCounts.getOrElse(2) { 0 } ?: 0
            )
        } else if (
            savedInspection != null &&
            savedInspection.total == unitsReceived &&
            allHaveCounts &&
            visibleVariantCount > 1 &&
            explicitSum + savedInspection.minorDefect + savedInspection.nonSellable + savedInspection.lost == unitsReceived
        ) {
            val extraAnomalies = savedInspection.minorDefect + savedInspection.nonSellable + savedInspection.lost
            Triple(
                (explicitCounts.getOrElse(0) { 0 } ?: 0) + extraAnomalies,
                explicitCounts.getOrElse(1) { 0 } ?: 0,
                explicitCounts.getOrElse(2) { 0 } ?: 0
            )
        } else {
            val v1 = when (visibleVariantCount) {
                1 -> unitsReceived
                2 -> (unitsReceived + 1) / 2
                else -> if (unitsReceived > 0) (unitsReceived * 0.5).toInt().coerceAtLeast(1) else 0
            }
            val v2 = when (visibleVariantCount) {
                1 -> 0
                2 -> (unitsReceived - v1).coerceAtLeast(0)
                else -> ((unitsReceived - v1) / 2).coerceAtLeast(0)
            }
            val v3 = (unitsReceived - v1 - v2).coerceAtLeast(0)
            Triple(v1, v2, v3)
        }
    }

    val restoreSavedSingleVariant = savedInspection != null &&
        savedInspection.total == unitsReceived &&
        parsedVariants.size == 1 &&
        v1Ordered == unitsReceived
    val restoreSavedMultiVariant = savedInspection != null &&
        savedInspection.total == unitsReceived &&
        parsedVariants.size > 1 &&
        parsedVariantEntries.take(parsedVariants.size.coerceAtMost(3)).all { it.second != null } &&
        (parsedVariantEntries.take(parsedVariants.size.coerceAtMost(3)).sumOf { it.second ?: 0 } +
            savedInspection.minorDefect + savedInspection.nonSellable + savedInspection.lost == unitsReceived)

    val initialV1Ok = when {
        restoreSavedSingleVariant -> savedInspection!!.ok
        restoreSavedMultiVariant -> parsedVariantEntries[0].second ?: v1Ordered
        else -> v1Ordered
    }
    val initialV1Minor = if (restoreSavedSingleVariant || restoreSavedMultiVariant) savedInspection!!.minorDefect else 0
    val initialV1NonSellable = if (restoreSavedSingleVariant || restoreSavedMultiVariant) savedInspection!!.nonSellable else 0
    val initialV1Lost = if (restoreSavedSingleVariant || restoreSavedMultiVariant) savedInspection!!.lost else 0

    var noirOk by rememberSaveable(order.id, v1Ordered, order.inspectionResults) { mutableIntStateOf(initialV1Ok) }
    var noirDefautMineur by rememberSaveable(order.id, v1Ordered, order.inspectionResults) { mutableIntStateOf(initialV1Minor) }
    var noirNonVendable by rememberSaveable(order.id, v1Ordered, order.inspectionResults) { mutableIntStateOf(initialV1NonSellable) }
    var noirPerdu by rememberSaveable(order.id, v1Ordered, order.inspectionResults) { mutableIntStateOf(initialV1Lost) }

    var blancOk by rememberSaveable(order.id, v2Ordered, order.inspectionResults) { mutableIntStateOf(v2Ordered) }
    var blancDefautMineur by rememberSaveable(order.id, v2Ordered, order.inspectionResults) { mutableIntStateOf(0) }
    var blancNonVendable by rememberSaveable(order.id, v2Ordered, order.inspectionResults) { mutableIntStateOf(0) }
    var blancPerdu by rememberSaveable(order.id, v2Ordered, order.inspectionResults) { mutableIntStateOf(0) }

    var rougeOk by rememberSaveable(order.id, v3Ordered, order.inspectionResults) { mutableIntStateOf(v3Ordered) }
    var rougeDefautVendable by rememberSaveable(order.id, v3Ordered, order.inspectionResults) { mutableIntStateOf(0) }
    var rougeNonVendable by rememberSaveable(order.id, v3Ordered, order.inspectionResults) { mutableIntStateOf(0) }
    var rougePerdu by rememberSaveable(order.id, v3Ordered, order.inspectionResults) { mutableIntStateOf(0) }

    val v1Sum = noirOk + noirDefautMineur + noirNonVendable + noirPerdu
    val v2Sum = blancOk + blancDefautMineur + blancNonVendable + blancPerdu
    val v3Sum = rougeOk + rougeDefautVendable + rougeNonVendable + rougePerdu

    val totalConforme = noirOk + blancOk + rougeOk
    val totalDefautVendable = noirDefautMineur + blancDefautMineur + rougeDefautVendable
    val totalVendable = totalConforme + totalDefautVendable
    val totalNonVendable = noirNonVendable + blancNonVendable + rougeNonVendable
    val totalPerdu = noirPerdu + blancPerdu + rougePerdu
    val totalDistributed = totalVendable + totalNonVendable + totalPerdu

    val isDistributedValid = (v1Sum == v1Ordered) && (v2Sum == v2Ordered) && (v3Sum == v3Ordered) && (totalDistributed == unitsReceived)
    val unitFifoCostAr = if (totalVendable > 0) breakdown.netCostAr / totalVendable else 0L

    var isInvoicePaid by rememberSaveable(order.id) { mutableStateOf(order.isFreightPaid) }
    LaunchedEffect(order.id, order.isFreightPaid) {
        isInvoicePaid = order.isFreightPaid
    }
    var showPaymentDialog by remember { mutableStateOf(false) }

    val ariaryAccounts = remember(accounts) {
        accounts.filter { !it.isYuanAccount && it.isActive }
    }
    var selectedAccountId by rememberSaveable(order.id, ariaryAccounts.size) {
        mutableStateOf(ariaryAccounts.firstOrNull()?.id ?: 0L)
    }

    val yuanAccounts = remember(accounts) {
        accounts.filter { it.isYuanAccount && it.isActive }
    }
    var selectedRefundYuanAccount by rememberSaveable(order.id, yuanAccounts.size) {
        mutableStateOf(yuanAccounts.firstOrNull()?.name ?: "Alipay")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Step6StepperCard(unitsReceived = unitsReceived, totalQty = totalQty)
        Step6InspectionOverviewPill(order = order, totalQty = totalQty)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Déclinaisons & États",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colors.textPrimary
            )
            Text(
                text = "4 statuts normalisés",
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }

        val v1Color = VariantColorUtils.getVariantColor(v1Name, 0)
        Step6VariantInspectionCard(
            variantName = v1Name,
            orderedCount = v1Ordered,
            badgeOuterColor = if (VariantColorUtils.isLightColor(v1Color)) colors.badgeNeutralBg else v1Color.copy(alpha = 0.16f),
            badgeInnerColor = v1Color,
            okCount = noirOk,
            minorDefectCount = noirDefautMineur,
            nonSellableCount = noirNonVendable,
            lostCount = noirPerdu,
            onOkChange = { noirOk = it },
            onMinorDefectChange = { noirDefautMineur = it },
            onNonSellableChange = { noirNonVendable = it },
            onLostChange = { noirPerdu = it }
        )

        if (parsedVariants.size >= 2 && v2Ordered > 0) {
            val v2Color = VariantColorUtils.getVariantColor(v2Name, 1)
            Step6VariantInspectionCard(
                variantName = v2Name,
                orderedCount = v2Ordered,
                badgeOuterColor = if (VariantColorUtils.isLightColor(v2Color)) colors.badgeNeutralBg else v2Color.copy(alpha = 0.16f),
                badgeInnerColor = v2Color,
                okCount = blancOk,
                minorDefectCount = blancDefautMineur,
                nonSellableCount = blancNonVendable,
                lostCount = blancPerdu,
                onOkChange = { blancOk = it },
                onMinorDefectChange = { blancDefautMineur = it },
                onNonSellableChange = { blancNonVendable = it },
                onLostChange = { blancPerdu = it }
            )
        }

        if (parsedVariants.size >= 3 && v3Ordered > 0) {
            val v3Color = VariantColorUtils.getVariantColor(v3Name, 2)
            Step6VariantInspectionCard(
                variantName = v3Name,
                orderedCount = v3Ordered,
                badgeOuterColor = if (VariantColorUtils.isLightColor(v3Color)) colors.badgeNeutralBg else v3Color.copy(alpha = 0.16f),
                badgeInnerColor = v3Color,
                okCount = rougeOk,
                minorDefectCount = rougeDefautVendable,
                nonSellableCount = rougeNonVendable,
                lostCount = rougePerdu,
                minorDefectLabel = "Défaut vendable",
                minorDefectTitleColor = colors.violetAccent,
                minorDefectContainerColor = colors.violetSoftBg,
                lostLabel = "Perdu / Volé",
                lostTitleColor = colors.dangerAccent,
                lostContainerColor = colors.dangerSoftBg,
                onOkChange = { rougeOk = it },
                onMinorDefectChange = { rougeDefautVendable = it },
                onNonSellableChange = { rougeNonVendable = it },
                onLostChange = { rougePerdu = it }
            )
        }

        Step6DistributionSummaryRow(
            totalConforme = totalConforme,
            totalDefautVendable = totalDefautVendable,
            totalPerdu = totalPerdu,
            totalDistributed = totalDistributed
        )

        Step6SupplierRefundCard(
            platform = order.platform,
            refundYuanInput = refundYuanInput,
            onRefundYuanInputChange = { refundYuanInput = it },
            refundAr = refundAr,
            effectiveCmupRate = kotlin.math.round(effectiveCmupRate).toLong(),
            yuanAccounts = yuanAccounts,
            selectedRefundYuanAccount = selectedRefundYuanAccount,
            onSelectRefundYuanAccount = { selectedRefundYuanAccount = it },
            isRefundAlreadyCredited = isRefundAlreadyCredited
        )

        Step6CertifiedFifoCard(
            totalVendable = totalVendable,
            totalConforme = totalConforme,
            totalDefautVendable = totalDefautVendable,
            unitFifoCostAr = unitFifoCostAr,
            merchandiseTotalAr = merchandiseTotalAr,
            domesticPortAr = domesticPortAr,
            freightCostAr = freightCostAr,
            customsTaxAr = customsTaxAr,
            refundAr = refundAr
        )

        Step6CarrierSettlementCard(
            order = order,
            freightCostAr = freightCostAr,
            isInvoicePaid = isInvoicePaid,
            onPayFreightClick = { showPaymentDialog = true }
        )

        Step6BottomValidationCard(
            isDistributedValid = isDistributedValid,
            totalDistributed = totalDistributed,
            unitsReceived = unitsReceived,
            totalVendable = totalVendable,
            onConfirmStockInjection = {
                val inspectionSummary = "$totalConforme conformes, $totalDefautVendable défauts mineurs, $totalNonVendable non vendables, $totalPerdu perdus"
                val variantSummary = buildList {
                    if (parsedVariants.isNotEmpty()) add("$v1Name ($noirOk ok)")
                    if (parsedVariants.size > 1) add("$v2Name ($blancOk ok)")
                    if (parsedVariants.size > 2) add("$v3Name ($rougeOk ok)")
                }.joinToString(", ")
                val finalRefundYuan = if (isRefundAlreadyCredited) alreadyRefundedYuan else refundYuan
                val updated = order.copy(
                    status = OrderStatus.IN_STOCK.code,
                    stockCount = totalVendable,
                    defectiveCount = totalNonVendable,
                    lostCount = totalPerdu,
                    customsTaxAr = customsTaxAr,
                    refundYuan = finalRefundYuan,
                    unitCostAr = unitFifoCostAr,
                    inspectionResults = inspectionSummary,
                    variants = variantSummary.ifBlank { "$v1Name ($noirOk ok)" },
                    freightCostAr = freightCostAr,
                    isFreightPaid = isInvoicePaid,
                    soldDate = System.currentTimeMillis()
                )
                if (!isRefundAlreadyCredited && finalRefundYuan > 0.0) {
                    onRefundYuans(
                        finalRefundYuan,
                        effectiveCmupRate.toDouble(),
                        selectedRefundYuanAccount.ifBlank { "Alipay" },
                        "Remboursement usine : ${order.product} (Lot #${order.id})"
                    )
                }
                onCompleteWizard(updated)
                Toast.makeText(context, "$totalVendable unités injectées dans le stock avec succès !", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showPaymentDialog) {
        Step6FreightPaymentDialog(
            order = order,
            freightCostAr = freightCostAr,
            isInvoicePaid = isInvoicePaid,
            ariaryAccounts = ariaryAccounts,
            selectedAccountId = selectedAccountId,
            onSelectAccount = { selectedAccountId = it },
            onDismiss = { showPaymentDialog = false },
            onConfirmPayment = {
                if (ariaryAccounts.isEmpty()) {
                    Toast.makeText(context, "Créez d'abord un compte Ariary dans Finances", Toast.LENGTH_SHORT).show()
                } else if (freightCostAr <= 0L) {
                    Toast.makeText(context, "Montant de fret invalide", Toast.LENGTH_SHORT).show()
                } else {
                    val chosenAccount = ariaryAccounts.find { it.id == selectedAccountId }?.name
                        ?: ariaryAccounts.first().name
                    isInvoicePaid = true
                    showPaymentDialog = false
                    onPayOrderFreight(order.copy(freightCostAr = freightCostAr), freightCostAr, chosenAccount)
                }
            }
        )
    }
}

@Composable
private fun Step6BottomValidationCard(
    isDistributedValid: Boolean,
    totalDistributed: Int,
    unitsReceived: Int,
    totalVendable: Int,
    onConfirmStockInjection: () -> Unit
) {
    val colors = rememberTransitaireColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isDistributedValid) Icons.Default.TaskAlt else Icons.Default.ReportProblem,
                    contentDescription = null,
                    tint = if (isDistributedValid) colors.cyanAccent else colors.dangerAccent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isDistributedValid) "Tous les compteurs sont validés ($totalDistributed/$unitsReceived)" else "Répartition incomplète ($totalDistributed/$unitsReceived réparties)",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isDistributedValid) colors.cyanAccent else colors.dangerAccent
                )
            }

            Button(
                onClick = { if (isDistributedValid) onConfirmStockInjection() },
                enabled = isDistributedValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_validate_stock_injection"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.cyanSolidBg,
                    disabledContainerColor = colors.badgeNeutralBg
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                val ctaColor = if (isDistributedValid) Color.White else colors.textSecondary
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Valider et ajouter les $totalVendable unités au stock",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ctaColor
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = ctaColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Step6FreightPaymentDialog(
    order: BusinessOrderEntity,
    freightCostAr: Long,
    isInvoicePaid: Boolean,
    ariaryAccounts: List<BusinessAccountEntity>,
    selectedAccountId: Long,
    onSelectAccount: (Long) -> Unit,
    onDismiss: () -> Unit,
    onConfirmPayment: () -> Unit
) {
    val colors = rememberTransitaireColors()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = colors.violetAccent)
                Text(
                    text = "Paiement Fret ${order.carrier.ifBlank { "Transitaire" }}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.textPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Règlement de ${CurrencyFormatter.formatAriary(freightCostAr)} au transitaire ${order.carrier.ifBlank { "partenaire" }}.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )

                if (ariaryAccounts.isNotEmpty()) {
                    Text(
                        text = "Compte source pour le débit :",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.textPrimary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ariaryAccounts.forEach { acct ->
                            val isSelected = acct.id == selectedAccountId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectAccount(acct.id) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) colors.violetSoftBg else colors.innerCardBg
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = acct.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (isSelected) colors.violetAccent else colors.textPrimary
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatAriary(acct.balanceAr),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = colors.cyanAccent
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(shape = RoundedCornerShape(8.dp), color = colors.innerCardBg, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Mode de règlement : Espèces / Mobile Money",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmPayment,
                enabled = !isInvoicePaid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.violetSolidBg,
                    disabledContainerColor = colors.badgeNeutralBg
                )
            ) {
                Text(
                    text = if (isInvoicePaid) "Déjà réglé" else "Confirmer le paiement",
                    color = if (isInvoicePaid) colors.textSecondary else Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
