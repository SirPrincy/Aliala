package com.example.ui.business.transitaires

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun TransitaireDelaysCard(
    transitaire: CatalogueTransitaireSummaryUi,
    colors: TransitaireColors,
    onEditClick: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Politique & Délais",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                }

                TextButton(onClick = onEditClick) {
                    Text(
                        text = "Modifier",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.cyanAccent
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Aérien Std", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = transitaire.airLeadDays.ifBlank { "Non défini" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.cyanAccent
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Aérien Fast", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = transitaire.airFastLeadDays.ifBlank { "Non défini" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.expressAccent
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Maritime", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = transitaire.seaLeadDays.ifBlank { "Non défini" },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.violetAccent
                        )
                    }
                }
            }

            if (transitaire.internalNotes.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.subtleBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = transitaire.internalNotes,
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textPrimary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
internal fun TransitaireSynthesisCard(
    airTariffsEmpty: Boolean,
    seaTariffsEmpty: Boolean,
    airMinPrice: Long,
    airMaxPrice: Long,
    seaMinPrice: Long,
    seaMaxPrice: Long,
    effectiveRateAr: Long,
    colors: TransitaireColors
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.violetSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QueryStats,
                        contentDescription = null,
                        tint = colors.violetAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Synthèse tarifaire & Réel constaté",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Fourchette Aérien", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = if (airTariffsEmpty) "Aucun tarif"
                            else if (airMinPrice == airMaxPrice) "${CurrencyFormatter.formatAriary(airMinPrice)}/kg"
                            else "${CurrencyFormatter.formatAriary(airMinPrice)} – ${CurrencyFormatter.formatAriary(airMaxPrice)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.cyanAccent
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Fourchette Maritime", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = if (seaTariffsEmpty) "Aucun tarif"
                            else if (seaMinPrice == seaMaxPrice) "${CurrencyFormatter.formatAriary(seaMinPrice)}/m³"
                            else "${CurrencyFormatter.formatAriary(seaMinPrice)} – ${CurrencyFormatter.formatAriary(seaMaxPrice)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.violetAccent
                        )
                    }
                }
            }

            if (effectiveRateAr > 0L) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.badgeNeutralBg)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Coût moyen réel constaté",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Basé sur vos expéditions enregistrées",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                        Text(
                            text = "${CurrencyFormatter.formatAriary(effectiveRateAr)} / kg",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = colors.violetAccent
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.subtleBg)
                        .padding(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Le coût réel moyen sera calculé automatiquement dès votre première expédition pesée avec ce transitaire.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TransitaireWarehouseContactsCard(
    warehouseAddress: String,
    contactStr: String,
    wechatId: String,
    whatsappNum: String,
    colors: TransitaireColors,
    onEditClick: () -> Unit,
    onCopy: (label: String, text: String, feedback: String) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.cyanSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warehouse,
                        contentDescription = null,
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Entrepôt & Contacts Chine",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )
            }

            if (warehouseAddress.isBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.subtleBg,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aucune adresse d'entrepôt enregistrée",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.cyanAccent),
                            border = BorderStroke(1.dp, colors.cyanAccent.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Ajouter", fontFamily = PlusJakartaSans, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.innerCardBg)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = null,
                                tint = colors.cyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Adresse entrepôt Chine",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                        }

                        Text(
                            text = warehouseAddress,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(start = 24.dp)
                        )

                        Box(modifier = Modifier.padding(start = 24.dp, top = 2.dp)) {
                            Button(
                                onClick = {
                                    onCopy(
                                        "Adresse Entrepôt Chine",
                                        warehouseAddress,
                                        "Adresse copiée dans le presse-papiers"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.cyanSoftBg,
                                    contentColor = colors.cyanAccent
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Copier l'adresse",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (contactStr.isBlank() && wechatId.isBlank() && whatsappNum.isBlank()) {
                Text(
                    text = "Aucun contact renseigné",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (contactStr.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.innerCardBg)
                                .clickable {
                                    onCopy("Contact transitaire", contactStr, "Contact copié : $contactStr")
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.cyanSoftBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = null,
                                            tint = colors.cyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Contact / Téléphone / WeChat",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                        Text(
                                            text = contactStr,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copier le contact",
                                    tint = colors.cyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (wechatId.isNotBlank() || whatsappNum.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (wechatId.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.innerCardBg)
                                        .clickable {
                                            onCopy("WeChat ID", wechatId, "WeChat ID copié : $wechatId")
                                        }
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.ChatBubbleOutline, null, tint = colors.cyanAccent, modifier = Modifier.size(18.dp))
                                        Column {
                                            Text("WeChat", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                                            Text(wechatId, fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                            if (whatsappNum.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.innerCardBg)
                                        .clickable {
                                            onCopy("WhatsApp", whatsappNum, "WhatsApp copié : $whatsappNum")
                                        }
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Call, null, tint = colors.cyanAccent, modifier = Modifier.size(18.dp))
                                        Column {
                                            Text("WhatsApp", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.textPrimary)
                                            Text(whatsappNum, fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
