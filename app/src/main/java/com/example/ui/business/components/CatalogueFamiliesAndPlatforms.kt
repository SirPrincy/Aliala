package com.example.ui.business.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.CatalogueFamilySummaryUi
import com.example.ui.business.CataloguePlatformSummaryUi
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

// ---------------------------------------------------------------------------
// SUB-TAB: FAMILLES CONTENT
// ---------------------------------------------------------------------------
@Composable
fun FamillesSubTabContent(
    families: List<CatalogueFamilySummaryUi>,
    onNewFamilyClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onCreateFamily: (name: String, emoji: String, colorHex: String) -> Unit = { _, _, _ -> },
    onFamilyClick: (CatalogueFamilySummaryUi) -> Unit
) {
    val colors = rememberTransitaireColors()
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("catalogue_families_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Summary Bar: X Familles actives / Y articles répertoriés + Pill button "+ Nouvelle famille"
        item(key = "header_summary") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${families.size} Familles actives",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val totalArticles = families.sumOf { it.articleCount }
                    Text(
                        text = "$totalArticles articles répertoriés",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Pill Button: "+ Nouvelle famille"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(CyanPrimary.copy(alpha = 0.15f))
                        .clickable {
                            onNewFamilyClick()
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("btn_new_family_pill"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, CyanPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = "Nouvelle famille",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CyanPrimary
                        )
                    }
                }
            }
        }

        // 2. Family Cards
        items(families, key = { "family_${it.id}_${it.name}" }) { family ->
            val parsedColor = remember(family.colorHex) {
                try {
                    Color(android.graphics.Color.parseColor(family.colorHex))
                } catch (e: Exception) {
                    when {
                        family.name.contains("Mode", ignoreCase = true) -> Color(0xFFEC4899)
                        family.name.contains("Maison", ignoreCase = true) -> Color(0xFFF97316)
                        family.name.contains("Accessoires", ignoreCase = true) -> Color(0xFF8B5CF6)
                        else -> CyanPrimary
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .clickable { onFamilyClick(family) }
                    .padding(14.dp)
                    .testTag("family_card_${family.name}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Emoji Container
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(parsedColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = family.emoji.ifBlank { "🎧" },
                                fontSize = 24.sp
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = family.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Subtitle: "14 articles (12 actifs) · 2 masqués"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "${family.articleCount} articles ",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "(${family.activeCount} actifs)",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (family.hiddenCount > 0) {
                                    Text(
                                        text = " · ${family.hiddenCount} masqués",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            // Valeur stock : 1 845 000 Ar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Valeur stock :",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = CurrencyFormatter.formatAriary(family.stockValueAr),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Drag indicator & Chevron
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragIndicator,
                            contentDescription = "Réorganiser",
                            tint = colors.textSecondary.copy(alpha = 0.55f),
                            modifier = Modifier.size(20.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = colors.textSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (families.isEmpty()) {
            item(key = "empty_families") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.innerCardBg)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune famille enregistrée. Appuyez sur « Nouvelle famille » pour en créer une.",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        // 4. "Protection des données" Notice Card
        item(key = "card_data_protection") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
                    .testTag("card_data_protection")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyanPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Protection des données",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "En cas de suppression d'une famille, les articles associés sont automatiquement transférés vers une famille de destination pour éviter toute perte de stock ou de chiffre d'affaires.",
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
}

// ---------------------------------------------------------------------------
// SUB-TAB: PLATEFORMES CONTENT (100% Hors-ligne / Room persistant)
// ---------------------------------------------------------------------------
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
        // 1. Summary Banner Card
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

        // 2. Platforms Cards Stack (Persistant & Dynamique)
        items(platforms, key = { it.id.toString() + "_" + it.name }) { platform ->
            PlatformCardItem(
                platform = platform,
                onToggleActive = { isActive -> onTogglePlatformActive(platform.id, isActive) },
                onDetailsClick = { selectedPlatformDetails = platform }
            )
        }

        // 3. Quick Reference Info Card ("Frais de port en Chine")
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

    // Modal: Ajouter une plateforme
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

    // Modal: Détails & Tarifs transit
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

// ---------------------------------------------------------------------------
// COMPOSABLE: PLATFORM CARD ITEM
// ---------------------------------------------------------------------------
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
            // Header Row: Icon + Names + Switch
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
                    // Badge logo icon box
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

            // Tags row (Port Chine + Condition)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isFreeShipping = platform.shippingTag.contains("gratuit", ignoreCase = true) || platform.shippingTag.contains("包邮")
                val isDevis = platform.shippingTag.contains("devis", ignoreCase = true)

                // Shipping pill
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

                // Secondary tag pill
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

            // Metrics Box (Dépenses totales + Activité récente)
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
                    // Column 1: Dépenses totales
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

                    // Column 2: Activité récente
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

            // Action button for details
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

// ---------------------------------------------------------------------------
// BADGE LOGO ICON CONTAINER
// ---------------------------------------------------------------------------
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

// ---------------------------------------------------------------------------
// DIALOG: AJOUTER UNE PLATEFORME (ROOM PERSISTANT)
// ---------------------------------------------------------------------------
@Composable
private fun AddPlatformDialog(
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

// ---------------------------------------------------------------------------
// DIALOG: DÉTAILS ET TARIFS TRANSIT
// ---------------------------------------------------------------------------
@Composable
private fun PlatformTransitDetailsDialog(
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

                // Toggle Mode de port intérieur
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

internal fun formatYuanAmount(amount: Double): String {
    val rounded = amount.toLong()
    return String.format(Locale.FRENCH, "%,d", rounded).replace('\u00A0', ' ')
}
