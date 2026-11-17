package com.example.ui.business.wizard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.util.Locale

@Composable
internal fun Step4TransitCostCard(
    order: BusinessOrderEntity,
    isMaritime: Boolean,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    totalCostOfGoodsAr: Long,
    unitCostAr: Long,
    totalQty: Int,
    unitPriceYuan: Double,
    merchandiseTotalAr: Long,
    domesticPortAr: Long,
    weightKg: Double,
    volumeM3: Double,
    freightCostAr: Long
) {
    val colors = rememberTransitaireColors()
    val chevronRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "chevron")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(22.dp))
                    Column {
                        Text("Coût engagé en transit", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                        Text("${CurrencyFormatter.formatAriary(totalCostOfGoodsAr)} cumulés (~${CurrencyFormatter.formatAriary(unitCostAr)} / u)", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                    }
                }

                Icon(Icons.Default.ExpandMore, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(22.dp).rotate(chevronRotation))
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Marchandise ($totalQty u)", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                        Text("¥ ${(unitPriceYuan * totalQty).toInt()} · ${CurrencyFormatter.formatAriary(merchandiseTotalAr)}", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Port local Chine", fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                        Text("¥ ${String.format(Locale.US, "%.2f", order.domesticShippingYuan)} · ${CurrencyFormatter.formatAriary(domesticPortAr)}", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val freightLineLabel = if (isMaritime) {
                            "Fret maritime engagé (${String.format(Locale.FRENCH, "%.5f", volumeM3)} m³)"
                        } else {
                            "Fret aérien engagé (${String.format(Locale.US, "%.1f", weightKg)} kg)"
                        }
                        Text(freightLineLabel, fontFamily = Inter, fontSize = 12.sp, color = colors.textSecondary)
                        Text(CurrencyFormatter.formatAriary(freightCostAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Coût unitaire provisoire", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.cyanAccent)
                        Text("${CurrencyFormatter.formatAriary(unitCostAr)} / pièce", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.cyanAccent)
                    }
                }
            }
        }
    }
}

@Composable
internal fun Step4ReportIssueBanner(onClick: () -> Unit) {
    val colors = rememberTransitaireColors()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = colors.innerCardBg
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = colors.dangerAccent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Signaler un retard ou problème de fret", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textSecondary)
        }
    }
}

@Composable
internal fun Step4BottomActionsCard(
    onValidateCustoms: () -> Unit,
    onPreviousStep: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onValidateCustoms,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_validate_flight_to_mada"),
                colors = ButtonDefaults.buttonColors(containerColor = colors.violetSolidBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Text("Valider : En route vers Madagascar", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPreviousStep)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Modifier les infos de transport", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = colors.textSecondary)
            }
        }
    }
}

@Composable
internal fun Step4RadarDialog(
    isMaritime: Boolean,
    carrierName: String,
    originHubLabel: String,
    arrivalPortCity: String,
    awbNumber: String,
    estimatedArrival: Long,
    onDismiss: () -> Unit
) {
    val colors = rememberTransitaireColors()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(if (isMaritime) Icons.Default.LocalShipping else Icons.Default.Flight, contentDescription = null, tint = colors.violetAccent)
                Text(if (isMaritime) "Suivi Navire Cargo" else "Suivi Vol Cargo", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Transporteur : $carrierName", fontFamily = Inter, fontSize = 12.sp, color = colors.textPrimary)
                Text("Bordereau / Référence : $awbNumber", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = colors.textSecondary)
                Surface(shape = RoundedCornerShape(8.dp), color = colors.innerCardBg, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Acheminement :", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = colors.violetAccent)
                        Text("De $originHubLabel vers $arrivalPortCity (Madagascar)", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = if (estimatedArrival > 0L) "Arrivée estimée : ${DateUtils.formatShortDate(estimatedArrival)}" else "Arrivée estimée : Selon délai transitaire",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.cyanAccent
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = colors.violetSolidBg)) {
                Text("Fermer", color = Color.White)
            }
        }
    )
}

@Composable
internal fun Step4IncidentDialog(
    incidentText: String,
    onIncidentTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = rememberTransitaireColors()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = colors.dangerAccent)
                Text("Signaler un retard ou anomalie", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Décrivez l'anomalie constatée sur le vol cargo (retard de correspondance, douane bloquante, météo) :",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                OutlinedTextField(
                    value = incidentText,
                    onValueChange = onIncidentTextChange,
                    placeholder = { Text("Ex: Retard douane, colis endommagé...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent)) {
                Text("Envoyer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
