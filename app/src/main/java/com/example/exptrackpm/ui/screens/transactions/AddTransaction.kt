package com.example.exptrackpm.ui.screens.transactions

import TransactionType
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.exptrackpm.domain.model.Category
import com.example.exptrackpm.theme.ExpTrackPMTheme
import com.example.exptrackpm.ui.screens.categories.CategoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    trnViewModel: TransactionViewModel = viewModel(),
    catViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var receiptUrl by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val customCategories by catViewModel.categories.collectAsStateWithLifecycle()
//    val categories = listOf(
//        "Food",
//        "Travel",
//        "Salary",
//        "Work",
//        "Entertainment",
//        "Shopping",
//        "Transfers",
//        "General",
//        "Services",
//        "Groceries",
//        "Other")
    val categories = listOf(
        Category(name = "Food", icon = "🍔"),
        Category(name = "Travel", icon = "✈️"),
        Category(name = "Salary", icon = "💰"),
        Category(name = "Work", icon = "💼"),
        Category(name = "Entertainment", icon = "🎬"),
        Category(name = "Shopping", icon = "🛍️"),
        Category(name = "Transfers", icon = "💸"),
        Category(name = "General", icon = "🏠"),
        Category(name = "Services", icon = "🔧"),
        Category(name = "Groceries", icon = "🛒"),
        Category(name = "Other", icon = "🤷"),
        // Add more as needed
    )

//    LaunchedEffect(Unit) {
//        viewModel.getUserCategories { fetchedCategories ->
//            customCategories.value = fetchedCategories
//        }
//    }

//    val allCategories = listOf(
//        "Food", "Travel", "Salary", "Entertainment", "Shopping", "Transfers", "General"
//    ) + customCategories.value

    val allCategories = remember(customCategories) {
        (categories + customCategories.map { it.name })
            .distinct()
    }

    val allCategoriesForDisplay = remember(customCategories) {
        val combinedList = mutableListOf<Category>()

        // Add default categories first
        categories.forEach { defaultCat ->
            combinedList.add(defaultCat)
        }

        // Add custom categories, preferring custom ones if names overlap
        customCategories.forEach { customCat ->
            val existingIndex = combinedList.indexOfFirst { it.name == customCat.name }
            if (existingIndex != -1) {
                // If a custom category with the same name exists, replace the default one
                combinedList[existingIndex] = customCat
            } else {
                // Otherwise, add the custom category
                combinedList.add(customCat)
            }
        }
        combinedList.sortedBy { it.name } // Sort by name for display
    }



    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Transaction Type Toggle
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(TransactionType.EXPENSE, TransactionType.INCOME).forEach { type ->
                    FilterChip(
                        selected = transactionType == type,
                        onClick = { transactionType = type },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            // Dropdown for Category
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
                            cat -> // Renamed `option` to `cat` for clarity
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between emoji and text
                                ) {
                                    Text(text = cat.icon ?: "") // Display emoji
                                    Text(text = cat.name)        // Display category name
                                }
                            },
                            onClick = {
                                category = cat.name // Save only the name to the transaction
                                expanded = false
                            }
                        )
//                        option ->
//                        DropdownMenuItem(
//                            text = { Text(option) },
//                            onClick = {
//                                category = option
//                                expanded = false
//                            }
//                        )
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
                value = receiptUrl,
                onValueChange = { receiptUrl = it },
                label = { Text("Receipt URL (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (amountDouble != null && description.isNotBlank() && category.isNotBlank()) {
                        isSubmitting = true
                        scope.launch {
                            trnViewModel.addTransaction(
                                amount = amountDouble,
                                description = description,
                                category = category,
                                type = transactionType,
                                receiptUrl = if (receiptUrl.isBlank()) null else receiptUrl
                            )
                            isSubmitting = false
                            Toast.makeText(context, "Transaction added", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    } else {
                        Toast.makeText(context, "Please fill out all required fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Save")
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    val navController = rememberNavController()
    ExpTrackPMTheme {
       AddTransactionScreen(navController = navController)
    }
}
