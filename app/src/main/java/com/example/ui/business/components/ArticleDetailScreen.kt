package com.example.ui.business.components

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
fun ArticleDetailScreen(
    article: BusinessOrderEntity,
    onBackClick: () -> Unit,
    onBuyClick: (BusinessOrderEntity) -> Unit = {},
    onSellClick: (BusinessOrderEntity) -> Unit = {},
    onAdjustStockClick: (BusinessOrderEntity) -> Unit = {},
    onEditArticleClick: (BusinessOrderEntity) -> Unit = {},
    onToggleHideArticle: (BusinessOrderEntity) -> Unit = {},
    onDeleteArticle: (BusinessOrderEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showMenu by remember { mutableStateOf(false) }
    var currentCarouselPage by remember { mutableIntStateOf(1) }

    val totalStock = article.stockCount
    val inTransit = if (article.status in listOf("ORDERED", "WAREHOUSE", "GUANGZHOU", "FLIGHT", "CUSTOMS")) article.quantityBought else 0
    val defective = article.defectiveCount
    val totalInventory = totalStock + inTransit + defective

    // Stock percentages
    val safeTotal = totalInventory.coerceAtLeast(1)
    val pctDispo = if (totalInventory > 0) ((totalStock.toFloat() / safeTotal) * 100).toInt().coerceIn(0, 100) else 0
    val pctTransit = if (totalInventory > 0) ((inTransit.toFloat() / safeTotal) * 100).toInt().coerceIn(0, 100) else 0
    val pctDefect = if (totalInventory > 0) (100 - pctDispo - pctTransit).coerceAtLeast(0) else 0

    val unitCostAr = if (article.unitCostAr > 0L) {
        article.unitCostAr
    } else if (article.priceYuan > 0) {
        (article.priceYuan * article.exchangeRate).toLong() + (if (article.quantityBought > 0) article.freightCostAr / article.quantityBought else 0L)
    } else {
        0L
    }

    val unitPriceYuan = article.priceYuan
    val weightGrams = if (article.weightKg != null && article.weightKg > 0) (article.weightKg * 1000).toInt() else 0
    val volumeM3 = if (article.volumeM3 != null && article.volumeM3 > 0) {
        String.format(java.util.Locale.US, "%.4f", article.volumeM3)
    } else if (weightGrams > 0) {
        String.format(java.util.Locale.US, "%.4f", weightGrams * 0.0000067)
    } else {
        "—"
    }
    val airFreightEstAr = (weightGrams * 38).toLong() // 38 000 Ar/kg aligné avec le reste de l'application

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFAF8FF),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFAF8FF).copy(alpha = 0.95f),
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
                            onClick = onBackClick,
                            modifier = Modifier.testTag("btn_back_article_detail")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = Color(0xFF131B2E),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Détail Business",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00687A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. CAROUSEL PHOTO & BADGES
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEAEDFF))
                    .testTag("article_detail_carousel")
            ) {
                // Background artistic representation
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (article.family.contains("MODE", ignoreCase = true)) "🎒" else "🎧",
                            fontSize = 72.sp
                        )
                        Text(
                            text = article.product,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF131B2E).copy(alpha = 0.6f)
                        )
                    }
                }

                // Top Left Badges: [Actif] [TWS-P5-PRO]
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFF00687A).copy(alpha = 0.90f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFACEDFF))
                            )
                            Text(
                                text = if (article.isHidden) "Masqué" else "Actif",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color.White.copy(alpha = 0.92f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = article.referenceCode.ifBlank { "SKU-${article.id}" },
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF131B2E)
                        )
                    }
                }

                // Top Right: Context Action Button (3 dots) with Dropdown
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f))
                            .clickable { showMenu = true }
                            .testTag("btn_article_options"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF131B2E),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Modifier", fontFamily = PlusJakartaSans) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF6D797D)) },
                            onClick = {
                                showMenu = false
                                onEditArticleClick(article)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Dupliquer", fontFamily = PlusJakartaSans) },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF6D797D)) },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, "Fiche dupliquée", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (article.isHidden) "Réafficher" else "Masquer", fontFamily = PlusJakartaSans) },
                            leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = Color(0xFF6D797D)) },
                            onClick = {
                                showMenu = false
                                onToggleHideArticle(article)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Supprimer", fontFamily = PlusJakartaSans, color = Color(0xFFBA1A1A)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFBA1A1A)) },
                            onClick = {
                                showMenu = false
                                onDeleteArticle(article)
                                onBackClick()
                            }
                        )
                    }
                }

                // Bottom Pagination Dots
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..4).forEach { page ->
                        val isCurrent = page == currentCarouselPage
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isCurrent) 20.dp else 6.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(if (isCurrent) CyanPrimary else Color.White.copy(alpha = 0.7f))
                                .clickable { currentCarouselPage = page }
                        )
                    }
                }

                // Page indicator badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF283044).copy(alpha = 0.75f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$currentCarouselPage / 4",
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }

            // ==========================================
            // 2. TITLE & CATEGORY INFO
            // ==========================================
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = Color(0xFFE2E7FF)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = article.family.ifBlank { "Électronique" },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF00687A)
                            )
                        }
                    }

                    Text(
                        text = "• SKU: ${article.referenceCode.ifBlank { "RÉF-${article.id}" }}",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = Color(0xFF6D797D)
                    )
                }

                Text(
                    text = article.product.ifBlank { "Article #${article.id}" },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF131B2E)
                )

                Text(
                    text = if (article.variants.isNotBlank()) article.variants else "Bluetooth 5.3, Réduction de bruit active (ANC), étui de charge rapide USB-C.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = Color(0xFF3D494C)
                )
            }

            // ==========================================
            // 3. QUICK ACTION BAR: Acheter / Vendre / Ajuster
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Acheter (Cyan Primary)
                Button(
                    onClick = { onBuyClick(article) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_article_buy"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00687A)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Acheter",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }

                // Vendre (Secondary Violet Soft)
                Button(
                    onClick = { onSellClick(article) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_article_sell"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = Color(0xFF25005A),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Vendre",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF25005A)
                        )
                    }
                }

                // Ajuster (Neutral)
                Button(
                    onClick = { onAdjustStockClick(article) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_article_adjust"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E7FF)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color(0xFF131B2E),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Ajuster",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF131B2E)
                        )
                    }
                }
            }

            // ==========================================
            // 4. CARD: ÉTAT DES STOCKS (44 unités)
            // ==========================================
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyanPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = Color(0xFF00687A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "État des Stocks",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF131B2E)
                                )
                                Text(
                                    text = "Total inventorié: $totalInventory unités",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6D797D)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0xFFE2E7FF)
                        ) {
                            Text(
                                text = "Temps réel",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = Color(0xFF3D494C),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // 3 Metric Boxes: Disponible / En transit / Défectueux
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Disponible
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF2F3FF))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Disponible", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                                Text(text = "$totalStock", fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF00687A))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(12.dp))
                                    Text(text = "En rayon", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF00687A))
                                }
                            }
                        }

                        // En transit
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF2F3FF))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "En transit", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                                Text(text = "$inTransit", fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF712AE2))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = Color(0xFF712AE2), modifier = Modifier.size(12.dp))
                                    Text(text = "SinoExpress", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF712AE2))
                                }
                            }
                        }

                        // Défectueux
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF2F3FF))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Défectueux", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                                Text(text = "$defective", fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFFBA1A1A))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(12.dp))
                                    Text(text = "SAV", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFBA1A1A))
                                }
                            }
                        }
                    }

                    // Stacked Distribution Bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(Color(0xFFEAEDFF))
                        ) {
                            Box(modifier = Modifier.weight((pctDispo.toFloat()).coerceAtLeast(1f)).fillMaxSize().background(Color(0xFF00687A)))
                            Box(modifier = Modifier.weight((pctTransit.toFloat()).coerceAtLeast(1f)).fillMaxSize().background(Color(0xFF712AE2)))
                            Box(modifier = Modifier.weight((pctDefect.toFloat()).coerceAtLeast(1f)).fillMaxSize().background(Color(0xFFBA1A1A)))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "$pctDispo% Dispo", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF6D797D))
                            Text(text = "$pctTransit% Transit", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF6D797D))
                            Text(text = "$pctDefect% Défect.", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF6D797D))
                        }
                    }
                }
            }

            // ==========================================
            // 5. CARD: DÉCLINAISONS & COULEURS
            // ==========================================
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEADDFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Color(0xFF712AE2),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Déclinaisons & Couleurs",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF131B2E)
                                )
                                val parsedVariantsList = remember(article.variants) {
                                    article.variants.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotBlank() }
                                }
                                Text(
                                    text = if (parsedVariantsList.isNotEmpty()) "${parsedVariantsList.size} variante(s) configurée(s)" else "Aucune variante enregistrée",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6D797D)
                                )
                            }
                        }
                    }

                    val parsedVariantsList = remember(article.variants) {
                        article.variants.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                    }

                    if (parsedVariantsList.isNotEmpty()) {
                        parsedVariantsList.forEachIndexed { idx, varText ->
                            val dotColor = when (idx % 4) {
                                0 -> Color(0xFF131B2E)
                                1 -> Color(0xFF00687A)
                                2 -> Color(0xFF712AE2)
                                else -> Color(0xFFEA580C)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF2F3FF))
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
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                    Text(
                                        text = varText,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF2F3FF))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Aucune déclinaison spécifique enregistrée",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF6D797D)
                            )
                        }
                    }

                    // Bouton Ajouter une couleur
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE2E7FF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Ajout d'une couleur", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = Color(0xFF00687A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ajouter une couleur",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF00687A)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 6. CARD: SPÉCIFICATIONS & FRET
            // ==========================================
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFD8E2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF005AC2),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Spécifications & Fret",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = "Paramètres volumétriques d'import",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF6D797D)
                            )
                        }
                    }

                    // Grid 2 colonnes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF2F3FF))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Poids unitaire", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                                Text(text = "$weightGrams g", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF131B2E))
                                Text(text = "Boîte + accessoires", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF2F3FF))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Volume unitaire", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                                Text(text = "$volumeM3 m³", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF131B2E))
                                Text(text = "800 cm³", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF3D494C))
                            }
                        }
                    }

                    // Dimensions boîte
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dimensions boîte",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color(0xFF6D797D)
                        )
                        Text(
                            text = "8,5 × 6,0 × 3,2 cm",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF131B2E)
                        )
                    }

                    // Impact Fret Estimé
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF78A5FF).copy(alpha = 0.20f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flight,
                                contentDescription = null,
                                tint = Color(0xFF005AC2),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Impact fret estimé",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF005AC2)
                                )
                                Text(
                                    text = "≈ ${CurrencyFormatter.formatAriary(airFreightEstAr)} / unité en fret aérien rapide (base tarifaire 48 000 Ar/kg).",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF131B2E)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 7. CARD: HISTORIQUE COÛTS & ACHATS
            // ==========================================
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyanPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = Color(0xFF00687A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Historique Coûts & Achats",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF131B2E)
                                )
                                Text(
                                    text = "Coût unitaire FIFO réel",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF00687A)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0xFFE2E7FF)
                        ) {
                            Text(
                                text = CurrencyFormatter.formatAriary(unitCostAr),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF00687A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Tendance visuelle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Coût de revient final", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                                Text(text = "Unitaire réel", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF00687A))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${article.quantityBought} pcs achetées", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                                Text(text = "${CurrencyFormatter.formatAriary(unitCostAr)} / pc", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF131B2E))
                            }
                        }
                    }

                    Text(
                        text = "DERNIERS APPROVISIONNEMENTS",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF6D797D),
                        letterSpacing = 0.5.sp
                    )

                    // Purchase Row 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(16.dp))
                            val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE).format(java.util.Date(article.orderDate))
                            Column {
                                Text(text = dateStr, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF131B2E))
                                Text(text = "${article.quantityBought} pcs via ${article.platform.ifBlank { "1688" }}", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF6D797D))
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "${CurrencyFormatter.formatAriary(unitCostAr)} / pc", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF131B2E))
                            Text(text = "¥ ${String.format(java.util.Locale.US, "%.2f", unitPriceYuan)} / u", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF6D797D))
                        }
                    }
                }
            }

            // ==========================================
            // 8. CARD: PERFORMANCES VENTES
            // ==========================================
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEADDFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueryStats,
                                contentDescription = null,
                                tint = Color(0xFF712AE2),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Performances Ventes",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF131B2E)
                            )
                            Text(
                                text = "Bilan commercial global",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = Color(0xFF6D797D)
                            )
                        }
                    }

                    // Metrics Triplet
                    val realProfitAr = (article.resalePriceAr - unitCostAr).coerceAtLeast(0L) * article.unitsSold
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF2F3FF))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Volume vendu", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                                Text(text = "${article.unitsSold} pcs", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF131B2E))
                                Text(text = "Écoulé", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF00687A))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF2F3FF))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Prix moyen", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                                Text(text = if (article.resalePriceAr > 0) CurrencyFormatter.formatAriary(article.resalePriceAr) else "0 Ar", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF131B2E))
                                Text(text = "Prix public", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF6D797D))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF2F3FF))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Marge brute", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                                Text(text = "${article.marginPercent} %", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF00687A))
                                Text(text = if (realProfitAr > 0) "+${CurrencyFormatter.formatAriary(realProfitAr)}" else "0 Ar", fontFamily = PlusJakartaSans, fontSize = 10.sp, color = Color(0xFF00687A))
                            }
                        }
                    }

                    // Rentabilité catégorie pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(18.dp))
                            Text(text = "Statut commercial", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF131B2E))
                        }
                        val statusLabel = when {
                            article.unitsSold >= 20 -> "Forte rotation"
                            article.unitsSold > 0 -> "Ventes actives"
                            article.stockCount > 0 -> "En stock"
                            else -> "Épuisé"
                        }
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0xFFACEDFF)
                        ) {
                            Text(
                                text = statusLabel,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF00424F),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 9. CARD: FOURNISSEURS CHINOIS
            // ==========================================
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE2E7FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = Color(0xFF131B2E),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Fournisseurs Chinois",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF131B2E)
                                )
                                Text(
                                    text = "Comparatif des sourcing directs",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6D797D)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Ouverture lien fournisseur", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(text = "Liens", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF00687A))
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF00687A), modifier = Modifier.size(14.dp))
                        }
                    }

                    // Supplier 1: 1688.com
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF00687A)))
                                    Text(text = "1688.com (Direct usine)", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF131B2E))
                                }
                                Text(text = "¥ 28.00", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF00687A))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Port Chine: Payant (¥ 15 / carton)", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF6D797D))
                                Text(text = "Fournisseur recommandé", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color(0xFF00687A))
                            }
                        }
                    }

                    // Supplier 2: Taobao
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F3FF))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF6D797D)))
                                    Text(text = "Taobao", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF131B2E))
                                }
                                Text(text = "¥ 32.50", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF131B2E))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Port Chine: Gratuit inclus", fontFamily = Inter, fontSize = 11.sp, color = Color(0xFF6D797D))
                                Text(text = "Dépannage petites séries", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0xFF6D797D))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
