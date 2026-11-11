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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import com.example.ui.theme.VioletSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreModulesSheet(
    onDismiss: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenGerman: () -> Unit,
    onOpenHealth: () -> Unit,
    onOpenLoans: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenBusiness: () -> Unit,
    onOpenCatalogue: () -> Unit = {},
    catalogueItemCount: Int = 28,
    onOpenSettings: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("more_modules_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Plus d'onglets & modules",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Accède aux autres espaces de ta vie ou à tes sauvegardes",
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            // Section Principale : Autres Espaces de Vie
            Text(
                text = "Espaces de vie",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            MoreModuleItemCard(
                title = "Objectifs",
                subtitle = "Grands projets de vie, épargne & jalons",
                icon = Icons.Default.Flag,
                accentColor = GoalsColor,
                tintColor = GoalsTint,
                testTag = "more_item_goals",
                onClick = {
                    onDismiss()
                    onOpenGoals()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            MoreModuleItemCard(
                title = "Allemand",
                subtitle = "Panorama A2, révisions & 15 mots du jour",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                accentColor = LanguageColor,
                tintColor = LanguageTint,
                testTag = "more_item_german",
                onClick = {
                    onDismiss()
                    onOpenGerman()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            MoreModuleItemCard(
                title = "Santé & Bien-être",
                subtitle = "Sommeil, eau, poids, pas & humeur",
                icon = Icons.Default.Favorite,
                accentColor = HealthColor,
                tintColor = HealthTint,
                testTag = "more_item_health",
                onClick = {
                    onDismiss()
                    onOpenHealth()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            MoreModuleItemCard(
                title = "Emprunts & Dettes",
                subtitle = "Prêts bancaires, dettes dues & remises",
                icon = Icons.Default.Handshake,
                accentColor = LoansColor,
                tintColor = LoansTint,
                testTag = "more_item_loans",
                onClick = {
                    onDismiss()
                    onOpenLoans()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            MoreModuleItemCard(
                title = "Habitudes",
                subtitle = "Routine quotidienne, rituels & heatmap",
                icon = Icons.Default.LocalFireDepartment,
                accentColor = HabitsColor,
                tintColor = HabitsTint,
                testTag = "more_item_habits",
                onClick = {
                    onDismiss()
                    onOpenHabits()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            MoreModuleItemCard(
                title = "Business Chine",
                subtitle = "Commandes en transit, stock & bénéfices",
                icon = Icons.Default.Storefront,
                accentColor = BusinessColor,
                tintColor = BusinessTint,
                testTag = "more_item_business",
                onClick = {
                    onDismiss()
                    onOpenBusiness()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            MoreModuleItemCard(
                title = "Catalogue",
                subtitle = "Articles, familles & plateformes ($catalogueItemCount références)",
                icon = Icons.Default.Sell,
                accentColor = Color(0xFFF97316),
                tintColor = Color(0xFFFFF7ED),
                testTag = "more_item_catalogue",
                onClick = {
                    onDismiss()
                    onOpenCatalogue()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section Sauvegarde & Paramètres
            Text(
                text = "Système & Données",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            MoreModuleItemCard(
                title = "Sauvegarde & Paramètres",
                subtitle = "Exporter/Importer JSON, thème & données de démo",
                icon = Icons.Default.Settings,
                accentColor = VioletSecondary,
                tintColor = VioletSecondary.copy(alpha = 0.14f),
                testTag = "more_item_settings",
                onClick = {
                    onDismiss()
                    onOpenSettings()
                }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun MoreModuleItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    tintColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tintColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
