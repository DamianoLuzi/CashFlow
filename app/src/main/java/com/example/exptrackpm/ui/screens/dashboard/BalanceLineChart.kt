package com.example.exptrackpm.ui.screens.dashboard


import Transaction
import TransactionType
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
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * A Composable function that displays a line chart of the cumulative net balance over time.
 *
 * @param transactions The list of all transactions (income and expenses).
 * @param allDatesSorted A sorted list of unique dates for the X-axis.
 * @param displayLabels The formatted labels for the X-axis (e.g., "Mon", "Jan 01").
 * @param modifier Modifier for the LineChart composable.
 */
@Composable
fun BalanceLineChart(
    transactions: List<Transaction>,
    allDatesSorted: List<String>,
    displayLabels: List<String>,
    modifier: Modifier = Modifier
) {
    // Group transactions by day and calculate daily net change
    val dailyNetChange = allDatesSorted.associateWith { 0f }.toMutableMap()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    transactions.forEach { transaction ->
        val dateKey = sdf.format(transaction.date.toDate())
        val amount = transaction.amount.toDouble().toFloat()
        dailyNetChange[dateKey] = dailyNetChange.getOrDefault(dateKey, 0f) +
                (if (transaction.type == TransactionType.INCOME) amount else -amount)
    }

    // Calculate cumulative balance
    var runningBalance = 0f
    val balancePoints = allDatesSorted.mapIndexed { index, dateKey ->
        runningBalance += dailyNetChange[dateKey] ?: 0f
        Point(index.toFloat(), runningBalance)
    }

    if (balancePoints.isEmpty()) {
        Text("No balance data to display for the selected period.")
        return
    }

    // Determine Y-axis max/min for balance
    val minY = balancePoints.minOfOrNull { it.y }?.let { if (it < 0f) it.coerceAtMost(-10f) else 0f } ?: 0f
    val maxY = balancePoints.maxOfOrNull { it.y }?.let { if (it > 0f) it.coerceAtLeast(10f) else 0f } ?: 0f

    val yAxisData = AxisData.Builder()
        .steps(5)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            val value = minY + (i * ((maxY - minY) / 5f))
            val formatter = DecimalFormat("0.00")
            "€${formatter.format(value)}"
        }
        .build()

    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp) // Dynamic step size based on date range could be more complex here
        .backgroundColor(Color.Transparent)
        .steps(allDatesSorted.size - 1)
        .labelData { i -> displayLabels.getOrElse(i) { "" } }
        .labelAndAxisLinePadding(10.dp)
        .build()

    Text(text = "Cumulative Balance Over Time", style = MaterialTheme.typography.titleLarge)

    LineChart(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        lineChartData = LineChartData(
            linePlotData = LinePlotData(
                lines = listOf(
                    Line(
                        dataPoints = balancePoints,
                        lineStyle = LineStyle(color = Color.Blue), // Net balance in blue
                        intersectionPoint = IntersectionPoint(),
                        selectionHighlightPoint = SelectionHighlightPoint(),
                        shadowUnderLine = ShadowUnderLine(color = Color.Blue.copy(alpha = 0.2f)),
                        selectionHighlightPopUp = SelectionHighlightPopUp(
                            popUpLabel = { x, y ->
                                val index = x.toInt().coerceIn(0, displayLabels.lastIndex)
                                val label = displayLabels[index]
                                val amount = DecimalFormat("0.00").format(y)
                                "$label: Balance €$amount"
                            }
                        )
                    )
                )
            ),
            xAxisData = xAxisData,
            yAxisData = yAxisData,
            gridLines = GridLines(),
            backgroundColor = Color.White
        )
    )
}
