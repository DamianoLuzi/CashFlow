package com.example.exptrackpm.ui.screens.budgets

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.exptrackpm.domain.model.Budget
import com.example.exptrackpm.domain.model.Category
import com.example.exptrackpm.ui.screens.categories.CategoryViewModel
import com.example.exptrackpm.ui.screens.transactions.defaultCategories
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(),
    catViewModel: CategoryViewModel = viewModel(),
    navController: NavController
) {
    val userId = FirebaseAuth.getInstance().currentUser!!.uid
    val budgets by viewModel.budgets.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val customCategories by catViewModel.categories.collectAsStateWithLifecycle()
    var category by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var budgetToDelete by remember { mutableStateOf<Budget?>(null) }

    val allCategoriesForDisplay = remember(customCategories) {
        val combinedList = mutableListOf<Category>()
        defaultCategories.forEach { defaultCat ->
            combinedList.add(defaultCat)
        }
        customCategories.forEach { customCat ->
            val existingIndex = combinedList.indexOfFirst { it.name == customCat.name }
            if (existingIndex != -1) {
                combinedList[existingIndex] = customCat
            } else {
                combinedList.add(customCat)
            }
        }
        combinedList.sortedBy { it.name }
    }

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

    Scaffold(
        topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Budgets") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        }) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)
        ) {

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown Arrow")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    allCategoriesForDisplay.forEach {
                            cat ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = cat.icon ?: "")
                                    Text(text = cat.name)
                                }
                            },
                            onClick = {
                                category = cat.name
                                expanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("➕ Add Custom Category") },
                        onClick = {
                            expanded = false
                            navController.navigate("addcategory")
                        }
                    )
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Limit Amount") },
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
                    BudgetItem(budget) {
                        budgetToDelete = budget
                        showDeleteDialog = true
                    }
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Budget") },
            text = { Text("Are you sure you want to delete the budget for '${budgetToDelete?.category}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        budgetToDelete?.let {
                            viewModel.deleteBudget(it.id!!)
                        }
                        showDeleteDialog = false
                        budgetToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    budgetToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BudgetItem(budget: Budget, onDeleteClick: (Budget) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(budget.category, style = MaterialTheme.typography.titleMedium)
                Text("Limit: ${budget.amount} ${budget.currency}", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = { onDeleteClick(budget) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Budget")
            }
        }
    }
}

@Composable
fun BI(budget: Budget) {
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
            Text("${budget.amount}")
            Text("${budget.currency}")
        }
    }
}
