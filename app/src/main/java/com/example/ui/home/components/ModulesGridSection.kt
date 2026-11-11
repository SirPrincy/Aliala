package com.example.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BusinessColor
import com.example.ui.theme.BusinessTint
import com.example.ui.theme.FinanceColor
import com.example.ui.theme.FinanceTint
import com.example.ui.theme.GoalsColor
import com.example.ui.theme.GoalsTint
import com.example.ui.theme.HabitsColor
import com.example.ui.theme.HabitsTint
import com.example.ui.theme.HealthColor
import com.example.ui.theme.HealthTint
import com.example.ui.theme.LanguageColor
import com.example.ui.theme.LanguageTint
import com.example.ui.theme.LoansColor
import com.example.ui.theme.LoansTint
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.TasksColor
import com.example.ui.theme.TasksTint
import com.example.util.CurrencyFormatter

@Composable
fun ModulesGridSection(
    tasksCount: Int,
    habitStreak: Int,
    financeTotal: Long,
    loansCount: Int,
    ordersCount: Int,
    sleepHours: Double,
    goalTitle: String,
    goalPercent: Int,
    onModuleClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Modules",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "8 actifs",
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2x4 Grid of Cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Row 1: Tâches & Habitudes
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModuleCard(
                    modifier = Modifier.weight(1f),
                    title = "Tâches",
                    subtitle = if (tasksCount > 0) "$tasksCount à faire aujourd'hui" else "Aucune tâche",
                    icon = Icons.Default.Assignment,
                    accentColor = TasksColor,
                    tintColor = TasksTint,
                    testTag = "module_card_tasks",
                    onClick = { onModuleClick("taches") }
                )
                ModuleCard(
                    modifier = Modifier.weight(1f),
                    title = "Habitudes",
                    subtitle = if (habitStreak > 0) "Série de $habitStreak jours" else "0 jour de série",
                    icon = Icons.Default.LocalFireDepartment,
                    accentColor = HabitsColor,
                    tintColor = HabitsTint,
                    testTag = "module_card_habits",
                    onClick = { onModuleClick("habitudes") }
                )
            }

            // Row 2: Finances & Emprunts
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModuleCard(
                    modifier = Modifier.weight(1f),
                    title = "Finances",
                    subtitle = CurrencyFormatter.formatAriary(financeTotal),
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = FinanceColor,
                    tintColor = FinanceTint,
                    testTag = "module_card_finance",
                    onClick = { onModuleClick("finances") }
                )
                ModuleCard(
                    modifier = Modifier.weight(1f),
                    title = "Emprunts",
                    subtitle = if (loansCount > 0) "$loansCount en cours" else "0 en cours",
                    icon = Icons.Default.Handshake,
                    accentColor = LoansColor,
                    tintColor = LoansTint,
                    testTag = "module_card_loans",
                    onClick = { onModuleClick("emprunts") }
                )
            }

            // Row 3: Business & Santé
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModuleCard(
                    modifier = Modifier.weight(1f),
                    title = "Business",
                    subtitle = if (ordersCount > 0) "$ordersCount commandes en cours" else "0 commande",
                    icon = Icons.Default.Storefront,
                    accentColor = BusinessColor,
                    tintColor = BusinessTint,
                    testTag = "module_card_business",
                    onClick = { onModuleClick("business") }
                )
                ModuleCard(
                    modifier = Modifier.weight(1f),
                    title = "Santé",
                    subtitle = if (sleepHours > 0) "Sommeil ${sleepHours.toInt()}h${((sleepHours % 1) * 60).toInt().toString().padStart(2, '0')}" else "Bien-être du jour",
                    icon = Icons.Default.Favorite,
                    accentColor = HealthColor,
                    tintColor = HealthTint,
                    testTag = "module_card_health",
                    onClick = { onModuleClick("sante") }
                )
            }

            // Row 4: Allemand & Objectifs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModuleCard(
                    modifier = Modifier.weight(1f),
                    title = "Allemand",
                    subtitle = "A2 · 15 mots",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    accentColor = LanguageColor,
                    tintColor = LanguageTint,
                    testTag = "module_card_language",
                    onClick = { onModuleClick("allemand") }
                )
                ModuleCard(
                    modifier = Modifier.weight(1f),
                    title = "Objectifs",
                    subtitle = if (goalTitle.isNotEmpty()) "$goalTitle · $goalPercent%" else "Définir un objectif",
                    icon = Icons.Default.Flag,
                    accentColor = GoalsColor,
                    tintColor = GoalsTint,
                    testTag = "module_card_goals",
                    onClick = { onModuleClick("objectifs") }
                )
            }
        }
    }
}

@Composable
private fun ModuleCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    tintColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = subtitle,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
