package com.example.ui.business.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.VioletSecondary
import com.example.util.DateUtils

@Composable
internal fun HumanSectionCard(
    modifier: Modifier = Modifier,
    colors: TransitaireColors = rememberTransitaireColors(),
    containerColor: Color = colors.cardBg,
    cornerRadius: Dp = 24.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clip(RoundedCornerShape(cornerRadius)).clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = if (colors.isDark) 0.dp else 1.dp,
        border = BorderStroke(1.dp, colors.border.copy(alpha = if (colors.isDark) 0.65f else 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
internal fun HumanHeroCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF06B6D4),
                        Color(0xFF0E7490),
                        Color(0xFF6D28D9)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.16f),
                    radius = size.width * 0.55f
                ),
                center = Offset(size.width * 0.88f, size.height * 0.16f),
                radius = size.width * 0.55f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFA78BFA).copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(size.width * 0.12f, size.height * 0.92f),
                    radius = size.width * 0.45f
                ),
                center = Offset(size.width * 0.12f, size.height * 0.92f),
                radius = size.width * 0.45f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
internal fun HumanIconBadge(
    icon: ImageVector,
    tint: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp,
    cornerRadius: Dp = 14.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
internal fun HumanAvatarCircle(
    initials: String,
    tint: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.take(2).uppercase(),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.34f).sp,
            color = tint
        )
    }
}

@Composable
internal fun HumanProgressBar(
    progress: Float,
    trackColor: Color,
    fillColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 7.dp
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "humanProgress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        if (animated > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(fillColor)
            )
        }
    }
}

@Composable
internal fun HumanSegmentedSwitch(
    options: List<Pair<String, String>>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    colors: TransitaireColors = rememberTransitaireColors()
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.toggleTrackBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (key, label) ->
                val selected = key == selectedKey
                val bg by animateColorAsState(
                    targetValue = if (selected) colors.cardBg else Color.Transparent,
                    label = "segBg"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable { onSelect(key) }
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (selected) colors.textPrimary else colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
internal fun HumanChipRow(
    items: List<Pair<String, String>>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    colors: TransitaireColors = rememberTransitaireColors(),
    accentColor: Color = CyanPrimary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (key, label) ->
            val selected = key == selectedKey
            val bg by animateColorAsState(
                targetValue = if (selected) accentColor else colors.cardBg,
                label = "chipBg"
            )
            Surface(
                shape = CircleShape,
                color = bg,
                border = if (selected) null else BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .heightIn(min = 38.dp)
                    .clip(CircleShape)
                    .clickable { onSelect(key) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (selected) Color.White else colors.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
internal fun HumanSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    colors: TransitaireColors = rememberTransitaireColors()
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = Inter,
                fontSize = 14.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Effacer la recherche",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.cardBg,
            unfocusedContainerColor = colors.cardBg,
            focusedBorderColor = colors.cyanAccent,
            unfocusedBorderColor = colors.border
        ),
        textStyle = TextStyle(
            fontFamily = Inter,
            fontSize = 14.sp,
            color = colors.textPrimary
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
internal fun HumanEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    colors: TransitaireColors = rememberTransitaireColors()
) {
    HumanSectionCard(
        modifier = modifier,
        colors = colors,
        contentPadding = PaddingValues(26.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HumanIconBadge(
                icon = icon,
                tint = colors.cyanAccent,
                backgroundColor = colors.cyanSoftBg,
                size = 54.dp,
                iconSize = 26.dp,
                cornerRadius = 18.dp
            )
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontFamily = Inter,
                fontSize = 13.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.height(4.dp))
                HumanTonalButton(
                    text = actionLabel,
                    onClick = onActionClick,
                    colors = colors
                )
            }
        }
    }
}

@Composable
internal fun HumanPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = CyanPrimary,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier.heightIn(min = 50.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun HumanTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: TransitaireColors = rememberTransitaireColors(),
    containerColor: Color = colors.cyanSoftBg,
    contentColor: Color = colors.cyanAccent,
    icon: ImageVector? = null
) {
    FilledTonalButton(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier.heightIn(min = 44.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun HumanTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    suffixText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    colors: TransitaireColors = rememberTransitaireColors()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 12.sp
            )
        },
        placeholder = if (placeholder.isNotBlank()) {
            { Text(text = placeholder, fontFamily = Inter, fontSize = 14.sp, color = colors.textSecondary) }
        } else null,
        suffix = if (!suffixText.isNullOrBlank()) {
            { Text(text = suffixText, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textSecondary) }
        } else null,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.inputBg,
            unfocusedContainerColor = colors.inputBg,
            focusedBorderColor = colors.cyanAccent,
            unfocusedBorderColor = Color.Transparent
        ),
        textStyle = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = colors.textPrimary
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
internal fun HumanHairline(colors: TransitaireColors = rememberTransitaireColors()) {
    HorizontalDivider(color = colors.hairline, thickness = 1.dp)
}

@Composable
internal fun HumanSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    colors: TransitaireColors = rememberTransitaireColors()
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = colors.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }
        if (!actionText.isNullOrBlank() && onActionClick != null) {
            Text(
                text = actionText,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.cyanAccent,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HumanSheetContainer(
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    colors: TransitaireColors = rememberTransitaireColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.cardBg,
        contentColor = colors.textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = BUSINESS_DIALOG_MAX_WIDTH)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colors.textPrimary
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
            }
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VariantChipsEditor(
    variantsText: String,
    onVariantsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Variantes",
    placeholder: String = "Ajouter une puce (ex : Noir, 5 Blanc)",
    colors: TransitaireColors = rememberTransitaireColors()
) {
    val items = remember(variantsText) { VariantColorUtils.parseVariants(variantsText) }
    var draft by remember { mutableStateOf("") }

    fun commitDraft() {
        val additions = VariantColorUtils.parseVariants(draft)
        if (additions.isNotEmpty()) {
            val merged = (items + additions).distinctBy { it.lowercase() }
            onVariantsChange(merged.joinToString(" · "))
        }
        draft = ""
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = colors.textPrimary
            )
        }

        if (items.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEachIndexed { idx, variant ->
                    val dotColor = VariantColorUtils.getVariantColor(variant, idx)
                    Surface(
                        shape = CircleShape,
                        color = colors.innerCardBg,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                                    .then(
                                        if (VariantColorUtils.isLightColor(dotColor)) {
                                            Modifier.border(1.dp, colors.border, CircleShape)
                                        } else Modifier
                                    )
                            )
                            Text(
                                text = variant,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val remaining = items.filterIndexed { i, _ -> i != idx }
                                        onVariantsChange(remaining.joinToString(" · "))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Retirer $variant",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { typed ->
                    if (typed.contains(",")) {
                        val parts = typed.split(",")
                        val ready = parts.dropLast(1).flatMap { VariantColorUtils.parseVariants(it) }
                        if (ready.isNotEmpty()) {
                            val merged = (items + ready).distinctBy { it.lowercase() }
                            onVariantsChange(merged.joinToString(" · "))
                        }
                        draft = parts.last()
                    } else {
                        draft = typed
                    }
                },
                placeholder = {
                    Text(
                        text = placeholder,
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { commitDraft() }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.inputBg,
                    unfocusedContainerColor = colors.inputBg,
                    focusedBorderColor = colors.cyanAccent,
                    unfocusedBorderColor = Color.Transparent
                ),
                textStyle = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                ),
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (draft.isNotBlank()) colors.cyanAccent else colors.innerCardBg,
                border = if (draft.isNotBlank()) null else BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = draft.isNotBlank()) { commitDraft() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter variante",
                        tint = if (draft.isNotBlank()) Color.White else colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HumanDatePickerField(
    dateMillis: Long,
    onDateSelected: (Long) -> Unit,
    label: String = "Date de règlement (jj/mm/aaaa)",
    modifier: Modifier = Modifier,
    colors: TransitaireColors = rememberTransitaireColors()
) {
    var dateText by remember(dateMillis) { mutableStateOf(DateUtils.formatShortDate(dateMillis)) }
    var showDatePicker by remember { mutableStateOf(false) }
    val isInvalid = dateText.isNotBlank() && !DateUtils.isValidFrenchDate(dateText)

    HumanTextField(
        value = dateText,
        onValueChange = { raw ->
            val filtered = raw.filter { c -> c.isDigit() || c == '/' }.take(10)
            dateText = filtered
            if (DateUtils.isValidFrenchDate(filtered)) {
                onDateSelected(DateUtils.frenchDateToEpochMillis(filtered))
            }
        },
        label = label,
        placeholder = "jj/mm/aaaa",
        keyboardType = KeyboardType.Number,
        isError = isInvalid,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Choisir une date",
                    tint = colors.cyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        colors = colors,
        modifier = modifier
    )

    if (showDatePicker) {
        val initialUtcMillis = remember(dateText, dateMillis) {
            val validStr = if (DateUtils.isValidFrenchDate(dateText)) dateText else DateUtils.formatShortDate(dateMillis)
            DateUtils.frenchDateToUtcPickerMillis(validStr)
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val chosenMillis = datePickerState.selectedDateMillis?.let { utcMs ->
                            val frenchDate = DateUtils.utcPickerMillisToFrenchDate(utcMs)
                            dateText = frenchDate
                            DateUtils.frenchDateToEpochMillis(frenchDate)
                        } ?: dateMillis
                        onDateSelected(chosenMillis)
                        showDatePicker = false
                    }
                ) {
                    Text("OK", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler", fontFamily = PlusJakartaSans)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


