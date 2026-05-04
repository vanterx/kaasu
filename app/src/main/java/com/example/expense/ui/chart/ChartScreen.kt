package com.example.expense.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.expense.data.db.CategoryTotal
import com.example.expense.ui.theme.ChartColors
import com.example.expense.util.formatCurrency
import com.example.expense.util.formatMonthYear

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(viewModel: ChartViewModel) {
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val total by viewModel.monthlyTotal.collectAsState()
    val monthYear by viewModel.selectedMonth.collectAsState()
    val (month, year) = monthYear

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Charts") },
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
                    "No expenses in ${formatMonthYear(month, year)}",
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PieChartCard(
                    title = "Spending by Category",
                    total = total,
                    totals = totalsWithData,
                    uncategorizedTotal = uncategorizedTotal
                )

                totalsWithData.forEach { item ->
                    BarChartCard(
                        categoryName = item.name,
                        amount = item.total ?: 0.0,
                        total = total,
                        color = ChartColors[item.colorIndex % ChartColors.size]
                    )
                }

                if (uncategorizedTotal > 0) {
                    BarChartCard(
                        categoryName = "Uncategorized",
                        amount = uncategorizedTotal,
                        total = total,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun PieChartCard(
    title: String,
    total: Double,
    totals: List<CategoryTotal>,
    uncategorizedTotal: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                formatCurrency(total),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            PieChart(
                modifier = Modifier.size(200.dp),
                totals = totals,
                uncategorizedTotal = uncategorizedTotal,
                total = total
            )

            Spacer(modifier = Modifier.height(12.dp))

            totals.forEach { item ->
                LegendRow(
                    name = item.name,
                    amount = item.total ?: 0.0,
                    total = total,
                    color = ChartColors[item.colorIndex % ChartColors.size]
                )
            }

            if (uncategorizedTotal > 0) {
                LegendRow(
                    name = "Uncategorized",
                    amount = uncategorizedTotal,
                    total = total,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun PieChart(
    modifier: Modifier,
    totals: List<CategoryTotal>,
    uncategorizedTotal: Double,
    total: Double
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 40f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f

        val slices = totals.map {
            it to (it.total ?: 0.0)
        } + if (uncategorizedTotal > 0) listOf(null to uncategorizedTotal) else emptyList()

        for ((item, amount) in slices) {
            val sweepAngle = (amount / total * 360).toFloat()
            val color = when {
                item != null -> ChartColors[item.colorIndex % ChartColors.size]
                else -> Color.Gray
            }
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun LegendRow(
    name: String,
    amount: Double,
    total: Double,
    color: Color
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
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            formatCurrency(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            " (${String.format("%.0f", percentage)}%)",
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
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(categoryName, style = MaterialTheme.typography.bodyMedium)
                Text(
                    formatCurrency(amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            val fraction = if (total > 0) (amount / total).toFloat() else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.small
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(12.dp)
                        .background(color, MaterialTheme.shapes.small)
                )
            }
        }
    }
}
