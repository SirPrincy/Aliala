package com.example.ui.business.wizard

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.business.CatalogueFamilySummaryUi
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.PlusJakartaSans

@Composable
fun LogisticWizardScreen(
    order: BusinessOrderEntity,
    cmupRate: Long,
    availableYuans: Double = 0.0,
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    families: List<CatalogueFamilySummaryUi> = emptyList(),
    accounts: List<BusinessAccountEntity> = emptyList(),
    onSpendYuans: (amount: Double, rateAr: Double, yuanAccountName: String, note: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onRefundYuans: (amount: Double, rateAr: Double, yuanAccountName: String, note: String) -> Unit = { _, _, _, _ -> },
    onBuyYuans: (amountYuan: Double, rateAr: Double, platform: String, sourceAccountName: String, purchaseDateMillis: Long) -> Unit = { _, _, _, _, _ -> },
    onPayOrderFreight: (order: BusinessOrderEntity, amountAr: Long, accountName: String) -> Unit = { _, _, _ -> },
    onDismiss: () -> Unit,
    onAdvanceStatus: (BusinessOrderEntity) -> Unit = {},
    onEditOrder: (BusinessOrderEntity) -> Unit = {},
    onDeleteOrder: (BusinessOrderEntity) -> Unit,
    onUpdateOrder: (BusinessOrderEntity) -> Unit = {},
    onViewStock: () -> Unit = {},
    onCreateSale: () -> Unit = {},
    onReorder: () -> Unit = {}
) {
    val context = LocalContext.current
    val orderStatusEnum = OrderStatus.fromCode(order.status)
    val currentStepIndex = orderStatusEnum.stepIndex
    var isSuccessScreen by rememberSaveable(order.id) {
        mutableStateOf(orderStatusEnum.stepIndex >= 6)
    }
    var viewedStepIndex by rememberSaveable(order.id) {
        mutableIntStateOf(currentStepIndex.coerceIn(0, 5))
    }
    var lastObservedStatusCode by rememberSaveable(order.id) {
        mutableStateOf(order.status)
    }
    var showExitConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showStatusPickerDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(order.id, order.status) {
        if (order.status != lastObservedStatusCode) {
            lastObservedStatusCode = order.status
            val mapped = OrderStatus.fromCode(order.status)
            if (mapped.stepIndex >= 6) {
                isSuccessScreen = true
            } else {
                isSuccessScreen = false
                viewedStepIndex = mapped.stepIndex.coerceIn(0, 5)
            }
        }
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(viewedStepIndex, isSuccessScreen) {
        scrollState.scrollTo(0)
    }

    BackHandler {
        showExitConfirmDialog = true
    }

    val colors = rememberTransitaireColors()

    Dialog(
        onDismissRequest = { showExitConfirmDialog = true },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.pageBg
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.pageBg.copy(alpha = 0.95f),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = { showExitConfirmDialog = true },
                                    modifier = Modifier.testTag("btn_back_wizard")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Retour",
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = "Assistant d'import",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = colors.cyanSoftBg,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .clickable { showStatusPickerDialog = true }
                                        .testTag("btn_wizard_lot_status_pill")
                                ) {
                                    Text(
                                        text = if (isSuccessScreen) "Bilan" else "Lot #${order.id} · ${viewedStepIndex + 1}/6",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = colors.cyanAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onEditOrder(order) },
                                    modifier = Modifier.testTag("btn_wizard_edit_order")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Modifier l'achat",
                                        tint = colors.cyanAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { showDeleteConfirmDialog = true },
                                    modifier = Modifier.testTag("btn_wizard_delete_order")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Supprimer l'achat",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    WizardInteractiveStepBar(
                        viewedStepIndex = viewedStepIndex,
                        actualStatus = orderStatusEnum,
                        isSuccessScreen = isSuccessScreen,
                        colors = colors,
                        onSelectStep = { idx ->
                            isSuccessScreen = false
                            viewedStepIndex = idx.coerceIn(0, 5)
                        },
                        onSelectSuccess = {
                            isSuccessScreen = true
                        },
                        onOpenStatusPicker = { showStatusPickerDialog = true }
                    )

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        val contentMaxWidth = if (maxWidth >= 840.dp) 1040.dp else 680.dp
                        val horizontalPad = if (maxWidth >= 600.dp) 24.dp else 16.dp
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = contentMaxWidth)
                                .padding(horizontal = horizontalPad, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (!isSuccessScreen && (viewedStepIndex != currentStepIndex.coerceIn(0, 5) || currentStepIndex >= 6)) {
                                WizardEditingOtherStepBanner(
                                    viewedStepIndex = viewedStepIndex,
                                    actualStatus = orderStatusEnum,
                                    colors = colors,
                                    onActivateViewedStep = {
                                        val targetStatus = WIZARD_IMPORT_STEPS.getOrNull(viewedStepIndex)?.status
                                        if (targetStatus != null) {
                                            onUpdateOrder(order.copy(status = targetStatus.code))
                                            Toast.makeText(
                                                context,
                                                "Étape active modifiée : ${targetStatus.label}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            }

                            if (isSuccessScreen) {
                                LogisticSuccessView(
                                    order = order,
                                    cmupRate = cmupRate,
                                    transitaires = transitaires,
                                    onDismiss = onDismiss,
                                    onViewStock = {
                                        onViewStock()
                                        onDismiss()
                                    },
                                    onCreateSale = {
                                        onCreateSale()
                                        onDismiss()
                                    },
                                    onReorder = {
                                        onReorder()
                                        onDismiss()
                                    }
                                )
                            } else if (viewedStepIndex == 0) {
                                LogisticStep1View(
                                    order = order,
                                    cmupRate = cmupRate,
                                    availableYuans = availableYuans,
                                    transitaires = transitaires,
                                    families = families,
                                    accounts = accounts,
                                    onUpdateOrder = onUpdateOrder,
                                    onSpendYuans = onSpendYuans,
                                    onBuyYuans = onBuyYuans,
                                    onConfirmOrder = { updated ->
                                        onUpdateOrder(updated)
                                    },
                                    onSaveAsDraft = {
                                        val updated = order.copy(status = OrderStatus.DRAFT.code)
                                        onUpdateOrder(updated)
                                        Toast.makeText(context, "Enregistré comme brouillon", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                )
                            } else if (viewedStepIndex == 1) {
                                LogisticStep2View(
                                    order = order,
                                    cmupRate = cmupRate,
                                    transitaires = transitaires,
                                    families = families,
                                    onUpdateOrder = onUpdateOrder,
                                    onAdvanceToWeighing = { updated ->
                                        onUpdateOrder(updated)
                                    },
                                    onPreviousStep = {
                                        val updated = order.copy(status = OrderStatus.ORDERED.code)
                                        onUpdateOrder(updated)
                                        Toast.makeText(context, "Retour à l'étape 1", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else if (viewedStepIndex == 2) {
                                LogisticStep3View(
                                    order = order,
                                    cmupRate = cmupRate,
                                    transitaires = transitaires,
                                    onUpdateOrder = onUpdateOrder,
                                    onAdvanceToFlight = { updated ->
                                        onUpdateOrder(updated)
                                    },
                                    onPreviousStep = {
                                        val updated = order.copy(status = OrderStatus.WAREHOUSE.code)
                                        onUpdateOrder(updated)
                                        Toast.makeText(context, "Retour à l'étape 2 (Livraison Chine)", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else if (viewedStepIndex == 3) {
                                LogisticStep4View(
                                    order = order,
                                    cmupRate = cmupRate,
                                    transitaires = transitaires,
                                    onUpdateOrder = onUpdateOrder,
                                    onAdvanceToCustoms = { updated ->
                                        onUpdateOrder(updated)
                                    },
                                    onPreviousStep = {
                                        val updated = order.copy(status = OrderStatus.GUANGZHOU.code)
                                        onUpdateOrder(updated)
                                        Toast.makeText(context, "Retour à l'étape 3 (Entrepôt transitaire)", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else if (viewedStepIndex == 4) {
                                LogisticStep5View(
                                    order = order,
                                    cmupRate = cmupRate,
                                    transitaires = transitaires,
                                    onUpdateOrder = onUpdateOrder,
                                    onAdvanceToQualityControl = { updated ->
                                        onUpdateOrder(updated)
                                    },
                                    onPreviousStep = {
                                        val updated = order.copy(status = OrderStatus.FLIGHT.code)
                                        onUpdateOrder(updated)
                                        Toast.makeText(context, "Retour à l'étape 4 (Transit international)", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                LogisticStep6View(
                                    order = order,
                                    cmupRate = cmupRate,
                                    transitaires = transitaires,
                                    accounts = accounts,
                                    onUpdateOrder = onUpdateOrder,
                                    onRefundYuans = onRefundYuans,
                                    onPayOrderFreight = onPayOrderFreight,
                                    onCompleteWizard = { updated ->
                                        onUpdateOrder(updated)
                                        Toast.makeText(context, "Stock mis à jour avec succès !", Toast.LENGTH_SHORT).show()
                                        isSuccessScreen = true
                                    },
                                    onPreviousStep = {
                                        val updated = order.copy(status = OrderStatus.CUSTOMS.code)
                                        onUpdateOrder(updated)
                                        val isMaritimeOrder = order.freightMode.equals("MARITIME", ignoreCase = true) || order.freightMode.equals("SEA", ignoreCase = true)
                                        val portLabel = if (isMaritimeOrder) "Toamasina" else "Ivato"
                                        Toast.makeText(context, "Retour à l'étape 5 (Arrivée $portLabel)", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = {
                Text(
                    text = "Quitter l'assistant d'import ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Toutes vos données et étapes validées sont sauvegardées. Vous pourrez reprendre cet import à tout moment.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Quitter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("Continuer l'import")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Supprimer cet achat ?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                val paidYuan = if (order.yuanDebited) {
                    val base = if (order.yuanDebitedAmount > 0.0) order.yuanDebitedAmount else (order.priceYuan * order.quantityBought) + order.domesticShippingYuan
                    (base - order.refundYuan).coerceAtLeast(0.0)
                } else 0.0
                val yuanInfo = if (paidYuan > 0.0) {
                    val fmt = if (paidYuan % 1.0 == 0.0) "${paidYuan.toInt()}" else String.format(java.util.Locale.US, "%.2f", paidYuan)
                    " Les ¥ $fmt débités seront restitués sur votre compte Yuan."
                } else ""
                Text(
                    text = "Voulez-vous vraiment supprimer « ${order.product} » (${order.quantityBought} pcs) ?$yuanInfo Cette action est irréversible.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteOrder(order)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("btn_wizard_confirm_delete")
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showStatusPickerDialog) {
        WizardChangeOrderStatusDialog(
            currentStatus = orderStatusEnum,
            colors = colors,
            onDismiss = { showStatusPickerDialog = false },
            onSelectStatus = { picked ->
                showStatusPickerDialog = false
                lastObservedStatusCode = picked.code
                onUpdateOrder(order.copy(status = picked.code))
                if (picked.stepIndex >= 6) {
                    isSuccessScreen = true
                } else {
                    isSuccessScreen = false
                    viewedStepIndex = picked.stepIndex.coerceIn(0, 5)
                }
                Toast.makeText(context, "Étape mise à jour : ${picked.label}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
