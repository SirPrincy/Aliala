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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Payments
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
import com.example.ui.theme.PlusJakartaSans

import androidx.compose.material.icons.filled.LocalFireDepartment

@Composable
fun QuickActionsSection(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onAddTask: () -> Unit,
    onAddOrder: () -> Unit,
    onAddLoan: () -> Unit,
    onAddHabit: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionButton(
            label = "Dépense",
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            bgColor = Color(0xFFEF4444).copy(alpha = 0.15f),
            iconColor = Color(0xFFEF4444),
            testTag = "quick_action_depense",
            onClick = onAddExpense
        )
        QuickActionButton(
            label = "Revenu",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            bgColor = Color(0xFF0284C7).copy(alpha = 0.15f),
            iconColor = Color(0xFF0284C7),
            testTag = "quick_action_revenu",
            onClick = onAddIncome
        )
        QuickActionButton(
            label = "Tâche",
            icon = Icons.Default.AssignmentTurnedIn,
            bgColor = Color(0xFF06B6D4).copy(alpha = 0.15f),
            iconColor = Color(0xFF06B6D4),
            testTag = "quick_action_tache",
            onClick = onAddTask
        )
        if (onAddHabit != null) {
            QuickActionButton(
                label = "Habitude",
                icon = Icons.Default.LocalFireDepartment,
                bgColor = Color(0xFF22C55E).copy(alpha = 0.15f),
                iconColor = Color(0xFF22C55E),
                testTag = "quick_action_habitude",
                onClick = onAddHabit
            )
        }
        QuickActionButton(
            label = "Commande",
            icon = Icons.Default.LocalMall,
            bgColor = Color(0xFF7C3AED).copy(alpha = 0.15f),
            iconColor = Color(0xFF7C3AED),
            testTag = "quick_action_commande",
            onClick = onAddOrder
        )
        QuickActionButton(
            label = "Emprunt",
            icon = Icons.Default.Payments,
            bgColor = Color(0xFFD946EF).copy(alpha = 0.15f),
            iconColor = Color(0xFFD946EF),
            testTag = "quick_action_emprunt",
            onClick = onAddLoan
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(testTag)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
