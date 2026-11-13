package com.example.ui.business.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.model.TariffItem
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale
import java.util.UUID

internal data class TransitaireColors(
    val isDark: Boolean,
    val pageBg: Color,
    val cardBg: Color,
    val innerCardBg: Color,
    val subtleBg: Color,
    val inputBg: Color,
    val toggleTrackBg: Color,
    val badgeNeutralBg: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val cyanAccent: Color,
    val cyanSolidBg: Color,
    val cyanSoftBg: Color,
    val violetAccent: Color,
    val violetSolidBg: Color,
    val violetSoftBg: Color,
    val expressAccent: Color,
    val expressSoftBg: Color,
    val dangerAccent: Color,
    val dangerSoftBg: Color,
    val successAccent: Color,
    val successSoftBg: Color,
    val warningAccent: Color,
    val warningSoftBg: Color
)

@Composable
internal fun rememberTransitaireColors(): TransitaireColors {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f
    return remember(isDark, colorScheme) {
        TransitaireColors(
            isDark = isDark,
            pageBg = colorScheme.background,
            cardBg = colorScheme.surface,
            innerCardBg = if (isDark) Color(0xFF263349) else Color(0xFFF2F3FF),
            subtleBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
            inputBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF2F3FF),
            toggleTrackBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
            badgeNeutralBg = if (isDark) Color(0xFF334155) else Color(0xFFEAEDFF),
            border = colorScheme.outline,
            textPrimary = colorScheme.onSurface,
            textSecondary = colorScheme.onSurfaceVariant,
            cyanAccent = if (isDark) Color(0xFF22D3EE) else Color(0xFF00687A),
            cyanSolidBg = Color(0xFF06B6D4),
            cyanSoftBg = if (isDark) Color(0xFF083344) else Color(0xFFE0F7FA),
            violetAccent = if (isDark) Color(0xFFA78BFA) else Color(0xFF712AE2),
            violetSolidBg = if (isDark) Color(0xFF7C3AED) else Color(0xFF712AE2),
            violetSoftBg = if (isDark) Color(0xFF3B0764) else Color(0xFFF3E8FF),
            expressAccent = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
            expressSoftBg = if (isDark) Color(0xFF0C4A6E) else Color(0xFFE0F2FE),
            dangerAccent = if (isDark) Color(0xFFF87171) else Color(0xFFBA1A1A),
            dangerSoftBg = if (isDark) Color(0xFF450A0A) else Color(0xFFFFDAD6),
            successAccent = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D),
            successSoftBg = if (isDark) Color(0xFF052E16) else Color(0xFFDCFCE7),
            warningAccent = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
            warningSoftBg = if (isDark) Color(0xFF451A03) else Color(0xFFFEF3C7)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransitaireTariffGridSection(
    tariffItems: List<TariffItem>,
    onTariffItemsChanged: (List<TariffItem>) -> Unit,
    modifier: Modifier = Modifier,
    headerTitle: String = "Grille Tarifaire",
    headerBadge: String? = null
) {
    val colors = rememberTransitaireColors()
    var selectedMode by remember { mutableStateOf("AÉRIEN") }
    var editingItem by remember { mutableStateOf<TariffItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val airItems = tariffItems.filter { it.mode == "AÉRIEN" }
    val seaItems = tariffItems.filter { it.mode == "MARITIME" }
    val filteredItems = if (selectedMode == "AÉRIEN") airItems else seaItems

    val activeAccent = if (selectedMode == "AÉRIEN") colors.cyanAccent else colors.violetAccent
    val activeSoftBg = if (selectedMode == "AÉRIEN") colors.cyanSoftBg else colors.violetSoftBg
    val activeSolidBg = if (selectedMode == "AÉRIEN") {
        if (colors.isDark) Color(0xFF0891B2) else Color(0xFF00687A)
    } else {
        colors.violetSolidBg
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
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
            // Header
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = headerTitle,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${airItems.size} aérien(s) • ${seaItems.size} maritime(s)",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                if (!headerBadge.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.badgeNeutralBg
                    ) {
                        Text(
                            text = headerBadge,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Segmented Mode Toggle (Aérien vs Maritime) with item counts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.toggleTrackBg)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isAir = selectedMode == "AÉRIEN"
                val isSea = selectedMode == "MARITIME"

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAir) (if (colors.isDark) Color(0xFF0891B2) else Color(0xFF00687A)) else Color.Transparent)
                        .clickable { selectedMode = "AÉRIEN" }
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                        .testTag("toggle_mode_aerien"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flight,
                            contentDescription = null,
                            tint = if (isAir) Color.White else colors.textSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "Aérien (${airItems.size})",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isAir) Color.White else colors.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSea) colors.violetSolidBg else Color.Transparent)
                        .clickable { selectedMode = "MARITIME" }
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                        .testTag("toggle_mode_maritime"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = if (isSea) Color.White else colors.textSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "Maritime (${seaItems.size})",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSea) Color.White else colors.textSecondary
                        )
                    }
                }
            }

            // Tariff Cards List
            if (filteredItems.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.subtleBg,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier.fillMaxWidth()
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(activeSoftBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "Aucun tarif configuré en $selectedMode",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (selectedMode == "MARITIME") {
                                "Ajoutez des tarifs par volume (ex: > 0.25 CBM ou < 0.25 CBM) ou par type de marchandise."
                            } else {
                                "Ajoutez des tarifs par poids, type d'envoi ou catégorie de marchandise."
                            },
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredItems.forEach { item ->
                        TariffGridItemCard(
                            item = item,
                            colors = colors,
                            accentColor = activeAccent,
                            softBgColor = activeSoftBg,
                            onEdit = {
                                editingItem = item
                                showAddDialog = true
                            },
                            onDelete = {
                                onTariffItemsChanged(tariffItems.filter { it.id != item.id })
                            }
                        )
                    }
                }
            }

            // Full-width Ergonomic CTA to Add a New Tariff
            OutlinedButton(
                onClick = {
                    editingItem = null
                    showAddDialog = true
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, activeAccent.copy(alpha = 0.55f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = activeSoftBg.copy(alpha = if (colors.isDark) 0.55f else 0.45f),
                    contentColor = activeAccent
                ),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_add_tariff_item")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ajouter un nouveau tarif (${if (selectedMode == "AÉRIEN") "Aérien" else "Maritime"})",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }

    if (showAddDialog) {
        AddEditTariffDialog(
            initialItem = editingItem,
            defaultMode = selectedMode,
            colors = colors,
            onDismiss = { showAddDialog = false },
            onConfirm = { newItems ->
                val updated = if (editingItem != null && newItems.size == 1) {
                    val edited = newItems.first()
                    tariffItems.map { if (it.id == edited.id) edited else it }
                } else {
                    tariffItems + newItems
                }
                val firstAddedMode = newItems.firstOrNull()?.mode
                if (firstAddedMode != null) {
                    selectedMode = firstAddedMode
                }
                onTariffItemsChanged(updated)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TariffGridItemCard(
    item: TariffItem,
    colors: TransitaireColors,
    accentColor: Color,
    softBgColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isSea = item.mode == "MARITIME"
    val unitSuffix = if (isSea) "/ m³" else "/ kg"
    val condLabel = TariffGridParser.formatCondition(item)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        color = colors.innerCardBg,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top badges row: Shipment Type + Threshold Condition + Lead time
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = softBgColor
                ) {
                    Text(
                        text = item.shipmentType.ifBlank { "Standard" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (condLabel.isNotBlank()) {
                    val isUpperTier = item.conditionOp == "GT" || item.conditionOp == "GTE"
                    val tierBg = if (isUpperTier) colors.successSoftBg else colors.warningSoftBg
                    val tierFg = if (isUpperTier) colors.successAccent else colors.warningAccent
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tierBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Scale,
                                contentDescription = null,
                                tint = tierFg,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = condLabel,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = tierFg
                            )
                        }
                    }
                }

                if (item.leadDays.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.cardBg,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = item.leadDays,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Goods Category + Price + Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.goodsCategory.ifBlank { "Marchandise Générale" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${CurrencyFormatter.formatAriary(item.priceAr)} $unitSuffix",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = accentColor
                        )

                        if (item.usdPrice != null && item.usdPrice > 0.0) {
                            val usdFormatted = if (item.usdPrice % 1.0 == 0.0) {
                                "${item.usdPrice.toInt()} $"
                            } else {
                                "${String.format(Locale.US, "%.2f", item.usdPrice)} $"
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.badgeNeutralBg
                            ) {
                                Text(
                                    text = "$usdFormatted/${if (isSea) "CBM" else "kg"}",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier ce tarif",
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer ce tarif",
                            tint = colors.dangerAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddEditTariffDialog(
    initialItem: TariffItem?,
    defaultMode: String,
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onConfirm: (List<TariffItem>) -> Unit
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(initialItem?.mode ?: defaultMode) }
    var shipmentType by remember { mutableStateOf(initialItem?.shipmentType ?: if (mode == "MARITIME") "Groupage LCL" else "Standard Cargo") }
    var goodsCategory by remember { mutableStateOf(initialItem?.goodsCategory ?: "Marchandise Générale") }

    var conditionOp by remember { mutableStateOf(initialItem?.conditionOp ?: "NONE") }
    var conditionValText by remember {
        mutableStateOf(
            initialItem?.conditionValue?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            } ?: if (mode == "MARITIME") "0.25" else "5"
        )
    }

    var useUsd by remember { mutableStateOf(initialItem?.usdPrice != null) }
    var usdRateText by remember { mutableStateOf("4500") }
    var priceText by remember {
        mutableStateOf(
            if (initialItem?.usdPrice != null) {
                val u = initialItem.usdPrice
                if (u % 1.0 == 0.0) u.toInt().toString() else u.toString()
            } else if ((initialItem?.priceAr ?: 0L) > 0L) {
                initialItem!!.priceAr.toString()
            } else {
                ""
            }
        )
    }

    var alsoCreateOppositeTier by remember { mutableStateOf(false) }
    var oppositePriceText by remember { mutableStateOf("") }

    val (parsedMin, parsedMax) = remember(initialItem, mode) {
        TariffGridParser.parseMinMaxDays(
            initialItem?.leadDays ?: if (mode == "AÉRIEN") "7 à 10 jours" else "30 à 45 jours",
            if (mode == "AÉRIEN") 7 else 30,
            if (mode == "AÉRIEN") 10 else 45
        )
    }
    var minDays by remember { mutableIntStateOf(parsedMin) }
    var maxDays by remember { mutableIntStateOf(parsedMax) }

    val usdRate = usdRateText.toLongOrNull()?.coerceAtLeast(1L) ?: 4500L
    val unitUnitLabel = if (mode == "MARITIME") "CBM (m³)" else "kg"

    val shipmentSuggestions = if (mode == "AÉRIEN") {
        listOf("Standard Cargo", "Express Direct", "Fret Économique", "Vol Spécial")
    } else {
        listOf("Groupage LCL", "Conteneur 20ft", "Conteneur 40ft", "Maritime Express")
    }

    val goodsSuggestions = listOf(
        "Marchandise Générale",
        "Électronique & Batterie",
        "Liquides & Poudres",
        "Téléphones & High-Tech",
        "Textile & Vêtements",
        "Lourde & Volumineuse"
    )

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.inputBg,
        unfocusedContainerColor = colors.inputBg,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        focusedBorderColor = colors.cyanAccent,
        unfocusedBorderColor = colors.border,
        focusedLabelColor = colors.cyanAccent,
        unfocusedLabelColor = colors.textSecondary,
        cursorColor = colors.cyanAccent
    )

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.cardBg,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .heightIn(max = 680.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Dialog Top Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.innerCardBg)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialItem != null) "Modifier le tarif" else "Nouveau tarif",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.textPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (mode == "AÉRIEN") colors.cyanSoftBg else colors.violetSoftBg
                    ) {
                        Text(
                            text = mode,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (mode == "AÉRIEN") colors.cyanAccent else colors.violetAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Mode toggle inside dialog
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.toggleTrackBg)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isAir = mode == "AÉRIEN"
                        val isSea = mode == "MARITIME"

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAir) (if (colors.isDark) Color(0xFF0891B2) else Color(0xFF00687A)) else Color.Transparent)
                                .clickable {
                                    mode = "AÉRIEN"
                                    if (shipmentType !in listOf("Standard Cargo", "Express Direct", "Fret Économique", "Vol Spécial")) {
                                        shipmentType = "Standard Cargo"
                                    }
                                    if (conditionOp == "NONE") conditionValText = "5"
                                }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aérien (/ kg)", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isAir) Color.White else colors.textSecondary)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSea) colors.violetSolidBg else Color.Transparent)
                                .clickable {
                                    mode = "MARITIME"
                                    if (shipmentType !in listOf("Groupage LCL", "Conteneur 20ft", "Conteneur 40ft", "Maritime Express")) {
                                        shipmentType = "Groupage LCL"
                                    }
                                    if (conditionOp == "NONE") conditionValText = "0.25"
                                }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Maritime (/ CBM)", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isSea) Color.White else colors.textSecondary)
                        }
                    }

                    // 2. Condition / Palier de volume ou poids
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.innerCardBg)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (mode == "MARITIME") "Condition de volume (CBM / m³)" else "Condition de poids (kg)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val quickConds = if (mode == "MARITIME") {
                                listOf(
                                    Triple("Standard (Tout)", "NONE", ""),
                                    Triple("< 0.25 CBM", "LT", "0.25"),
                                    Triple("> 0.25 CBM", "GT", "0.25"),
                                    Triple("≥ 0.25 CBM", "GTE", "0.25"),
                                    Triple("≥ 1 CBM", "GTE", "1")
                                )
                            } else {
                                listOf(
                                    Triple("Standard (Tout)", "NONE", ""),
                                    Triple("< 5 kg", "LT", "5"),
                                    Triple("≥ 5 kg", "GTE", "5"),
                                    Triple("≥ 20 kg", "GTE", "20"),
                                    Triple("≥ 50 kg", "GTE", "50")
                                )
                            }
                            quickConds.forEach { (lbl, op, vStr) ->
                                val active = conditionOp == op && (op == "NONE" || conditionValText == vStr)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (active) (if (colors.isDark) Color(0xFF0891B2) else Color(0xFF00687A)) else colors.cardBg,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            conditionOp = op
                                            if (vStr.isNotEmpty()) conditionValText = vStr
                                        }
                                ) {
                                    Text(
                                        text = lbl,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = if (active) Color.White else colors.textPrimary,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        if (conditionOp != "NONE") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("LT" to "<", "LTE" to "≤", "GT" to ">", "GTE" to "≥").forEach { (opCode, sym) ->
                                    val isSel = conditionOp == opCode
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) colors.violetSolidBg else colors.cardBg)
                                            .clickable { conditionOp = opCode },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = sym,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = if (isSel) Color.White else colors.textPrimary
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = conditionValText,
                                    onValueChange = { input ->
                                        conditionValText = input.replace(',', '.').filter { it.isDigit() || it == '.' }
                                    },
                                    suffix = { Text(if (mode == "MARITIME") "CBM" else "kg", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = fieldColors,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 3. Tarif & Devise (Ariary ou Dollar $)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tarif par $unitUnitLabel",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.toggleTrackBg)
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (!useUsd) (if (colors.isDark) Color(0xFF0891B2) else Color(0xFF00687A)) else Color.Transparent)
                                        .clickable { useUsd = false }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Ar", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (!useUsd) Color.White else colors.textSecondary)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (useUsd) colors.violetSolidBg else Color.Transparent)
                                        .clickable { useUsd = true }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("$ USD", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (useUsd) Color.White else colors.textSecondary)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { input ->
                                    priceText = if (useUsd) {
                                        input.replace(',', '.').filter { it.isDigit() || it == '.' }
                                    } else {
                                        input.filter { it.isDigit() }
                                    }
                                },
                                label = { Text(if (useUsd) "Prix ($)" else "Prix (Ar)") },
                                placeholder = { Text(if (useUsd) "ex: 380" else if (mode == "AÉRIEN") "ex: 45000" else "ex: 1710000") },
                                suffix = { Text(if (useUsd) "$/$unitUnitLabel" else "Ar", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary) },
                                keyboardOptions = KeyboardOptions(keyboardType = if (useUsd) KeyboardType.Decimal else KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = fieldColors,
                                modifier = Modifier.weight(1f)
                            )

                            if (useUsd) {
                                OutlinedTextField(
                                    value = usdRateText,
                                    onValueChange = { usdRateText = it.filter { c -> c.isDigit() } },
                                    label = { Text("1 $ en Ar") },
                                    suffix = { Text("Ar", fontSize = 11.sp, color = colors.textSecondary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = fieldColors,
                                    modifier = Modifier.width(120.dp)
                                )
                            }
                        }

                        if (useUsd) {
                            val enteredUsd = priceText.toDoubleOrNull() ?: 0.0
                            val convertedAr = (enteredUsd * usdRate).toLong()
                            if (convertedAr > 0L) {
                                Text(
                                    text = "Équivalent : ${CurrencyFormatter.formatAriary(convertedAr)} / ${if (mode == "MARITIME") "m³" else "kg"}",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = colors.cyanAccent
                                )
                            }
                        }
                    }

                    // Option: Créer aussi le palier opposé en 1 fois
                    if (initialItem == null && conditionOp != "NONE") {
                        val oppOp = TariffGridParser.oppositeOp(conditionOp)
                        val oppSym = when (oppOp) {
                            "LT" -> "<"
                            "LTE" -> "≤"
                            "GT" -> ">"
                            "GTE" -> "≥"
                            else -> ""
                        }
                        val unitShort = if (mode == "MARITIME") "CBM" else "kg"

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.innerCardBg)
                                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Ajouter aussi le palier $oppSym $conditionValText $unitShort",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Pratique pour définir 2 tranches d'un coup (ex: 380$ et 430$)",
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Switch(
                                    checked = alsoCreateOppositeTier,
                                    onCheckedChange = { alsoCreateOppositeTier = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colors.cyanSolidBg
                                    )
                                )
                            }

                            if (alsoCreateOppositeTier) {
                                OutlinedTextField(
                                    value = oppositePriceText,
                                    onValueChange = { input ->
                                        oppositePriceText = if (useUsd) {
                                            input.replace(',', '.').filter { it.isDigit() || it == '.' }
                                        } else {
                                            input.filter { it.isDigit() }
                                        }
                                    },
                                    label = { Text("Prix pour $oppSym $conditionValText $unitShort (${if (useUsd) "$" else "Ar"})") },
                                    placeholder = { Text(if (useUsd) "ex: 430" else "ex: 1935000") },
                                    suffix = { Text(if (useUsd) "$" else "Ar", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = if (useUsd) KeyboardType.Decimal else KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = fieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // 4. Type d'envoi
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Type d'envoi", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            shipmentSuggestions.forEach { suggestion ->
                                val isSel = shipmentType.equals(suggestion, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = if (isSel) (if (colors.isDark) Color(0xFF0891B2) else Color(0xFF00687A)) else colors.badgeNeutralBg,
                                    modifier = Modifier.clickable { shipmentType = suggestion }
                                ) {
                                    Text(
                                        text = suggestion,
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else colors.textPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = shipmentType,
                            onValueChange = { shipmentType = it },
                            placeholder = { Text("Ou type personnalisé") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 5. Type de marchandise
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Catégorie de marchandise", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            goodsSuggestions.take(4).forEach { suggestion ->
                                val isSel = goodsCategory.equals(suggestion, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = if (isSel) colors.violetSolidBg else colors.badgeNeutralBg,
                                    modifier = Modifier.clickable { goodsCategory = suggestion }
                                ) {
                                    Text(
                                        text = suggestion,
                                        fontFamily = Inter,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else colors.textPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = goodsCategory,
                            onValueChange = { goodsCategory = it },
                            placeholder = { Text("ex: Électronique & Batterie") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 6. Délai d'acheminement (min / max jours)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Délai d'acheminement (jours)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DayStepperInput(
                                label = "Min. jours",
                                value = minDays,
                                onValueChange = {
                                    minDays = it.coerceAtLeast(1)
                                    if (maxDays < minDays) maxDays = minDays
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "à",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            DayStepperInput(
                                label = "Max. jours",
                                value = maxDays,
                                onValueChange = { maxDays = it.coerceAtLeast(minDays) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Footer Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.innerCardBg)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler", color = colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val rawVal = priceText.toDoubleOrNull() ?: 0.0
                            val pAr = if (useUsd) (rawVal * usdRate).toLong() else rawVal.toLong()
                            if (pAr <= 0L) {
                                Toast.makeText(context, "Veuillez indiquer un tarif valide", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val condVal = if (conditionOp != "NONE") conditionValText.toDoubleOrNull() else null
                            if (conditionOp != "NONE" && (condVal == null || condVal <= 0.0)) {
                                Toast.makeText(context, "Veuillez indiquer un seuil valide (ex: 0.25)", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val formattedDays = TariffGridParser.formatLeadDays(minDays, maxDays)
                            val primaryItem = TariffItem(
                                id = initialItem?.id ?: UUID.randomUUID().toString(),
                                mode = mode,
                                shipmentType = shipmentType.trim().ifBlank { "Standard" },
                                goodsCategory = goodsCategory.trim().ifBlank { "Marchandise générale" },
                                priceAr = pAr,
                                leadDays = formattedDays,
                                conditionOp = if (condVal != null) conditionOp else "NONE",
                                conditionValue = condVal,
                                usdPrice = if (useUsd) rawVal else null
                            )

                            val resultList = mutableListOf(primaryItem)

                            if (initialItem == null && conditionOp != "NONE" && alsoCreateOppositeTier && condVal != null) {
                                val oppRaw = oppositePriceText.toDoubleOrNull() ?: 0.0
                                val oppAr = if (useUsd) (oppRaw * usdRate).toLong() else oppRaw.toLong()
                                if (oppAr <= 0L) {
                                    Toast.makeText(context, "Indiquez aussi le tarif de la 2ᵉ tranche", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                resultList.add(
                                    TariffItem(
                                        id = UUID.randomUUID().toString(),
                                        mode = mode,
                                        shipmentType = shipmentType.trim().ifBlank { "Standard" },
                                        goodsCategory = goodsCategory.trim().ifBlank { "Marchandise générale" },
                                        priceAr = oppAr,
                                        leadDays = formattedDays,
                                        conditionOp = TariffGridParser.oppositeOp(conditionOp),
                                        conditionValue = condVal,
                                        usdPrice = if (useUsd) oppRaw else null
                                    )
                                )
                            }

                            onConfirm(resultList)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.cyanSolidBg, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enregistrer le tarif", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
