package com.example.ui.business.catalogue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.CataloguePlatformSummaryUi
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun AddPlatformDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, subtitle: String, shippingTag: String, secondaryTag: String) -> Unit
) {
    val colors = rememberTransitaireColors()
    var name by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var shippingChoice by remember { mutableStateOf("Port Gratuit (包邮)") }
    var secondaryTag by remember { mutableStateOf("") }

    val shippingOptions = listOf(
        "Port Gratuit (包邮)",
        "Port Payant (¥ 10 - ¥ 25)",
        "Port Payant (Sur devis)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Text(
                text = "Nouvelle plateforme sourcing",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.textPrimary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la plateforme") },
                    placeholder = { Text("ex: AliExpress, Weidian, JD.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Description / Spécialité") },
                    placeholder = { Text("ex: Grossiste électronique Shenzhen") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Mode de port intérieur en Chine",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    shippingOptions.forEach { opt ->
                        val isSelected = shippingChoice == opt
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.violetSoftBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(1.dp, if (isSelected) colors.violetAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { shippingChoice = opt }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = opt,
                                fontFamily = PlusJakartaSans,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) colors.violetAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = secondaryTag,
                    onValueChange = { secondaryTag = it },
                    label = { Text("Condition / Tag") },
                    placeholder = { Text("ex: Livraison rapide état côtier") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            if (subtitle.isBlank()) "Fournisseur Chine" else subtitle.trim(),
                            shippingChoice,
                            secondaryTag.trim()
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.violetSolidBg,
                    contentColor = Color.White
                )
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
internal fun PlatformTransitDetailsDialog(
    platform: CataloguePlatformSummaryUi,
    onDismiss: () -> Unit,
    onUpdateShippingTag: (String) -> Unit,
    onDelete: () -> Unit
) {
    val colors = rememberTransitaireColors()
    val shippingModes = listOf(
        "Port Gratuit (包邮)",
        "Port Payant (Sur devis)",
        "Variable (Sur devis usine)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = platform.name,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.textPrimary
                )
                if (platform.isVerified) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Certifié",
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = platform.subtitle,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Mode de port intérieur (Chine)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.textPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        shippingModes.forEach { mode ->
                            val isSelected = platform.shippingTag.equals(mode, ignoreCase = true) ||
                                (mode.contains("Gratuit") && platform.shippingTag.contains("gratuit", ignoreCase = true)) ||
                                (mode.contains("Payant") && platform.shippingTag.contains("payant", ignoreCase = true)) ||
                                (mode.contains("Variable") && (platform.shippingTag.contains("variable", ignoreCase = true) || platform.shippingTag.contains("devis", ignoreCase = true)))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUpdateShippingTag(mode) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) colors.cyanSoftBg else colors.innerCardBg,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) colors.cyanAccent else colors.border.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = mode,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.cyanAccent else colors.textPrimary
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = colors.cyanAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Logistique & Notes",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.cyanAccent
                        )
                        Text(
                            text = platform.transitNotes.ifBlank { "Aucune note logistique renseignée pour cette plateforme." },
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Dépenses totales", fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            "¥ ${formatYuanAmount(platform.totalSpentYuan)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Équivalent Ariary", fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            CurrencyFormatter.formatAriary(platform.totalSpentAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.cyanAccent
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text("Fermer", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = colors.dangerAccent)
            ) {
                Text("Supprimer", fontFamily = PlusJakartaSans)
            }
        }
    )
}
