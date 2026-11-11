package com.example.ui.business.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.util.Locale

@Composable
fun AddTransitaireDialog(
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
        internalNotes: String
    ) -> Unit
) {
    val context = LocalContext.current

    // Form fields
    var nomTransitaire by remember { mutableStateOf("") }
    var contactTransitaire by remember { mutableStateOf("") }
    var adresseChine by remember { mutableStateOf("") }
    var isTransitaireActif by remember { mutableStateOf(true) }

    // Rates
    var tarifAerienInput by remember { mutableStateOf("38000") }
    var tarifMaritimeInput by remember { mutableStateOf("1200000") }
    var dateApplication by remember { mutableStateOf("24/10/2026") }

    // Test simulation inputs
    var testKgInput by remember { mutableStateOf("3.2") }
    var testCbmInput by remember { mutableStateOf("0.05") }

    // Policy & Notes
    var airLeadDays by remember { mutableStateOf("5 à 8 jours") }
    var seaLeadDays by remember { mutableStateOf("30 à 45 jours") }
    var notesInternes by remember { mutableStateOf("") }

    var showErrorName by remember { mutableStateOf(false) }

    // Numeric conversions
    val tarifAerien = tarifAerienInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val tarifMaritime = tarifMaritimeInput.filter { it.isDigit() }.toLongOrNull() ?: 0L

    val testKg = testKgInput.replace(',', '.').toDoubleOrNull() ?: 0.0
    val testCbm = testCbmInput.replace(',', '.').toDoubleOrNull() ?: 0.0

    val totalAirComputed = (testKg * tarifAerien).toLong()
    val totalSeaComputed = (testCbm * tarifMaritime).toLong()

    val formattedTestKg = String.format(Locale.FRENCH, "%.1f", testKg).replace('.', ',')
    val formattedTestCbm = String.format(Locale.FRENCH, "%.2f", testCbm).replace('.', ',')

    fun submit() {
        if (nomTransitaire.trim().isBlank()) {
            showErrorName = true
            Toast.makeText(context, "Veuillez saisir le nom du transitaire", Toast.LENGTH_SHORT).show()
            return
        }

        val hub = if (adresseChine.isNotBlank()) {
            adresseChine.lines().firstOrNull()?.take(40) ?: "Hub Guangzhou & Yiwu"
        } else {
            "Hub Guangzhou & Yiwu"
        }

        onConfirm(
            nomTransitaire.trim(),
            hub,
            tarifAerien,
            tarifMaritime,
            contactTransitaire.trim(),
            adresseChine.trim(),
            isTransitaireActif,
            dateApplication.trim(),
            airLeadDays.trim(),
            seaLeadDays.trim(),
            notesInternes.trim()
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF8FF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // ---------------------------------------------------------
                // 1. TOP HEADER (Nouveau Transitaire + Back + Profile)
                // ---------------------------------------------------------
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFAF8FF).copy(alpha = 0.96f),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = Color(0xFF131B2E),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Nouveau Transitaire",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF131B2E)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color(0xFF3D494C),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00687A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profil",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // ---------------------------------------------------------
                // 2. SCROLLABLE FORM CONTENT
                // ---------------------------------------------------------
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Modal Action Bar inside Content Slot
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF2F3FF))
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fermer",
                                    tint = Color(0xFF3D494C),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "FICHE LOGISTIQUE",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF00687A),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Nouveau Transitaire",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }
                        }

                        Button(
                            onClick = { submit() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF06B6D4),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("btn_save_transitaire_top")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Enregistrer",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // SECTION 1: INFORMATIONS GÉNÉRALES
                    // -----------------------------------------------------
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
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
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF00687A).copy(alpha = 0.10f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = Color(0xFF00687A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Informations Générales",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Text(
                                        text = "Identification et localisation du partenaire",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                }
                            }

                            // Nom du transitaire *
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Nom du transitaire ",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Text(
                                        text = "*",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFBA1A1A)
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
                                            "ex: SinoExpress Guangzhou",
                                            fontFamily = Inter,
                                            fontSize = 14.sp,
                                            color = Color(0xFF3D494C).copy(alpha = 0.5f)
                                        )
                                    },
                                    isError = showErrorName,
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF00687A),
                                        unfocusedBorderColor = Color.Transparent,
                                        errorBorderColor = Color(0xFFBA1A1A)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_nom_transitaire")
                                )
                            }

                            // Contact / Téléphone / WeChat ID
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Contact / Téléphone / WeChat ID",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF131B2E)
                                )
                                OutlinedTextField(
                                    value = contactTransitaire,
                                    onValueChange = { contactTransitaire = it },
                                    placeholder = {
                                        Text(
                                            "+86 138 0000 0000 / @sino_freight",
                                            fontFamily = Inter,
                                            fontSize = 14.sp,
                                            color = Color(0xFF3D494C).copy(alpha = 0.5f)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = null,
                                            tint = Color(0xFF3D494C),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF00687A),
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_contact_transitaire")
                                )
                            }

                            // Adresse entrepôt Chine
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Adresse entrepôt en Chine",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF131B2E)
                                )
                                OutlinedTextField(
                                    value = adresseChine,
                                    onValueChange = { adresseChine = it },
                                    placeholder = {
                                        Text(
                                            "Guangzhou, Baiyun District, Shijing Logistics Hub, Gate 4...",
                                            fontFamily = Inter,
                                            fontSize = 13.sp,
                                            color = Color(0xFF3D494C).copy(alpha = 0.5f)
                                        )
                                    },
                                    minLines = 2,
                                    maxLines = 4,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF00687A),
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_adresse_chine")
                                )
                            }

                            // Switch toggle 'Transitaire actif'
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF2F3FF))
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
                                            color = Color(0xFF131B2E)
                                        )
                                        Text(
                                            text = "Disponible pour l'assignation des futurs colis",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                    }
                                    Switch(
                                        checked = isTransitaireActif,
                                        onCheckedChange = { isTransitaireActif = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF00687A),
                                            uncheckedThumbColor = Color(0xFF6D797D),
                                            uncheckedTrackColor = Color(0xFFD2D9F4)
                                        ),
                                        modifier = Modifier.testTag("switch_transitaire_actif")
                                    )
                                }
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // SECTION 2: GRILLE TARIFAIRE INITIALE
                    // -----------------------------------------------------
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
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
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF712AE2).copy(alpha = 0.10f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = Color(0xFF712AE2),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Grille Tarifaire Initiale",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Text(
                                        text = "Index de tarification au départ de Chine",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                }
                            }

                            // Hint box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF2F3FF))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF00687A),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Text(
                                        text = "Le poids et le volume réels seront saisis à l'arrivée des colis à l'entrepôt. Tous les tarifs sont TTC en Ariary.",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                }
                            }

                            // Tarif Aérien
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tarif fret aérien",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FlightTakeoff,
                                            contentDescription = null,
                                            tint = Color(0xFF00687A),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Express",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF00687A)
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = tarifAerienInput,
                                    onValueChange = { tarifAerienInput = it.filter { c -> c.isDigit() } },
                                    trailingIcon = {
                                        Text(
                                            text = "Ar / kg",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF3D494C),
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF131B2E)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF00687A),
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_tarif_aerien")
                                )
                            }

                            // Tarif Maritime
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tarif fret maritime",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsBoat,
                                            contentDescription = null,
                                            tint = Color(0xFF712AE2),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Groupage",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF712AE2)
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = tarifMaritimeInput,
                                    onValueChange = { tarifMaritimeInput = it.filter { c -> c.isDigit() } },
                                    trailingIcon = {
                                        Text(
                                            text = "Ar / m³",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF3D494C),
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF131B2E)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF712AE2),
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_tarif_maritime")
                                )
                            }

                            // Date d'application de la grille
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Date d'application de la grille",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF131B2E)
                                )
                                OutlinedTextField(
                                    value = dateApplication,
                                    onValueChange = { dateApplication = it },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Event,
                                            contentDescription = null,
                                            tint = Color(0xFF3D494C),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF00687A),
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_date_application")
                                )
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // SECTION 3: SIMULATEUR DE CALCUL DIRECT
                    // -----------------------------------------------------
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
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
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF06B6D4).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Calculate,
                                            contentDescription = null,
                                            tint = Color(0xFF00687A),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Exemple de fret calculé",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                        Text(
                                            text = "Simulation dynamique en direct",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFFEAEDFF))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "APERÇU",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF00687A),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            // Carte test Aérien
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF2F3FF))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Flight,
                                                contentDescription = null,
                                                tint = Color(0xFF00687A),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Colis test Aérien",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF00687A)
                                            )
                                        }

                                        // Editable input for test weight
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            BasicTextField(
                                                value = testKgInput,
                                                onValueChange = { testKgInput = it },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true,
                                                textStyle = TextStyle(
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    textAlign = TextAlign.End,
                                                    color = Color(0xFF131B2E)
                                                ),
                                                modifier = Modifier.width(44.dp)
                                            )
                                            Text(
                                                text = "kg",
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = Color(0xFF3D494C)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = "$formattedTestKg kg × ${CurrencyFormatter.formatAriary(tarifAerien)}",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                        Text(
                                            text = CurrencyFormatter.formatAriary(totalAirComputed),
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = Color(0xFF00687A)
                                        )
                                    }
                                }
                            }

                            // Carte test Maritime
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF2F3FF))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsBoat,
                                                contentDescription = null,
                                                tint = Color(0xFF712AE2),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Carton test Maritime",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF712AE2)
                                            )
                                        }

                                        // Editable input for test volume
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            BasicTextField(
                                                value = testCbmInput,
                                                onValueChange = { testCbmInput = it },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true,
                                                textStyle = TextStyle(
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    textAlign = TextAlign.End,
                                                    color = Color(0xFF131B2E)
                                                ),
                                                modifier = Modifier.width(44.dp)
                                            )
                                            Text(
                                                text = "m³",
                                                fontFamily = Inter,
                                                fontSize = 12.sp,
                                                color = Color(0xFF3D494C)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = "$formattedTestCbm m³ × ${CurrencyFormatter.formatAriary(tarifMaritime)}",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                        Text(
                                            text = CurrencyFormatter.formatAriary(totalSeaComputed),
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = Color(0xFF712AE2)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // SECTION 4: POLITIQUE & DÉLAIS
                    // -----------------------------------------------------
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
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
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEAEDFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFF3D494C),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Politique & Délais",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Text(
                                        text = "Engagement et stipulations de transport",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                }
                            }

                            // 2 Mini Cards for lead times
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Flight,
                                                contentDescription = null,
                                                tint = Color(0xFF3D494C),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "DÉLAI AÉRIEN",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = Color(0xFF3D494C),
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Text(
                                            text = airLeadDays,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                        Text(
                                            text = "Jours ouvrés après vol",
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF2F3FF))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsBoat,
                                                contentDescription = null,
                                                tint = Color(0xFF3D494C),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "DÉLAI MARITIME",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = Color(0xFF3D494C),
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Text(
                                            text = seaLeadDays,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                        Text(
                                            text = "Départ conteneur régulier",
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                    }
                                }
                            }

                            // Notes internes
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Notes internes et conditions spéciales",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF131B2E)
                                )
                                OutlinedTextField(
                                    value = notesInternes,
                                    onValueChange = { notesInternes = it },
                                    placeholder = {
                                        Text(
                                            "Ex: Facturation au poids volumétrique si supérieur au poids réel (ratio 1:6000). Pas de batterie sans déclaration.",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            color = Color(0xFF3D494C).copy(alpha = 0.5f)
                                        )
                                    },
                                    minLines = 3,
                                    maxLines = 5,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF00687A),
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_notes_internes")
                                )
                            }
                        }
                    }

                    // Spacer for bottom sticky bar clearance
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }

            // -------------------------------------------------------------
            // 3. STICKY BOTTOM CONFIRMATION CTA
            // -------------------------------------------------------------
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color(0xFFFAF8FF).copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { submit() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF06B6D4),
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
                                text = "Enregistrer le transitaire",
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
