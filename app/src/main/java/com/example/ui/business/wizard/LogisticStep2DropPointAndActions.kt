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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans

@Composable
internal fun Step2ChinaDropPointCard(
    order: BusinessOrderEntity,
    selectedTransitaire: CatalogueTransitaireSummaryUi?,
    colors: TransitaireColors,
    onSelectTransitaire: () -> Unit,
    onCopyText: (label: String, text: String, toast: String) -> Unit
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Point de chute Chine",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.violetSoftBg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .clickable(onClick = onSelectTransitaire)
                ) {
                    Text(
                        text = if (selectedTransitaire != null || order.carrier.isNotBlank()) "Changer" else "Choisir",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.violetAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.innerCardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedTransitaire != null) {
                        Text(
                            text = selectedTransitaire.name,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                        if (selectedTransitaire.routeHub.isNotBlank()) {
                            Text(
                                text = "Hub : ${selectedTransitaire.routeHub}",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.cyanAccent
                            )
                        }

                        if (selectedTransitaire.chinaWarehouseAddress.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedTransitaire.chinaWarehouseAddress,
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        onCopyText(
                                            "Adresse entrepôt Chine",
                                            selectedTransitaire.chinaWarehouseAddress,
                                            "Adresse entrepôt copiée !"
                                        )
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copier l'adresse entrepôt",
                                        tint = colors.cyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Adresse entrepôt Chine non renseignée dans la fiche transitaire.",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }

                        val contactText = listOfNotNull(
                            selectedTransitaire.contact.takeIf { it.isNotBlank() },
                            selectedTransitaire.wechat.takeIf { it.isNotBlank() }?.let { "WeChat: $it" },
                            selectedTransitaire.whatsapp.takeIf { it.isNotBlank() }?.let { "WhatsApp: $it" }
                        ).joinToString(" · ")

                        if (contactText.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = colors.violetAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = contactText,
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        onCopyText(
                                            "Contact transitaire",
                                            contactText,
                                            "Contact transitaire copié !"
                                        )
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copier le contact",
                                        tint = colors.violetAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    } else if (order.carrier.isNotBlank()) {
                        Text(
                            text = "Transitaire : ${order.carrier}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Ce nom n'est pas encore relié à une fiche du catalogue Transitaires. Touchez « Changer » pour afficher l'adresse Chine.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    } else {
                        Text(
                            text = "Aucun transitaire assigné à ce lot",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.warningAccent
                        )
                        Text(
                            text = "Sélectionnez votre transitaire pour afficher ici l'adresse de livraison Chine à copier au fournisseur.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun Step2BottomActionsCard(
    parcelsCount: Int,
    deliveredParcelsCount: Int,
    colors: TransitaireColors,
    onAdvanceClick: () -> Unit,
    onPreviousStep: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colors.cyanSoftBg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colors.cyanAccent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Marquez chaque colis comme réceptionné dès que le transporteur intérieur confirme la livraison à l'entrepôt Chine pour passer à la pesée (étape 3).",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textPrimary,
                    lineHeight = 18.sp
                )
            }
        }

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
                val hasDeliveredParcels = deliveredParcelsCount > 0

                Button(
                    onClick = onAdvanceClick,
                    enabled = hasDeliveredParcels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("btn_all_parcels_arrived"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.cyanAccent,
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
                                parcelsCount == 0 -> "Ajoutez et réceptionnez un colis"
                                hasDeliveredParcels -> "Passer à la pesée ($deliveredParcelsCount/$parcelsCount reçus)"
                                else -> "Réceptionnez au moins 1 colis"
                            },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (hasDeliveredParcels) Color.White else colors.textSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (hasDeliveredParcels) Color.White else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                TextButton(
                    onClick = onPreviousStep,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .heightIn(min = 48.dp)
                        .testTag("btn_back_to_step1")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Retour à l'étape 1 (Commande)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}
