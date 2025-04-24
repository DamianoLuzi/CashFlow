package com.example.exptrackpm.ui.screens.dashboard

import Transaction
import TransactionType
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
import com.example.exptrackpm.auth.SessionManager
import com.example.exptrackpm.theme.ExpTrackPMTheme
import com.example.exptrackpm.ui.screens.transactions.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(viewModel: TransactionViewModel = viewModel(), navController: NavController) {
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val now = Calendar.getInstance().time

    var selectedRange by remember { mutableStateOf("7d") }
    val options = listOf("7d", "30d", "90d", "1Y")
    val daysBack = when (selectedRange) {
        "7d" -> 7
        "30d" -> 30
        "90d" -> 90
        "1Y" -> 365
        else -> 7
    }

    val filtered = transactions.filter {
        val diff = ((now.time - it.date.toDate().time) / (1000 * 60 * 60 * 24)).toInt()
        diff in 0 until daysBack
    }

    val expenses = filtered.filter { it.type == TransactionType.EXPENSE }
    val incomes = filtered.filter { it.type == TransactionType.INCOME }

    val totalSpent = expenses.sumOf { it.amount }
    val totalEarned = incomes.sumOf { it.amount }

    fun groupByDay(transactions: List<Transaction>): Map<String, Float> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return transactions.groupBy { sdf.format(it.date.toDate()) }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() }.toFloat() }
    }

    val expenseGrouped = groupByDay(expenses).toSortedMap()
    val incomeGrouped = groupByDay(incomes).toSortedMap()

    val dateFormat = when (daysBack) {
        in 1..7 -> "EEE"
        in 8..30 -> "MMM dd"
        else -> "MMM"
    }

    val displayFormat = SimpleDateFormat(dateFormat, Locale.getDefault())

    val allDates = (expenseGrouped.keys + incomeGrouped.keys).toSortedSet()
    val displayLabels = allDates.map {
        displayFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)!!)
    }

    val expensePoints = allDates.mapIndexed { index, date ->
        Point(index.toFloat(), expenseGrouped[date] ?: 0f)
    }

    val incomePoints = allDates.mapIndexed { index, date ->
        Point(index.toFloat(), incomeGrouped[date] ?: 0f)
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val stepSize = screenWidth / (expensePoints.size + 1)

    val xAxisData = AxisData.Builder()
        .axisStepSize(stepSize)
        .backgroundColor(Color.Transparent)
        .steps(expensePoints.size - 1)
        .labelData { i -> displayLabels.getOrElse(i) { "" } }
        .labelAndAxisLinePadding(10.dp)
        .build()

    val maxY = listOf(
        expensePoints.maxOfOrNull { it.y } ?: 0f,
        incomePoints.maxOfOrNull { it.y } ?: 0f
    ).maxOrNull()!!.coerceAtLeast(100f)

    val yAxisData = AxisData.Builder()
        .steps(5)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i -> "%.2f".format(i * (maxY / 5)) }
        .build()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Text("Total Spent", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("$${"%.2f".format(totalSpent)}", fontSize = 30.sp)

            Text("Total Earned", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("+$${"%.2f".format(totalEarned)}", fontSize = 30.sp)

            Spacer(Modifier.height(24.dp))

            if (expensePoints.isNotEmpty() || incomePoints.isNotEmpty()) {
                LineChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    lineChartData = LineChartData(
                        linePlotData = LinePlotData(
                            lines = listOf(
                                Line(
                                    dataPoints = expensePoints,
                                    lineStyle = LineStyle(color = Color.Red),
                                    intersectionPoint = IntersectionPoint(),
                                    selectionHighlightPoint = SelectionHighlightPoint(),
                                    shadowUnderLine = ShadowUnderLine(),
                                    selectionHighlightPopUp = SelectionHighlightPopUp()
                                ),
                                Line(
                                    dataPoints = incomePoints,
                                    lineStyle = LineStyle(color = Color.Green),
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
                Text("Not enough data to show chart.")
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

            Button(
                onClick = { SessionManager.logout() },
                modifier = Modifier.padding(4.dp)
            ) {
                Text("Log Out")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RefinedDashboardPreview() {
    val navController = rememberNavController()
    ExpTrackPMTheme {
        Dashboard(navController = navController)
    }
}
