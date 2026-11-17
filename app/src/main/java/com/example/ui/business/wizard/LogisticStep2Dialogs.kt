package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.model.DomesticParcelItem
import com.example.ui.business.model.DomesticParcelStorage
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step2AddParcelDialog(
    order: BusinessOrderEntity,
    totalQty: Int,
    parcels: List<DomesticParcelItem>,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onAdd: (DomesticParcelItem) -> Unit
) {
    var carrierInput by remember { mutableStateOf("") }
    var trackingInput by remember { mutableStateOf("") }
    var contentInput by remember {
        mutableStateOf(
            buildString {
                if (totalQty > 0) append("$totalQty u")
                if (order.variants.isNotBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(order.variants)
                } else if (order.product.isNotBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(order.product)
                }
            }
        )
    }
    var routeInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Ajouter un colis", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Transporteur intérieur :",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DomesticParcelStorage.commonCarriers.take(6).forEach { preset ->
                        val selected = carrierInput.equals(preset, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) colors.cyanAccent else colors.badgeNeutralBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { carrierInput = preset }
                        ) {
                            Text(
                                text = preset,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = if (selected) Color.White else colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = carrierInput,
                    onValueChange = { carrierInput = it },
                    label = { Text("Nom du transporteur") },
                    placeholder = { Text("Ex: SF Express, ZTO, YTO...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = trackingInput,
                    onValueChange = {
                        trackingInput = it
                        errorMessage = null
                    },
                    label = { Text("Numéro de tracking *") },
                    placeholder = { Text("Ex: SF160498210344") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    label = { Text("Contenu du colis") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = routeInput,
                    onValueChange = { routeInput = it },
                    label = { Text("Trajet / hub (optionnel)") },
                    placeholder = { Text("Ex: Shenzhen → Guangzhou") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let { msg ->
                    Text(text = msg, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanedTracking = trackingInput.trim()
                    when {
                        cleanedTracking.isEmpty() -> errorMessage = "Veuillez saisir un numéro de suivi."
                        parcels.any { it.trackingNumber.equals(cleanedTracking, ignoreCase = true) } ->
                            errorMessage = "Ce numéro de suivi est déjà enregistré."
                        else -> {
                            onAdd(
                                DomesticParcelItem(
                                    trackingNumber = cleanedTracking,
                                    carrierName = carrierInput.trim().ifBlank {
                                        DomesticParcelStorage.inferCarrierFromTracking(cleanedTracking)
                                    },
                                    contentDescription = contentInput.trim().ifBlank { "Marchandise" },
                                    checkpointTitle = "Pris en charge par le transporteur",
                                    checkpointRoute = routeInput.trim(),
                                    updatedAtMillis = System.currentTimeMillis(),
                                    isDelivered = false
                                )
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
            ) {
                Text("Ajouter", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step2EditParcelDialog(
    parcel: DomesticParcelItem,
    parcels: List<DomesticParcelItem>,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onSave: (DomesticParcelItem) -> Unit
) {
    var carrierInput by remember(parcel) { mutableStateOf(parcel.carrierName) }
    var trackingInput by remember(parcel) { mutableStateOf(parcel.trackingNumber) }
    var contentInput by remember(parcel) { mutableStateOf(parcel.contentDescription) }
    var checkpointInput by remember(parcel) { mutableStateOf(parcel.checkpointTitle) }
    var routeInput by remember(parcel) { mutableStateOf(parcel.checkpointRoute) }
    var isDeliveredState by remember(parcel) { mutableStateOf(parcel.isDelivered) }
    var errorMessage by remember(parcel) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = colors.cyanAccent)
                Text("Suivi & Mise à jour", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = trackingInput,
                    onValueChange = {
                        trackingInput = it
                        errorMessage = null
                    },
                    label = { Text("Numéro de suivi *") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = carrierInput,
                    onValueChange = { carrierInput = it },
                    label = { Text("Transporteur intérieur") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Étape de livraison :",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DomesticParcelStorage.checkpointPresets.forEach { stage ->
                        val active = checkpointInput.equals(stage, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) colors.cyanAccent else colors.badgeNeutralBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    checkpointInput = stage
                                    if (stage.contains("entrepôt transitaire", ignoreCase = true)) {
                                        isDeliveredState = true
                                    }
                                }
                        ) {
                            Text(
                                text = stage,
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = if (active) Color.White else colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = checkpointInput,
                    onValueChange = { checkpointInput = it },
                    label = { Text("Statut personnalisé") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = routeInput,
                    onValueChange = { routeInput = it },
                    label = { Text("Itinéraire / étape") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    label = { Text("Contenu associé") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDeliveredState) colors.successSoftBg else colors.innerCardBg)
                        .clickable { isDeliveredState = !isDeliveredState }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDeliveredState) "Colis réceptionné à l'entrepôt" else "Marquer comme réceptionné",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (isDeliveredState) colors.successAccent else colors.textPrimary
                    )
                    if (isDeliveredState) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = colors.successAccent, modifier = Modifier.size(18.dp))
                    }
                }
                errorMessage?.let { msg ->
                    Text(text = msg, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanedTracking = trackingInput.trim()
                    if (cleanedTracking.isEmpty()) {
                        errorMessage = "Le numéro de suivi est obligatoire."
                    } else if (
                        !cleanedTracking.equals(parcel.trackingNumber, ignoreCase = true) &&
                        parcels.any { it.trackingNumber.equals(cleanedTracking, ignoreCase = true) }
                    ) {
                        errorMessage = "Un autre colis utilise déjà ce numéro."
                    } else {
                        onSave(
                            parcel.copy(
                                trackingNumber = cleanedTracking,
                                carrierName = carrierInput.trim().ifBlank {
                                    DomesticParcelStorage.inferCarrierFromTracking(cleanedTracking)
                                },
                                contentDescription = contentInput.trim().ifBlank { "Marchandise" },
                                checkpointTitle = checkpointInput.trim().ifBlank {
                                    if (isDeliveredState) "Arrivé à l'entrepôt transitaire" else "En cours d'acheminement"
                                },
                                checkpointRoute = routeInput.trim(),
                                updatedAtMillis = System.currentTimeMillis(),
                                isDelivered = isDeliveredState
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
            ) {
                Text("Enregistrer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
internal fun Step2DeleteParcelDialog(
    target: DomesticParcelItem,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Supprimer ce colis ?", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Text(
                text = "Le numéro de suivi ${target.trackingNumber} sera retiré de ce lot.",
                fontFamily = Inter,
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = colors.dangerAccent)
            ) {
                Text("Supprimer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step2EditEtaDialog(
    currentEstimatedArrival: Long,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onApplyNewEta: (Long) -> Unit
) {
    val currentRemainingDays = (((currentEstimatedArrival - System.currentTimeMillis()) / 86_400_000L).toInt()).coerceAtLeast(1)
    var daysInput by remember { mutableStateOf(currentRemainingDays.toString()) }
    var etaError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Ajuster l'arrivée prévue", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Actuelle : ${DateUtils.formatShortDate(currentEstimatedArrival)}",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 3, 5, 7, 15, 30, 45, 60, 90).forEach { presetDays ->
                        val active = daysInput == presetDays.toString()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) colors.cyanAccent else colors.badgeNeutralBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    daysInput = presetDays.toString()
                                    etaError = null
                                }
                        ) {
                            Text(
                                text = "+${presetDays}j",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = if (active) Color.White else colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = daysInput,
                    onValueChange = {
                        daysInput = it.filter { ch -> ch.isDigit() }
                        etaError = null
                    },
                    label = { Text("Nombre de jours à compter d'aujourd'hui") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = etaError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                val previewDays = daysInput.toIntOrNull()
                if (previewDays != null && previewDays > 0) {
                    val previewMillis = System.currentTimeMillis() + previewDays * 86_400_000L
                    Text(
                        text = "Nouvelle date : ${DateUtils.formatShortDate(previewMillis)}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.cyanAccent
                    )
                }
                etaError?.let { msg ->
                    Text(text = msg, fontFamily = Inter, fontSize = 12.sp, color = colors.dangerAccent)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysInput.toIntOrNull()
                    if (days == null || days <= 0) {
                        etaError = "Entrez un nombre de jours supérieur à 0."
                    } else {
                        onApplyNewEta(System.currentTimeMillis() + days * 86_400_000L)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
            ) {
                Text("Valider", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
internal fun Step2PartialProceedDialog(
    deliveredParcelsCount: Int,
    totalParcelsCount: Int,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onConfirmProceed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Réception partielle", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Text(
                text = "$deliveredParcelsCount colis sur $totalParcelsCount sont marqués comme réceptionnés à l'entrepôt. Voulez-vous tout de même passer à l'étape 3 (Pesée) ?",
                fontFamily = Inter,
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmProceed,
                colors = ButtonDefaults.buttonColors(containerColor = colors.cyanAccent)
            ) {
                Text("Passer à la pesée", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Attendre les autres")
            }
        }
    )
}
