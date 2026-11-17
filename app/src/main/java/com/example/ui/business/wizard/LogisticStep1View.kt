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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.business.CatalogueFamilySummaryUi
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.finances.BuyYuansDialog
import com.example.ui.business.model.DomesticParcelStorage
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans

@Composable
fun LogisticStep1View(
    order: BusinessOrderEntity,
    cmupRate: Long,
    availableYuans: Double = 0.0,
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    families: List<CatalogueFamilySummaryUi> = emptyList(),
    accounts: List<BusinessAccountEntity> = emptyList(),
    onUpdateOrder: (BusinessOrderEntity) -> Unit = {},
    onConfirmOrder: (updatedOrder: BusinessOrderEntity) -> Unit,
    onSaveAsDraft: () -> Unit,
    onSpendYuans: (amountYuan: Double, rateAr: Double, yuanAccountName: String, note: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onBuyYuans: (amountYuan: Double, rateAr: Double, platform: String, sourceAccountName: String, purchaseDateMillis: Long) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val colors = rememberTransitaireColors()

    var isDraftCheckboxChecked by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showBuyYuansDialog by remember { mutableStateOf(false) }
    var showEditOrderDialog by remember { mutableStateOf(false) }
    var showEditDomesticShippingDialog by remember { mutableStateOf(false) }
    var showEditReferenceDialog by remember { mutableStateOf(false) }
    var showEditDispatchDialog by remember { mutableStateOf(false) }
    var showSelectTransitaireDialog by remember { mutableStateOf(false) }
    var showAddTrackingDialog by remember { mutableStateOf(false) }
    var editingOldTracking by remember { mutableStateOf<String?>(null) }
    var newTrackingInput by remember { mutableStateOf("") }
    var newTrackingCarrierInput by remember { mutableStateOf("") }

    val isAlreadyDebited = order.yuanDebited

    val yuanAccounts = remember(accounts) {
        accounts.filter { it.isYuanAccount && it.isActive }
    }
    var selectedYuanAccountName by remember(yuanAccounts) {
        mutableStateOf(
            (yuanAccounts.maxByOrNull { it.balanceYuan } ?: yuanAccounts.firstOrNull())?.name ?: ""
        )
    }

    val isIndicativeRate = order.exchangeRate <= 0.0 && cmupRate <= 0L
    var appliedRateText by remember(order.id, order.exchangeRate, cmupRate) {
        val defaultRate = when {
            order.exchangeRate > 0.0 -> order.exchangeRate
            cmupRate > 0L -> cmupRate.toDouble()
            else -> 650.0
        }
        mutableStateOf(defaultRate.toInt().toString())
    }

    val effectiveCmupRate = appliedRateText.toDoubleOrNull()?.takeIf { it > 0.0 }
        ?: (if (cmupRate > 0L) cmupRate.toDouble() else 650.0)

    val unitPriceYuan = if (order.priceYuan > 0.0) order.priceYuan else 0.0
    val totalQty = if (order.quantityBought > 0) order.quantityBought else 0
    val shippingNationalYuan = order.domesticShippingYuan.coerceAtLeast(0.0)
    val isOrderValid = totalQty > 0 && unitPriceYuan > 0.0 && effectiveCmupRate > 0.0

    val goodsTotalYuan = if (totalQty > 0 && unitPriceYuan > 0.0) totalQty * unitPriceYuan else 0.0
    val totalOrderYuan = goodsTotalYuan + shippingNationalYuan
    val totalOrderAr = if (effectiveCmupRate > 0.0) (totalOrderYuan * effectiveCmupRate).toLong() else 0L

    val selectedYuanAccount = yuanAccounts.firstOrNull {
        it.name.equals(selectedYuanAccountName, ignoreCase = true)
    }
    val availableWalletYuans = when {
        yuanAccounts.isEmpty() -> 0.0
        yuanAccounts.sumOf { it.balanceYuan } > 0.0 -> selectedYuanAccount?.balanceYuan ?: 0.0
        else -> availableYuans
    }
    val remainingWalletYuans = availableWalletYuans - totalOrderYuan
    val unitPriceAr = if (unitPriceYuan > 0.0 && effectiveCmupRate > 0.0) (unitPriceYuan * effectiveCmupRate).toLong() else 0L
    val shippingNationalAr = if (effectiveCmupRate > 0.0) (shippingNationalYuan * effectiveCmupRate).toLong() else 0L

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

    val selectedTransitaire = remember(transitaires, order.carrier) {
        if (order.carrier.isBlank()) null
        else transitaires.firstOrNull {
            it.name.equals(order.carrier, ignoreCase = true) ||
                order.carrier.contains(it.name, ignoreCase = true)
        }
    }
    val isMaritime = order.freightMode.equals("MARITIME", ignoreCase = true) ||
        order.freightMode.equals("SEA", ignoreCase = true)

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

    val currentStatusEnum = OrderStatus.fromCode(order.status)
    val isDraftStatus = currentStatusEnum == OrderStatus.DRAFT

    fun copyToClipboard(label: String, text: String, toastMsg: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
    }

    val confirmAction: () -> Unit = {
        val isBalanceSufficient = remainingWalletYuans >= 0.0
        val canConfirm = isOrderValid && !isSubmitting && (isAlreadyDebited || isDraftCheckboxChecked || isBalanceSufficient)
        if (canConfirm && !isSubmitting) {
            isSubmitting = true
            val nextStatus = OrderStatus.WAREHOUSE.code
            val shouldDebitNow = !isAlreadyDebited && !isDraftCheckboxChecked && totalOrderYuan > 0
            val targetAccountName = selectedYuanAccountName.ifBlank { "Alipay" }
            val syncedOrder = DomesticParcelStorage.applyToOrder(order, parcels).copy(
                status = nextStatus,
                priceYuan = unitPriceYuan,
                quantityBought = totalQty,
                domesticShippingYuan = shippingNationalYuan,
                exchangeRate = effectiveCmupRate,
                yuanDebited = isAlreadyDebited || shouldDebitNow,
                yuanDebitedAmount = if (shouldDebitNow) totalOrderYuan else if (isAlreadyDebited) order.yuanDebitedAmount else 0.0
            )

            if (shouldDebitNow) {
                onSpendYuans(
                    totalOrderYuan,
                    effectiveCmupRate,
                    targetAccountName,
                    "Paiement ${order.platform.ifBlank { "1688" }} ($targetAccountName): ${order.product} (Lot #${order.id})"
                ) { success, msg ->
                    isSubmitting = false
                    if (success) {
                        onConfirmOrder(syncedOrder)
                        Toast.makeText(
                            context,
                            msg.ifBlank { "Commande confirmée et payée en Yuans !" },
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            msg.ifBlank { "Paiement Yuan refusé" },
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                isSubmitting = false
                onConfirmOrder(syncedOrder)
                val msg = if (isAlreadyDebited) {
                    "Commande mise à jour · Passage à l'étape 2"
                } else {
                    "Commande validée sans débit immédiat"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val saveWithoutAdvancingAction: () -> Unit = {
        val baseUpdated = DomesticParcelStorage.applyToOrder(order, parcels).copy(
            exchangeRate = effectiveCmupRate,
            domesticShippingYuan = shippingNationalYuan
        )
        if (isAlreadyDebited) {
            onUpdateOrder(baseUpdated)
            Toast.makeText(context, "Modifications enregistrées", Toast.LENGTH_SHORT).show()
        } else {
            onUpdateOrder(baseUpdated)
            onSaveAsDraft()
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
            if (!isOrderValid) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.dangerSoftBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.dangerAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Informations requises manquantes",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.dangerAccent
                            )
                            Text(
                                text = "Vérifiez le prix unitaire (¥), la quantité et le taux CMUP (Ar/¥).",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                        }
                        TextButton(onClick = { showEditOrderDialog = true }) {
                            Text("Compléter", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.dangerAccent)
                        }
                    }
                }
            }

            WizardStepHeaderCard(
                stepNumber = 1,
                stepTitle = "Validation & Débit",
                statusText = if (isDraftStatus) "Statut : Brouillon (en attente)" else if (isAlreadyDebited) "Statut : Payé en ¥ · Prêt pour expédition" else "Statut : Commandé chez le fournisseur",
                statusIcon = if (isDraftStatus) Icons.Default.Bookmark else Icons.Default.LocalShipping,
                statusAccentColor = if (isDraftStatus) colors.violetAccent else colors.cyanAccent,
                statusContainerColor = if (isDraftStatus) colors.violetSoftBg else colors.cyanSoftBg
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
                        Step1OrderSummaryCard(
                            order = order,
                            familyEmoji = familyEmoji,
                            totalQty = totalQty,
                            unitPriceYuan = unitPriceYuan,
                            unitPriceAr = unitPriceAr,
                            shippingNationalYuan = shippingNationalYuan,
                            shippingNationalAr = shippingNationalAr,
                            totalOrderYuan = totalOrderYuan,
                            totalOrderAr = totalOrderAr,
                            colors = colors,
                            onEditOrder = { showEditOrderDialog = true },
                            onEditDomesticShipping = { showEditDomesticShippingDialog = true }
                        )
                        Step1SupplierLogisticsCard(
                            order = order,
                            selectedTransitaire = selectedTransitaire,
                            transitairesCount = transitaires.size,
                            isMaritime = isMaritime,
                            parcels = parcels,
                            isCompactPhone = false,
                            colors = colors,
                            onEditReference = { showEditReferenceDialog = true },
                            onEditDispatch = { showEditDispatchDialog = true },
                            onSelectTransitaire = { showSelectTransitaireDialog = true },
                            onCopyAddress = { addr -> copyToClipboard("Adresse entrepôt Chine", addr, "Adresse entrepôt copiée !") },
                            onEditTracking = { parcel ->
                                editingOldTracking = parcel.trackingNumber
                                newTrackingInput = parcel.trackingNumber
                                newTrackingCarrierInput = parcel.carrierName
                                showAddTrackingDialog = true
                            },
                            onRemoveTracking = { parcel ->
                                onUpdateOrder(DomesticParcelStorage.removeTracking(order, parcel.trackingNumber))
                            },
                            onAddTracking = {
                                editingOldTracking = null
                                newTrackingInput = ""
                                newTrackingCarrierInput = ""
                                showAddTrackingDialog = true
                            }
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Step1YuanWalletCard(
                            order = order,
                            cmupRate = cmupRate,
                            isIndicativeRate = isIndicativeRate,
                            appliedRateText = appliedRateText,
                            onAppliedRateChange = { appliedRateText = it },
                            effectiveCmupRate = effectiveCmupRate,
                            yuanAccounts = yuanAccounts,
                            selectedYuanAccountName = selectedYuanAccountName,
                            onSelectYuanAccount = { selectedYuanAccountName = it },
                            isAlreadyDebited = isAlreadyDebited,
                            availableWalletYuans = availableWalletYuans,
                            totalOrderYuan = totalOrderYuan,
                            totalOrderAr = totalOrderAr,
                            remainingWalletYuans = remainingWalletYuans,
                            isDraftCheckboxChecked = isDraftCheckboxChecked,
                            onDraftCheckboxChange = { isDraftCheckboxChecked = it },
                            isCompactPhone = false,
                            colors = colors,
                            onOpenBuyYuans = { showBuyYuansDialog = true }
                        )
                        Step1BottomActionsCard(
                            isOrderValid = isOrderValid,
                            isSubmitting = isSubmitting,
                            isAlreadyDebited = isAlreadyDebited,
                            isDraftCheckboxChecked = isDraftCheckboxChecked,
                            remainingWalletYuans = remainingWalletYuans,
                            totalOrderYuan = totalOrderYuan,
                            totalOrderAr = totalOrderAr,
                            colors = colors,
                            onConfirmClick = confirmAction,
                            onSaveWithoutAdvancingClick = saveWithoutAdvancingAction
                        )
                    }
                }
            } else {
                Step1OrderSummaryCard(
                    order = order,
                    familyEmoji = familyEmoji,
                    totalQty = totalQty,
                    unitPriceYuan = unitPriceYuan,
                    unitPriceAr = unitPriceAr,
                    shippingNationalYuan = shippingNationalYuan,
                    shippingNationalAr = shippingNationalAr,
                    totalOrderYuan = totalOrderYuan,
                    totalOrderAr = totalOrderAr,
                    colors = colors,
                    onEditOrder = { showEditOrderDialog = true },
                    onEditDomesticShipping = { showEditDomesticShippingDialog = true }
                )
                Step1YuanWalletCard(
                    order = order,
                    cmupRate = cmupRate,
                    isIndicativeRate = isIndicativeRate,
                    appliedRateText = appliedRateText,
                    onAppliedRateChange = { appliedRateText = it },
                    effectiveCmupRate = effectiveCmupRate,
                    yuanAccounts = yuanAccounts,
                    selectedYuanAccountName = selectedYuanAccountName,
                    onSelectYuanAccount = { selectedYuanAccountName = it },
                    isAlreadyDebited = isAlreadyDebited,
                    availableWalletYuans = availableWalletYuans,
                    totalOrderYuan = totalOrderYuan,
                    totalOrderAr = totalOrderAr,
                    remainingWalletYuans = remainingWalletYuans,
                    isDraftCheckboxChecked = isDraftCheckboxChecked,
                    onDraftCheckboxChange = { isDraftCheckboxChecked = it },
                    isCompactPhone = isCompactPhone,
                    colors = colors,
                    onOpenBuyYuans = { showBuyYuansDialog = true }
                )
                Step1SupplierLogisticsCard(
                    order = order,
                    selectedTransitaire = selectedTransitaire,
                    transitairesCount = transitaires.size,
                    isMaritime = isMaritime,
                    parcels = parcels,
                    isCompactPhone = isCompactPhone,
                    colors = colors,
                    onEditReference = { showEditReferenceDialog = true },
                    onEditDispatch = { showEditDispatchDialog = true },
                    onSelectTransitaire = { showSelectTransitaireDialog = true },
                    onCopyAddress = { addr -> copyToClipboard("Adresse entrepôt Chine", addr, "Adresse entrepôt copiée !") },
                    onEditTracking = { parcel ->
                        editingOldTracking = parcel.trackingNumber
                        newTrackingInput = parcel.trackingNumber
                        newTrackingCarrierInput = parcel.carrierName
                        showAddTrackingDialog = true
                    },
                    onRemoveTracking = { parcel ->
                        onUpdateOrder(DomesticParcelStorage.removeTracking(order, parcel.trackingNumber))
                    },
                    onAddTracking = {
                        editingOldTracking = null
                        newTrackingInput = ""
                        newTrackingCarrierInput = ""
                        showAddTrackingDialog = true
                    }
                )
                Step1BottomActionsCard(
                    isOrderValid = isOrderValid,
                    isSubmitting = isSubmitting,
                    isAlreadyDebited = isAlreadyDebited,
                    isDraftCheckboxChecked = isDraftCheckboxChecked,
                    remainingWalletYuans = remainingWalletYuans,
                    totalOrderYuan = totalOrderYuan,
                    totalOrderAr = totalOrderAr,
                    colors = colors,
                    onConfirmClick = confirmAction,
                    onSaveWithoutAdvancingClick = saveWithoutAdvancingAction
                )
            }
        }
    }

    if (showBuyYuansDialog) {
        BuyYuansDialog(
            accounts = accounts,
            defaultRate = cmupRate,
            onDismiss = { showBuyYuansDialog = false },
            onConfirm = { amountYuan, rateAr, platform, sourceAccountName, purchaseDateMillis ->
                onBuyYuans(amountYuan, rateAr, platform, sourceAccountName, purchaseDateMillis)
                if (appliedRateText.isBlank() || appliedRateText == "0") {
                    appliedRateText = rateAr.toInt().toString()
                }
                showBuyYuansDialog = false
            }
        )
    }

    if (showEditOrderDialog) {
        Step1EditOrderDialog(
            order = order,
            colors = colors,
            onDismiss = { showEditOrderDialog = false },
            onSave = { updated ->
                onUpdateOrder(updated)
                showEditOrderDialog = false
            }
        )
    }

    if (showEditDomesticShippingDialog) {
        Step1EditDomesticShippingDialog(
            order = order,
            colors = colors,
            onDismiss = { showEditDomesticShippingDialog = false },
            onApply = { valYuan ->
                onUpdateOrder(order.copy(domesticShippingYuan = valYuan))
                showEditDomesticShippingDialog = false
            }
        )
    }

    if (showEditReferenceDialog) {
        Step1EditReferenceDialog(
            order = order,
            colors = colors,
            onDismiss = { showEditReferenceDialog = false },
            onSave = { platform, ref ->
                onUpdateOrder(order.copy(platform = platform, referenceCode = ref))
                showEditReferenceDialog = false
            }
        )
    }

    if (showEditDispatchDialog) {
        Step1EditDispatchDialog(
            order = order,
            colors = colors,
            onDismiss = { showEditDispatchDialog = false },
            onSave = { delay ->
                onUpdateOrder(order.copy(supplierDispatchDelay = delay))
                showEditDispatchDialog = false
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

    if (showAddTrackingDialog) {
        Step1AddTrackingDialog(
            order = order,
            parcels = parcels,
            editingOldTracking = editingOldTracking,
            initialTracking = newTrackingInput,
            initialCarrier = newTrackingCarrierInput,
            colors = colors,
            onDismiss = { showAddTrackingDialog = false },
            onSave = { updated ->
                onUpdateOrder(updated)
                showAddTrackingDialog = false
            }
        )
    }
}
