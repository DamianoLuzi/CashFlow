package com.example.exptrackpm.ui.screens.dashboard

import android.util.Log
import co.yml.charts.common.model.Point
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.exptrackpm.theme.ExpTrackPMTheme
import com.example.exptrackpm.ui.screens.expenselist.ExpenseListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard( viewModel: ExpenseListViewModel = viewModel(), navController: NavController) {
    val expenses by viewModel.expenseList.collectAsStateWithLifecycle()
    Log.d("expenses", expenses.toString())
    val totalSpent = expenses.sumOf { it.amount }

    val pointsData: List<Point> =
        listOf(Point(0f, 40f), Point(1f, 90f), Point(2f, 0f), Point(3f, 60f), Point(4f, 10f))

    val xAxisData = AxisData.Builder()
        .axisStepSize(100.dp)
        .backgroundColor(Color.Blue)
        .steps(pointsData.size - 1)
        .labelData { i -> i.toString() }
        .labelAndAxisLinePadding(15.dp)
        .build()
    val steps = 5
    val yAxisData = AxisData.Builder()
        .steps(steps)
        .backgroundColor(Color.Red)
        .labelAndAxisLinePadding(20.dp).build()

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

            LineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                lineChartData = lineChartData
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    val navController = rememberNavController()
    ExpTrackPMTheme {
        Dashboard(navController = navController)
    }
}
