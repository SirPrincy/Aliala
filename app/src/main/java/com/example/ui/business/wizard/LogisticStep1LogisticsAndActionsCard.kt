package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.DomesticParcelItem
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.util.Locale

@Composable
internal fun Step1SupplierLogisticsCard(
    order: BusinessOrderEntity,
    selectedTransitaire: CatalogueTransitaireSummaryUi?,
    transitairesCount: Int,
    isMaritime: Boolean,
    parcels: List<DomesticParcelItem>,
    isCompactPhone: Boolean,
    colors: TransitaireColors,
    onEditReference: () -> Unit,
    onEditDispatch: () -> Unit,
    onSelectTransitaire: () -> Unit,
    onCopyAddress: (String) -> Unit,
    onEditTracking: (DomesticParcelItem) -> Unit,
    onRemoveTracking: (DomesticParcelItem) -> Unit,
    onAddTracking: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.violetSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AltRoute,
                        contentDescription = null,
                        tint = colors.violetAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Suivi Logistique Usine",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Numéro de commande fournisseur (${order.platform.ifBlank { "Plateforme" }})",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = colors.textPrimary
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.innerCardBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onEditReference)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Tag, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (order.referenceCode.isNotBlank()) order.referenceCode else "Aucun numéro renseigné",
                                fontFamily = if (order.referenceCode.isNotBlank()) FontFamily.Monospace else Inter,
                                fontWeight = if (order.referenceCode.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (order.referenceCode.isNotBlank()) FontStyle.Normal else FontStyle.Italic,
                                fontSize = 13.sp,
                                color = if (order.referenceCode.isNotBlank()) colors.textPrimary else colors.textSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier le numéro de commande",
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (isCompactPhone) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Step1InfoTile(
                        label = "Date commande",
                        value = DateUtils.formatShortDate(order.orderDate),
                        valueColor = colors.textPrimary,
                        bgColor = colors.innerCardBg,
                        labelColor = colors.textSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Step1DispatchTile(
                        delay = order.supplierDispatchDelay,
                        colors = colors,
                        onClick = onEditDispatch,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Step1InfoTile(
                        label = "Date commande",
                        value = DateUtils.formatShortDate(order.orderDate),
                        valueColor = colors.textPrimary,
                        bgColor = colors.innerCardBg,
                        labelColor = colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Step1DispatchTile(
                        delay = order.supplierDispatchDelay,
                        colors = colors,
                        onClick = onEditDispatch,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transitaire & Entrepôt Chine",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = colors.textPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isMaritime) colors.violetSoftBg else colors.cyanSoftBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isMaritime) Icons.Default.DirectionsBoat else Icons.Default.Flight,
                                contentDescription = null,
                                tint = if (isMaritime) colors.violetAccent else colors.cyanAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isMaritime) "MARITIME" else "AÉRIEN",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isMaritime) colors.violetAccent else colors.cyanAccent
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.innerCardBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onSelectTransitaire)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = if (order.carrier.isNotBlank()) colors.cyanAccent else colors.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            if (selectedTransitaire != null) {
                                Text(
                                    text = selectedTransitaire.name,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                                val subInfo = selectedTransitaire.chinaWarehouseAddress.ifBlank {
                                    selectedTransitaire.routeHub.ifBlank { "Adresse entrepôt non renseignée" }
                                }
                                Text(
                                    text = subInfo,
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val rateStr = if (isMaritime) {
                                    "${CurrencyFormatter.formatAriary(selectedTransitaire.seaRateAr)}/m³ · ${selectedTransitaire.seaLeadDays}"
                                } else {
                                    "${CurrencyFormatter.formatAriary(selectedTransitaire.airRateAr)}/kg · ${selectedTransitaire.airLeadDays}"
                                }
                                Text(
                                    text = rateStr,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = colors.cyanAccent
                                )
                            } else if (order.carrier.isNotBlank()) {
                                Text(
                                    text = order.carrier,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Touchez pour associer un transitaire du catalogue",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            } else {
                                Text(
                                    text = "Aucun transitaire sélectionné",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = colors.warningAccent
                                )
                                Text(
                                    text = if (transitairesCount == 0) "Créez d'abord un transitaire ou saisissez un nom"
                                    else "Touchez pour choisir ($transitairesCount disponible(s))",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.cyanAccent
                                )
                            }
                        }
                        if (selectedTransitaire != null && selectedTransitaire.chinaWarehouseAddress.isNotBlank()) {
                            IconButton(
                                onClick = { onCopyAddress(selectedTransitaire.chinaWarehouseAddress) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copier l'adresse entrepôt",
                                    tint = colors.cyanAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Changer de transitaire",
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Numéros de suivi intérieur (Tracking)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${parcels.size} colis",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                if (parcels.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.innerCardBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Aucun numéro de suivi enregistré pour le moment.",
                            fontFamily = Inter,
                            fontStyle = FontStyle.Italic,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    parcels.forEachIndexed { idx, parcel ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.innerCardBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(colors.violetSoftBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = colors.violetAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = parcel.trackingNumber,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${parcel.carrierName} · Colis ${idx + 1}",
                                            fontFamily = Inter,
                                            fontSize = 10.sp,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (parcel.isDelivered) colors.successSoftBg else colors.badgeNeutralBg
                                    ) {
                                        Text(
                                            text = if (parcel.isDelivered) "Reçu" else "En attente",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp,
                                            color = if (parcel.isDelivered) colors.successAccent else colors.textSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onEditTracking(parcel) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = colors.cyanAccent, modifier = Modifier.size(15.dp))
                                    }
                                    IconButton(
                                        onClick = { onRemoveTracking(parcel) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = colors.dangerAccent, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.badgeNeutralBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddTracking)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = colors.cyanAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (parcels.isEmpty()) "Ajouter un numéro de suivi" else "Ajouter un autre numéro de suivi",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.cyanAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun Step1BottomActionsCard(
    isOrderValid: Boolean,
    isSubmitting: Boolean,
    isAlreadyDebited: Boolean,
    isDraftCheckboxChecked: Boolean,
    remainingWalletYuans: Double,
    totalOrderYuan: Double,
    totalOrderAr: Long,
    colors: TransitaireColors,
    onConfirmClick: () -> Unit,
    onSaveWithoutAdvancingClick: () -> Unit
) {
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
            val isBalanceSufficient = remainingWalletYuans >= 0.0
            val canConfirm = isOrderValid && !isSubmitting && (isAlreadyDebited || isDraftCheckboxChecked || isBalanceSufficient)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAlreadyDebited) "Paiement Yuan réglé :" else if (isDraftCheckboxChecked) "Montant (sans débit) :" else "Montant à débiter :",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (totalOrderYuan > 0.0) "¥ ${String.format(Locale.US, "%.2f", totalOrderYuan)}" else "—",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (isAlreadyDebited) colors.cyanAccent else colors.violetAccent
                    )
                    Text(
                        text = if (totalOrderAr > 0L) "(${CurrencyFormatter.formatAriary(totalOrderAr)})" else "—",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Button(
                onClick = onConfirmClick,
                enabled = canConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("btn_confirm_pay_order"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDraftCheckboxChecked && !isAlreadyDebited) colors.violetAccent else colors.cyanAccent,
                    disabledContainerColor = colors.badgeNeutralBg
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            !isOrderValid -> "Complétez prix, quantité ou CMUP"
                            isSubmitting -> "Traitement en cours..."
                            isAlreadyDebited -> "Valider & Passer à l'étape 2 (Déjà Payé)"
                            !isBalanceSufficient && !isDraftCheckboxChecked -> "Solde Yuan insuffisant (Rechargez ou 'Sans débit')"
                            isDraftCheckboxChecked -> "Enregistrer Commande (Sans Débit)"
                            else -> "Confirmer et Payer la Commande"
                        },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (canConfirm) Color.White else colors.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isDraftCheckboxChecked && !isAlreadyDebited) Icons.Default.Bookmark else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (canConfirm) Color.White else colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            TextButton(
                onClick = onSaveWithoutAdvancingClick,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .heightIn(min = 44.dp)
            ) {
                Text(
                    text = if (isAlreadyDebited) "Enregistrer les modifications sans avancer" else "Enregistrer comme brouillon sans finaliser",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
internal fun Step1InfoTile(
    label: String,
    value: String,
    valueColor: Color,
    bgColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(10.dp)
    ) {
        Column {
            Text(label, fontFamily = Inter, fontSize = 11.sp, color = labelColor)
            Text(
                text = value,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = valueColor
            )
        }
    }
}

@Composable
internal fun Step1DispatchTile(
    delay: String,
    colors: TransitaireColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.innerCardBg)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Expédition usine", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                Text(
                    text = delay.ifBlank { "À définir" },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (delay.isNotBlank()) colors.textPrimary else colors.cyanAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Définir délai d'expédition",
                tint = colors.cyanAccent,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
