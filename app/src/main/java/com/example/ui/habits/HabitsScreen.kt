package com.example.ui.habits

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.AppBottomNavBar
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.VioletSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel,
    onNavigateBack: () -> Unit,
    onTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Native hardware Back handler
    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("habits_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AppBottomNavBar(
                selectedTab = uiState.activeBottomNavTab,
                onTabSelected = { tab ->
                    if (tab == 0) {
                        onNavigateBack()
                    } else {
                        onTabSelected(tab)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddHabitSheet() },
                containerColor = CyanPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp, end = 8.dp)
                    .size(56.dp)
                    .testTag("habits_fab_add")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Créer une habitude",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .verticalScroll(scrollState)
                    .padding(bottom = 24.dp)
            ) {
            // 1. TOP APP BAR / CONTEXT HEADER
            HabitsTopBar(
                onNavigateBack = onNavigateBack,
                onOpenCalendar = {
                    Toast.makeText(context, "Calendrier des habitudes", Toast.LENGTH_SHORT).show()
                },
                onAddHabit = { viewModel.openAddHabitSheet() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. WEEK STRIP (BANDEAU HEBDOMADAIRE)
            WeekStripCard(
                weekDays = uiState.weekDays,
                dateLabel = uiState.selectedDateLabel,
                regularity = uiState.regularityPercent,
                onSelectDate = { viewModel.onSelectDate(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. PROGRESS HERO CARD ("Ta journée d'habitudes")
            ProgressHeroCard(
                dayPercent = uiState.dayPercent,
                completedCount = uiState.completedTodayCount,
                totalCount = uiState.totalHabitsCount,
                streakDays = uiState.streakDays,
                recordStreak = uiState.recordStreak,
                level = uiState.level,
                xpCurrent = uiState.xpCurrent,
                xpMax = uiState.xpMax,
                motivationalMessage = uiState.motivationalMessage
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. FILTER CHIPS
            FilterChipsRow(
                activeFilter = uiState.activeFilter,
                allCount = uiState.allCount,
                todoCount = uiState.todoCount,
                doneCount = uiState.doneCount,
                onSelectFilter = { viewModel.onSelectFilter(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. HABITS LIST (AUJOURD'HUI)
            HabitsListSection(
                habits = uiState.habits,
                onIncrementWater = { viewModel.incrementWater() },
                onDecrementWater = { viewModel.decrementWater() },
                onToggleHabit = { viewModel.toggleHabit(it) },
                onToggleReadingTimer = { viewModel.toggleReadingTimer() },
                onOpenStudy = { viewModel.openStudyDialog() },
                onToggleSwipe = { viewModel.toggleSwipe(it) },
                onDeleteHabit = { viewModel.confirmDeleteHabit(it) },
                onArchiveHabit = {
                    Toast.makeText(context, "Habitude '${it.name}' archivée", Toast.LENGTH_SHORT).show()
                    viewModel.toggleSwipe(it.id)
                },
                onEditHabit = {
                    viewModel.toggleSwipe(it.id)
                    viewModel.openEditHabitSheet(it)
                },
                onAddHabit = { viewModel.openAddHabitSheet() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 6. WEEKLY PERFORMANCE CARD ("Performance hebdo")
            WeeklyPerformanceCard(
                weeklyBars = uiState.weeklyBars,
                weeklyAverage = uiState.weeklyAverage,
                bestDay = uiState.weeklyBestDay,
                delta = uiState.weeklyDelta
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 7. CONSISTENCY HEATMAP CARD ("Régularité globale")
            ConsistencyHeatmapCard(
                monthDisplay = uiState.heatmapMonthDisplay,
                heatmapDays = uiState.heatmapDays,
                perfectDays = uiState.perfectDaysCount,
                successRate = uiState.averageSuccessRate,
                activeStreak = uiState.activeStreakDays,
                onPrevMonth = { viewModel.previousHeatmapMonth() },
                onNextMonth = { viewModel.nextHeatmapMonth() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 8. HABIT RANKING CARD ("Classement sur 30 jours")
            HabitRankingCard(rankings = uiState.rankings)

            Spacer(modifier = Modifier.height(12.dp))

            // 9. BADGES & SUCCÈS CARD ("Badges & Trophées")
            BadgesCard(badges = uiState.badges)

            Spacer(modifier = Modifier.height(12.dp))

            // 10. ROUTINE INSIGHT CARD
            RoutineInsightCard(
                tip = uiState.insightTip,
                currentIndex = uiState.insightTipIndex,
                onNextTip = { viewModel.nextTip() }
            )
        }
    }
}

    // Modal Sheet: Créer ou Modifier une habitude
    if (uiState.isAddHabitSheetOpen || uiState.habitToEdit != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val currentEdit = uiState.habitToEdit
        ModalBottomSheet(
            onDismissRequest = {
                if (currentEdit != null) viewModel.closeEditHabitSheet() else viewModel.closeAddHabitSheet()
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null
        ) {
            CreateHabitSheet(
                habitToEdit = currentEdit,
                onDismiss = {
                    if (currentEdit != null) viewModel.closeEditHabitSheet() else viewModel.closeAddHabitSheet()
                },
                onConfirm = { name, category, iconName, targetDays, habitType, targetCount, unit, colorHex, reminderTimes, note ->
                    if (currentEdit != null) {
                        viewModel.updateHabit(
                            currentEdit.id, name, category, iconName, targetDays,
                            habitType, targetCount, unit, colorHex, reminderTimes, note
                        )
                        Toast.makeText(context, "Habitude modifiée !", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.createHabit(
                            name, category, iconName, targetDays,
                            habitType, targetCount, unit, colorHex, reminderTimes, note
                        )
                        Toast.makeText(context, "Habitude '$name' créée !", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // Dialog: Révision Allemand
    if (uiState.isStudyDialogOpen) {
        GermanStudyDialog(
            onDismiss = { viewModel.closeStudyDialog() },
            onFinish = {
                viewModel.completeGermanStudy()
                Toast.makeText(context, "Session validée (+25 XP) !", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Confirmation suppression habitude
    uiState.habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = {
                Text(
                    text = "Supprimer l'habitude",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Voulez-vous vraiment retirer '${habit.name}' de vos rituels ? Son historique sera effacé.",
                    fontFamily = PlusJakartaSans
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeDeleteHabit() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Annuler")
                }
            }
        )
    }
}

// ==========================================
// 1. TOP APP BAR
// ==========================================
@Composable
private fun HabitsTopBar(
    onNavigateBack: () -> Unit,
    onOpenCalendar: () -> Unit,
    onAddHabit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("habits_btn_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "Habitudes",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 26.sp
                )
                Text(
                    text = "Santé & Routine quotidienne",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenCalendar,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Historique complet",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyanPrimary.copy(alpha = 0.15f))
                    .clickable { onAddHabit() }
                    .testTag("habits_btn_add_top"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Créer une habitude",
                    tint = CyanPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ==========================================
// 2. WEEK STRIP CARD
// ==========================================
@Composable
private fun WeekStripCard(
    weekDays: List<WeekDayItem>,
    dateLabel: String,
    regularity: Int,
    onSelectDate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
            .testTag("habits_week_strip")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // 7 Days Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekDays.forEach { day ->
                    val isDayActive = day.isSelected || day.isToday
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                if (isDayActive) {
                                    Modifier.background(CyanPrimary)
                                } else {
                                    Modifier.clickable { onSelectDate(day.dateStr) }
                                }
                            )
                            .padding(vertical = if (isDayActive) 6.dp else 4.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = day.dayLetter,
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDayActive) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${day.dayNumber}",
                                fontFamily = PlusJakartaSans,
                                fontSize = if (isDayActive) 16.sp else 14.sp,
                                fontWeight = if (isDayActive) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isDayActive) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            // Dot indicator
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDayActive -> Color.White
                                            day.isCompleted -> SuccessGreen
                                            day.isFuture -> MaterialTheme.colorScheme.surfaceVariant
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            // Bottom Subtitle Row: Date and Regularity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = dateLabel,
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(SuccessGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Régularité $regularity%",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. PROGRESS HERO CARD
// ==========================================
@Composable
private fun ProgressHeroCard(
    dayPercent: Int,
    completedCount: Int,
    totalCount: Int,
    streakDays: Int,
    recordStreak: Int,
    level: Int,
    xpCurrent: Int,
    xpMax: Int,
    motivationalMessage: String
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF06B6D4), // Cyan 500
            Color(0xFF0891B2), // Cyan 600
            Color(0xFF7C3AED)  // Violet 600
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .padding(16.dp)
            .testTag("habits_hero_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Main Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Circular Progress Ring
                    Box(
                        modifier = Modifier.size(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 5.dp.toPx()
                            val diameter = size.minDimension - strokeWidth
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                            // Track
                            drawArc(
                                color = Color.White.copy(alpha = 0.25f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(diameter, diameter),
                                style = Stroke(width = strokeWidth)
                            )
                            // Filled arc
                            val sweep = (dayPercent.toFloat() / 100f) * 360f
                            drawArc(
                                color = Color.White,
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(diameter, diameter),
                                style = Stroke(width = strokeWidth + 1.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "$dayPercent%",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Ta journée d'habitudes",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "$completedCount sur $totalCount complétées aujourd'hui",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🔥 $streakDays jours d'affilée",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Record : $recordStreak j",
                                fontFamily = PlusJakartaSans,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Level & XP Pillar
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Niveau $level",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    // XP Bar
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    ) {
                        val fraction = (xpCurrent.toFloat() / xpMax.toFloat()).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(6.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(Color.White)
                        )
                    }
                    Text(
                        text = "$xpCurrent/$xpMax XP",
                        fontFamily = PlusJakartaSans,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Divider & Bottom Motivational Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 $motivationalMessage",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.95f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ==========================================
// 4. FILTER CHIPS ROW
// ==========================================
@Composable
private fun FilterChipsRow(
    activeFilter: HabitFilter,
    allCount: Int,
    todoCount: Int,
    doneCount: Int,
    onSelectFilter: (HabitFilter) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Toutes
        FilterPill(
            label = "Toutes",
            count = allCount,
            isSelected = activeFilter == HabitFilter.ALL,
            onClick = { onSelectFilter(HabitFilter.ALL) }
        )
        // À faire
        FilterPill(
            label = "À faire",
            count = todoCount,
            isSelected = activeFilter == HabitFilter.TODO,
            onClick = { onSelectFilter(HabitFilter.TODO) }
        )
        // Faites
        FilterPill(
            label = "Faites",
            count = doneCount,
            isSelected = activeFilter == HabitFilter.DONE,
            onClick = { onSelectFilter(HabitFilter.DONE) }
        )
        // Matin
        FilterPill(
            label = "Matin",
            icon = Icons.Default.WbSunny,
            isSelected = activeFilter == HabitFilter.MORNING,
            onClick = { onSelectFilter(HabitFilter.MORNING) }
        )
        // Soir
        FilterPill(
            label = "Soir",
            icon = Icons.Default.DarkMode,
            isSelected = activeFilter == HabitFilter.EVENING,
            onClick = { onSelectFilter(HabitFilter.EVENING) }
        )
    }
}

@Composable
private fun FilterPill(
    label: String,
    count: Int? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) SuccessGreen else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) SuccessGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(9999.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = contentColor
            )
            count?.let {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$it",
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. HABITS LIST (AUJOURD'HUI)
// ==========================================
@Composable
private fun HabitsListSection(
    habits: List<HabitUiItem>,
    onIncrementWater: () -> Unit,
    onDecrementWater: () -> Unit,
    onToggleHabit: (Long) -> Unit,
    onToggleReadingTimer: () -> Unit,
    onOpenStudy: () -> Unit,
    onToggleSwipe: (Long) -> Unit,
    onDeleteHabit: (HabitUiItem) -> Unit,
    onArchiveHabit: (HabitUiItem) -> Unit,
    onEditHabit: (HabitUiItem) -> Unit,
    onAddHabit: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mes rituels du jour",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (habits.isNotEmpty()) {
                Text(
                    text = "Glisse pour options",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Text(
                        text = "Aucun rituel configuré",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Créez votre première habitude pour démarrer votre routine ou chargez les données de démo dans les Paramètres.",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Button(
                        onClick = onAddHabit,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Créer une habitude", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            habits.forEach { habit ->
                when (habit.type) {
                    HabitCardType.COUNTER -> {
                        WaterCounterCard(
                            habit = habit,
                            onIncrement = onIncrementWater,
                            onDecrement = onDecrementWater,
                            onEdit = { onEditHabit(habit) }
                        )
                    }
                    HabitCardType.CHECK -> {
                        SportCheckCard(
                            habit = habit,
                            onToggle = { onToggleHabit(habit.id) },
                            onEdit = { onEditHabit(habit) }
                        )
                    }
                    HabitCardType.TIMER -> {
                        ReadingTimerCard(
                            habit = habit,
                            onToggleTimer = onToggleReadingTimer,
                            onEdit = { onEditHabit(habit) }
                        )
                    }
                    HabitCardType.STUDY -> {
                        GermanStudyCard(
                            habit = habit,
                            onOpenStudy = onOpenStudy,
                            onToggleDone = { onToggleHabit(habit.id) },
                            onEdit = { onEditHabit(habit) }
                        )
                    }
                    HabitCardType.SWIPABLE, HabitCardType.STANDARD -> {
                        SwipableHabitCard(
                            habit = habit,
                            onToggleDone = { onToggleHabit(habit.id) },
                            onToggleSwipe = { onToggleSwipe(habit.id) },
                            onEdit = { onEditHabit(habit) },
                            onArchive = { onArchiveHabit(habit) },
                            onDelete = { onDeleteHabit(habit) }
                        )
                    }
                }
            }
        }
    }
}

// Card 1: Hydratation
@Composable
private fun WaterCounterCard(
    habit: HabitUiItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onEdit: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
            .testTag("habit_card_water")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.clickable { onEdit() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFECFEFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💧", fontSize = 22.sp)
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = habit.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StreakPill(streak = habit.streak)
                        }
                        Text(
                            text = habit.subtitle,
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Counter Widget
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onDecrement() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Diminuer",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "${habit.counterValue}/${habit.counterTarget}",
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary)
                            .clickable { onIncrement() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Augmenter",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Progress Bar
            val pct = (habit.counterValue.toFloat() / habit.counterTarget.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(pct)
                        .height(8.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(CyanPrimary)
                )
            }

            // 7 Days Mini Dot History
            MiniHistoryRow(
                label = "Historique 7j",
                dots = habit.history7Days
            )
        }
    }
}

// Card 2: Sport · 30 min
@Composable
private fun SportCheckCard(
    habit: HabitUiItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
            .testTag("habit_card_sport")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.clickable { onEdit() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF0FDF4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏃", fontSize = 22.sp)
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = habit.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (habit.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                            StreakPill(streak = habit.streak)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = habit.subtitle,
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            habit.completedTimeTag?.let { timeTag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(SuccessGreen.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = timeTag,
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }
                    }
                }

                // Checked Toggle Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (habit.isCompletedToday) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Décocher",
                        tint = if (habit.isCompletedToday) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            MiniHistoryRow(
                label = "✓ Validé ce matin (+25 XP)",
                dots = habit.history7Days,
                isLabelSuccess = true
            )
        }
    }
}

// Card 3: Lecture ciblée
@Composable
private fun ReadingTimerCard(
    habit: HabitUiItem,
    onToggleTimer: () -> Unit,
    onEdit: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
            .testTag("habit_card_reading")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.clickable { onEdit() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📖", fontSize = 22.sp)
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = habit.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StreakPill(streak = habit.streak)
                        }
                        Text(
                            text = habit.subtitle,
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Timer Action Button
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(CyanPrimary.copy(alpha = 0.15f))
                        .clickable { onToggleTimer() }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (habit.isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (habit.isTimerRunning) "Pause" else "Reprendre",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    }
                }
            }

            // Progression Séance text row
            val pct = (habit.timerElapsedMinutes.toFloat() / habit.timerTargetMinutes.toFloat()).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progression séance",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${habit.timerElapsedMinutes} / ${habit.timerTargetMinutes} min (${(pct * 100).roundToInt()}%)",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Progress Bar (Amber)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(pct)
                        .height(8.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFFF59E0B))
                )
            }

            MiniHistoryRow(
                label = "Historique 7j",
                dots = habit.history7Days
            )
        }
    }
}

// Card 4: Allemand · Vocabulaire
@Composable
private fun GermanStudyCard(
    habit: HabitUiItem,
    onOpenStudy: () -> Unit,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
            .testTag("habit_card_german")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.clickable { onEdit() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🇩🇪", fontSize = 22.sp)
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = habit.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StreakPill(streak = habit.streak)
                        }
                        Text(
                            text = habit.subtitle,
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Buttons: Réviser + Checkmark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFF4F46E5))
                            .clickable { onOpenStudy() }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Réviser",
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (habit.isCompletedToday) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onToggleDone() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Marquer fait",
                            tint = if (habit.isCompletedToday) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            MiniHistoryRow(
                label = "Série stable : ${habit.streak} jours",
                dots = habit.history7Days
            )
        }
    }
}

// Card 5: Swipable Sommeil Card
@Composable
private fun SwipableHabitCard(
    habit: HabitUiItem,
    onToggleDone: () -> Unit = {},
    onToggleSwipe: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val offsetTransition by animateFloatAsState(
        targetValue = if (habit.isSwipedOpen) -150f else 0f,
        label = "swipe_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("habit_card_swipable_${habit.id}")
    ) {
        // Hidden Action Buttons on the right
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Modifier
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0284C7))
                    .clickable { onEdit() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifier", tint = Color.White, modifier = Modifier.size(18.dp))
                    Text("Modifier", fontSize = 9.sp, color = Color.White, fontFamily = PlusJakartaSans)
                }
            }
            // Archiver
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFD97706))
                    .clickable { onArchive() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Archive, contentDescription = "Archiver", tint = Color.White, modifier = Modifier.size(18.dp))
                    Text("Archiver", fontSize = 9.sp, color = Color.White, fontFamily = PlusJakartaSans)
                }
            }
            // Retirer / Supprimer
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Retirer", tint = Color.White, modifier = Modifier.size(18.dp))
                    Text("Retirer", fontSize = 9.sp, color = Color.White, fontFamily = PlusJakartaSans)
                }
            }
        }

        // Foreground Card Layer
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetTransition.roundToInt(), 0) }
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onEdit() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF3E8FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = habit.iconEmoji, fontSize = 22.sp)
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = habit.name,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (habit.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                StreakPill(streak = habit.streak)
                            }
                            Text(
                                text = habit.subtitle,
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (habit.isCompletedToday) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onToggleDone() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Valider l'habitude",
                                tint = if (habit.isCompletedToday) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleSwipe,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                MiniHistoryRow(
                    label = if (habit.isCompletedToday) "✓ Validé aujourd'hui (+25 XP)" else "Série : ${habit.streak} jours",
                    dots = habit.history7Days,
                    isLabelSuccess = habit.isCompletedToday
                )
            }
        }
    }
}

// Mini Helpers
@Composable
private fun StreakPill(streak: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF59E0B).copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "🔥 $streak",
            fontFamily = PlusJakartaSans,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD97706)
        )
    }
}

@Composable
private fun MiniHistoryRow(
    label: String,
    dots: List<Int>,
    isLabelSuccess: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontSize = 11.sp,
            fontWeight = if (isLabelSuccess) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isLabelSuccess) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            dots.forEach { dot ->
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(
                            when (dot) {
                                1 -> SuccessGreen
                                2 -> CyanPrimary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                )
            }
        }
    }
}

// ==========================================
// 6. WEEKLY PERFORMANCE CARD
// ==========================================
@Composable
private fun WeeklyPerformanceCard(
    weeklyBars: List<WeeklyBar>,
    weeklyAverage: Int,
    bestDay: String,
    delta: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
            .testTag("habits_weekly_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Performance hebdo",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Moyenne $weeklyAverage% · Meilleur jour : ",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = bestDay,
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }

                val isPositive = !delta.startsWith("-")
                val deltaBg = if (isPositive) SuccessGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                val deltaText = if (isPositive) SuccessGreen else MaterialTheme.colorScheme.error

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(deltaBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = delta,
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = deltaText
                    )
                }
            }

            // 7-Column Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyBars.forEach { bar ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (bar.percentage > 0) "${bar.percentage}%" else "-",
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.sp,
                            fontWeight = if (bar.isHighlight || bar.isToday) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                bar.isHighlight -> SuccessGreen
                                bar.isToday -> CyanPrimary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (bar.isFuture) 0.4f else 0.8f)
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val heightFactor = (bar.percentage.toFloat() / 100f).coerceAtLeast(0.06f)
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height((heightFactor * 70).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    when {
                                        bar.isHighlight -> SuccessGreen
                                        bar.isToday -> CyanPrimary
                                        bar.percentage > 0 -> CyanPrimary.copy(alpha = 0.45f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            if (bar.isHighlight) {
                                Text("⭐", fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = bar.letter,
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            fontWeight = if (bar.isHighlight || bar.isToday) FontWeight.Bold else FontWeight.SemiBold,
                            color = when {
                                bar.isHighlight -> SuccessGreen
                                bar.isToday -> CyanPrimary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (bar.isFuture) 0.5f else 1f)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. CONSISTENCY HEATMAP CARD
// ==========================================
@Composable
private fun ConsistencyHeatmapCard(
    monthDisplay: String,
    heatmapDays: List<HeatmapDay>,
    perfectDays: Int,
    successRate: Int,
    activeStreak: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
            .testTag("habits_heatmap_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header with month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Régularité globale",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(onClick = onPrevMonth, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Mois précédent", modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = monthDisplay,
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Mois suivant", modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Weekday Headers (L, M, M, J, V, S, D)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("L", "M", "M", "J", "V", "S", "D").forEach { d ->
                    Text(
                        text = d,
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 31 Days Grid (approx 5 rows x 7 cols)
            val chunked = heatmapDays.chunked(7)
            chunked.forEach { weekRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    weekRow.forEach { day ->
                        if (day.dayNumber == 0) {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                            )
                        } else {
                            val cellBg = when (day.level) {
                                4 -> Color(0xFF059669) // Emerald 600
                                3 -> Color(0xFF34D399) // Emerald 400
                                2 -> Color(0xFFA7F3D0) // Emerald 200
                                1 -> Color(0xFFD1FAE5) // Emerald 100
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            val textColor = when (day.level) {
                                4 -> Color.White
                                3 -> Color(0xFF064E3B)
                                2 -> Color(0xFF064E3B)
                                1 -> Color(0xFF047857)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${day.dayNumber}",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    fontWeight = if (day.isToday || day.level >= 3) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                                if (day.isToday) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .align(Alignment.TopEnd)
                                            .offset((-2).dp, 2.dp)
                                            .clip(CircleShape)
                                            .background(CyanPrimary)
                                    )
                                }
                            }
                        }
                    }
                    // Fill remaining slots in week if less than 7
                    for (i in weekRow.size until 7) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Moins régulier",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    listOf(
                        Color(0xFFD1FAE5),
                        Color(0xFFA7F3D0),
                        Color(0xFF34D399),
                        Color(0xFF059669)
                    ).forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(c)
                        )
                    }
                }
                Text(
                    text = "Parfait (100%)",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 3 Quick Stats Tiles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeatmapStatTile(
                    modifier = Modifier.weight(1f),
                    value = "$perfectDays",
                    label = "Jours parfaits",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                HeatmapStatTile(
                    modifier = Modifier.weight(1f),
                    value = "$successRate%",
                    label = "Réussite moy.",
                    valueColor = SuccessGreen
                )
                HeatmapStatTile(
                    modifier = Modifier.weight(1f),
                    value = "$activeStreak j",
                    label = "Série active",
                    valueColor = Color(0xFFD97706)
                )
            }
        }
    }
}

@Composable
private fun HeatmapStatTile(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = valueColor
            )
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// 8. HABIT RANKING CARD (CLASSEMENT 30 JOURS)
// ==========================================
@Composable
private fun HabitRankingCard(rankings: List<HabitRankingItem>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
            .testTag("habits_ranking_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Classement sur 30 jours",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Taux de fidélité par habitude",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.Leaderboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (rankings.isEmpty()) {
                Text(
                    text = "Aucune habitude enregistrée pour le moment.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rankings.forEach { item ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(item.barColorHex))
                    } catch (_: Exception) {
                        CyanPrimary
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(item.emoji, fontSize = 14.sp)
                                Text(
                                    text = item.name,
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                item.badge?.let { b ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = b,
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${item.percentage}%",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }

                        // Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(item.percentage.toFloat() / 100f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

// ==========================================
// 9. BADGES & SUCCÈS CARD
// ==========================================
@Composable
private fun BadgesCard(badges: List<HabitBadgeItem>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
            .testTag("habits_badges_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Badges & Trophées",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val unlockedCount = badges.count { it.isUnlocked }
                Text(
                    text = "$unlockedCount / ${badges.size} acquis",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = VioletSecondary
                )
            }

            // Horizontal badges list
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                badges.forEach { badge ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(badge.accentColorHex))
                    } catch (_: Exception) {
                        Color(0xFFF59E0B)
                    }

                    Box(
                        modifier = Modifier
                            .width(92.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (badge.isUnlocked) color.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (badge.isUnlocked) color else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(badge.iconEmoji, fontSize = 18.sp)
                            }
                            Text(
                                text = badge.title,
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (badge.progress < 1.0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(badge.progress)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(CyanPrimary)
                                    )
                                }
                            }
                            Text(
                                text = badge.statusText,
                                fontFamily = PlusJakartaSans,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (badge.isUnlocked) color else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 10. ROUTINE INSIGHT CARD
// ==========================================
@Composable
private fun RoutineInsightCard(
    tip: String,
    currentIndex: Int,
    onNextTip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFECFEFF))
            .clickable { onNextTip() }
            .padding(14.dp)
            .testTag("habits_insight_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyanPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💡", fontSize = 18.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Conseil de routine personnalisé",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0E7490)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tip,
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = Color(0xFF155E75),
                        lineHeight = 17.sp
                    )
                }
            }

            // Pagination dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (0..2).forEach { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (i == currentIndex) 7.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == currentIndex) CyanPrimary else CyanPrimary.copy(alpha = 0.35f)
                            )
                    )
                }
            }
        }
    }
}



// ==========================================
// GERMAN VOCABULARY DIALOG
// ==========================================
@Composable
private fun GermanStudyDialog(
    onDismiss: () -> Unit,
    onFinish: () -> Unit
) {
    val words = listOf(
        Pair("das Buch", "le livre"),
        Pair("die Gesundheit", "la santé"),
        Pair("das Wasser", "l'eau"),
        Pair("die Gewohnheit", "l'habitude"),
        Pair("der Erfolg", "le succès")
    )
    var currentWordIdx by remember { mutableIntStateOf(0) }
    var showTranslation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🇩🇪", fontSize = 22.sp)
                Text(
                    text = "Révision Kapitel 5",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Carte ${currentWordIdx + 1} / ${words.size}",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEEF2FF))
                        .clickable { showTranslation = !showTranslation }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = words[currentWordIdx].first,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF312E81)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showTranslation) words[currentWordIdx].second else "Touchez pour révéler",
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            color = if (showTranslation) Color(0xFF4F46E5) else Color(0xFF6B7280),
                            fontWeight = if (showTranslation) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentWordIdx < words.lastIndex) {
                        currentWordIdx++
                        showTranslation = false
                    } else {
                        onFinish()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
            ) {
                Text(if (currentWordIdx < words.lastIndex) "Mot suivant" else "Terminer (+25 XP)", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Quitter")
            }
        }
    )
}
