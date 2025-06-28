// SpendingPieChart.kt
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
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData

@Composable
fun SpendingPieChart(
    transactions: List<Transaction>,
    title: String,
    modifier: Modifier = Modifier
) {
    val categoryAmounts = transactions
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() }.toFloat() }

    val totalAmount = categoryAmounts.values.sum()

    if (totalAmount == 0f || categoryAmounts.isEmpty()) {
        Text("No $title data to display for the selected period.")
        return
    }
    val colors = listOf(
        Color(0xFFE57373), // Red
        Color(0xFF81C784), // Green
        Color(0xFF64B5F6), // Blue
        Color(0xFFFFD54F), // Yellow
        Color(0xFFB39DDB), // Purple
        Color(0xFFB0BEC5), // Grey
        Color(0xFF4DB6AC), // Teal
        Color(0xFFF06292), // Pink
        Color(0xFFA1887F), // Brown
        Color(0xFFFF8A65), // Orange
        Color(0xFF90A4AE), // Blue Grey
        Color(0xFF7986CB), // Indigo
        Color(0xFFDCE775), // Lime
        Color(0xFFFFEE58), // Amber
        Color(0xFFBA68C8)  // Light Purple
    )
    var colorIndex = 0

    val pieChartConfig = PieChartConfig(
        isAnimationEnable = true,
        showSliceLabels = true,
        animationDuration = 1500
    )
    val slices = categoryAmounts.map { (category, amount) ->
        val percentage = (amount / totalAmount) * 100
        val color = colors[colorIndex % colors.size]
        colorIndex++

        PieChartData.Slice(
            label = category,
            value = amount,
            color = color,
            sliceDescription = { slicePercentage ->
                "Slice name : $category \nPercentage  : $slicePercentage %"         }
        )
    }

    Text(text = title, style = MaterialTheme.typography.titleLarge)

    PieChart(
        modifier = modifier
            .fillMaxWidth()
            .height(600.dp),
        pieChartData = PieChartData(
            slices = slices,
            plotType = PlotType.Pie,
        ),
        pieChartConfig = pieChartConfig
    )
}
