package com.example.ui.business.transitaires

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.model.TariffItem
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun AddEditTariffDialog(
    initialItem: TariffItem?,
    defaultMode: String,
    defaultEffectiveDate: String = "",
    colors: TransitaireColors,
    onDismiss: () -> Unit,
    onConfirm: (List<TariffItem>) -> Unit
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(initialItem?.mode ?: defaultMode) }
    var shipmentType by remember {
        mutableStateOf(
            initialItem?.shipmentType ?: if (mode == "MARITIME") "Groupage LCL" else "Standard Cargo"
        )
    }
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

    val initialDateText = remember(initialItem, defaultEffectiveDate) {
        initialItem?.effectiveDate?.takeIf { it.isNotBlank() }
            ?: defaultEffectiveDate.takeIf { it.isNotBlank() }
            ?: DateUtils.getTodayFrenchDate()
    }
    var effectiveDateText by remember { mutableStateOf(initialDateText) }
    var showDatePicker by remember { mutableStateOf(false) }

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

    if (showDatePicker) {
        val initialUtcMillis = DateUtils.frenchDateToUtcPickerMillis(effectiveDateText)
            ?: DateUtils.frenchDateToUtcPickerMillis(DateUtils.getTodayFrenchDate())
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { picked ->
                            effectiveDateText = DateUtils.utcPickerMillisToFrenchDate(picked)
                        }
                        showDatePicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

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

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                            Text(
                                "Aérien (/ kg)",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isAir) Color.White else colors.textSecondary
                            )
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
                            Text(
                                "Maritime (/ CBM)",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSea) Color.White else colors.textSecondary
                            )
                        }
                    }

                    TariffConditionSelectorBox(
                        mode = mode,
                        conditionOp = conditionOp,
                        conditionValText = conditionValText,
                        onConditionChange = { op, valText ->
                            conditionOp = op
                            conditionValText = valText
                        },
                        colors = colors,
                        fieldColors = fieldColors
                    )

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

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Date d'application du tarif",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                        OutlinedTextField(
                            value = effectiveDateText,
                            onValueChange = { effectiveDateText = it },
                            label = { Text("Date (jj/mm/aaaa)") },
                            placeholder = { Text("ex: 15/03/2025") },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Choisir dans le calendrier",
                                        tint = colors.cyanAccent
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Ce tarif reste actif à partir de cette date jusqu'au prochain changement daté.",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

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

                            val cleanDate = effectiveDateText.trim()
                            if (!DateUtils.isValidFrenchDate(cleanDate)) {
                                Toast.makeText(context, "Date invalide (format attendu : jj/mm/aaaa)", Toast.LENGTH_SHORT).show()
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
                                usdPrice = if (useUsd) rawVal else null,
                                effectiveDate = cleanDate
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
                                        usdPrice = if (useUsd) oppRaw else null,
                                        effectiveDate = cleanDate
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
