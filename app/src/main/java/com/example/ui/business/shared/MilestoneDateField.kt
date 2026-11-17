package com.example.ui.business.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.DateUtils
import com.example.util.TransitStatsCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MilestoneDateCard(
    title: String,
    subtitle: String,
    currentDateMillis: Long?,
    referenceEarlierMillis: Long? = null,
    referenceLabel: String = "",
    durationLabel: String? = null,
    colors: TransitaireColors = rememberTransitaireColors(),
    testTagPrefix: String = "milestone_date",
    onDateSelected: (Long?) -> Unit
) {
    var dateText by remember(currentDateMillis) {
        mutableStateOf(currentDateMillis?.let { DateUtils.formatShortDate(it) } ?: "")
    }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(currentDateMillis) {
        val formatted = currentDateMillis?.let { DateUtils.formatShortDate(it) } ?: ""
        if (dateText != formatted && (dateText.isBlank() || DateUtils.isValidFrenchDate(dateText))) {
            dateText = formatted
        }
    }

    val isTextValid = dateText.isBlank() || DateUtils.isValidFrenchDate(dateText)
    val parsedMillis = remember(dateText) {
        if (dateText.isNotBlank() && DateUtils.isValidFrenchDate(dateText)) {
            DateUtils.frenchDateToEpochMillis(dateText)
        } else {
            null
        }
    }
    val hasChronologyWarning = remember(parsedMillis, referenceEarlierMillis) {
        TransitStatsCalculator.isChronologicallyBefore(parsedMillis, referenceEarlierMillis)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBg,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cyanSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = colors.cyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = subtitle,
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                if (!durationLabel.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.violetSoftBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = colors.violetAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = durationLabel,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = colors.violetAccent
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { input ->
                        val cleaned = input.filter { it.isDigit() || it == '/' }.take(10)
                        dateText = cleaned
                        if (cleaned.isBlank()) {
                            onDateSelected(null)
                        } else if (cleaned.length == 10 && DateUtils.isValidFrenchDate(cleaned)) {
                            onDateSelected(DateUtils.frenchDateToEpochMillis(cleaned))
                        }
                    },
                    placeholder = {
                        Text(
                            text = "jj/mm/aaaa",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    },
                    singleLine = true,
                    isError = !isTextValid,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (dateText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        dateText = ""
                                        onDateSelected(null)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Effacer la date",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("${testTagPrefix}_picker_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Choisir dans le calendrier",
                                    tint = colors.cyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.cyanAccent,
                        unfocusedBorderColor = colors.border,
                        focusedContainerColor = colors.innerCardBg,
                        unfocusedContainerColor = colors.innerCardBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${testTagPrefix}_input")
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cyanSoftBg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val today = DateUtils.getTodayFrenchDate()
                            dateText = today
                            onDateSelected(DateUtils.frenchDateToEpochMillis(today))
                        }
                        .testTag("${testTagPrefix}_today_btn")
                ) {
                    Text(
                        text = "Aujourd'hui",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.cyanAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp)
                    )
                }
            }

            if (!isTextValid && dateText.length >= 6) {
                Text(
                    text = "Format attendu : jj/mm/aaaa (ex : 14/11/2025)",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.dangerAccent
                )
            } else if (hasChronologyWarning && referenceEarlierMillis != null) {
                Text(
                    text = "Attention : cette date est antérieure à ${referenceLabel.ifBlank { "l'étape précédente" }} (${DateUtils.formatShortDate(referenceEarlierMillis)}).",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = colors.warningAccent
                )
            }
        }
    }

    if (showDatePicker) {
        val initialUtcMillis = remember(dateText, currentDateMillis) {
            DateUtils.frenchDateToUtcPickerMillis(dateText)
                ?: currentDateMillis?.let { DateUtils.frenchDateToUtcPickerMillis(DateUtils.formatShortDate(it)) }
                ?: DateUtils.frenchDateToUtcPickerMillis(DateUtils.getTodayFrenchDate())
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedUtc ->
                            val pickedFrench = DateUtils.utcPickerMillisToFrenchDate(selectedUtc)
                            dateText = pickedFrench
                            onDateSelected(DateUtils.frenchDateToEpochMillis(pickedFrench))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Valider", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
