package com.example.ui.business.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val BUSINESS_MAX_WIDTH = 1040.dp
internal val BUSINESS_DIALOG_MAX_WIDTH = 560.dp

internal enum class BusinessWidthClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

internal fun widthClassFor(maxWidth: Dp): BusinessWidthClass = when {
    maxWidth < 600.dp -> BusinessWidthClass.COMPACT
    maxWidth < 840.dp -> BusinessWidthClass.MEDIUM
    else -> BusinessWidthClass.EXPANDED
}

@Composable
internal fun BusinessResponsiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = BUSINESS_MAX_WIDTH,
    content: @Composable (BusinessWidthClass, Dp) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        val availableWidth = this.maxWidth
        val widthClass = widthClassFor(availableWidth)
        val horizontalPadding = when (widthClass) {
            BusinessWidthClass.COMPACT -> 16.dp
            BusinessWidthClass.MEDIUM -> 24.dp
            BusinessWidthClass.EXPANDED -> 32.dp
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth)
                .padding(horizontal = horizontalPadding)
        ) {
            content(widthClass, availableWidth)
        }
    }
}

@Composable
internal fun BusinessResponsivePair(
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    minTwoColumnWidth: Dp = 600.dp,
    horizontalSpacing: Dp = 12.dp,
    verticalSpacing: Dp = 12.dp
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (this.maxWidth >= minTwoColumnWidth) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.weight(1f)) { first() }
                Box(modifier = Modifier.weight(1f)) { second() }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing)
            ) {
                first()
                second()
            }
        }
    }
}

@Composable
internal fun <T> BusinessAdaptiveGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    columnsForCompact: Int = 1,
    columnsForMedium: Int = 2,
    columnsForExpanded: Int = 2,
    horizontalSpacing: Dp = 12.dp,
    verticalSpacing: Dp = 12.dp,
    itemContent: @Composable (T) -> Unit
) {
    if (items.isEmpty()) return
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = when (widthClassFor(this.maxWidth)) {
            BusinessWidthClass.COMPACT -> columnsForCompact
            BusinessWidthClass.MEDIUM -> columnsForMedium
            BusinessWidthClass.EXPANDED -> columnsForExpanded
        }.coerceAtLeast(1)

        if (columns == 1) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing)
            ) {
                items.forEach { item ->
                    itemContent(item)
                }
            }
        } else {
            val rows = items.chunked(columns)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
                        verticalAlignment = Alignment.Top
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                itemContent(item)
                            }
                        }
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
