package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
internal fun Step5SecurityAndInvoiceCard(
    carrier: String,
    freightCostAr: Long
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF2F3FF)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF00687A),
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Règle de sécurisation du Stock",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF131B2E)
                        )
                        Text(
                            text = "Le stock ne sera incrémenté qu'après validation complète du contrôle qualité à l'Étape 6. Vos bilans d'inventaire restent protégés.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEAEDFF)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFF712AE2),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Facture ${carrier.ifBlank { "Transitaire" }}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = "Solde fret à régler",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyFormatter.formatAriary(freightCostAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF712AE2)
                        )
                        Text(
                            text = "Règlement au retrait",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = Color(0xFF3D494C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun Step5LiveCostCard(
    totalDisbursedAr: Long,
    merchandiseTotalAr: Long,
    domesticPortAr: Long,
    freightCostAr: Long
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEAEDFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = Color(0xFF00687A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Coût de revient en direct",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF131B2E)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = Color(0xFFACEDFF).copy(alpha = 0.40f)
                ) {
                    Text(
                        text = "Consolidé",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF00687A),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFAF8FF)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Total déboursé réel",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = Color(0xFF3D494C)
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(totalDisbursedAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFF00687A)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Marchandise usine (Chine)", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                        Text(CurrencyFormatter.formatAriary(merchandiseTotalAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Port Chine & logistique locale", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                        Text(CurrencyFormatter.formatAriary(domesticPortAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fret & Dédouanement Ivato", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                        Text(CurrencyFormatter.formatAriary(freightCostAr), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF131B2E))
                    }
                }
            }
        }
    }
}

@Composable
internal fun Step5BottomActionsCard(
    receivedParcelsCount: Int,
    expectedParcels: Int,
    onValidateArrival: () -> Unit,
    onOpenIncidentDialog: () -> Unit
) {
    val canAdvance = receivedParcelsCount > 0
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onValidateArrival,
                enabled = canAdvance,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_validate_arrival_and_qc"),
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
                        text = if (canAdvance) "Valider l'arrivée ($receivedParcelsCount/$expectedParcels colis) et lancer le contrôle" else "Sélectionnez au moins 1 colis reçu",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenIncidentDialog)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = null,
                    tint = Color(0xFFBA1A1A),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Signaler un carton endommagé ou manquant",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFFBA1A1A)
                )
            }
        }
    }
}

@Composable
internal fun Step5Dialogs(
    order: BusinessOrderEntity,
    expectedParcels: Int,
    weightKg: Double,
    showAddPhotoDialog: Boolean,
    showIncidentDialog: Boolean,
    incidentDescription: String,
    showColisDetailsDialog: Boolean,
    onDismissAddPhoto: () -> Unit,
    onConfirmAddPhoto: () -> Unit,
    onDismissIncident: () -> Unit,
    onIncidentDescriptionChange: (String) -> Unit,
    onConfirmIncident: () -> Unit,
    onDismissColisDetails: () -> Unit
) {
    if (showAddPhotoDialog) {
        AlertDialog(
            onDismissRequest = onDismissAddPhoto,
            title = {
                Text("Prendre une photo à l'arrivée", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Photographiez le déchargement des cartons sur le quai d'Ivato pour certifier l'intégrité des scellés.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = Color(0xFF3D494C)
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmAddPhoto,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00687A))
                ) {
                    Text("Capturer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissAddPhoto) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showIncidentDialog) {
        AlertDialog(
            onDismissRequest = onDismissIncident,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFBA1A1A))
                    Text("Signaler une anomalie", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Précisez les dommages constatés sur les colis au hub Ivato (carton écrasé, bande décollée, manquant) :",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = Color(0xFF3D494C)
                    )
                    OutlinedTextField(
                        value = incidentDescription,
                        onValueChange = onIncidentDescriptionChange,
                        placeholder = { Text("Ex: Colis 2 coin écrasé, ruban déchiré...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirmIncident,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissIncident) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showColisDetailsDialog) {
        AlertDialog(
            onDismissRequest = onDismissColisDetails,
            title = {
                Text("Détail inspection Colis", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Poids estimé : ${if (expectedParcels > 0 && weightKg > 0) String.format(Locale.US, "%.1f", weightKg / expectedParcels) + " kg" else "—"}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF131B2E)
                    )
                    val contentSummary = order.variants.ifBlank { order.product.ifBlank { "Articles commandés" } }
                    Text("Contenu théorique : $contentSummary", fontFamily = Inter, fontSize = 12.sp, color = Color(0xFF3D494C))
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF2F3FF), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Observation déchargement : Colis inspecté à Ivato avec scellé ${order.carrier.ifBlank { "transitaire" }} intact. Aucun article exposé.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = Color(0xFF3D494C),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissColisDetails,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00687A))
                ) {
                    Text("Compris", color = Color.White)
                }
            }
        )
    }
}
