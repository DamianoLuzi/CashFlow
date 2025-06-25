// CategorizedBarChart.kt
package com.example.exptrackpm.ui.screens.dashboard


import Transaction
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

/**
 * A Composable function that displays a bar chart of transaction amounts grouped by category.
 * Can be used for either expenses or income.
 *
 * @param transactions The list of transactions to display.
 * @param title The title for the chart (e.g., "Expenses by Category", "Income by Category").
 * @param barColor The color of the bars in the chart.
 * @param modifier Modifier for the BarChart composable.
 */
@Composable
fun CategorizedBarChart(
    transactions: List<Transaction>,
    title: String,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    // Group transactions by category and sum their amounts
    val categoryAmounts = transactions
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() }.toFloat() }
        .toSortedMap() // Sorts by category name, can be changed to sort by amount if preferred

    // Prepare data points for the bar chart
    // Corrected: Use .entries.mapIndexed to iterate over map entries with an index
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

    // Determine the maximum Y-axis value
    // Corrected: Explicitly name the lambda parameter 'barData' for clarity and type inference
    val maxY = barDataPoints.maxOfOrNull { barData -> barData.point.y }?.coerceAtLeast(10f) ?: 10f // Ensure min value for axis

    // X-axis data: Labels are category names
    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp) // Fixed step size for categories, might need adjustment based on number of categories
        .backgroundColor(Color.Transparent)
        .steps(barDataPoints.size - 1)
        .labelData { i -> categoryAmounts.keys.elementAtOrNull(i) ?: "" }
        .labelAndAxisLinePadding(10.dp)
        .build()

    // Y-axis data: Formatted for currency amounts
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
        barChartData = BarChartData( // Corrected BarChartData construction
            chartData = barDataPoints, // Directly pass List<BarData> to 'chartData'
            xAxisData = xAxisData,
            yAxisData = yAxisData,
            backgroundColor = Color.White
            // SelectionHighlightData and other parameters are not part of this specific BarChartData constructor
            // If selection popups are desired, they might need to be implemented via a custom drawBar
            // or if the BarChart composable itself provides such parameters.
        )
    )
}
