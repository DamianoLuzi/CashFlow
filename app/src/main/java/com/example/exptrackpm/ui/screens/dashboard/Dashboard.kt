package com.example.exptrackpm.ui.screens.dashboard

import android.util.Log
import co.yml.charts.common.model.Point
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import co.yml.charts.axis.AxisData
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
import com.example.exptrackpm.domain.model.Expense
import com.example.exptrackpm.theme.ExpTrackPMTheme
import com.example.exptrackpm.ui.screens.expenselist.ExpenseListViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun generatePointsFromExpenses(expenses: List<Expense>, daysBack: Int = 5): List<Point> {
    val calendar = Calendar.getInstance()
    val dailyTotals = FloatArray(daysBack) { 0f }

    val now = Date()

    for (expense in expenses) {
        val expenseDate = expense.date.toDate() // Convert Firestore Timestamp to Date
        val diff = ((now.time - expenseDate.time) / (1000 * 60 * 60 * 24)).toInt()

        if (diff in 0 until daysBack) {
            val index = daysBack - diff - 1 // Flip order to make 0 = oldest, last = today
            dailyTotals[index] += expense.amount.toFloat()
        }
    }

    return dailyTotals.mapIndexed { index, total -> Point(index.toFloat(), total) }
}

fun groupExpensesByDay(expenses: List<Expense>): Map<String, Float> {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    Log.d("expenses", expenses.toString())
    return expenses
        .groupBy { sdf.format(it.date.toDate()) }
        .mapValues { entry ->
            entry.value.sumOf { it.amount.toDouble() }.toFloat()
        }

}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefinedDashboard( viewModel: ExpenseListViewModel = viewModel(), navController: NavController) {
    val expenses by viewModel.expenseList.collectAsStateWithLifecycle()

    val now = Calendar.getInstance().time
    var selectedRange by remember { mutableStateOf("7d") }
    val options = listOf("7d", "30d", "90d")
    val daysBack = when (selectedRange) {
        "7d" -> 7
        "30d" -> 30
        "90d" -> 90
        else -> 7
    }
    val filteredExpenses = expenses.filter {
        val diff = ((now.time - it.date.toDate().time) / (1000 * 60 * 60 * 24)).toInt()
        diff in 0 until daysBack
    }

    val dateLabelFormat = when (daysBack) {
        in 1..7 -> "EEE"            // Short day names for last 7 days
        in 8..30 -> "MMM dd"        // Full dates for monthly view
        else -> "MMM"               // Just the month for longer views
    }
    val labelSDF = SimpleDateFormat(dateLabelFormat, Locale.getDefault())

    Log.d("expenses", expenses.toString())
    val totalSpent = expenses.sumOf { it.amount }
    val groupedExpenses = groupExpensesByDay(expenses).toSortedMap() // keeps dates sorted
    val labels = groupedExpenses.keys.toList()
    val displayLabels = labels.map {
        labelSDF.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)!!)
    }

    val pointsData = if (groupedExpenses.isNotEmpty()) {
        labels.mapIndexed { index, _ ->
            Point(index.toFloat(), (groupedExpenses[labels[index]] ?: 0f))
        }
    } else emptyList()

    val sdf = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
    val today = Calendar.getInstance()
    val dayLabels = (0 until 5).map {
        today.add(Calendar.DAY_OF_YEAR, -1)
        sdf.format(today.time)
    }.reversed()



    val xAxisData = AxisData.Builder()
        .axisStepSize(50.dp)
        .backgroundColor(Color.Transparent)
        .steps(pointsData.size - 1)
        .labelData { i -> displayLabels.getOrElse(i) { "" } }
        .labelAndAxisLinePadding(10.dp)
        .build()

    val maxY = (pointsData.maxOfOrNull { it.y } ?: 100f).coerceAtLeast(100f)
    val yAxisSteps = 5

    val yAxisData = AxisData.Builder()
        .steps(yAxisSteps)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            val step = maxY / yAxisSteps
            "%.2f".format(i * step)
        }
        .build()


    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    LineStyle(),
                    IntersectionPoint(),
                    SelectionHighlightPoint(),
                    ShadowUnderLine(),
                    SelectionHighlightPopUp()
                )
            ),
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(),
        backgroundColor = Color.White
    )
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Spent") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("Total Spent", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("$${"%.2f".format(totalSpent)}", fontSize = 32.sp)

            Spacer(Modifier.height(24.dp))

            //CategoryPieChart(expenses = expenses)

            Spacer(Modifier.height(24.dp))

            // Future: Add a bar chart here for monthly trends

            Text("Recent Transactions", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            expenses.take(5).forEach {
                Text("${it.description}: $${it.amount}")
            }
            if (pointsData.isNotEmpty()) {
            LineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                lineChartData = LineChartData(
                    linePlotData = LinePlotData(
                        lines = listOf(
                            Line(
                                dataPoints = pointsData,
                                lineStyle = LineStyle(color = Color.Blue),
                                intersectionPoint = IntersectionPoint(),
                                selectionHighlightPoint = SelectionHighlightPoint(),
                                shadowUnderLine = ShadowUnderLine(),
                                selectionHighlightPopUp = SelectionHighlightPopUp()
                            )
                        )
                    ),
                    xAxisData = xAxisData,
                    yAxisData = yAxisData,
                    gridLines = GridLines(),
                    backgroundColor = Color.White
                )
            )
            } else {
                Text("Not enough data to display chart.")
            }

            Row {
                options.forEach { label ->
                    Button(
                        onClick = { selectedRange = label },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRange == label) Color.Blue else Color.LightGray
                        ),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(label)
                    }
                }
            }

        }
    }
}




@Preview(showBackground = true)
@Composable
fun RefinedDashboardPreview() {
    val navController = rememberNavController()
    ExpTrackPMTheme {
        RefinedDashboard(navController = navController)
    }
}
