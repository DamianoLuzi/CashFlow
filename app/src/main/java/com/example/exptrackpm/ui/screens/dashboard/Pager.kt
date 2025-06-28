package com.example.exptrackpm.ui.screens.dashboard

import Transaction
import TransactionType
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import com.example.exptrackpm.ui.screens.transactions.TransactionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Pager(viewModel: TransactionViewModel = viewModel(), navController: NavController) {
    val scope = rememberCoroutineScope()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    Log.d("trn", transactions.toString())
    val now = Calendar.getInstance().time
    val dateRanges = listOf("7d", "30d", "90d", "1Y")
    var selectedRange by remember { mutableStateOf("7d") }
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


    fun groupByDay(transactions: List<Transaction>): Map<String, Float> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return transactions.groupBy { sdf.format(it.date.toDate()) }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() }.toFloat() }
    }

    val expenseGrouped = groupByDay(expenses).toSortedMap()
    val incomeGrouped = groupByDay(incomes).toSortedMap()
    Log.d("grouped",expenseGrouped.toString())
    Log.d("grouped",incomeGrouped.toString())
    //improv
    val allDatesSorted = (expenseGrouped.keys + incomeGrouped.keys).toSortedSet().toList()

    val dateFormat = when (daysBack) {
        in 1..7 -> "EEE"
        in 8..30 -> "MMM dd"
        else -> "MMM"
    }
    val dateToIndex = allDatesSorted.withIndex().associate { it.value to it.index.toFloat() }
    Log.d("index",dateToIndex.toString())



    val allDates = (expenseGrouped.keys + incomeGrouped.keys).toSortedSet()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    val paddedDates = mutableListOf<String>()
    if (allDatesSorted.isNotEmpty()) {
        val firstDate = sdf.parse(allDatesSorted.first())!!
        val lastDate = sdf.parse(allDatesSorted.last())!!

        // Add one day before and after for padding
        calendar.time = firstDate
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        paddedDates.add(sdf.format(calendar.time))

        paddedDates.addAll(allDatesSorted)

        calendar.time = lastDate
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        paddedDates.add(sdf.format(calendar.time))
    } else {
        paddedDates.addAll(allDatesSorted)
    }

    val finalDates = paddedDates.toSortedSet().toList()

//    val expensePoints = allDates.mapIndexed { index, date ->
//        Point(index.toFloat(), expenseGrouped[date] ?: 0f)
//    }
//
//    val incomePoints = allDates.mapIndexed { index, date ->
//        Point(index.toFloat(), incomeGrouped[date] ?: 0f)
//    }
    val displayFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
    val displayLabels = finalDates.map {
        displayFormat.format(sdf.parse(it)!!)
    }
//    val displayLabels = allDatesSorted.map {
//        displayFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)!!)
//    }

    val expensePoints = finalDates.mapIndexed { index, date ->
        Point(index.toFloat(), expenseGrouped[date] ?: 0f)
    }

    val incomePoints = finalDates.mapIndexed { index, date ->
        Point(index.toFloat(), incomeGrouped[date] ?: 0f)
    }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
//    val stepSize = if (allDatesSorted.isNotEmpty()) {
//        screenWidth / (allDatesSorted.size + 1)
//    } else {
//        1.dp // avoid divide-by-zero if somehow empty
//    }
    val stepSize = if (finalDates.isNotEmpty()) {
        screenWidth / (finalDates.size + 1)
    } else {
        1.dp
    }

    val xAxisData = AxisData.Builder()
        .axisStepSize(stepSize)
        .backgroundColor(Color.Transparent)
        //.steps(expensePoints.size - 1)
        .steps(finalDates.size - 1)
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
        .labelData { i -> "%.2f".format(i * (maxY/5f)) }
        .build()

    val pagerState = rememberPagerState(pageCount = { 3 }) // e.g., 3 tabs: Overview, Expenses, Income

    Scaffold(
        topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Analytics") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )

    },
        bottomBar = {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Overview") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Expenses") }
                )
                Tab(
                    selected = pagerState.currentPage == 2,
                    onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                    text = { Text("Income") }
                )
            }
        }) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            TimeRangePicker(options = dateRanges, selectedOption = selectedRange, onOptionSelected = { selectedRange = it })
            Spacer(Modifier.height(16.dp))
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Trend") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Expenses") }
                )
                Tab(
                    selected = pagerState.currentPage == 2,
                    onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                    text = { Text("Income") }
                )
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) { page ->
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())) {
                    when (page) {
                        0 -> {
                            Spacer(Modifier.height(24.dp))
                            BalanceLineChart(transactions,finalDates,displayLabels)
                            Spacer(Modifier.height(24.dp))
                        }
                        1 -> {
                            CategorizedBarChart(transactions = expenses, title = "Expenses by Category", barColor = Color.Red)
                            Spacer(Modifier.height(24.dp))
                            SpendingPieChart(transactions = expenses, title = "Spending Distribution")
                        }
                        2 -> {
                            CategorizedBarChart(transactions = incomes, title = "Income by Category", barColor = Color.Green)
                        }
                    }
                }
            }

        }
    }
}