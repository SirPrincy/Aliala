package com.example.ui.home.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HealthColor
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.VioletSecondary
import com.example.ui.theme.WarningAmber
import com.example.util.HealthSummary
import java.util.Locale

@Composable
fun HealthSection(
    summary: HealthSummary,
    onWaterClick: () -> Unit,
    onMoodSelect: (Int) -> Unit,
    onOpenHealth: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .testTag("health_section_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (onOpenHealth != null) Modifier.clickable { onOpenHealth() } else Modifier),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(HealthColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = HealthColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Santé & Bien-être",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Ouvrir ›",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DangerRed
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Metrics Grid: Verres, Sommeil, Poids, Pas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HealthMetricBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalDrink,
                    value = "${summary.waterGlasses}/8",
                    label = "verres",
                    isClickable = true,
                    onClick = onWaterClick
                )
                HealthMetricBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Nightlight,
                    value = if (summary.sleepHours > 0) "${summary.sleepHours.toInt()}h${((summary.sleepHours % 1) * 60).toInt().toString().padStart(2, '0')}" else "—",
                    label = "sommeil"
                )
                HealthMetricBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Scale,
                    value = if (summary.weightKg != null) "${String.format(Locale.FRENCH, "%.1f", summary.weightKg)} kg" else "— kg",
                    label = if (summary.weightKg != null) "actuel" else "aucun"
                )
                HealthMetricBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DirectionsWalk,
                    value = "${summary.steps}",
                    label = "pas"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sleep 7-days chart card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Régularité du sommeil (7 jours)",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (summary.sleepAverage7Days > 0) "Moyenne ${summary.sleepAverage7Days.toInt()}h${((summary.sleepAverage7Days % 1) * 60).toInt().toString().padStart(2, '0')}" else "Moyenne —",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Canvas line graph for sleep regularity
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    val points = summary.sleepPoints.filter { it > 0f }
                    if (points.size >= 2) {
                        val maxH = points.maxOrNull() ?: 10f
                        val minH = (points.minOrNull() ?: 5f).coerceAtLeast(4f)
                        val range = (maxH - minH).coerceAtLeast(1f)

                        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                        val path = Path()

                        points.forEachIndexed { i, p ->
                            val x = i * stepX
                            val normalized = (p - minH) / range
                            val y = size.height - (normalized * size.height * 0.8f) - (size.height * 0.1f)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = VioletSecondary,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Humeur aujourd'hui : 😀 🙂 😐 🙁 😞
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Humeur aujourd'hui :",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val moods = listOf("😀", "🙂", "😐", "🙁", "😞")
                    moods.forEachIndexed { index, emoji ->
                        val isSelected = summary.mood == index
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) WarningAmber.copy(alpha = 0.35f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onMoodSelect(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthMetricBox(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (isClickable) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = CyanPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
