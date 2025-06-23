package com.example.exptrackpm.ui.screens.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.exptrackpm.domain.model.Budget

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(),
    navController: NavController
) {

//            (
//    userId: String,
//    viewModel: BudgetViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
//        factory = BudgetViewModelFactory() // Inject UserRepository here as well
//    )
//) {
    val userId = ""
    val budgets by viewModel.budgets.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var category by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.loadBudgets(userId)
    }

    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        // Budget input
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (category.isNotBlank() && amount > 0) {
                    val budget = Budget(
                        category = category,
                        amount = amount,
                        userId = userId
                    )
                    viewModel.saveBudget(budget)
                    category = ""
                    amountText = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Budget")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Your Budgets", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(budgets) { budget ->
                BudgetItem(budget)
            }
        }
    }
}

@Composable
fun BudgetItem(budget: Budget) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(budget.category)
            Text("${budget.amount} ${budget.currency}")
        }
    }
}
