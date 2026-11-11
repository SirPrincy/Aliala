package com.example.ui.business.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun TransitairePriceComparisonDialog(
    transitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // State toggles
    var selectedFretMode by remember { mutableStateOf("AERIEN") } // "AERIEN" or "MARITIME"
    var selectedPeriod by remember { mutableStateOf("6m") } // "3m", "6m", "1y", "all"

    // Forwarder visibility toggles in chart
    var showSinoLine by remember { mutableStateOf(true) }
    var showMadaLine by remember { mutableStateOf(true) }
    var showZalyLine by remember { mutableStateOf(true) }

    // Revisions list filter
    var selectedProviderFilter by remember { mutableStateOf("all") } // "all", "SinoExpress", "MadaAir", "Zaly Transit"
    var showFilterDropdown by remember { mutableStateOf(false) }

    // Tooltip message
    var activeTooltipText by remember {
        mutableStateOf(
            if (selectedFretMode == "AERIEN") {
                "Octobre 2026 : MadaAir est 1 500 Ar/kg plus abordable que SinoExpress."
            } else {
                "Octobre 2026 (Maritime) : SinoExpress mène à 1 200 000 Ar/m³."
            }
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
                // 1. TOP HEADER (Back arrow, Title, Bell, Profile)
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
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_back_price_comparison")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = Color(0xFF131B2E),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Comparaison de Prix",
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
                // 2. SCROLLABLE CONTENT CARDS
                // ---------------------------------------------------------
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Sub-header context & Export toolbar
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
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00687A).copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = Color(0xFF00687A),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "MARCHÉ FRET MADAGASCAR",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF3D494C),
                                letterSpacing = 0.8.sp
                            )
                        }

                        Button(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Exportation du comparatif (PDF & CSV) générée avec succès.",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEAEDFF),
                                contentColor = Color(0xFF00687A)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("btn_export_comparison")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.IosShare,
                                    contentDescription = null,
                                    tint = Color(0xFF00687A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Exporter",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Segmented Controls: Mode & Duration
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Mode Toggle: Aérien / Maritime
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF2F3FF))
                                .padding(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedFretMode == "AERIEN") Color.White else Color.Transparent)
                                        .clickable {
                                            selectedFretMode = "AERIEN"
                                            activeTooltipText = "Octobre 2026 : MadaAir est 1 500 Ar/kg plus abordable que SinoExpress."
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FlightTakeoff,
                                            contentDescription = null,
                                            tint = if (selectedFretMode == "AERIEN") Color(0xFF00687A) else Color(0xFF3D494C),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Aérien (Ar/kg)",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = if (selectedFretMode == "AERIEN") FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (selectedFretMode == "AERIEN") Color(0xFF00687A) else Color(0xFF3D494C)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedFretMode == "MARITIME") Color.White else Color.Transparent)
                                        .clickable {
                                            selectedFretMode = "MARITIME"
                                            activeTooltipText = "Octobre 2026 (Maritime) : SinoExpress mène à 1 200 000 Ar/m³."
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsBoat,
                                            contentDescription = null,
                                            tint = if (selectedFretMode == "MARITIME") Color(0xFF712AE2) else Color(0xFF3D494C),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Maritime (Ar/m³)",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = if (selectedFretMode == "MARITIME") FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (selectedFretMode == "MARITIME") Color(0xFF712AE2) else Color(0xFF3D494C)
                                        )
                                    }
                                }
                            }
                        }

                        // Timeline Period Filter Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "3m" to "3 mois",
                                "6m" to "6 mois",
                                "1y" to "1 an",
                                "all" to "Tout"
                            ).forEach { (code, label) ->
                                val isSelected = selectedPeriod == code
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(if (isSelected) Color(0xFF06B6D4) else Color(0xFFF2F3FF))
                                        .clickable { selectedPeriod = code }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else Color(0xFF3D494C)
                                    )
                                }
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // STEP CHART CARD WITH DYNAMIC TOOLTIP & LEGEND
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Tendance comparative (${if (selectedPeriod == "6m") "6 mois" else selectedPeriod})",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Text(
                                        text = "Courbes en paliers selon révisions effectives",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00687A).copy(alpha = 0.10f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShowChart,
                                        contentDescription = null,
                                        tint = Color(0xFF00687A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Active Tooltip Indicator Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFDAE2FD))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Insights,
                                        contentDescription = null,
                                        tint = Color(0xFF00687A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = activeTooltipText,
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = Color(0xFF131B2E),
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            // Interactive SVG Visualizer Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF2F3FF))
                                    .padding(horizontal = 8.dp, vertical = 12.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height

                                    val leftPadding = 32f
                                    val rightPadding = 16f
                                    val bottomPadding = 24f
                                    val chartW = w - leftPadding - rightPadding
                                    val chartH = h - bottomPadding

                                    val yTop = 16f
                                    val yMid = yTop + chartH * 0.45f
                                    val yLow = yTop + chartH * 0.85f

                                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                                    // Dashed guideline grid
                                    drawLine(
                                        color = Color(0xFFBCC9CD).copy(alpha = 0.35f),
                                        start = Offset(leftPadding, yTop),
                                        end = Offset(w - rightPadding, yTop),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = dashedEffect
                                    )
                                    drawLine(
                                        color = Color(0xFFBCC9CD).copy(alpha = 0.35f),
                                        start = Offset(leftPadding, yMid),
                                        end = Offset(w - rightPadding, yMid),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = dashedEffect
                                    )
                                    drawLine(
                                        color = Color(0xFFBCC9CD).copy(alpha = 0.35f),
                                        start = Offset(leftPadding, yLow),
                                        end = Offset(w - rightPadding, yLow),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = dashedEffect
                                    )

                                    // Stepped Points X positions (Mai, Juin, Juil, Août, Sept, Oct)
                                    val x1 = leftPadding
                                    val x2 = leftPadding + chartW * 0.20f
                                    val x3 = leftPadding + chartW * 0.40f
                                    val x4 = leftPadding + chartW * 0.60f
                                    val x5 = leftPadding + chartW * 0.80f
                                    val x6 = w - rightPadding

                                    if (selectedFretMode == "AERIEN") {
                                        // Curve 1: SinoExpress (Orange #F97316) : 34k (low) -> 36k (mid) -> 38k (high)
                                        if (showSinoLine) {
                                            val sinoPath = Path().apply {
                                                moveTo(x1, yLow)
                                                lineTo(x3, yLow)
                                                lineTo(x3, yMid)
                                                lineTo(x5, yMid)
                                                lineTo(x5, yTop + (yMid - yTop) * 0.3f)
                                                lineTo(x6, yTop + (yMid - yTop) * 0.3f)
                                            }
                                            drawPath(
                                                path = sinoPath,
                                                color = Color(0xFFF97316),
                                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Miter)
                                            )
                                            drawCircle(color = Color(0xFFF97316), radius = 5.dp.toPx(), center = Offset(x6, yTop + (yMid - yTop) * 0.3f))
                                        }

                                        // Curve 2: MadaAir (Cyan #06B6D4) : 37k -> 36.5k stable
                                        if (showMadaLine) {
                                            val madaY = yMid + (yLow - yMid) * 0.2f
                                            val madaPath = Path().apply {
                                                moveTo(x1, madaY - 10f)
                                                lineTo(x2, madaY - 10f)
                                                lineTo(x2, madaY)
                                                lineTo(x6, madaY)
                                            }
                                            drawPath(
                                                path = madaPath,
                                                color = Color(0xFF06B6D4),
                                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Miter)
                                            )
                                            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(x6, madaY))
                                            drawCircle(color = Color(0xFF06B6D4), radius = 4.5.dp.toPx(), center = Offset(x6, madaY))
                                        }

                                        // Curve 3: Zaly Transit (Violet #7C3AED) : 39k -> 41k
                                        if (showZalyLine) {
                                            val zalyY1 = yTop + 16f
                                            val zalyY2 = yTop - 4f
                                            val zalyPath = Path().apply {
                                                moveTo(x1, zalyY1)
                                                lineTo(x2, zalyY1)
                                                lineTo(x2, zalyY2)
                                                lineTo(x6, zalyY2)
                                            }
                                            drawPath(
                                                path = zalyPath,
                                                color = Color(0xFF7C3AED),
                                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Miter)
                                            )
                                            drawCircle(color = Color(0xFF7C3AED), radius = 5.dp.toPx(), center = Offset(x6, zalyY2))
                                        }
                                    } else {
                                        // MARITIME MODE
                                        if (showSinoLine) {
                                            val sinoPath = Path().apply {
                                                moveTo(x1, yMid)
                                                lineTo(x4, yMid)
                                                lineTo(x4, yLow)
                                                lineTo(x6, yLow)
                                            }
                                            drawPath(
                                                path = sinoPath,
                                                color = Color(0xFFF97316),
                                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Miter)
                                            )
                                            drawCircle(color = Color(0xFFF97316), radius = 5.dp.toPx(), center = Offset(x6, yLow))
                                        }

                                        if (showMadaLine) {
                                            val madaPath = Path().apply {
                                                moveTo(x1, yMid - 10f)
                                                lineTo(x3, yMid - 10f)
                                                lineTo(x3, yMid + 10f)
                                                lineTo(x6, yMid + 10f)
                                            }
                                            drawPath(
                                                path = madaPath,
                                                color = Color(0xFF06B6D4),
                                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Miter)
                                            )
                                            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(x6, yMid + 10f))
                                            drawCircle(color = Color(0xFF06B6D4), radius = 4.5.dp.toPx(), center = Offset(x6, yMid + 10f))
                                        }

                                        if (showZalyLine) {
                                            val zalyPath = Path().apply {
                                                moveTo(x1, yTop + 10f)
                                                lineTo(x2, yTop + 10f)
                                                lineTo(x2, yTop)
                                                lineTo(x6, yTop)
                                            }
                                            drawPath(
                                                path = zalyPath,
                                                color = Color(0xFF7C3AED),
                                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Miter)
                                            )
                                            drawCircle(color = Color(0xFF7C3AED), radius = 5.dp.toPx(), center = Offset(x6, yTop))
                                        }
                                    }
                                }

                                // Month Labels X-Axis
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .padding(start = 28.dp, end = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf("Mai", "Juin", "Juil.", "Août", "Sept.").forEach { month ->
                                        Text(text = month, fontFamily = Inter, fontSize = 10.sp, color = Color(0xFF3D494C))
                                    }
                                    Text(
                                        text = "Oct.",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF00687A)
                                    )
                                }
                            }

                            // Toggleable Legend Badges (3 tiles)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // SinoExpress Tile
                                val activeTransitaires = transitaires.filter { !it.isArchived }
                                    .sortedBy { if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr }
                                val t1 = activeTransitaires.getOrNull(1)
                                val t0 = activeTransitaires.getOrNull(0)
                                val t2 = activeTransitaires.getOrNull(2)

                                // Tile 2nd cheapest (or SinoExpress fallback)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (showSinoLine) Color(0xFFF2F3FF) else Color(0xFFEAEDFF).copy(alpha = 0.5f))
                                        .clickable {
                                            showSinoLine = !showSinoLine
                                            if (showSinoLine && t1 != null) {
                                                val rate = if (selectedFretMode == "AERIEN") t1.airRateAr else t1.seaRateAr
                                                activeTooltipText = "${t1.name} : ${CurrencyFormatter.formatAriary(rate)}."
                                            }
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFF97316))
                                            )
                                            Text(
                                                text = t1?.name ?: "—",
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 11.sp,
                                                color = Color(0xFF131B2E),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = t1?.let { CurrencyFormatter.formatAriary(if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr) } ?: "—",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }
                                }

                                // Cheapest Tile (Le + Bas)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (showMadaLine) Color(0xFF00687A).copy(alpha = 0.10f) else Color(0xFFEAEDFF).copy(alpha = 0.5f))
                                        .clickable {
                                            showMadaLine = !showMadaLine
                                            if (showMadaLine && t0 != null) {
                                                val rate = if (selectedFretMode == "AERIEN") t0.airRateAr else t0.seaRateAr
                                                activeTooltipText = "${t0.name} : ${CurrencyFormatter.formatAriary(rate)} - Le plus économique."
                                            }
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF06B6D4))
                                            )
                                            Text(
                                                text = t0?.name ?: "—",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF00687A),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = t0?.let { CurrencyFormatter.formatAriary(if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr) } ?: "—",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF00687A)
                                        )
                                        Text(
                                            text = "LE + BAS",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            color = Color(0xFF00687A)
                                        )
                                    }
                                }

                                // 3rd Tile
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (showZalyLine) Color(0xFFF2F3FF) else Color(0xFFEAEDFF).copy(alpha = 0.5f))
                                        .clickable {
                                            showZalyLine = !showZalyLine
                                            if (showZalyLine && t2 != null) {
                                                val rate = if (selectedFretMode == "AERIEN") t2.airRateAr else t2.seaRateAr
                                                activeTooltipText = "${t2.name} : ${CurrencyFormatter.formatAriary(rate)}."
                                            }
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF7C3AED))
                                            )
                                            Text(
                                                text = t2?.name ?: "—",
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 11.sp,
                                                color = Color(0xFF131B2E),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = t2?.let { CurrencyFormatter.formatAriary(if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr) } ?: "—",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // SUMMARY MARKET KPIS (4 TILES GRID 2X2)
                    // -----------------------------------------------------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tile 1: Moins cher actuel
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "MOINS CHER ACTUEL",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF3D494C),
                                        letterSpacing = 0.5.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF06B6D4).copy(alpha = 0.20f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = null,
                                            tint = Color(0xFF00687A),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Column {
                                    Text(
                                        text = if (selectedFretMode == "AERIEN") "MadaAir" else "SinoExpress",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF00687A)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        val bestRate = transitaires.filter { !it.isArchived }
                                            .minOfOrNull { if (selectedFretMode == "AERIEN") it.airRateAr else it.seaRateAr } ?: 0L
                                        Text(
                                            text = if (bestRate > 0L) CurrencyFormatter.formatAriary(bestRate).replace(" Ar", "") else "—",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                        Text(
                                            text = if (selectedFretMode == "AERIEN") "Ar/kg" else "Ar/m³",
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                    }
                                }
                            }
                        }

                        // Tile 2: Plus forte hausse
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PLUS FORTE HAUSSE",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF3D494C),
                                        letterSpacing = 0.5.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFDAD6)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = null,
                                            tint = Color(0xFFBA1A1A),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Column {
                                    Text(
                                        text = "Zaly Transit",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "+5,1%",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFFBA1A1A)
                                        )
                                        Text(
                                            text = "(+2 000 Ar)",
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tile 3: Stabilité
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "STABILITÉ",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF3D494C),
                                        letterSpacing = 0.5.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEADDFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Balance,
                                            contentDescription = null,
                                            tint = Color(0xFF25005A),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Column {
                                    Text(
                                        text = "MadaAir",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "-1,3%",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF00687A)
                                        )
                                        Text(
                                            text = "sur 6 mois",
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                    }
                                }
                            }
                        }

                        // Tile 4: Prix moyen marché
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "MOYENNE MARCHÉ",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF3D494C),
                                        letterSpacing = 0.5.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEAEDFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Equalizer,
                                            contentDescription = null,
                                            tint = Color(0xFF3D494C),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Column {
                                    Text(
                                        text = if (selectedFretMode == "AERIEN") "38 500" else "1 276 000",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = if (selectedFretMode == "AERIEN") "Ar/kg" else "Ar/m³",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                        Text(
                                            text = "(Pondéré)",
                                            fontFamily = Inter,
                                            fontSize = 10.sp,
                                            color = Color(0xFF3D494C)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // CARD 'ÉCONOMIE POTENTIELLE'
                    // -----------------------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF06B6D4)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Économie potentielle",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF131B2E)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(Color(0xFF00687A).copy(alpha = 0.10f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "+24 000 Ar",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF00687A)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = buildAnnotatedString {
                                            append("Sur tes ")
                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF131B2E))) {
                                                append("4 dernières expéditions aériennes")
                                            }
                                            append(" (volume total ")
                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF131B2E))) {
                                                append("16 kg")
                                            }
                                            append("), tu aurais économisé ")
                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF131B2E))) {
                                                append("24 000 Ar")
                                            }
                                            append(" en confiant tous tes colis à ")
                                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = Color(0xFF00687A))) {
                                                append("MadaAir Logistics")
                                            }
                                            append(" au lieu de SinoExpress.")
                                        },
                                        fontFamily = Inter,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "Ouverture du simulateur comparateur multi-colis...", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00687A),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("btn_simuler_envoi")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Simuler un envoi personnalisé",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // TABLE 'RÉVISIONS RÉCENTES'
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Révisions récentes",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                    Text(
                                        text = "Journal des réévaluations déclarées",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                }

                                // Forwarder Filter Dropdown
                                Box {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF2F3FF))
                                            .clickable { showFilterDropdown = true }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = when (selectedProviderFilter) {
                                                    "SinoExpress" -> "SinoExpress"
                                                    "MadaAir" -> "MadaAir"
                                                    "Zaly Transit" -> "Zaly"
                                                    else -> "Tous transitaires"
                                                },
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = Color(0xFF3D494C),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showFilterDropdown,
                                        onDismissRequest = { showFilterDropdown = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Tous transitaires", fontFamily = PlusJakartaSans) },
                                            onClick = {
                                                selectedProviderFilter = "all"
                                                showFilterDropdown = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("SinoExpress", fontFamily = PlusJakartaSans) },
                                            onClick = {
                                                selectedProviderFilter = "SinoExpress"
                                                showFilterDropdown = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("MadaAir Logistics", fontFamily = PlusJakartaSans) },
                                            onClick = {
                                                selectedProviderFilter = "MadaAir"
                                                showFilterDropdown = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Zaly Transit", fontFamily = PlusJakartaSans) },
                                            onClick = {
                                                selectedProviderFilter = "Zaly Transit"
                                                showFilterDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Revisions List Items
                            val allRevisions = listOf(
                                RevisionItemData("SinoExpress", "Maritime", "15 août 2026", "1,24M → 1,20M Ar", "-3,2%", isDown = true, colorHex = "#F97316"),
                                RevisionItemData("SinoExpress", "Aérien", "1er juil. 2026", "36k → 38k Ar", "+5,6%", isDown = false, colorHex = "#F97316"),
                                RevisionItemData("MadaAir", "Aérien", "15 juin 2026", "37k → 36,5k Ar", "-1,4%", isDown = true, colorHex = "#06B6D4"),
                                RevisionItemData("Zaly Transit", "Aérien", "1er juin 2026", "39k → 41k Ar", "+5,1%", isDown = false, colorHex = "#7C3AED")
                            )

                            val filteredRevisions = allRevisions.filter {
                                selectedProviderFilter == "all" || it.provider == selectedProviderFilter
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                filteredRevisions.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF2F3FF))
                                            .padding(10.dp)
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
                                                        .size(32.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            when (item.provider) {
                                                                "SinoExpress" -> Color(0xFFF97316).copy(alpha = 0.15f)
                                                                "MadaAir" -> Color(0xFF06B6D4).copy(alpha = 0.20f)
                                                                else -> Color(0xFF7C3AED).copy(alpha = 0.15f)
                                                            }
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (item.mode == "Aérien") Icons.Default.FlightTakeoff else Icons.Default.DirectionsBoat,
                                                        contentDescription = null,
                                                        tint = when (item.provider) {
                                                            "SinoExpress" -> Color(0xFFF97316)
                                                            "MadaAir" -> Color(0xFF00687A)
                                                            else -> Color(0xFF7C3AED)
                                                        },
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                Column {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = item.provider,
                                                            fontFamily = PlusJakartaSans,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = Color(0xFF131B2E)
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color(0xFFEAEDFF))
                                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = item.mode,
                                                                fontFamily = PlusJakartaSans,
                                                                fontSize = 10.sp,
                                                                color = Color(0xFF3D494C)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = item.date,
                                                        fontFamily = Inter,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF3D494C)
                                                    )
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = item.range,
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF131B2E)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (item.isDown) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                                        contentDescription = null,
                                                        tint = if (item.isDown) Color(0xFF00687A) else Color(0xFFBA1A1A),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = item.delta,
                                                        fontFamily = PlusJakartaSans,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = if (item.isDown) Color(0xFF00687A) else Color(0xFFBA1A1A)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private data class RevisionItemData(
    val provider: String,
    val mode: String,
    val date: String,
    val range: String,
    val delta: String,
    val isDown: Boolean,
    val colorHex: String
)
