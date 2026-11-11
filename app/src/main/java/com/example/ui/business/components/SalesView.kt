package com.example.ui.business.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.ui.business.BusinessUiState
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesView(
    uiState: BusinessUiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDomainSelected: (String) -> Unit,
    onStatusFilterSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSaleClick: (BusinessSaleEntity) -> Unit,
    onNewSaleClick: () -> Unit,
    onViewLinkedOrder: (BusinessOrderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSearchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // -------------------------------------------------------------
        // 1. HEADER ACTION ROW & MONTH SWITCHER
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month Selector Capsule
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(9999.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { onPrevMonth() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Mois précédent",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = uiState.selectedMonthLabel,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { onNextMonth() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Mois suivant",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Quick Utilities: Search & Filter
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                        .clickable { isSearchExpanded = !isSearchExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher une vente",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                        .clickable {
                            Toast.makeText(context, "Filtres des ventes actifs", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filtrer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    // Indicator dot
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(7.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8A4CFC))
                    )
                }
            }
        }

        // Search Input (Collapsible or directly displayed when toggled or has text)
        if (isSearchExpanded || uiState.salesSearchQuery.isNotBlank()) {
            OutlinedTextField(
                value = uiState.salesSearchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Rechercher par client, article ou #facture...", fontFamily = Inter, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sales_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            )
        }

        // -------------------------------------------------------------
        // 2. HERO CARD: PERFORMANCE GRADIENT PANEL
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF59E0B),
                            Color(0xFFEA580C),
                            Color(0xFF8A4CFC)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Upper Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "CHIFFRE ENCAISSÉ",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = CurrencyFormatter.formatAriary(uiState.salesTotalCollectedAr).replace(" Ar", ""),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp,
                                color = Color.White,
                                lineHeight = 34.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ar",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }

                    // Delta Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF6EE7B7),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "+${uiState.salesGrowthVsPrevMonthPercent}%",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6EE7B7)
                                )
                            }
                            Text(
                                text = "vs sept.",
                                fontFamily = PlusJakartaSans,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }

                // Total Profit Stat Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bénéfice net total",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = CurrencyFormatter.formatAriary(uiState.salesNetProfitAr),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }

                // Profit Breakdown Multi-Segment Bar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Répartition du profit",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "100%",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }

                    // Progress Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(1.5.dp)
                    ) {
                        val prodW = uiState.salesProductMarginPercent.toFloat()
                        val exchW = uiState.salesExchangeGainPercent.toFloat()
                        val fretW = uiState.salesFreightMarginPercent.toFloat()
                        val sumW = prodW + exchW + fretW

                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (sumW > 0f) {
                                if (prodW > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .weight(prodW)
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFFFDE047))
                                    )
                                }
                                if (exchW > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .weight(exchW)
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFF67E8F9))
                                    )
                                }
                                if (fretW > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .weight(fretW)
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFFD8B4FE))
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color.White.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }

                    // Micro Legend Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFFDE047)))
                            Text("Prod ${uiState.salesProductMarginPercent}%", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF67E8F9)))
                            Text("Change ${uiState.salesExchangeGainPercent}%", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFD8B4FE)))
                            Text("Fret ${uiState.salesFreightMarginPercent}%", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }

                // Subtle Info Footer Note
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Les ventes famille & amis sont isolées du CA fiscal",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 3. THREE MINI SUMMARY CARDS GRID
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1: À encaisser
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFDAD6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PendingActions,
                            contentDescription = null,
                            tint = Color(0xFFBA1A1A),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "À encaisser",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (uiState.salesRemainingToCollectAr >= 1_000_000L) {
                            "${uiState.salesRemainingToCollectAr / 1000}k Ar"
                        } else {
                            CurrencyFormatter.formatAriary(uiState.salesRemainingToCollectAr)
                        },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFBA1A1A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Card 2: Factures Ouvertes
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Factures ouv.",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${uiState.salesOpenInvoicesCount}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Card 3: Commandes en attente
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF8A4CFC).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = Color(0xFF712AE2),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "En attente",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${uiState.salesPendingOrdersCount}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF712AE2)
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 4. DOMAIN SWITCHER PILL (Business Pro vs Famille & Amis)
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                val isBusiness = uiState.salesDomain == "BUSINESS"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (isBusiness) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { onDomainSelected("BUSINESS") }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Business Pro",
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (isBusiness) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (isBusiness) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val isFamily = uiState.salesDomain == "FAMILY_FRIENDS"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (isFamily) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { onDomainSelected("FAMILY_FRIENDS") }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Famille & Amis",
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (isFamily) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (isFamily) Color(0xFF712AE2) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 5. HORIZONTAL SCROLLABLE FILTER CHIPS
        // -------------------------------------------------------------
        val filterChips = listOf(
            "Toutes (${uiState.visibleSales.size})",
            "Impayées",
            "Partielles",
            "Réglées",
            "Sur commande",
            "Sur stock"
        )
        val chipsScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(chipsScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterChips.forEach { chipLabel ->
                val baseFilter = chipLabel.substringBefore(" (")
                val isSelected = uiState.salesStatusFilter == baseFilter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(
                            if (isSelected) CyanPrimary
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            1.dp,
                            if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            RoundedCornerShape(9999.dp)
                        )
                        .clickable { onStatusFilterSelected(baseFilter) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chipLabel,
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 6. INVOICE CARDS LIST HEADER & LIST
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Factures récentes",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Trier par date",
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.visibleSales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aucune facture enregistrée pour ce filtre",
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.visibleSales.forEach { sale ->
                    SaleInvoiceCard(
                        sale = sale,
                        onClick = { onSaleClick(sale) }
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 7. COMMANDES CLIENTS EN ATTENTE (Horizontal Carrousel)
        // -------------------------------------------------------------
        if (uiState.pendingCustomerOrders.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Commandes en attente",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(Color(0xFF8A4CFC).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${uiState.pendingCustomerOrders.size}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF712AE2)
                            )
                        }
                    }

                    Text(
                        text = "Voir tout",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = CyanPrimary,
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "${uiState.pendingCustomerOrders.size} commandes en cours", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Horizontal Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.pendingCustomerOrders.forEach { pendingOrder ->
                        PendingCustomerOrderCard(
                            order = pendingOrder,
                            onViewClick = { onViewLinkedOrder(pendingOrder) }
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// INVOICE CARD COMPONENT
// ---------------------------------------------------------------------------
@Composable
private fun SaleInvoiceCard(
    sale: BusinessSaleEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val remaining = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
    val progress = if (sale.totalPriceAr > 0L) {
        (sale.paidAmountAr.toFloat() / sale.totalPriceAr.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val isOverdue = !sale.isFullyPaid && sale.dueDate < System.currentTimeMillis()
    val overdueDays = if (isOverdue) {
        ((System.currentTimeMillis() - sale.dueDate) / 86400000L).coerceAtLeast(1L)
    } else 0L

    val marginAr = (sale.totalPriceAr - (sale.unitCostAr * sale.quantity)).coerceAtLeast(0L)
    val initials = sale.clientName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
        .ifBlank { "CL" }

    // Avatar color based on initials hash
    val avatarBg = when ((initials.hashCode() % 3 + 3) % 3) {
        0 -> Color(0xFF8A4CFC).copy(alpha = 0.15f)
        1 -> CyanPrimary.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val avatarTextColor = when ((initials.hashCode() % 3 + 3) % 3) {
        0 -> Color(0xFF712AE2)
        1 -> CyanPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Upper Header: Avatar + Client Name + Price & Margin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Initials Avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = avatarTextColor
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = sale.clientName,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (sale.invoiceNumber.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = sale.invoiceNumber,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH).format(Date(sale.saleDate))
                        Text(
                            text = "$dateStr • ${sale.productName}",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Price & Margin
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatAriary(sale.totalPriceAr),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFECFDF5))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (marginAr >= 1000) "Marge +${marginAr / 1000}k" else "Marge +$marginAr",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }

            // Payment Gauge & Status text
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        sale.isFullyPaid -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Intégralement réglé (${CurrencyFormatter.formatAriary(sale.paidAmountAr)})",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF059669)
                                )
                            }
                            Text(
                                text = "100%",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF059669)
                            )
                        }
                        sale.paidAmountAr > 0L -> {
                            Text(
                                text = "Réglé : ${CurrencyFormatter.formatAriary(sale.paidAmountAr)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFFD97706)
                            )
                            Text(
                                text = "Reste : ${CurrencyFormatter.formatAriary(remaining)}",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> {
                            Text(
                                text = "Réglé : 0 Ar",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                text = "Reste : ${CurrencyFormatter.formatAriary(remaining)}",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }

                // Gauge Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    val gaugeColor = when {
                        sale.isFullyPaid -> Color(0xFF10B981)
                        sale.paidAmountAr > 0L -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }
                    if (progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(8.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(gaugeColor)
                        )
                    }
                }
            }

            // Bottom Badges & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Status Badge (Partielle, Réglée, Impayée)
                    val (statusLabel, statusBg, statusColor) = when {
                        sale.isFullyPaid -> Triple("Réglée", Color(0xFFDCFCE7), Color(0xFF166534))
                        sale.paidAmountAr > 0L -> Triple("Partielle", Color(0xFFFEF3C7), Color(0xFF92400E))
                        else -> Triple("Impayée", Color(0xFFFFE4E6), Color(0xFF9F1239))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = statusColor
                        )
                    }

                    // Source Badge (Sur commande / Sur stock)
                    val isOrder = sale.saleSource == "ORDER"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isOrder) Color(0xFF8A4CFC).copy(alpha = 0.12f) else CyanPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isOrder) "Sur commande" else "Sur stock",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = if (isOrder) Color(0xFF712AE2) else CyanPrimary
                        )
                    }

                    // Overdue alert badge
                    if (isOverdue) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(Color(0xFFFFDAD6))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFBA1A1A),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "En retard de ${overdueDays}j",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFFBA1A1A)
                                )
                            }
                        }
                    }
                }

                // Action button: Relancer WhatsApp for unpaid or overdue sales
                if (!sale.isFullyPaid && (isOverdue || sale.paidAmountAr == 0L)) {
                    Button(
                        onClick = {
                            val msg = "Bonjour ${sale.clientName}, rappel amical pour votre commande (${sale.productName}) d'un solde restant de ${CurrencyFormatter.formatAriary(remaining)}."
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Relance", msg))
                            Toast.makeText(context, "Message de relance copié !", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(9999.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Relancer WhatsApp",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// PENDING CUSTOMER ORDER CARD
// ---------------------------------------------------------------------------
@Composable
private fun PendingCustomerOrderCard(
    order: BusinessOrderEntity,
    onViewClick: () -> Unit
) {
    val context = LocalContext.current
    val isReady = order.status in listOf("FLIGHT", "WAREHOUSE", "GUANGZHOU")

    Box(
        modifier = Modifier
            .width(285.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header: Client & Delay badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLIENT • ${order.assignedClient.ifBlank { "Client non assigné" }.uppercase()}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isReady) Color(0xFF8A4CFC).copy(alpha = 0.12f) else Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isReady) "Dans 6 jours" else "Action requise",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = if (isReady) Color(0xFF712AE2) else Color(0xFF92400E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Product Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconVector = when {
                            order.imageUrl == "earbuds" || order.product.contains("Écouteurs", ignoreCase = true) -> Icons.Default.Headphones
                            order.imageUrl == "smartwatch" || order.product.contains("Montre", ignoreCase = true) -> Icons.Default.Watch
                            else -> Icons.Default.ShoppingCartCheckout
                        }
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = order.product,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${order.stockCount.coerceAtLeast(order.quantityBought)} pcs • ${order.variants.ifBlank { "Standard" }}",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price & logistics box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Prix convenu",
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${CurrencyFormatter.formatAriary(order.resalePriceAr)} / u",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isReady) Icons.Default.FlightLand else Icons.Default.ShoppingCartCheckout,
                                contentDescription = null,
                                tint = if (isReady) Color(0xFF712AE2) else Color(0xFFB45309),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isReady) "Achat lié • Vers Mada" else "En attente d'achat",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isReady) Color(0xFF712AE2) else Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            Button(
                onClick = onViewClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReady) Color(0xFF8A4CFC).copy(alpha = 0.15f) else Color(0xFFF59E0B),
                    contentColor = if (isReady) Color(0xFF712AE2) else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Icon(
                    imageVector = if (isReady) Icons.Default.Visibility else Icons.Default.AddLink,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isReady) "Voir l'achat lié" else "Relier à un achat",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
