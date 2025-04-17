package com.example.exptrackpm.ui.screens.expenselist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.exptrackpm.domain.model.Expense


@Composable
fun ExpenseListScreen(
    viewModel: ExpenseListViewModel = viewModel(),
    navController: NavController
) {
    val expenses by viewModel.expenseList.collectAsStateWithLifecycle()

    Scaffold { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(expenses) { expense ->
                ExpenseItem(expense)
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense) {
    val formattedDate = remember(expense.date) {
        java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
            .format(expense.date.toDate())
    }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(6.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$${expense.amount}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = expense.category.replaceFirstChar { it.uppercase() },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = formattedDate,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (expense.description.isNotBlank()) {
                Text(
                    text = expense.description,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            expense.receiptUrl?.let { url ->
                Text(
                    text = "Receipt Available",
                    color = androidx.compose.ui.graphics.Color.Blue,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}


//@Composable
//fun ExpenseListScreen(
//    viewModel: ExpenseListViewModel = viewModel())  {
//    val expenses by viewModel.expenseList.collectAsStateWithLifecycle()
//
//    Scaffold { paddingValues ->
//
//        LazyColumn(
//            contentPadding = PaddingValues(20.dp),
//            modifier = Modifier.padding(paddingValues),
//            verticalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            items(expenses) { expense ->
//                Row(
//                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        text = expense.amount.toString(),
//                        fontSize = 28.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Text(
//                        text = expense.category,
//                        fontSize = 28.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Text(
//                        text = expense.date.toDate().toString(),
//                        fontSize = 28.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//
//            }
//        }
//
//    }
//
//
//}