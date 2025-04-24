package com.example.exptrackpm.ui.screens.transactions

import Transaction
import TransactionType
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.exptrackpm.theme.ExpTrackPMTheme
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = viewModel(),
    navController: NavController
) {
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsState()
    Log.d("transactions", transactions.toString())
    Log.d("transactions-filter", filter.toString())
    Column {
        TransactionFilterBar(selectedFilter = filter, onFilterChange = viewModel::setFilter)
        TransactionList(transactions)
    }
}


@Composable
fun TransactionFilterBar(
    selectedFilter: TransactionType?,
    onFilterChange: (TransactionType?) -> Unit
) {
    val filters = listOf(null to "All", TransactionType.INCOME to "Income", TransactionType.EXPENSE to "Expenses")

    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        filters.forEach { (type, label) ->
            Button(
                onClick = { onFilterChange(type) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFilter == type) Color.Blue else Color.LightGray
                )
            ) {
                Text(label)
            }
        }
    }
}

@Composable
fun TransactionList(transactions: List<Transaction>) {
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(transactions) { txn ->
            TransactionItem(txn)
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val isIncome = transaction.type == TransactionType.INCOME
    val formattedDate = remember(transaction.date) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(transaction.date.toDate())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(
                    text = "${if (isIncome) "+" else "-"}$${transaction.amount}",
                    color = if (isIncome) Color(0xFF2E7D32) else Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(transaction.category, fontSize = 16.sp)
            }
            Text(formattedDate, fontSize = 14.sp, color = Color.Gray)
            if (transaction.description.isNotBlank()) {
                Text(transaction.description, fontSize = 15.sp)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TransactionScreenPreview() {
    val navController = rememberNavController()
    ExpTrackPMTheme {
        TransactionListScreen(navController = navController)
    }
}


