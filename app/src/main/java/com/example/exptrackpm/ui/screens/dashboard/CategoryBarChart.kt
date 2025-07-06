package com.example.exptrackpm.ui.screens.dashboard


import Transaction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val axisLabelColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val axisLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)


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
        .axisLabelColor(axisLabelColor)
        .axisLineColor(axisLineColor)
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