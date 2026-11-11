package com.example.ui.business.components

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.business.CatalogueTransitaireSummaryUi
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun TransitairesSubTabContent(
    transitaires: List<CatalogueTransitaireSummaryUi>,
    onCreateTransitaire: (name: String, hub: String, airRate: Long, seaRate: Long) -> Unit,
    onToggleArchived: (Long) -> Unit = {},
    onDeleteTransitaire: (Long) -> Unit = {},
    onUpdateRates: (id: Long, airRate: Long, seaRate: Long) -> Unit = { _, _, _ -> },
    onRecordPayment: (id: Long, amountAr: Long) -> Unit = { _, _ -> },
    onAddClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTransitaireForDetail by remember { mutableStateOf<CatalogueTransitaireSummaryUi?>(null) }
    var showComparisonDialog by remember { mutableStateOf(false) }

    val filteredList = transitaires.filter {
        searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.routeHub.contains(searchQuery, ignoreCase = true)
    }

    val activeCount = transitaires.count { !it.isArchived }
    val archivedCount = transitaires.count { it.isArchived }
    val totalPendingFreightAr = transitaires.filter { !it.isArchived }.sumOf { it.balanceDueAr }
    val activeShipmentsTotal = transitaires.filter { !it.isArchived }.sumOf { it.activeShipmentsCount }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // -----------------------------------------------------------------
        // 1. RECHERCHE & BOUTON TENDANCE
        // -----------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher",
                        tint = Color(0xFF6D797D),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Rechercher un transitaire...",
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                color = Color(0xFFBCC9CD)
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                color = Color(0xFF131B2E)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_transitaire_input")
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Effacer",
                            tint = Color(0xFF6D797D),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { showComparisonDialog = true }
                    .testTag("btn_transitaire_trends"),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = "Évolution des prix",
                        tint = Color(0xFF00687A),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // -----------------------------------------------------------------
        // 2. STRIP RÉSUMÉ : 3 KPI CARDS DYNAMIQUES
        // -----------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // KPI 1: Actifs
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIFS",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF3D494C),
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF06B6D4))
                        )
                    }

                    Column {
                        Text(
                            text = "$activeCount",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color(0xFF131B2E),
                            lineHeight = 24.sp
                        )
                        Text(
                            text = "Partenaires",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF00687A)
                        )
                    }
                }
            }

            // KPI 2: À payer
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "À PAYER",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFFBA1A1A),
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = null,
                            tint = Color(0xFFBA1A1A),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (totalPendingFreightAr > 0) CurrencyFormatter.formatAriary(totalPendingFreightAr) else "0 Ar",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFFBA1A1A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFFDAD6).copy(alpha = 0.6f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "1 facture",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color(0xFFBA1A1A)
                            )
                        }
                    }
                }
            }

            // KPI 3: Expéditions en cours
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EN COURS",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF3D494C),
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color(0xFF712AE2),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "$activeShipmentsTotal",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color(0xFF131B2E),
                            lineHeight = 24.sp
                        )
                        Text(
                            text = "Lots actifs",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF712AE2)
                        )
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 3. TARIFS DU MOMENT : LIVE BENCHMARK GRID
        // -----------------------------------------------------------------
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showComparisonDialog = true },
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
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
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFACEDFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriceChange,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Tarifs du moment",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF131B2E)
                        )
                    }
                    Text(
                        text = "Mis à jour hier",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = Color(0xFF6D797D)
                    )
                }

                // 2-Column Rates Comparison
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Col 1: Aérien (Ar/kg)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    text = "Aérien (Ar/kg)",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val sortedTransitaires = transitaires
                                    .filter { !it.isArchived && it.airRateAr > 0L }
                                    .sortedBy { it.airRateAr }
                                    .take(3)
                                if (sortedTransitaires.isEmpty()) {
                                    Text("Aucun tarif enregistré", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                                } else {
                                    sortedTransitaires.forEachIndexed { idx, t ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = t.name,
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = Color(0xFF3D494C),
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Text(
                                                    text = CurrencyFormatter.formatAriary(t.airRateAr).replace(" Ar", ""),
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 11.sp,
                                                    color = if (idx == 0) Color(0xFF00687A) else Color(0xFF131B2E)
                                                )
                                                if (idx == 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(Color(0xFF06B6D4))
                                                            .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                                    ) {
                                                        Text("Top", fontFamily = PlusJakartaSans, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Col 2: Maritime (Ar/m³)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBoat,
                                    contentDescription = null,
                                    tint = Color(0xFF005AC2),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Maritime (Ar/m³)",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val sortedSeaTransitaires = transitaires
                                    .filter { !it.isArchived && it.seaRateAr > 0L }
                                    .sortedBy { it.seaRateAr }
                                    .take(3)
                                if (sortedSeaTransitaires.isEmpty()) {
                                    Text("Aucun tarif enregistré", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                                } else {
                                    sortedSeaTransitaires.forEachIndexed { idx, t ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = t.name,
                                                fontFamily = Inter,
                                                fontSize = 11.sp,
                                                color = Color(0xFF3D494C),
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Text(
                                                    text = CurrencyFormatter.formatCompactAriary(t.seaRateAr).replace(" Ar", ""),
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 11.sp,
                                                    color = if (idx == 0) Color(0xFF00687A) else Color(0xFF131B2E)
                                                )
                                                if (idx == 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(Color(0xFF06B6D4))
                                                            .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                                    ) {
                                                        Text("Top", fontFamily = PlusJakartaSans, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 4. LIST HEADER
        // -----------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tous les transitaires",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF131B2E)
            )
            Text(
                text = "$activeCount actifs · $archivedCount archivé",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFF6D797D)
            )
        }

        // -----------------------------------------------------------------
        // 5. CARTES DES TRANSITAIRES
        // -----------------------------------------------------------------
        filteredList.forEach { trans ->
            TransitaireCard(
                trans = trans,
                onClick = {
                    selectedTransitaireForDetail = trans
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
            shape = RoundedCornerShape(9999.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_nouveau_transitaire_list")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Nouveau transitaire",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }

    // Modal Fiche Détaillée Transitaire
    selectedTransitaireForDetail?.let { trans ->
        val currentTrans = transitaires.find { it.id == trans.id } ?: trans
        TransitaireDetailDialog(
            transitaire = currentTrans,
            onDismiss = { selectedTransitaireForDetail = null },
            onToggleArchived = { id ->
                onToggleArchived(id)
            },
            onDelete = { id ->
                onDeleteTransitaire(id)
                selectedTransitaireForDetail = null
            },
            onUpdateRates = onUpdateRates,
            onRecordPayment = onRecordPayment
        )
    }

    if (showComparisonDialog) {
        TransitairePriceComparisonDialog(
            transitaires = transitaires,
            onDismiss = { showComparisonDialog = false }
        )
    }
}

@Composable
private fun TransitaireCard(
    trans: CatalogueTransitaireSummaryUi,
    onClick: () -> Unit
) {
    if (trans.isArchived) {
        // Archived state card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.65f)
                .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF2F3FF).copy(alpha = 0.7f),
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFBCC9CD).copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = trans.initials,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF6D797D)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = trans.name,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = Color(0xFF131B2E)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFFBCC9CD).copy(alpha = 0.5f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Archivé",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        color = Color(0xFF3D494C)
                                    )
                                }
                            }
                            Text(
                                text = trans.routeHub,
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF6D797D)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Verrouillé",
                        tint = Color(0xFF6D797D),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF6D797D),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Non sélectionnable pour les nouveaux achats",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = Color(0xFF6D797D)
                    )
                }
            }
        }
    } else {
        // Active transitaire card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .testTag("transitaire_card_${trans.name}"),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Row (Avatar, Name, Status, Chevron)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when (trans.avatarType) {
                                        "GRADIENT" -> Brush.linearGradient(
                                            listOf(Color(0xFF712AE2), Color(0xFF06B6D4))
                                        )
                                        "CYAN" -> Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF06B6D4)))
                                        "VIOLET" -> Brush.linearGradient(listOf(Color(0xFF8A4CFC), Color(0xFF8A4CFC)))
                                        else -> Brush.linearGradient(listOf(Color(0xFF00687A), Color(0xFF00687A)))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = trans.initials,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (trans.avatarType == "CYAN") Color(0xFF00424F) else Color.White
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = trans.name,
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
                                        text = "Actif",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF00687A)
                                    )
                                }
                            }
                            Text(
                                text = trans.routeHub,
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF3D494C)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F3FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Détails",
                            tint = Color(0xFF3D494C),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Dynamic Tariff Sparkline Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chip Aérien
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flight,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${CurrencyFormatter.formatAriary(trans.airRateAr)}/kg",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFF131B2E)
                            )
                            if (trans.isAirCheapest) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF06B6D4).copy(alpha = 0.3f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("Moins cher", fontFamily = PlusJakartaSans, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00424F))
                                }
                            } else if (trans.airRateTrend.isNotBlank()) {
                                MiniSparkline(isUp = true, color = Color(0xFFBA1A1A))
                            }
                        }
                    }

                    // Chip Maritime
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBoat,
                                contentDescription = null,
                                tint = Color(0xFF005AC2),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${CurrencyFormatter.formatAriary(trans.seaRateAr)}/m³",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFF131B2E)
                            )
                            if (trans.seaRateTrend == "-3.2%") {
                                MiniSparkline(isUp = false, color = Color(0xFF06B6D4))
                            }
                        }
                    }
                }

                // Outstanding & Status bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (trans.balanceDueAr > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Solde dû :", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF3D494C))
                            Text(
                                text = CurrencyFormatter.formatAriary(trans.balanceDueAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFBA1A1A)
                            )
                            if (trans.balanceDueDate.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFFDAD6))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = trans.balanceDueDate,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF93000A)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Solde :", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF3D494C))
                            Text(
                                text = "À jour ✓",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF00687A)
                            )
                        }
                    }

                    Text(
                        text = if (trans.activeShipmentsCount > 0) "${trans.activeShipmentsCount} expéditions en cours"
                        else if (trans.lastShipmentDate.isNotBlank()) "0 en cours · ${trans.lastShipmentDate}"
                        else "0 expédition en cours",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = Color(0xFF3D494C)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniSparkline(
    isUp: Boolean,
    color: Color,
    modifier: Modifier = Modifier.size(width = 24.dp, height = 10.dp)
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            if (isUp) {
                // "M1 10L9 8L17 9L27 2" normalized
                moveTo(size.width * 0.05f, size.height * 0.9f)
                lineTo(size.width * 0.35f, size.height * 0.7f)
                lineTo(size.width * 0.65f, size.height * 0.8f)
                lineTo(size.width * 0.95f, size.height * 0.15f)
            } else {
                // "M1 2L9 4L17 3L27 10" normalized
                moveTo(size.width * 0.05f, size.height * 0.2f)
                lineTo(size.width * 0.35f, size.height * 0.4f)
                lineTo(size.width * 0.65f, size.height * 0.3f)
                lineTo(size.width * 0.95f, size.height * 0.9f)
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 1.8f.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
