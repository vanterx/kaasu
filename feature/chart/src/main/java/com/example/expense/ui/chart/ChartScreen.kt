package com.example.expense.ui.chart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense.core.domain.model.AccountTotal
import com.example.expense.core.domain.model.CategoryTotal
import com.example.expense.ui.theme.ChartColors
import com.example.expense.util.formatCurrency
import com.example.expense.util.formatMonthYear

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(viewModel: ChartViewModel, currencyCode: String) {
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val accountTotals by viewModel.accountTotals.collectAsState()
    val total by viewModel.monthlyTotal.collectAsState()
    val monthYear by viewModel.selectedMonth.collectAsState()
    val isYtdMode by viewModel.isYtdMode.collectAsState()
    val (month, year) = monthYear

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Reports", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (isYtdMode) "YTD Jan – ${formatMonthYear(month, year)}"
                            else formatMonthYear(month, year),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.Default.ChevronLeft, "Previous month")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Default.ChevronRight, "Next month")
                    }
                }
            )
        }
    ) { padding ->
        val totalsWithData = categoryTotals.filter { (it.total ?: 0.0) > 0 }
        val uncategorizedTotal = total - totalsWithData.sumOf { it.total ?: 0.0 }

        if (total == 0.0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isYtdMode) "No expenses this year"
                    else "No expenses in ${formatMonthYear(month, year)}",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = isYtdMode,
                        onClick = { viewModel.toggleYtdMode() },
                        label = { Text("YTD") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                "YTD",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    )
                }

                Text(
                    "SPENDING BY CATEGORY",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PieChart(
                            modifier = Modifier.size(200.dp),
                            totals = totalsWithData,
                            uncategorizedTotal = uncategorizedTotal,
                            total = total,
                            centerLabel = formatCurrency(total, currencyCode),
                            onSurfaceArgb = onSurfaceColor.toArgb()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        totalsWithData.forEach { item ->
                            LegendRow(
                                name = item.name,
                                amount = item.total ?: 0.0,
                                total = total,
                                color = ChartColors[item.colorIndex % ChartColors.size],
                                currencyCode = currencyCode
                            )
                        }
                        if (uncategorizedTotal > 0) {
                            LegendRow(
                                name = "Uncategorized",
                                amount = uncategorizedTotal,
                                total = total,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                currencyCode = currencyCode
                            )
                        }
                    }
                }

                if (accountTotals.isNotEmpty()) {
                    Text(
                        "SPENDING BY ACCOUNT",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            accountTotals.forEach { item ->
                                AccountSummaryRow(
                                    accountTotal = item,
                                    total = total,
                                    currencyCode = currencyCode
                                )
                            }
                        }
                    }
                }

                if (totalsWithData.isNotEmpty() || uncategorizedTotal > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "BREAKDOWN",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                totalsWithData.forEach { item ->
                    BarChartCard(
                        categoryName = item.name,
                        amount = item.total ?: 0.0,
                        total = total,
                        color = ChartColors[item.colorIndex % ChartColors.size],
                        currencyCode = currencyCode
                    )
                }

                if (uncategorizedTotal > 0) {
                    BarChartCard(
                        categoryName = "Uncategorized",
                        amount = uncategorizedTotal,
                        total = total,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        currencyCode = currencyCode
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AccountSummaryRow(
    accountTotal: AccountTotal,
    total: Double,
    currencyCode: String
) {
    val percentage = if (total > 0) (accountTotal.total / total * 100) else 0.0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            accountTotal.account ?: "None",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            formatCurrency(accountTotal.total, currencyCode),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            "  ${String.format("%.0f", percentage)}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PieChart(
    modifier: Modifier,
    totals: List<CategoryTotal>,
    uncategorizedTotal: Double,
    total: Double,
    centerLabel: String,
    onSurfaceArgb: Int
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 36f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f

        val slices = totals.map { it to (it.total ?: 0.0) } +
            if (uncategorizedTotal > 0) listOf(null to uncategorizedTotal) else emptyList()

        for ((item, amount) in slices) {
            val sweepAngle = (amount / total * 360).toFloat()
            val color = when {
                item != null -> ChartColors[item.colorIndex % ChartColors.size]
                else -> Color.Gray
            }
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle - 1f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweepAngle
        }

        val paint = android.graphics.Paint().apply {
            color = onSurfaceArgb
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 38f
            isFakeBoldText = true
            isAntiAlias = true
        }
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawText(
                centerLabel,
                size.width / 2f,
                size.height / 2f + 14f,
                paint
            )
        }
    }
}

@Composable
private fun LegendRow(
    name: String,
    amount: Double,
    total: Double,
    color: Color,
    currencyCode: String
) {
    val percentage = if (total > 0) (amount / total * 100) else 0.0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            formatCurrency(amount, currencyCode),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            "  ${String.format("%.0f", percentage)}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BarChartCard(
    categoryName: String,
    amount: Double,
    total: Double,
    color: Color,
    currencyCode: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(categoryName, style = MaterialTheme.typography.bodyMedium)
                Text(
                    formatCurrency(amount, currencyCode),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val fraction = if (total > 0) (amount / total).toFloat() else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(3.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .background(color, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}
