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
import com.example.ui.business.shared.VariantColorUtils
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.LotCostCalculator
import java.util.Locale

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
    accounts: List<BusinessAccountEntity> = emptyList(),
    onUpdateOrder: (updatedOrder: BusinessOrderEntity) -> Unit = {},
    onCompleteWizard: (updatedOrder: BusinessOrderEntity) -> Unit,
    onPreviousStep: () -> Unit,
    onRefundYuans: (amount: Double, rateAr: Double, yuanAccountName: String, note: String) -> Unit = { _, _, _, _ -> },
    onPayOrderFreight: (order: BusinessOrderEntity, amountAr: Long, accountName: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current

    var refundYuanInput by rememberSaveable {
        mutableStateOf(if (order.refundYuan > 0.0) String.format(Locale.US, "%.1f", order.refundYuan) else "0")
    }
    val refundYuan = refundYuanInput.replace(',', '.').toDoubleOrNull() ?: 0.0

    var customsTaxInput by rememberSaveable {
        mutableStateOf(if (order.customsTaxAr > 0) order.customsTaxAr.toString() else "0")
    }
    val customsTaxAr = customsTaxInput.toLongOrNull() ?: 0L

    val breakdown = remember(order, cmupRate, refundYuan, customsTaxAr) {
        LotCostCalculator.calculate(order, cmupRate, customRefundYuan = refundYuan, customsTaxArInput = customsTaxAr)
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

    val (v1Ordered, v2Ordered, v3Ordered) = remember(parsedVariantEntries, unitsReceived) {
        val explicitCounts = parsedVariantEntries.take(3).map { it.second }
        val allHaveCounts = explicitCounts.all { it != null } && explicitCounts.sumOf { it ?: 0 } == unitsReceived
        if (allHaveCounts) {
            Triple(
                explicitCounts.getOrElse(0) { 0 } ?: 0,
                explicitCounts.getOrElse(1) { 0 } ?: 0,
                explicitCounts.getOrElse(2) { 0 } ?: 0
            )
        } else {
            val v1 = when (parsedVariants.size) {
                1 -> unitsReceived
                2 -> (unitsReceived + 1) / 2
                else -> (unitsReceived * 0.5).toInt().coerceAtLeast(if (unitsReceived > 0) 1 else 0)
            }
            val v2 = when (parsedVariants.size) {
                1 -> 0
                2 -> (unitsReceived - v1).coerceAtLeast(0)
                else -> ((unitsReceived - v1) / 2).coerceAtLeast(0)
            }
            val v3 = (unitsReceived - v1 - v2).coerceAtLeast(0)
            Triple(v1, v2, v3)
        }
    }

    var noirOk by rememberSaveable(v1Ordered) { mutableIntStateOf(v1Ordered) }
    var noirDefautMineur by rememberSaveable { mutableIntStateOf(0) }
    var noirNonVendable by rememberSaveable { mutableIntStateOf(0) }
    var noirPerdu by rememberSaveable { mutableIntStateOf(0) }

    var blancOk by rememberSaveable(v2Ordered) { mutableIntStateOf(v2Ordered) }
    var blancDefautMineur by rememberSaveable { mutableIntStateOf(0) }
    var blancNonVendable by rememberSaveable { mutableIntStateOf(0) }
    var blancPerdu by rememberSaveable { mutableIntStateOf(0) }

    var rougeOk by rememberSaveable(v3Ordered) { mutableIntStateOf(v3Ordered) }
    var rougeDefautVendable by rememberSaveable { mutableIntStateOf(0) }
    var rougeNonVendable by rememberSaveable { mutableIntStateOf(0) }
    var rougePerdu by rememberSaveable { mutableIntStateOf(0) }

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
    LaunchedEffect(order.isFreightPaid) {
        isInvoicePaid = order.isFreightPaid
    }
    var showPaymentDialog by remember { mutableStateOf(false) }

    val ariaryAccounts = remember(accounts) {
        accounts.filter { !it.isYuanAccount && it.isActive }
    }
    var selectedAccountId by rememberSaveable(accounts) {
        mutableStateOf(ariaryAccounts.firstOrNull()?.id ?: 0L)
    }

    val yuanAccounts = remember(accounts) {
        accounts.filter { it.isYuanAccount && it.isActive }
    }
    var selectedRefundYuanAccount by rememberSaveable(yuanAccounts) {
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
                color = Color(0xFF131B2E)
            )
            Text(
                text = "4 statuts normalisés",
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = Color(0xFF3D494C)
            )
        }

        Step6VariantInspectionCard(
            variantName = v1Name,
            orderedCount = v1Ordered,
            badgeOuterColor = Color(0xFF283044),
            badgeInnerColor = Color(0xFFFAF8FF),
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
            Step6VariantInspectionCard(
                variantName = v2Name,
                orderedCount = v2Ordered,
                badgeOuterColor = Color(0xFFDAE2FD),
                badgeInnerColor = Color.White,
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
            Step6VariantInspectionCard(
                variantName = v3Name,
                orderedCount = v3Ordered,
                badgeOuterColor = Color(0xFFFFDAD6),
                badgeInnerColor = Color(0xFFBA1A1A),
                okCount = rougeOk,
                minorDefectCount = rougeDefautVendable,
                nonSellableCount = rougeNonVendable,
                lostCount = rougePerdu,
                minorDefectLabel = "Défaut vendable",
                minorDefectTitleColor = Color(0xFF712AE2),
                minorDefectContainerColor = Color(0xFFEADDFF).copy(alpha = 0.35f),
                lostLabel = "Perdu / Volé",
                lostTitleColor = Color(0xFFBA1A1A),
                lostContainerColor = Color(0xFFFFDAD6).copy(alpha = 0.50f),
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
            refundYuanInput = refundYuanInput,
            onRefundYuanInputChange = { refundYuanInput = it },
            refundAr = refundAr,
            effectiveCmupRate = kotlin.math.round(effectiveCmupRate).toLong(),
            yuanAccounts = yuanAccounts,
            selectedRefundYuanAccount = selectedRefundYuanAccount,
            onSelectRefundYuanAccount = { selectedRefundYuanAccount = it }
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
            onSaveInvoiceClick = {
                Toast.makeText(
                    context,
                    "Facture ${order.carrier.ifBlank { "transitaire" }} enregistrée dans vos documents.",
                    Toast.LENGTH_SHORT
                ).show()
            },
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
                val updated = order.copy(
                    status = OrderStatus.IN_STOCK.code,
                    stockCount = totalVendable,
                    defectiveCount = totalNonVendable,
                    lostCount = totalPerdu,
                    customsTaxAr = customsTaxAr,
                    refundYuan = refundYuan,
                    unitCostAr = unitFifoCostAr,
                    inspectionResults = inspectionSummary,
                    variants = variantSummary.ifBlank { "$v1Name ($noirOk ok)" },
                    freightCostAr = freightCostAr,
                    isFreightPaid = isInvoicePaid,
                    soldDate = System.currentTimeMillis()
                )
                if (refundYuan > 0.0) {
                    onRefundYuans(
                        refundYuan,
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
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
                    tint = if (isDistributedValid) Color(0xFF00687A) else Color(0xFFBA1A1A),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isDistributedValid) "Tous les compteurs sont validés ($totalDistributed/$unitsReceived)" else "Répartition incomplète ($totalDistributed/$unitsReceived réparties)",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isDistributedValid) Color(0xFF00687A) else Color(0xFFBA1A1A)
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
                    containerColor = Color(0xFF00687A),
                    disabledContainerColor = Color(0xFFBCC9CD)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Valider et ajouter les $totalVendable unités au stock",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF712AE2))
                Text("Paiement Fret ${order.carrier.ifBlank { "Transitaire" }}", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Règlement de ${CurrencyFormatter.formatAriary(freightCostAr)} au transitaire ${order.carrier.ifBlank { "partenaire" }}.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = Color(0xFF131B2E)
                )

                if (ariaryAccounts.isNotEmpty()) {
                    Text(
                        text = "Compte source pour le débit :",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF131B2E)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ariaryAccounts.forEach { acct ->
                            val isSelected = acct.id == selectedAccountId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectAccount(acct.id) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFEADDFF).copy(alpha = 0.5f) else Color(0xFFF2F3FF)
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
                                        color = if (isSelected) Color(0xFF712AE2) else Color(0xFF131B2E)
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatAriary(acct.balanceAr),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF00687A)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF2F3FF), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Mode de règlement : Espèces / Mobile Money",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF712AE2))
            ) {
                Text(if (isInvoicePaid) "Déjà réglé" else "Confirmer le paiement", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
