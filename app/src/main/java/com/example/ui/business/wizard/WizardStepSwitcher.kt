package com.example.ui.business.wizard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans

internal data class WizardStepChoice(
    val index: Int,
    val shortTitle: String,
    val status: OrderStatus
)

internal val WIZARD_IMPORT_STEPS = listOf(
    WizardStepChoice(0, "1. Commande", OrderStatus.ORDERED),
    WizardStepChoice(1, "2. Chine", OrderStatus.WAREHOUSE),
    WizardStepChoice(2, "3. Hub", OrderStatus.GUANGZHOU),
    WizardStepChoice(3, "4. Transit", OrderStatus.FLIGHT),
    WizardStepChoice(4, "5. Mada", OrderStatus.CUSTOMS),
    WizardStepChoice(5, "6. Réception", OrderStatus.QUALITY_CONTROL)
)

@Composable
internal fun WizardInteractiveStepBar(
    viewedStepIndex: Int,
    actualStatus: OrderStatus,
    isSuccessScreen: Boolean,
    colors: TransitaireColors,
    onSelectStep: (Int) -> Unit,
    onSelectSuccess: () -> Unit,
    onOpenStatusPicker: () -> Unit
) {
    val actualStepIdx = actualStatus.stepIndex
    val hasSummaryStep = actualStepIdx >= 6

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.cardBg,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WIZARD_IMPORT_STEPS.forEach { step ->
                val isSelected = !isSuccessScreen && viewedStepIndex == step.index
                val isCompleted = step.index < actualStepIdx
                val isCurrentActual = step.index == actualStepIdx.coerceIn(0, 5) && !hasSummaryStep

                val bgColor = when {
                    isSelected -> colors.cyanAccent
                    isCompleted -> colors.cyanSoftBg
                    isCurrentActual -> colors.violetSoftBg
                    else -> colors.innerCardBg
                }
                val textColor = when {
                    isSelected -> Color.White
                    isCompleted -> colors.cyanAccent
                    isCurrentActual -> colors.violetAccent
                    else -> colors.textSecondary
                }
                val border = if (isCurrentActual && !isSelected) {
                    BorderStroke(1.dp, colors.violetAccent.copy(alpha = 0.45f))
                } else null

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = bgColor,
                    border = border,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(9999.dp))
                        .clickable { onSelectStep(step.index) }
                        .testTag("wizard_step_tab_${step.index + 1}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isCompleted && !isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = step.shortTitle,
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (isSelected || isCurrentActual) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = textColor
                        )
                    }
                }
            }

            if (hasSummaryStep) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (isSuccessScreen) colors.cyanAccent else colors.cyanSoftBg,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(9999.dp))
                        .clickable { onSelectSuccess() }
                        .testTag("wizard_step_tab_summary")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isSuccessScreen) Color.White else colors.cyanAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Bilan",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isSuccessScreen) Color.White else colors.cyanAccent
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = colors.violetSoftBg,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(9999.dp))
                    .clickable { onOpenStatusPicker() }
                    .testTag("btn_wizard_change_status")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Changer l'étape d'import",
                        tint = colors.violetAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Statut",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.violetAccent
                    )
                }
            }
        }
    }
}

@Composable
internal fun WizardEditingOtherStepBanner(
    viewedStepIndex: Int,
    actualStatus: OrderStatus,
    colors: TransitaireColors,
    onActivateViewedStep: () -> Unit
) {
    val targetChoice = WIZARD_IMPORT_STEPS.getOrNull(viewedStepIndex) ?: return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.violetSoftBg,
        border = BorderStroke(1.dp, colors.violetAccent.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Édition : ${targetChoice.shortTitle}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = colors.violetAccent
                )
                Text(
                    text = "Statut actuel du lot : ${actualStatus.label}",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = colors.violetAccent,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(9999.dp))
                    .clickable { onActivateViewedStep() }
                    .testTag("btn_activate_viewed_step")
            ) {
                Text(
                    text = "Activer cette étape",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
internal fun WizardChangeOrderStatusDialog(
    currentStatus: OrderStatus,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onSelectStatus: (OrderStatus) -> Unit
) {
    val selectableStatuses = listOf(
        OrderStatus.DRAFT to "Brouillon (en attente de paiement)",
        OrderStatus.ORDERED to "Étape 1 · Commandé fournisseur",
        OrderStatus.WAREHOUSE to "Étape 2 · Livraison Chine vers dépôt",
        OrderStatus.GUANGZHOU to "Étape 3 · Arrivé au Hub / Pesée",
        OrderStatus.FLIGHT to "Étape 4 · En transit vers Madagascar",
        OrderStatus.CUSTOMS to "Étape 5 · Arrivée Mada / Dédouanement",
        OrderStatus.QUALITY_CONTROL to "Étape 6 · Contrôle qualité & Réception",
        OrderStatus.DELIVERY_READY to "En stock / Prêt à livrer",
        OrderStatus.COMPLETED to "Importation terminée (Bilan)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Modifier l'étape du lot",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Choisissez directement l'étape active pour ce lot :",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                selectableStatuses.forEach { (status, description) ->
                    val isCurrent = status == currentStatus
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrent) colors.cyanSoftBg else colors.innerCardBg,
                        border = if (isCurrent) BorderStroke(1.dp, colors.cyanAccent) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectStatus(status) }
                            .testTag("wizard_status_option_${status.code}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = description,
                                fontFamily = PlusJakartaSans,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isCurrent) colors.cyanAccent else colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Étape actuelle",
                                    tint = colors.cyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
