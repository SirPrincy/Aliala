package com.example.ui.business.clients

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.BusinessClientUi
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

internal fun formatCompactAriary(amountAr: Long): String {
    return when {
        amountAr >= 1_000_000L -> {
            val m = amountAr / 1_000_000.0
            if (m % 1.0 == 0.0) "${m.toInt()}M Ar" else String.format(Locale.FRANCE, "%.1fM Ar", m)
        }
        amountAr >= 10_000L -> "${amountAr / 1000}k Ar"
        else -> CurrencyFormatter.formatAriary(amountAr)
    }
}

@Composable
internal fun ClientPrioritySpotlightCard(
    spotlightClient: BusinessClientUi,
    colors: TransitaireColors,
    onRecordPayment: (BusinessClientUi) -> Unit,
    onWhatsAppRelance: (BusinessClientUi) -> Unit
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
                .background(color = colors.innerCardBg.copy(alpha = 0.5f))
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.violetSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "FICHE PRIORITAIRE",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = colors.violetAccent,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = spotlightClient.name,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val isUrgent = spotlightClient.isDueUrgent
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (isUrgent) colors.dangerSoftBg else colors.warningSoftBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isUrgent) "Échéance dépassée" else "Solde dû",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isUrgent) colors.dangerAccent else colors.warningAccent
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.cardBg)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total achats", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = CurrencyFormatter.formatAriary(spotlightClient.totalPurchasesAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    }
                    Column {
                        Text("Réglé", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = CurrencyFormatter.formatAriary(spotlightClient.totalPaidAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.cyanAccent
                        )
                    }
                    Column {
                        Text("Solde dû", fontFamily = Inter, fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = CurrencyFormatter.formatAriary(spotlightClient.balanceDueAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.dangerAccent
                        )
                    }
                }
            }

            if (spotlightClient.suggestedRelanceMsg.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.badgeNeutralBg)
                        .padding(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = colors.violetAccent,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 1.dp)
                        )
                        Text(
                            text = "« ${spotlightClient.suggestedRelanceMsg} »",
                            fontFamily = Inter,
                            fontStyle = FontStyle.Italic,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onRecordPayment(spotlightClient) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.violetSolidBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_encaisser_client_spotlight")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Encaisser",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Button(
                    onClick = { onWhatsAppRelance(spotlightClient) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.cardBg,
                        contentColor = colors.cyanAccent
                    ),
                    border = BorderStroke(1.dp, colors.cyanAccent.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(48.dp)
                        .testTag("btn_relance_whatsapp_spotlight")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Relance WhatsApp",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ClientCard(
    client: BusinessClientUi,
    colors: TransitaireColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("client_card_${client.id}"),
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
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val avatarBg = when (client.avatarType) {
                        "CYAN" -> colors.cyanSolidBg
                        "RED" -> colors.dangerSoftBg
                        "ORANGE" -> Color(0xFFEA580C)
                        "INDIGO" -> colors.expressSoftBg
                        else -> colors.violetSolidBg
                    }
                    val avatarTextColor = when (client.avatarType) {
                        "RED" -> colors.dangerAccent
                        "INDIGO" -> colors.expressAccent
                        else -> Color.White
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = client.initials,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = avatarTextColor
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = client.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(
                                        when (client.category) {
                                            "Business" -> colors.cyanSoftBg
                                            "Myself" -> colors.warningSoftBg
                                            else -> colors.violetSoftBg
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = client.category,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = when (client.category) {
                                        "Business" -> colors.cyanAccent
                                        "Myself" -> colors.warningAccent
                                        else -> colors.violetAccent
                                    }
                                )
                            }
                        }

                        Text(
                            text = "${CurrencyFormatter.formatAriary(client.totalPurchasesAr)} cumulés • ${client.totalOrdersCount} achat${if (client.totalOrdersCount > 1) "s" else ""}",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(22.dp)
                        .padding(top = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.border.copy(alpha = 0.3f))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (client.balanceDueAr > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.dangerAccent)
                        )
                        Text(
                            text = "Reste : ${CurrencyFormatter.formatAriary(client.balanceDueAr)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.dangerAccent
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "À jour ✓",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.cyanAccent
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(
                            when {
                                client.balanceDueAr > 0 && client.isDueUrgent -> colors.dangerSoftBg
                                client.balanceDueAr > 0 -> colors.warningSoftBg
                                else -> colors.innerCardBg
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (client.lastPurchaseDate.isNotBlank() && client.balanceDueAr == 0L && client.totalOrdersCount > 0) {
                            "${client.statusTag} • ${client.lastPurchaseDate}"
                        } else {
                            client.statusTag
                        },
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (client.isDueUrgent) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        color = when {
                            client.balanceDueAr > 0 && client.isDueUrgent -> colors.dangerAccent
                            client.balanceDueAr > 0 -> colors.warningAccent
                            else -> colors.textSecondary
                        }
                    )
                }
            }
        }
    }
}
