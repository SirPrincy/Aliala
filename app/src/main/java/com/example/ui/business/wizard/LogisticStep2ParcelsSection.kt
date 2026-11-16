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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.model.DomesticParcelItem
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.DateUtils

@Composable
internal fun Step2ParcelsTrackingSection(
    parcels: List<DomesticParcelItem>,
    deliveredParcelsCount: Int,
    allParcelsDelivered: Boolean,
    isCompactPhone: Boolean,
    colors: TransitaireColors,
    onMarkAllDelivered: () -> Unit,
    onCopyTracking: (String) -> Unit,
    onDeleteParcel: (DomesticParcelItem) -> Unit,
    onEditParcel: (DomesticParcelItem) -> Unit,
    onToggleParcelDelivered: (Int, DomesticParcelItem) -> Unit,
    onAddParcel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = colors.cyanAccent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Colis en transit intérieur",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (parcels.size >= 2 && !allParcelsDelivered) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.successSoftBg,
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .clickable(onClick = onMarkAllDelivered)
                    ) {
                        Text(
                            text = "Tout recevoir",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.successAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = colors.cyanSoftBg
                ) {
                    Text(
                        text = "$deliveredParcelsCount/${parcels.size} reçu${if (deliveredParcelsCount > 1) "s" else ""}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.cyanAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (parcels.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = colors.cardBg,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "Aucun numéro de suivi enregistré",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Ajoutez le ou les numéros de tracking fournis pour suivre la livraison intérieure jusqu'à l'entrepôt transitaire.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            parcels.forEachIndexed { index, parcel ->
                val isPurpleAccent = index % 2 == 1
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = parcel.carrierName,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = colors.textPrimary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = parcel.trackingNumber,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    IconButton(
                                        onClick = { onCopyTracking(parcel.trackingNumber) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copier le numéro",
                                            tint = colors.cyanAccent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteParcel(parcel) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Supprimer le colis",
                                            tint = colors.dangerAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = when {
                                    parcel.isDelivered -> colors.successSoftBg
                                    isPurpleAccent -> colors.violetSoftBg
                                    else -> colors.cyanSoftBg
                                }
                            ) {
                                Text(
                                    text = if (parcel.isDelivered) "Réceptionné" else "En acheminement",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = when {
                                        parcel.isDelivered -> colors.successAccent
                                        isPurpleAccent -> colors.violetAccent
                                        else -> colors.cyanAccent
                                    },
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
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (parcel.isDelivered) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Hub,
                                            contentDescription = null,
                                            tint = colors.successAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(
                                                text = parcel.checkpointTitle,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = DateUtils.formatTransactionDate(parcel.updatedAtMillis),
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.cyanSolidBg)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(22.dp)
                                                    .background(colors.badgeNeutralBg)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.badgeNeutralBg)
                                            )
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = parcel.checkpointTitle,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = DateUtils.formatTransactionDate(parcel.updatedAtMillis),
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                            if (parcel.checkpointRoute.isNotBlank()) {
                                                Text(
                                                    text = parcel.checkpointRoute,
                                                    fontFamily = Inter,
                                                    fontSize = 11.sp,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Contenu : ${parcel.contentDescription}",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = colors.textPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (isCompactPhone) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ParcelActionPillButton(
                                    text = "Mettre à jour",
                                    icon = Icons.Default.Edit,
                                    bgColor = colors.badgeNeutralBg,
                                    contentColor = colors.textPrimary,
                                    onClick = { onEditParcel(parcel) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                ParcelActionPillButton(
                                    text = if (parcel.isDelivered) "Reçu entrepôt" else "Réceptionné",
                                    icon = Icons.Default.DoneAll,
                                    bgColor = if (parcel.isDelivered) colors.successSoftBg else colors.cyanSoftBg,
                                    contentColor = if (parcel.isDelivered) colors.successAccent else colors.cyanAccent,
                                    onClick = { onToggleParcelDelivered(index, parcel) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ParcelActionPillButton(
                                    text = "Mettre à jour",
                                    icon = Icons.Default.Edit,
                                    bgColor = colors.badgeNeutralBg,
                                    contentColor = colors.textPrimary,
                                    onClick = { onEditParcel(parcel) },
                                    modifier = Modifier.weight(1f)
                                )
                                ParcelActionPillButton(
                                    text = if (parcel.isDelivered) "Reçu entrepôt" else "Réceptionné",
                                    icon = Icons.Default.DoneAll,
                                    bgColor = if (parcel.isDelivered) colors.successSoftBg else colors.cyanSoftBg,
                                    contentColor = if (parcel.isDelivered) colors.successAccent else colors.cyanAccent,
                                    onClick = { onToggleParcelDelivered(index, parcel) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onAddParcel)
                .testTag("btn_add_parcel_step2"),
            shape = RoundedCornerShape(14.dp),
            color = colors.cardBg,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = colors.cyanAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ajouter un colis / numéro de suivi",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = colors.cyanAccent
                )
            }
        }
    }
}

@Composable
private fun ParcelActionPillButton(
    text: String,
    icon: ImageVector,
    bgColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
