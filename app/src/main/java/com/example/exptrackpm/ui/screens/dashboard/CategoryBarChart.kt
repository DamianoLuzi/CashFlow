// CategorizedBarChart.kt
package com.example.exptrackpm.ui.screens.dashboard


import Transaction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import java.text.DecimalFormat


@Composable
fun CategorizedBarChart(
    transactions: List<Transaction>,
    title: String,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val categoryAmounts = transactions
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() }.toFloat() }
        .toSortedMap()
    val barDataPoints = categoryAmounts.entries.mapIndexed { index, entry ->
        BarData(
            point = Point(index.toFloat(), entry.value),
            label = entry.key,
            color = barColor
        )
    }

    if (barDataPoints.isEmpty()) {
        Text("No $title data to display for the selected period.")
        return
    }

    val maxY = barDataPoints.maxOfOrNull { barData -> barData.point.y }?.coerceAtLeast(10f) ?: 10f

    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp)
        .backgroundColor(Color.Transparent)
        .steps(barDataPoints.size - 1)
        .labelData { i -> categoryAmounts.keys.elementAtOrNull(i) ?: "" }
        .labelAndAxisLinePadding(10.dp)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(5)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            val value = i * (maxY / 5f)
            val formatter = DecimalFormat("0.00")
            "€${formatter.format(value)}"
        }
        .build()

    Text(text = title, style = MaterialTheme.typography.titleLarge)

    BarChart(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        barChartData = BarChartData(
            chartData = barDataPoints,
            xAxisData = xAxisData,
            yAxisData = yAxisData,
            backgroundColor = Color.White
        )
    )
}


@Composable
fun CategorizedBarChart1(
    transactions: List<Transaction>,
    title: String,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val categoryAmounts = transactions
        .groupBy { it.category }
        .mapValues { it.value.sumOf { it.amount.toDouble() }.toFloat() }
        .toSortedMap()

    val colors = listOf(
        Color(0xFF90CAF9), Color(0xFF81C784), Color(0xFFFFB74D),
        Color(0xFFE57373), Color(0xFFBA68C8), Color(0xFFFF8A65)
    )
    val axisLabelColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val axisLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    val barDataPoints = categoryAmounts.entries.mapIndexed { index, entry ->
        BarData(
            point = Point(index.toFloat(), entry.value),
            label = entry.key,
            color = colors[index % colors.size]
        )
    }

    if (barDataPoints.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No $title data to display.", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxY = barDataPoints.maxOf { it.point.y }.coerceAtLeast(10f)

    val xAxisData = AxisData.Builder()
        .steps(barDataPoints.size - 1)
        .axisStepSize(30.dp)
        .labelData { i ->
            val label = categoryAmounts.keys.elementAtOrNull(i) ?: ""
            if (label.length > 10) label.take(8) + "…" else label
        }
        .labelAndAxisLinePadding(10.dp)
        .axisLabelColor(axisLabelColor)
        .axisLineColor(axisLineColor)
        .backgroundColor(Color.Transparent)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(5)
        .labelData { i ->
            val value = i * (maxY / 5f)
            "€${DecimalFormat("0.00").format(value)}"
        }
        .labelAndAxisLinePadding(20.dp)
        .backgroundColor(Color.Transparent)
        .build()

    val total = categoryAmounts.values.sum()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                "Total: €${DecimalFormat("0.00").format(total)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            BarChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                barChartData = BarChartData(
                    chartData = barDataPoints,
                    xAxisData = xAxisData,
                    yAxisData = yAxisData,
                    backgroundColor = MaterialTheme.colorScheme.background
                )
            )
        }
    }
}
