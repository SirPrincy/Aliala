package com.example.ui.business.catalogue

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
fun PlateformesSubTabContent(
    platforms: List<CataloguePlatformSummaryUi>,
    onTogglePlatformActive: (Long, Boolean) -> Unit,
    onUpdatePlatformShippingTag: (Long, String) -> Unit,
    onCreatePlatform: (name: String, subtitle: String, shippingTag: String, secondaryTag: String, iconType: String, colorHex: String) -> Unit,
    onDeletePlatform: (Long) -> Unit
) {
    val colors = rememberTransitaireColors()
    var showAddPlatformDialog by remember { mutableStateOf(false) }
    var selectedPlatformDetails by remember { mutableStateOf<CataloguePlatformSummaryUi?>(null) }

    val activeCount = platforms.count { it.isActive }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("plateformes_subtab_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item(key = "card_platform_summary") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
                    .testTag("card_platform_summary")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TravelExplore,
                                    contentDescription = null,
                                    tint = colors.violetAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "SOURCING CHINE",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp,
                                    color = colors.violetAccent
                                )
                            }

                            Text(
                                text = "Plateformes Chinoises d'Achat",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Suivi des fournisseurs, frais de port intérieurs et dépenses cumulées.",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.violetSoftBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = colors.violetAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

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
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary)
                            )
                            Text(
                                text = "$activeCount actives répertoriées",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showAddPlatformDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.violetSolidBg,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("btn_new_platform_banner")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Nouvelle plateforme",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        items(platforms, key = { it.id.toString() + "_" + it.name }) { platform ->
            PlatformCardItem(
                platform = platform,
                onToggleActive = { isActive -> onTogglePlatformActive(platform.id, isActive) },
                onDetailsClick = { selectedPlatformDetails = platform }
            )
        }

        item(key = "card_freight_info") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
                    .testTag("card_freight_info")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Frais de port en Chine (运费)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Si le port est payant sur la plateforme, il est imputé sur le coût de revient du lot avant l'envoi vers le transitaire.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAddPlatformDialog) {
        AddPlatformDialog(
            onDismiss = { showAddPlatformDialog = false },
            onConfirm = { name, subtitle, shipping, secondary ->
                val iconType = when {
                    name.contains("1688", ignoreCase = true) -> "TEXT_1688"
                    name.contains("taobao", ignoreCase = true) -> "CART"
                    name.contains("pinduoduo", ignoreCase = true) -> "PRICE"
                    name.contains("wechat", ignoreCase = true) -> "WECHAT"
                    else -> "STORE"
                }
                onCreatePlatform(name, subtitle, shipping, secondary, iconType, "#712ae2")
                showAddPlatformDialog = false
            }
        )
    }

    selectedPlatformDetails?.let { plat ->
        PlatformTransitDetailsDialog(
            platform = plat,
            onDismiss = { selectedPlatformDetails = null },
            onUpdateShippingTag = { newTag ->
                onUpdatePlatformShippingTag(plat.id, newTag)
            },
            onDelete = {
                onDeletePlatform(plat.id)
                selectedPlatformDetails = null
            }
        )
    }
}

@Composable
private fun PlatformCardItem(
    platform: CataloguePlatformSummaryUi,
    onToggleActive: (Boolean) -> Unit,
    onDetailsClick: () -> Unit
) {
    val colors = rememberTransitaireColors()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("platform_card_${platform.name}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    PlatformBadgeIcon(
                        iconType = platform.iconType,
                        name = platform.name,
                        colorHex = platform.colorHex
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = platform.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (platform.isVerified) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Certifié",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (platform.badgeText.isNotBlank()) {
                                val isUsine = platform.badgeText.contains("Usine", ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isUsine) colors.dangerSoftBg else MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = platform.badgeText,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = if (isUsine) colors.dangerAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = platform.subtitle,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = platform.isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.violetSolidBg,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("switch_platform_${platform.name}")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isFreeShipping = platform.shippingTag.contains("gratuit", ignoreCase = true) || platform.shippingTag.contains("包邮")
                val isDevis = platform.shippingTag.contains("devis", ignoreCase = true)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (isFreeShipping) colors.cyanSoftBg else colors.violetSoftBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isFreeShipping -> Icons.Default.CheckCircle
                                isDevis -> Icons.Default.RequestQuote
                                else -> Icons.Default.LocalShipping
                            },
                            contentDescription = null,
                            tint = if (isFreeShipping) colors.cyanAccent else colors.violetAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = platform.shippingTag,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isFreeShipping) colors.cyanAccent else colors.violetAccent
                        )
                    }
                }

                if (platform.secondaryTag.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = platform.secondaryTag,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Dépenses totales",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "¥ ${formatYuanAmount(platform.totalSpentYuan)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "~ ${CurrencyFormatter.formatAriary(platform.totalSpentAr)}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = CyanPrimary
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (platform.totalLots > 0) "Activité récente" else "Commandes passées",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (platform.totalLots > 0) Icons.Default.ShoppingBag else Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${platform.totalLots} commandes",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (platform.lastOrderDate.isBlank()) "Aucune commande"
                            else if (platform.lastOrderDate.startsWith("Dernière") || platform.lastOrderDate.startsWith("Suivi") || platform.lastOrderDate.startsWith("Aucune")) platform.lastOrderDate
                            else "Dernière : ${platform.lastOrderDate}",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.cyanSoftBg)
                    .clickable { onDetailsClick() }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Détails & Tarifs transit",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.cyanAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colors.cyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlatformBadgeIcon(
    iconType: String,
    name: String,
    colorHex: String
) {
    val colors = rememberTransitaireColors()
    val bgModifier = Modifier
        .size(44.dp)
        .clip(RoundedCornerShape(14.dp))

    when {
        iconType == "TEXT_1688" || name.contains("1688", ignoreCase = true) -> {
            Box(
                modifier = bgModifier.background(colors.violetSoftBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "1688",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = colors.violetAccent
                )
            }
        }
        iconType == "CART" || name.contains("taobao", ignoreCase = true) -> {
            Box(
                modifier = bgModifier.background(colors.violetSolidBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        iconType == "PRICE" || name.contains("pinduoduo", ignoreCase = true) -> {
            Box(
                modifier = bgModifier.background(colors.dangerSoftBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PriceChange,
                    contentDescription = null,
                    tint = colors.dangerAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        iconType == "WECHAT" || name.contains("wechat", ignoreCase = true) -> {
            Box(
                modifier = bgModifier.background(colors.cyanSoftBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = colors.cyanAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        else -> {
            val fallbackColor = try {
                Color(android.graphics.Color.parseColor(colorHex))
            } catch (e: Exception) {
                Color(0xFF712AE2)
            }
            Box(
                modifier = bgModifier.background(fallbackColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = fallbackColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
