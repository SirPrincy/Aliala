package com.example.ui.business.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.TariffGridParser
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

internal fun formatYuanCompact(value: Double): String {
    if (value <= 0.0) return "0"
    return if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format(Locale.US, "%.2f", value)
}

@Composable
internal fun WizardStepHeaderCard(
    stepNumber: Int,
    stepTitle: String,
    statusText: String,
    statusIcon: ImageVector,
    statusAccentColor: Color,
    statusContainerColor: Color,
    totalSteps: Int = 6
) {
    val colors = rememberTransitaireColors()
    val infiniteTransition = rememberInfiniteTransition(label = "wizard_step_pulse_$stepNumber")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stepNumber.toString(),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.cyanAccent
                        )
                    }
                    Text(
                        text = "ÉTAPE $stepNumber SUR $totalSteps",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.8.sp
                    )
                }

                Text(
                    text = stepTitle,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.cyanAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 6-capsule unified step progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 1..totalSteps) {
                    val capsuleColor = when {
                        i < stepNumber -> colors.cyanSolidBg
                        i == stepNumber -> colors.cyanSolidBg.copy(alpha = pulseAlpha)
                        else -> colors.badgeNeutralBg
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(capsuleColor)
                    )
                }
            }

            // Unified Status Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusContainerColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusAccentColor,
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = statusText,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = statusAccentColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(statusAccentColor.copy(alpha = pulseAlpha))
                    )
                }
            }
        }
    }
}

@Composable
internal fun WizardSelectTransitaireDialog(
    order: BusinessOrderEntity,
    transitaires: List<CatalogueTransitaireSummaryUi>,
    onDismiss: () -> Unit,
    onApply: (BusinessOrderEntity) -> Unit
) {
    val colors = rememberTransitaireColors()
    val initialMaritime = order.freightMode.equals("MARITIME", ignoreCase = true) ||
        order.freightMode.equals("SEA", ignoreCase = true)
    var selectedModeTmp by remember { mutableStateOf(if (initialMaritime) "MARITIME" else "AÉRIEN") }
    var selectedCarrierTmp by remember { mutableStateOf(order.carrier) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Transitaire & Mode de fret",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.toggleTrackBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("AÉRIEN" to "Aérien", "MARITIME" to "Maritime").forEach { (code, label) ->
                        val active = selectedModeTmp == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) colors.cyanAccent else Color.Transparent)
                                .clickable { selectedModeTmp = code }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (active) Color.White else colors.textSecondary
                            )
                        }
                    }
                }

                if (transitaires.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.innerCardBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Aucun transitaire enregistré dans votre catalogue. Ajoutez-en un dans Catalogue > Transitaires, ou saisissez un nom ci-dessous.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    OutlinedTextField(
                        value = selectedCarrierTmp,
                        onValueChange = { selectedCarrierTmp = it },
                        label = { Text("Nom du transitaire (optionnel)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        transitaires.forEach { t ->
                            val isSel = selectedCarrierTmp.equals(t.name, ignoreCase = true)
                            val rateLine = if (selectedModeTmp == "MARITIME") {
                                "${CurrencyFormatter.formatAriary(t.seaRateAr)}/m³ · ${t.seaLeadDays}"
                            } else {
                                "${CurrencyFormatter.formatAriary(t.airRateAr)}/kg · ${t.airLeadDays}"
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) colors.cyanSoftBg else colors.innerCardBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedCarrierTmp = t.name }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = t.name,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.textPrimary
                                        )
                                        if (t.chinaWarehouseAddress.isNotBlank()) {
                                            Text(
                                                text = t.chinaWarehouseAddress,
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = colors.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = rateLine,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = colors.cyanAccent
                                        )
                                    }
                                    if (isSel) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = colors.cyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val chosen = transitaires.firstOrNull { it.name.equals(selectedCarrierTmp, ignoreCase = true) }
                    val leadStr = if (chosen != null) {
                        if (selectedModeTmp == "MARITIME") chosen.seaLeadDays else chosen.airLeadDays
                    } else ""
                    val (_, maxDays) = if (leadStr.isNotBlank()) {
                        TariffGridParser.parseMinMaxDays(
                            leadStr,
                            if (selectedModeTmp == "MARITIME") 30 else 7,
                            if (selectedModeTmp == "MARITIME") 45 else 10
                        )
                    } else {
                        (if (selectedModeTmp == "MARITIME") 30 else 7) to (if (selectedModeTmp == "MARITIME") 45 else 10)
                    }
                    val newEstimatedArrival = order.orderDate + maxDays * 86_400_000L
                    onApply(
                        order.copy(
                            carrier = selectedCarrierTmp.trim(),
                            freightMode = selectedModeTmp,
                            estimatedArrival = newEstimatedArrival
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
            ) {
                Text("Appliquer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
