package com.example.ui.business.transitaires

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.business.model.TariffGridParser
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransitaireDialog(
    initialTransitaire: CatalogueTransitaireSummaryUi? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        hub: String,
        airRate: Long,
        seaRate: Long,
        contact: String,
        chinaWarehouseAddress: String,
        isActive: Boolean,
        rateApplicationDate: String,
        airLeadDays: String,
        seaLeadDays: String,
        internalNotes: String,
        airFastRate: Long,
        airFastLeadDays: String,
        goodsCategoryDelays: String
    ) -> Unit
) {
    val context = LocalContext.current
    val colors = rememberTransitaireColors()
    val isEditing = initialTransitaire != null

    var nomTransitaire by remember { mutableStateOf(initialTransitaire?.name ?: "") }
    var contactTransitaire by remember { mutableStateOf(initialTransitaire?.contact ?: "") }
    var adresseChine by remember { mutableStateOf(initialTransitaire?.chinaWarehouseAddress ?: "") }
    var isTransitaireActif by remember { mutableStateOf(initialTransitaire?.isActive ?: true) }

    var tariffItems by remember {
        mutableStateOf(
            if (initialTransitaire == null) {
                emptyList()
            } else {
                TariffGridParser.parse(
                    raw = initialTransitaire.goodsCategoryDelays,
                    airRateAr = initialTransitaire.airRateAr,
                    airFastRateAr = initialTransitaire.airFastRateAr,
                    seaRateAr = initialTransitaire.seaRateAr
                )
            }
        )
    }

    var dateApplication by remember {
        mutableStateOf(initialTransitaire?.rateApplicationDate ?: "")
    }
    var showAppDatePicker by remember { mutableStateOf(false) }

    var testKgInput by remember { mutableStateOf("3.2") }
    var testCbmInput by remember { mutableStateOf("0.30") }

    var airLeadDays by remember { mutableStateOf(initialTransitaire?.airLeadDays ?: "") }
    var airFastLeadDays by remember { mutableStateOf(initialTransitaire?.airFastLeadDays ?: "") }
    var seaLeadDays by remember { mutableStateOf(initialTransitaire?.seaLeadDays ?: "") }
    var notesInternes by remember { mutableStateOf(initialTransitaire?.internalNotes ?: "") }

    var showErrorName by remember { mutableStateOf(false) }

    val testKg = testKgInput.replace(',', '.').toDoubleOrNull() ?: 0.0
    val testCbm = testCbmInput.replace(',', '.').toDoubleOrNull() ?: 0.0

    val matchedAirTariff = TariffGridParser.matchTariffForMeasure(tariffItems, "AÉRIEN", testKg)
    val matchedSeaTariff = TariffGridParser.matchTariffForMeasure(tariffItems, "MARITIME", testCbm)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.inputBg,
        unfocusedContainerColor = colors.inputBg,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        focusedBorderColor = colors.cyanAccent,
        unfocusedBorderColor = if (colors.isDark) colors.border else Color.Transparent,
        errorBorderColor = colors.dangerAccent,
        cursorColor = colors.cyanAccent
    )

    fun submit() {
        if (nomTransitaire.trim().isBlank()) {
            showErrorName = true
            Toast.makeText(context, "Veuillez saisir le nom du transitaire", Toast.LENGTH_SHORT).show()
            return
        }

        val hub = if (adresseChine.isNotBlank()) {
            adresseChine.lines().firstOrNull()?.trim()?.take(40) ?: ""
        } else {
            ""
        }

        val airItems = tariffItems.filter { it.mode == "AÉRIEN" }
        val nonExpressAirItems = airItems.filterNot { it.shipmentType.contains("Express", ignoreCase = true) }
        val airBaseForSave = TariffGridParser.matchTariffForMeasure(nonExpressAirItems, "AÉRIEN", 1.0)?.priceAr
            ?: nonExpressAirItems.firstOrNull()?.priceAr
            ?: airItems.firstOrNull()?.priceAr
            ?: 0L
        val airFastForSave = airItems.firstOrNull { it.shipmentType.contains("Express", ignoreCase = true) }?.priceAr
            ?: airItems.getOrNull(1)?.priceAr
            ?: 0L
        val seaBaseForSave = TariffGridParser.matchTariffForMeasure(tariffItems, "MARITIME", 1.0)?.priceAr
            ?: tariffItems.firstOrNull { it.mode == "MARITIME" }?.priceAr
            ?: 0L

        val goodsDelaysEncoded = TariffGridParser.encode(tariffItems)
        val hasAnyRate = airBaseForSave > 0L || airFastForSave > 0L || seaBaseForSave > 0L
        val cleanDateApp = dateApplication.trim()
        if (hasAnyRate && cleanDateApp.isNotBlank() && !DateUtils.isValidFrenchDate(cleanDateApp)) {
            Toast.makeText(context, "Date des tarifs invalide (jj/mm/aaaa)", Toast.LENGTH_SHORT).show()
            return
        }
        val latestItemDate = tariffItems
            .mapNotNull { item -> item.effectiveDate.takeIf { DateUtils.isValidFrenchDate(it) } }
            .maxByOrNull { DateUtils.frenchDateToEpochMillis(it) }
            .orEmpty()
        val effectiveDate = when {
            !hasAnyRate -> ""
            cleanDateApp.isNotBlank() -> cleanDateApp
            latestItemDate.isNotBlank() -> latestItemDate
            else -> DateUtils.getTodayFrenchDate()
        }

        onConfirm(
            nomTransitaire.trim(),
            hub,
            airBaseForSave,
            seaBaseForSave,
            contactTransitaire.trim(),
            adresseChine.trim(),
            isTransitaireActif,
            effectiveDate,
            airLeadDays.trim(),
            seaLeadDays.trim(),
            notesInternes.trim(),
            airFastForSave,
            airFastLeadDays.trim(),
            goodsDelaysEncoded
        )
    }

    if (showAppDatePicker) {
        val initialUtcMillis = DateUtils.frenchDateToUtcPickerMillis(dateApplication)
            ?: DateUtils.frenchDateToUtcPickerMillis(DateUtils.getTodayFrenchDate())
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)
        DatePickerDialog(
            onDismissRequest = { showAppDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { picked ->
                            dateApplication = DateUtils.utcPickerMillisToFrenchDate(picked)
                        }
                        showAppDatePicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAppDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.cardBg,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_back_add_transitaire")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEditing) "Modifier le transitaire" else "Nouveau transitaire",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Fiche partenaire, tarifs & délais",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.cyanSoftBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = colors.cyanAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Informations Générales",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Identification et localisation du partenaire",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Nom du transitaire ",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "*",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.dangerAccent
                                    )
                                }
                                OutlinedTextField(
                                    value = nomTransitaire,
                                    onValueChange = {
                                        nomTransitaire = it
                                        if (it.isNotBlank()) showErrorName = false
                                    },
                                    placeholder = {
                                        Text(
                                            "Ex : Nom du transitaire",
                                            fontFamily = Inter,
                                            fontSize = 14.sp,
                                            color = colors.textSecondary.copy(alpha = 0.65f)
                                        )
                                    },
                                    isError = showErrorName,
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_nom_transitaire")
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Contact / Téléphone / WeChat",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                                OutlinedTextField(
                                    value = contactTransitaire,
                                    onValueChange = { contactTransitaire = it },
                                    placeholder = {
                                        Text(
                                            "Téléphone, WhatsApp ou identifiant WeChat",
                                            fontFamily = Inter,
                                            fontSize = 14.sp,
                                            color = colors.textSecondary.copy(alpha = 0.65f)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_contact_transitaire")
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Adresse entrepôt en Chine (optionnel)",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                                OutlinedTextField(
                                    value = adresseChine,
                                    onValueChange = { adresseChine = it },
                                    placeholder = {
                                        Text(
                                            "Ville, district, rue, porte de l'entrepôt...",
                                            fontFamily = Inter,
                                            fontSize = 13.sp,
                                            color = colors.textSecondary.copy(alpha = 0.65f)
                                        )
                                    },
                                    minLines = 2,
                                    maxLines = 4,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_adresse_chine")
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.innerCardBg)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Transitaire actif",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "Disponible pour l'assignation des futurs colis",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                    Switch(
                                        checked = isTransitaireActif,
                                        onCheckedChange = { isTransitaireActif = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = colors.cyanSolidBg,
                                            uncheckedThumbColor = colors.textSecondary,
                                            uncheckedTrackColor = colors.badgeNeutralBg
                                        ),
                                        modifier = Modifier.testTag("switch_transitaire_actif")
                                    )
                                }
                            }
                        }
                    }

                    TransitaireTariffGridSection(
                        tariffItems = tariffItems,
                        onTariffItemsChanged = { updatedItems ->
                            val changedItemDate = updatedItems
                                .firstOrNull { updated ->
                                    val prev = tariffItems.firstOrNull { it.id == updated.id }
                                    (prev == null || prev.effectiveDate != updated.effectiveDate || prev.priceAr != updated.priceAr) &&
                                        DateUtils.isValidFrenchDate(updated.effectiveDate)
                                }?.effectiveDate
                            if (!changedItemDate.isNullOrBlank()) {
                                dateApplication = changedItemDate
                            } else if (updatedItems.isEmpty()) {
                                dateApplication = ""
                            }
                            tariffItems = updatedItems
                        },
                        headerBadge = when {
                            dateApplication.isNotBlank() -> "Au $dateApplication"
                            !isEditing -> "Optionnel"
                            else -> null
                        },
                        defaultEffectiveDate = dateApplication
                    )

                    if (tariffItems.isNotEmpty() || dateApplication.isNotBlank()) {
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
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Date d'application de ces tarifs",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                                OutlinedTextField(
                                    value = dateApplication,
                                    onValueChange = { dateApplication = it },
                                    placeholder = { Text("jj/mm/aaaa (ex: 15/03/2025)") },
                                    trailingIcon = {
                                        IconButton(onClick = { showAppDatePicker = true }) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Choisir une date",
                                                tint = colors.cyanAccent
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = fieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "Laissez la date d'origine ou choisissez une date passée pour vos anciennes données.",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    AddTransitaireSimulatorCard(
                        testKgInput = testKgInput,
                        onTestKgInputChange = { testKgInput = it },
                        testCbmInput = testCbmInput,
                        onTestCbmInputChange = { testCbmInput = it },
                        testKg = testKg,
                        testCbm = testCbm,
                        matchedAirTariff = matchedAirTariff,
                        matchedSeaTariff = matchedSeaTariff,
                        colors = colors
                    )

                    TransitaireLeadTimesPolicySection(
                        airLeadDays = airLeadDays,
                        onAirLeadDaysChange = { airLeadDays = it },
                        airFastLeadDays = airFastLeadDays,
                        onAirFastLeadDaysChange = { airFastLeadDays = it },
                        seaLeadDays = seaLeadDays,
                        onSeaLeadDaysChange = { seaLeadDays = it },
                        notesInternes = notesInternes,
                        onNotesInternesChange = { notesInternes = it }
                    )

                    Spacer(modifier = Modifier.height(82.dp))
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = colors.cardBg,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Button(
                        onClick = { submit() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.cyanSolidBg,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_save_transitaire_bottom")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = if (isEditing) "Enregistrer les modifications" else "Enregistrer le transitaire",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
