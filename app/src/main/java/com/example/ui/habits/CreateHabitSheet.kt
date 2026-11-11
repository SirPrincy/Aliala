package com.example.ui.habits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.FinanceColor
import com.example.ui.theme.HabitsColor
import com.example.ui.theme.LanguageColor
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.VioletSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class HabitTemplate(
    val name: String,
    val emoji: String,
    val type: String, // "BOOLEAN", "COUNTER", "DURATION", "AVOID"
    val target: Int,
    val unit: String,
    val moment: String,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateHabitSheet(
    habitToEdit: HabitUiItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        category: String,
        iconName: String,
        targetDays: Int,
        habitType: String,
        targetCount: Int,
        unit: String,
        colorHex: String,
        reminderTimes: String,
        note: String
    ) -> Unit
) {
    val scrollState = rememberScrollState()

    // 10 theme colors
    val paletteColors = listOf(
        CyanPrimary,
        VioletSecondary,
        FinanceColor,
        Color(0xFF0D9488), // Teal
        Color(0xFFF43F5E), // Corail
        Color(0xFF38BDF8), // Ciel
        Color(0xFFA855F7), // Lilas
        LanguageColor,      // Pervenche (0xFF6366F1)
        Color(0xFF64748B), // Gris doux
        HabitsColor         // Menthe (0xFF22C55E)
    )

    // Initial state from habitToEdit or defaults
    var name by remember(habitToEdit) { mutableStateOf(habitToEdit?.name ?: "Boire de l'eau") }
    var selectedEmoji by remember(habitToEdit) { mutableStateOf(habitToEdit?.iconEmoji ?: "💧") }

    val initialColor = remember(habitToEdit) {
        if (habitToEdit != null && habitToEdit.colorHex.isNotBlank()) {
            try {
                Color(android.graphics.Color.parseColor(habitToEdit.colorHex))
            } catch (e: Exception) {
                CyanPrimary
            }
        } else CyanPrimary
    }
    var selectedColor by remember(habitToEdit) { mutableStateOf(initialColor) }

    val initialType = remember(habitToEdit) {
        when (habitToEdit?.type) {
            HabitCardType.COUNTER -> "COUNTER"
            HabitCardType.TIMER -> "DURATION"
            HabitCardType.SWIPABLE -> "AVOID"
            else -> "BOOLEAN"
        }
    }
    var habitType by remember(habitToEdit) { mutableStateOf(if (habitToEdit != null) initialType else "COUNTER") }
    var dailyTarget by remember(habitToEdit) { mutableIntStateOf(habitToEdit?.counterTarget ?: 8) }
    var selectedUnit by remember(habitToEdit) { mutableStateOf(habitToEdit?.counterUnit ?: "verres") }
    var selectedFrequency by remember(habitToEdit) { mutableStateOf("EVERY_DAY") }
    var selectedDays by remember(habitToEdit) {
        val count = habitToEdit?.targetDaysPerWeek ?: 7
        mutableStateOf(if (count >= 7) (1..7).toSet() else (1..count).toSet())
    }
    var selectedMoment by remember(habitToEdit) { mutableStateOf(habitToEdit?.category ?: "Matin") }

    val initialReminders = remember(habitToEdit) {
        val raw = habitToEdit?.reminderTimes ?: ""
        if (raw.isNotBlank()) raw.split(",").filter { it.isNotBlank() } else listOf("08:00", "14:00")
    }
    var remindersEnabled by remember { mutableStateOf(habitToEdit == null || habitToEdit.reminderTimes.isNotBlank()) }
    var reminderTimes by remember(habitToEdit) { mutableStateOf(initialReminders) }
    var hasEndDate by remember { mutableStateOf(false) }
    var isPartialSuccess by remember { mutableStateOf(true) }
    var personalNote by remember(habitToEdit) { mutableStateOf(habitToEdit?.note ?: "") }

    var isPreviewExpanded by remember { mutableStateOf(true) }
    var isAdvancedOptionsExpanded by remember { mutableStateOf(false) }
    var previewCount by remember { mutableIntStateOf(0) }
    var emojiSearch by remember { mutableStateOf("") }
    var selectedEmojiCategory by remember { mutableStateOf("Santé") }
    var isCustomSelected by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddTimeDialog by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val picker = android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                startDate = LocalDate.of(year, month + 1, dayOfMonth)
                showDatePicker = false
            },
            startDate.year,
            startDate.monthValue - 1,
            startDate.dayOfMonth
        )
        picker.setOnDismissListener { showDatePicker = false }
        picker.show()
    }

    if (showAddTimeDialog) {
        val timePicker = android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val formatted = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                if (!reminderTimes.contains(formatted)) {
                    reminderTimes = (reminderTimes + formatted).sorted()
                }
                showAddTimeDialog = false
            },
            8,
            0,
            true
        )
        timePicker.setOnDismissListener { showAddTimeDialog = false }
        timePicker.show()
    }

    // Pulse animation for Live Preview indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Formatted current date
    val todayFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH))
    }

    // Templates
    val templatesSante = listOf(
        HabitTemplate("Boire de l'eau", "💧", "COUNTER", 8, "verres", "Matin", CyanPrimary),
        HabitTemplate("Dormir avant 23h", "😴", "BOOLEAN", 1, "fois", "Soir", VioletSecondary),
        HabitTemplate("Marcher 8 000 pas", "👟", "COUNTER", 8000, "pas", "Matin", FinanceColor),
        HabitTemplate("Faire du sport", "🏃", "DURATION", 30, "min", "Matin", HabitsColor)
    )

    val templatesEsprit = listOf(
        HabitTemplate("Lecture quotidienne", "📖", "DURATION", 20, "min", "Matin", VioletSecondary),
        HabitTemplate("Méditation guidée", "🧘", "DURATION", 10, "min", "Matin", CyanPrimary),
        HabitTemplate("Réviser l'allemand", "🇩🇪", "DURATION", 15, "min", "Soir", LanguageColor),
        HabitTemplate("Écrire mon journal", "✍️", "BOOLEAN", 1, "fois", "Soir", Color(0xFF38BDF8))
    )

    val templatesQuotidien = listOf(
        HabitTemplate("Planifier ma journée", "📅", "BOOLEAN", 1, "fois", "Matin", CyanPrimary),
        HabitTemplate("Revoir mes finances", "💰", "BOOLEAN", 1, "fois", "Soir", HabitsColor)
    )

    val templatesEviter = listOf(
        HabitTemplate("Zéro grignotage", "🍫", "AVOID", 0, "fois", "N'importe quand", DangerRed),
        HabitTemplate("Réseaux sociaux limités", "📵", "AVOID", 60, "min", "Soir", DangerRed),
        HabitTemplate("Ne pas fumer", "🚭", "AVOID", 0, "fois", "N'importe quand", DangerRed)
    )

    val applyTemplate: (HabitTemplate) -> Unit = { tmpl ->
        name = tmpl.name
        selectedEmoji = tmpl.emoji
        habitType = tmpl.type
        dailyTarget = tmpl.target
        selectedUnit = tmpl.unit
        selectedMoment = tmpl.moment
        selectedColor = tmpl.color
        previewCount = 0
        isCustomSelected = false
        showError = false
    }

    // Emojis dictionary
    val emojiCategories = mapOf(
        "Santé" to listOf("💧", "🏃", "😴", "👟", "🥗", "🏋️", "❤️", "🍎", "🧘"),
        "Sport" to listOf("🏃", "👟", "🏋️", "🚴", "🏊", "⚽", "🧘", "🧗"),
        "Esprit" to listOf("📖", "🧘", "✍️", "🇩🇪", "🧠", "📚", "🎨", "💡"),
        "Quotidien" to listOf("📅", "💰", "☕", "💻", "⏰", "🧹", "🛒", "🚿"),
        "Loisirs" to listOf("🎸", "🎮", "🎬", "🎧", "✈️", "🪴", "🐶", "🍳")
    )

    val currentEmojis = remember(selectedEmojiCategory, emojiSearch) {
        val baseList = emojiCategories[selectedEmojiCategory] ?: emojiCategories["Santé"] ?: emptyList()
        if (emojiSearch.isBlank()) baseList else baseList
    }

    // Convert color to hex
    val colorToHex = { color: Color ->
        try {
            String.format(
                "#%02X%02X%02X",
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt()
            )
        } catch (e: Exception) {
            "#06B6D4"
        }
    }

    // Save action helper
    val triggerSave = {
        if (name.isNotBlank()) {
            val hex = colorToHex(selectedColor)
            val remindersStr = if (remindersEnabled) reminderTimes.joinToString(",") else ""
            onConfirm(
                name.trim(),
                selectedMoment,
                selectedEmoji,
                selectedDays.size.coerceIn(1, 7),
                habitType,
                dailyTarget,
                selectedUnit,
                hex,
                remindersStr,
                personalNote.trim()
            )
        } else {
            showError = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.TopCenter
    ) {
        val isTablet = maxWidth >= 600.dp
        val maxContentWidth = 640.dp
        val horizontalPad = if (isTablet) 24.dp else 16.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = maxContentWidth)
        ) {
            // ==========================================
            // HEADER BAR
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPad, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("habit_form_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (habitToEdit != null) "Modifier l'habitude" else "Nouvelle Habitude",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = triggerSave,
                        enabled = name.isNotBlank(),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("habit_top_save_btn")
                    ) {
                        Text(
                            text = "Enregistrer",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (name.isNotBlank()) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Utilisateur",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ==========================================
            // SCROLLABLE FORM BODY (RESPONSIVE)
            // ==========================================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = horizontalPad)
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ==========================================
                // 1. CARTE APERÇU EN DIRECT (LIVE PREVIEW)
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card Header with toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(CyanPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(CyanPrimary)
                                            .alpha(pulseAlpha)
                                    )
                                    Text(
                                        text = "APERÇU EN DIRECT",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.6.sp,
                                        color = CyanPrimary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isPreviewExpanded = !isPreviewExpanded },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPreviewExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Replier l'aperçu",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isPreviewExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Live card item
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(selectedColor.copy(alpha = 0.18f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = selectedEmoji, fontSize = 26.sp)
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = name.ifBlank { "Sans titre" },
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(9999.dp))
                                                        .background(VioletSecondary.copy(alpha = 0.12f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "🔥 ${habitToEdit?.streak ?: 0} j",
                                                        fontFamily = PlusJakartaSans,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        color = VioletSecondary
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            val subtext = when (habitType) {
                                                "COUNTER" -> "$dailyTarget $selectedUnit / jour · $selectedMoment"
                                                "DURATION" -> "$dailyTarget $selectedUnit / jour · $selectedMoment"
                                                "AVOID" -> "Abstinence · $selectedMoment"
                                                else -> "1 fois / jour · $selectedMoment"
                                            }
                                            Text(
                                                text = subtext,
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Stepper or Checkbox
                                    if (habitType == "COUNTER" || habitType == "DURATION") {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                                .padding(3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                                    .clickable {
                                                        if (previewCount > 0) previewCount--
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Diminuer",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Text(
                                                text = "$previewCount/$dailyTarget",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = selectedColor,
                                                modifier = Modifier.padding(horizontal = 6.dp)
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(selectedColor)
                                                    .clickable {
                                                        if (previewCount < dailyTarget) previewCount++
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Augmenter",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                                .border(1.5.dp, selectedColor, RoundedCornerShape(10.dp))
                                                .clickable { previewCount = if (previewCount == 0) 1 else 0 },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (previewCount > 0) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Validé",
                                                    tint = selectedColor,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Natural Summary banner
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                    )
                                    val freqSummary = when (selectedFrequency) {
                                        "EVERY_DAY" -> "tous les jours"
                                        "SOME_DAYS" -> "${selectedDays.size} jours sélectionnés"
                                        else -> "${selectedDays.size} fois / sem."
                                    }
                                    val remindersSummary = if (remindersEnabled && reminderTimes.isNotEmpty()) {
                                        "rappels à ${reminderTimes.joinToString(" & ")}"
                                    } else "sans rappel"

                                    Text(
                                        text = "Résumé : ${name.ifBlank { "Habitude" }} · $dailyTarget $selectedUnit, $freqSummary, le ${selectedMoment.lowercase()}, $remindersSummary.",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 2. ÉTAPE 0 : MODÈLES EN 1 TAP
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            Text(
                                text = "Par quoi veux-tu commencer ?",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "MODÈLES",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = CyanPrimary,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = "Touche une suggestion pour pré-remplir l'habitude en un éclair.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Option personnalisée
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isCustomSelected) CyanPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLow)
                                .border(
                                    if (isCustomSelected) 1.5.dp else 0.dp,
                                    if (isCustomSelected) CyanPrimary else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { isCustomSelected = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CyanPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = "Personnalisé",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Créer mon habitude personnalisée",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Paramétrage complet ci-dessous",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.sp,
                                        color = CyanPrimary
                                    )
                                }
                            }
                            if (isCustomSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Sélectionné",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Santé & Vitalité
                        Text(
                            text = "SANTÉ & VITALITÉ",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            templatesSante.forEach { tmpl ->
                                TemplateChip(
                                    emoji = tmpl.emoji,
                                    label = "${tmpl.name} (${tmpl.target} ${tmpl.unit})",
                                    onClick = { applyTemplate(tmpl) }
                                )
                            }
                        }

                        // Esprit & Apprentissage
                        Text(
                            text = "ESPRIT & APPRENTISSAGE",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            templatesEsprit.forEach { tmpl ->
                                TemplateChip(
                                    emoji = tmpl.emoji,
                                    label = "${tmpl.name} (${tmpl.target} ${tmpl.unit})",
                                    onClick = { applyTemplate(tmpl) }
                                )
                            }
                        }

                        // Productivité & Quotidien
                        Text(
                            text = "PRODUCTIVITÉ & QUOTIDIEN",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            templatesQuotidien.forEach { tmpl ->
                                TemplateChip(
                                    emoji = tmpl.emoji,
                                    label = tmpl.name,
                                    onClick = { applyTemplate(tmpl) }
                                )
                            }
                        }

                        // À éviter (Libération)
                        Text(
                            text = "À ÉVITER (LIBÉRATION)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = DangerRed,
                            letterSpacing = 0.5.sp
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            templatesEviter.forEach { tmpl ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(DangerRed.copy(alpha = 0.12f))
                                        .clickable { applyTemplate(tmpl) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${tmpl.emoji} ${tmpl.name}",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = DangerRed
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 3. NOM DE L'HABITUDE
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nom de l'habitude",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${name.length} / 40",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                if (it.length <= 40) {
                                    name = it
                                    if (it.isNotBlank()) showError = false
                                }
                            },
                            placeholder = { Text("Ex. Méditer 10 minutes", fontSize = 14.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("habit_name_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            isError = showError,
                            trailingIcon = {
                                if (name.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Valide",
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nom court, clair et motivant.",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (name.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Nom valide",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.sp,
                                        color = CyanPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. ICÔNE & COULEUR
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Icône & Couleur",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Identité visuelle",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Search emoji
                        OutlinedTextField(
                            value = emojiSearch,
                            onValueChange = { emojiSearch = it },
                            placeholder = { Text("Rechercher un émoji...", fontSize = 13.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Recherche",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )

                        // Category tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Santé", "Sport", "Esprit", "Quotidien", "Loisirs").forEach { cat ->
                                val isSelected = selectedEmojiCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceContainerLow)
                                        .clickable { selectedEmojiCategory = cat }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Emoji Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            currentEmojis.take(7).forEach { emoji ->
                                val isSelected = selectedEmoji == emoji
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerLow
                                        )
                                        .border(
                                            if (isSelected) 2.dp else 0.dp,
                                            if (isSelected) CyanPrimary else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Palette of 10 colors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            paletteColors.forEach { col ->
                                val isSelected = selectedColor == col
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 34.dp else 28.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .clickable { selectedColor = col },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Sélectionné",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 5. TYPE D'HABITUDE (RESPONSIVE 2x2 or 4x1)
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            Text(
                                text = "Type d'habitude",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Comment la mesurer ?",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isTablet) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TypeCard(
                                    title = "Oui / Non",
                                    subtitle = "Fait ou pas fait",
                                    icon = Icons.Default.CheckBox,
                                    isSelected = habitType == "BOOLEAN",
                                    modifier = Modifier.weight(1f),
                                    onClick = { habitType = "BOOLEAN" }
                                )
                                TypeCard(
                                    title = "Compteur",
                                    subtitle = "Quantités et paliers",
                                    icon = Icons.Default.Pin,
                                    isSelected = habitType == "COUNTER",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        habitType = "COUNTER"
                                        if (selectedUnit == "fois" || selectedUnit == "min") selectedUnit = "verres"
                                    }
                                )
                                TypeCard(
                                    title = "Durée",
                                    subtitle = "Minuteur chronométré",
                                    icon = Icons.Default.Timer,
                                    isSelected = habitType == "DURATION",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        habitType = "DURATION"
                                        selectedUnit = "min"
                                    }
                                )
                                TypeCard(
                                    title = "À éviter",
                                    subtitle = "Jours d'abstinence",
                                    icon = Icons.Default.DoNotDisturbOn,
                                    isSelected = habitType == "AVOID",
                                    modifier = Modifier.weight(1f),
                                    onClick = { habitType = "AVOID" }
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TypeCard(
                                    title = "Oui / Non",
                                    subtitle = "Fait ou pas fait",
                                    icon = Icons.Default.CheckBox,
                                    isSelected = habitType == "BOOLEAN",
                                    modifier = Modifier.weight(1f),
                                    onClick = { habitType = "BOOLEAN" }
                                )
                                TypeCard(
                                    title = "Compteur",
                                    subtitle = "Quantités et paliers",
                                    icon = Icons.Default.Pin,
                                    isSelected = habitType == "COUNTER",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        habitType = "COUNTER"
                                        if (selectedUnit == "fois" || selectedUnit == "min") selectedUnit = "verres"
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TypeCard(
                                    title = "Durée",
                                    subtitle = "Minuteur chronométré",
                                    icon = Icons.Default.Timer,
                                    isSelected = habitType == "DURATION",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        habitType = "DURATION"
                                        selectedUnit = "min"
                                    }
                                )
                                TypeCard(
                                    title = "À éviter",
                                    subtitle = "Jours d'abstinence",
                                    icon = Icons.Default.DoNotDisturbOn,
                                    isSelected = habitType == "AVOID",
                                    modifier = Modifier.weight(1f),
                                    onClick = { habitType = "AVOID" }
                                )
                            }
                        }

                        // Detail Panel for COUNTER & DURATION
                        AnimatedVisibility(
                            visible = habitType == "COUNTER" || habitType == "DURATION",
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Objectif quotidien :",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                            .padding(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                                .clickable {
                                                    if (dailyTarget > 1) dailyTarget--
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Moins",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Text(
                                            text = "$dailyTarget",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = CyanPrimary
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(CyanPrimary)
                                                .clickable { dailyTarget++ },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Plus",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "UNITÉ DE MESURE :",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("verres", "pages", "pas", "min", "km", "fois", "heures").forEach { u ->
                                        val isSelected = selectedUnit == u
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceContainerLowest)
                                                .clickable { selectedUnit = u }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = u,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 6. FRÉQUENCE
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fréquence",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val freqLabel = when {
                                selectedDays.size == 7 -> "Tous les jours"
                                selectedDays.size == 5 && !selectedDays.contains(6) && !selectedDays.contains(7) -> "Semaine (L-V)"
                                selectedDays.size == 2 && selectedDays.contains(6) && selectedDays.contains(7) -> "Week-end (S-D)"
                                else -> "${selectedDays.size} jours / sem."
                            }
                            Text(
                                text = freqLabel,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = CyanPrimary
                            )
                        }

                        // 3-way toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "EVERY_DAY" to "Tous les jours",
                                "SOME_DAYS" to "Certains jours",
                                "X_PER_WEEK" to "X fois / sem."
                            ).forEach { (mode, label) ->
                                val isSelected = selectedFrequency == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent)
                                        .clickable {
                                            selectedFrequency = mode
                                            if (mode == "EVERY_DAY") selectedDays = (1..7).toSet()
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Day chips L, M, M, J, V, S, D
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val days = listOf(1 to "L", 2 to "M", 3 to "M", 4 to "J", 5 to "V", 6 to "S", 7 to "D")
                            days.forEach { (d, letter) ->
                                val isChecked = selectedDays.contains(d)
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isChecked) CyanPrimary else MaterialTheme.colorScheme.surfaceContainerLow)
                                        .clickable {
                                            selectedDays = if (isChecked) {
                                                if (selectedDays.size > 1) selectedDays - d else selectedDays
                                            } else {
                                                selectedDays + d
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = letter,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isChecked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                        .clickable { selectedDays = setOf(1, 2, 3, 4, 5) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Semaine (L-V)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                        .clickable { selectedDays = setOf(6, 7) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Week-end (S-D)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Text(
                                text = "${selectedDays.size}/7 j cochés",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ==========================================
                // 7. MOMENT DE LA JOURNÉE (RESPONSIVE)
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Moment de la journée",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Organisation",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val moments = listOf(
                            Triple("Matin", "06:00 - 12:00", Icons.Default.WbSunny),
                            Triple("Après-midi", "12:00 - 18:00", Icons.Default.WbSunny),
                            Triple("Soir", "18:00 - 23:00", Icons.Default.Bedtime),
                            Triple("N'importe quand", "Toute la journée", Icons.Default.AllInclusive)
                        )

                        if (isTablet) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                moments.forEach { (m, hours, icon) ->
                                    MomentCard(
                                        title = m,
                                        subtitle = hours,
                                        icon = icon,
                                        isSelected = selectedMoment == m,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedMoment = m }
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                moments.take(2).forEach { (m, hours, icon) ->
                                    MomentCard(
                                        title = m,
                                        subtitle = hours,
                                        icon = icon,
                                        isSelected = selectedMoment == m,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedMoment = m }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                moments.takeLast(2).forEach { (m, hours, icon) ->
                                    MomentCard(
                                        title = m,
                                        subtitle = hours,
                                        icon = icon,
                                        isSelected = selectedMoment == m,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedMoment = m }
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 8. RAPPELS PROGRAMMÉS
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Rappels",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Rappels programmés",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Switch(
                                checked = remindersEnabled,
                                onCheckedChange = { remindersEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CyanPrimary
                                )
                            )
                        }

                        AnimatedVisibility(visible = remindersEnabled) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    reminderTimes.forEach { time ->
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(CyanPrimary.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = CyanPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = time,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Supprimer",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable {
                                                        if (reminderTimes.size > 1) reminderTimes = reminderTimes - time
                                                    }
                                            )
                                        }
                                    }

                                    // Add hour button
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                            .clickable { showAddTimeDialog = true }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Ajouter une heure",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 11.sp,
                                            color = CyanPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Les rappels s'afficheront même si l'application est en veille.",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 9. PÉRIODE D'ACTIVITÉ (CALENDRIER)
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Période d'activité",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Calendrier",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Départ
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Départ",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val dateStr = startDate.format(DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH))
                                    Text(
                                        text = "Aujourd'hui ($dateStr)",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            TextButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = "Modifier",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = CyanPrimary
                                )
                            }
                        }

                        // Fin
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventBusy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Fin",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (hasEndDate) "Dans 30 jours" else "Sans limite de fin",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Switch(
                                checked = hasEndDate,
                                onCheckedChange = { hasEndDate = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CyanPrimary
                                )
                            )
                        }
                    }
                }

                // ==========================================
                // 10. OPTIONS AVANCÉES (ACCORDION)
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isAdvancedOptionsExpanded = !isAdvancedOptionsExpanded }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Options avancées",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = if (isAdvancedOptionsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Déplier options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = isAdvancedOptionsExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Synchronisation automatique
                                val syncText = when {
                                    name.contains("eau", ignoreCase = true) || selectedEmoji == "💧" -> "Lié au module Santé (Hydratation)"
                                    name.contains("allemand", ignoreCase = true) || selectedEmoji == "🇩🇪" -> "Lié au module Allemand"
                                    name.contains("sport", ignoreCase = true) || selectedEmoji == "🏃" -> "Lié au module Santé & Activité"
                                    else -> "Synchronisation automatique active"
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(CyanPrimary.copy(alpha = 0.12f))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(CyanPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SyncAlt,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "Synchronisation automatique",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = syncText,
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 11.sp,
                                                color = CyanPrimary
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Compter un succès partiel
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Compter un succès partiel",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Créditer la flamme si ≥ 50% atteint",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Switch(
                                        checked = isPartialSuccess,
                                        onCheckedChange = { isPartialSuccess = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = CyanPrimary
                                        )
                                    )
                                }

                                // Note personnelle ou motivation
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Note personnelle ou motivation",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    OutlinedTextField(
                                        value = personalNote,
                                        onValueChange = { personalNote = it },
                                        placeholder = {
                                            Text(
                                                text = "Ex: Remplir ma gourde de 1L le matin et après le déjeuner...",
                                                fontSize = 12.sp
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        maxLines = 3,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanPrimary,
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Info footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enregistrement local et synchronisation instantanée au tableau de bord.",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ==========================================
        // FIXED BOTTOM STICKY BAR (RESPONSIVE)
        // ==========================================
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                .padding(horizontal = horizontalPad, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = triggerSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = maxContentWidth)
                    .height(52.dp)
                    .testTag("habit_bottom_save_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                enabled = name.isNotBlank()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (habitToEdit != null) "Enregistrer les modifications" else "Enregistrer l'habitude",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// HELPER COMPOSABLES
// ==========================================
@Composable
private fun TemplateChip(
    emoji: String,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TypeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) CyanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow
            )
            .border(
                if (isSelected) 1.5.dp else 0.dp,
                if (isSelected) CyanPrimary else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp,
                    color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MomentCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) CyanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow
            )
            .border(
                if (isSelected) 1.5.dp else 0.dp,
                if (isSelected) CyanPrimary else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        Column {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSans,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
