package com.example.ui.business.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans

@Composable
internal fun Step6StepperCard(
    unitsReceived: Int,
    totalQty: Int
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { idx ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(16.dp))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (idx == 4) Color(0xFF712AE2) else Color(0xFF06B6D4).copy(alpha = 0.30f))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF712AE2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("6", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFFEADDFF)
                    ) {
                        Text(
                            text = "Étape 6 sur 6",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color(0xFF712AE2),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "Contrôle Qualité & Répartition",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF131B2E)
                    )
                }

                Text(
                    text = "$unitsReceived / $totalQty reçues",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF00687A)
                )
            }
        }
    }
}

@Composable
internal fun Step6InspectionOverviewPill(
    order: BusinessOrderEntity,
    totalQty: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF2F3FF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEAEDFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF00687A),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "${order.lotNumber.ifBlank { "Lot #${order.id}" }} · ${order.product.ifBlank { "Article #${order.id}" }}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF131B2E)
                    )
                    Text(
                        text = "Fournisseur ${order.supplier.ifBlank { "Chine" }}",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = Color(0xFF3D494C)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$totalQty",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color(0xFF00687A)
                )
                Text(
                    text = "commandées",
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp,
                    color = Color(0xFF6D797D)
                )
            }
        }
    }
}

@Composable
internal fun Step6VariantInspectionCard(
    variantName: String,
    orderedCount: Int,
    badgeOuterColor: Color,
    badgeInnerColor: Color,
    okCount: Int,
    minorDefectCount: Int,
    nonSellableCount: Int,
    lostCount: Int,
    minorDefectLabel: String = "Défaut mineur",
    minorDefectTitleColor: Color = Color(0xFF3D494C),
    minorDefectContainerColor: Color = Color(0xFFF2F3FF),
    lostLabel: String = "Manquant/Perdu",
    lostTitleColor: Color = Color(0xFF6D797D),
    lostContainerColor: Color = Color(0xFFF2F3FF),
    onOkChange: (Int) -> Unit,
    onMinorDefectChange: (Int) -> Unit,
    onNonSellableChange: (Int) -> Unit,
    onLostChange: (Int) -> Unit
) {
    val sum = okCount + minorDefectCount + nonSellableCount + lostCount
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(badgeOuterColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(badgeInnerColor))
                    }
                    Column {
                        Text(variantName, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF131B2E))
                        Text("$orderedCount unités commandées", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (sum == orderedCount) Color(0xFFACEDFF) else Color(0xFFFFDAD6)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (sum == orderedCount) Icons.Default.CheckCircle else Icons.Default.ReportProblem,
                            contentDescription = null,
                            tint = if (sum == orderedCount) Color(0xFF001F26) else Color(0xFFBA1A1A),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            "$sum/$orderedCount réparties",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (sum == orderedCount) Color(0xFF001F26) else Color(0xFFBA1A1A)
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusCounterBox(
                    title = "Conforme (OK)",
                    count = okCount,
                    titleColor = Color(0xFF00687A),
                    onDecrement = { if (okCount > 0) onOkChange(okCount - 1) },
                    onIncrement = { if (sum < orderedCount) onOkChange(okCount + 1) },
                    modifier = Modifier.weight(1f)
                )
                StatusCounterBox(
                    title = minorDefectLabel,
                    count = minorDefectCount,
                    titleColor = minorDefectTitleColor,
                    containerColor = minorDefectContainerColor,
                    onDecrement = { if (minorDefectCount > 0) onMinorDefectChange(minorDefectCount - 1) },
                    onIncrement = { if (sum < orderedCount) onMinorDefectChange(minorDefectCount + 1) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusCounterBox(
                    title = "Non vendable",
                    count = nonSellableCount,
                    titleColor = Color(0xFFBA1A1A),
                    onDecrement = { if (nonSellableCount > 0) onNonSellableChange(nonSellableCount - 1) },
                    onIncrement = { if (sum < orderedCount) onNonSellableChange(nonSellableCount + 1) },
                    modifier = Modifier.weight(1f)
                )
                StatusCounterBox(
                    title = lostLabel,
                    count = lostCount,
                    titleColor = lostTitleColor,
                    containerColor = lostContainerColor,
                    onDecrement = { if (lostCount > 0) onLostChange(lostCount - 1) },
                    onIncrement = { if (sum < orderedCount) onLostChange(lostCount + 1) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun Step6DistributionSummaryRow(
    totalConforme: Int,
    totalDefautVendable: Int,
    totalPerdu: Int,
    totalDistributed: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF2F3FF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF6D797D), modifier = Modifier.size(15.dp))
                Text(
                    text = "Bilan : $totalConforme OK · $totalDefautVendable défectueux vendable · $totalPerdu perdu",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    color = Color(0xFF3D494C)
                )
            }
            Text(
                text = "Total $totalDistributed",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF00687A)
            )
        }
    }
}

@Composable
internal fun StatusCounterBox(
    title: String,
    count: Int,
    titleColor: Color,
    containerColor: Color = Color(0xFFF2F3FF),
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = titleColor
                )
                Text(
                    text = "$count",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF131B2E)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Surface(
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onDecrement() },
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    shadowElevation = 0.5.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, contentDescription = "Diminuer", tint = Color(0xFF131B2E), modifier = Modifier.size(14.dp))
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onIncrement() },
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    shadowElevation = 0.5.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Augmenter", tint = Color(0xFF131B2E), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
