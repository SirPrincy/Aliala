package com.example.ui.business.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.theme.PlusJakartaSans

@Composable
fun LogisticWizardScreen(
    order: BusinessOrderEntity,
    cmupRate: Long,
    availableYuans: Double = 1840.0,
    onSpendYuans: (amount: Double, rateAr: Double, note: String) -> Unit = { _, _, _ -> },
    onRefundYuans: (amount: Double, rateAr: Double, note: String) -> Unit = { _, _, _ -> },
    onDismiss: () -> Unit,
    onAdvanceStatus: (BusinessOrderEntity) -> Unit = {},
    onDeleteOrder: (BusinessOrderEntity) -> Unit,
    onUpdateOrder: (BusinessOrderEntity) -> Unit = {},
    onViewStock: () -> Unit = {},
    onCreateSale: () -> Unit = {},
    onReorder: () -> Unit = {}
) {
    val context = LocalContext.current
    var isSuccessScreen by rememberSaveable { mutableStateOf(order.status == OrderStatus.SOLD.code || order.status == OrderStatus.COMPLETED.code) }
    var showExitConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val orderStatusEnum = OrderStatus.fromCode(order.status)
    val currentStepIndex = orderStatusEnum.stepIndex

    val scrollState = rememberScrollState()

    // Remise à zéro du scroll au changement d'étape
    LaunchedEffect(currentStepIndex, isSuccessScreen) {
        scrollState.scrollTo(0)
    }

    // Gestion du retour physique Android
    BackHandler {
        showExitConfirmDialog = true
    }

    Dialog(
        onDismissRequest = { showExitConfirmDialog = true },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFFAF8FF)
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
                    // TOP APP HEADER: "← Business Import Wizard    [notif] [avatar]"
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFAF8FF).copy(alpha = 0.95f),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { showExitConfirmDialog = true },
                                    modifier = Modifier.testTag("btn_back_wizard")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Retour",
                                        tint = Color(0xFF131B2E),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = "Business Import Wizard",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { Toast.makeText(context, "Pas de nouvelle notification", Toast.LENGTH_SHORT).show() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color(0xFF3D494C),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00687A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // SCROLLABLE CONTENT WITH STEPS 1 TO 6 OR FINAL SUCCESS VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (isSuccessScreen) {
                            // ÉCRAN FINAL DE SUCCÈS : Achat Terminé avec Succès !
                            LogisticSuccessView(
                                order = order,
                                cmupRate = cmupRate,
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
                        } else if (currentStepIndex == 0) {
                            // ÉTAPE 1 SUR 6 : VALIDATION & DÉBIT (Étape 1)
                            LogisticStep1View(
                                order = order,
                                cmupRate = cmupRate,
                                availableYuans = availableYuans,
                                onSpendYuans = onSpendYuans,
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
                        } else if (currentStepIndex == 1) {
                            // ÉTAPE 2 SUR 6 : VERS L'ENTREPÔT TRANSITAIRE (Étape 2)
                            LogisticStep2View(
                                order = order,
                                cmupRate = cmupRate,
                                onAdvanceToWeighing = { updated ->
                                    onUpdateOrder(updated)
                                },
                                onPreviousStep = {
                                    val updated = order.copy(status = OrderStatus.ORDERED.code)
                                    onUpdateOrder(updated)
                                    Toast.makeText(context, "Retour à l'étape 1", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else if (currentStepIndex == 2) {
                            // ÉTAPE 3 SUR 6 : ARRIVÉ À L'ENTREPÔT DU TRANSITAIRE (Étape 3)
                            LogisticStep3View(
                                order = order,
                                cmupRate = cmupRate,
                                onAdvanceToFlight = { updated ->
                                    onUpdateOrder(updated)
                                },
                                onPreviousStep = {
                                    val updated = order.copy(status = OrderStatus.WAREHOUSE.code)
                                    onUpdateOrder(updated)
                                    Toast.makeText(context, "Retour à l'étape 2 (Livraison Chine)", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else if (currentStepIndex == 3) {
                            // ÉTAPE 4 SUR 6 : EN TRANSIT VERS MADAGASCAR (Étape 4)
                            LogisticStep4View(
                                order = order,
                                cmupRate = cmupRate,
                                onAdvanceToCustoms = { updated ->
                                    onUpdateOrder(updated)
                                },
                                onPreviousStep = {
                                    val updated = order.copy(status = OrderStatus.GUANGZHOU.code)
                                    onUpdateOrder(updated)
                                    Toast.makeText(context, "Retour à l'étape 3 (Entrepôt transitaire)", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else if (currentStepIndex == 4) {
                            // ÉTAPE 5 SUR 6 : ARRIVÉ À MADAGASCAR & DÉDOUANEMENT (Étape 5)
                            LogisticStep5View(
                                order = order,
                                cmupRate = cmupRate,
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
                            // ÉTAPE 6 SUR 6 : CONTRÔLE QUALITÉ & ENTRÉE EN STOCK (Étape 6)
                            LogisticStep6View(
                                order = order,
                                cmupRate = cmupRate,
                                onRefundYuans = onRefundYuans,
                                onCompleteWizard = { updated ->
                                    onUpdateOrder(updated)
                                    Toast.makeText(context, "Stock mis à jour avec succès !", Toast.LENGTH_SHORT).show()
                                    isSuccessScreen = true
                                },
                                onPreviousStep = {
                                    val updated = order.copy(status = OrderStatus.CUSTOMS.code)
                                    onUpdateOrder(updated)
                                    Toast.makeText(context, "Retour à l'étape 5 (Dédouanement Ivato)", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
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
}
