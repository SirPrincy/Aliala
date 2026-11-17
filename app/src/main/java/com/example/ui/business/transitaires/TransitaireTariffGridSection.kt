package com.example.ui.business.transitaires

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.model.TariffItem
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransitaireTariffGridSection(
    tariffItems: List<TariffItem>,
    onTariffItemsChanged: (List<TariffItem>) -> Unit,
    modifier: Modifier = Modifier,
    headerTitle: String = "Grille Tarifaire",
    headerBadge: String? = null,
    defaultEffectiveDate: String = ""
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
            defaultEffectiveDate = defaultEffectiveDate,
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

                if (item.effectiveDate.isNotBlank()) {
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
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Depuis ${item.effectiveDate}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

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
internal fun TariffConditionSelectorBox(
    mode: String,
    conditionOp: String,
    conditionValText: String,
    onConditionChange: (op: String, valText: String) -> Unit,
    colors: TransitaireColors,
    fieldColors: TextFieldColors
) {
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
                            onConditionChange(op, if (vStr.isNotEmpty()) vStr else conditionValText)
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
                            .clickable { onConditionChange(opCode, conditionValText) },
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
                        onConditionChange(
                            conditionOp,
                            input.replace(',', '.').filter { it.isDigit() || it == '.' }
                        )
                    },
                    suffix = {
                        Text(
                            if (mode == "MARITIME") "CBM" else "kg",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = fieldColors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
