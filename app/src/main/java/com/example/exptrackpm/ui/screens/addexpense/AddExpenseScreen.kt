package com.example.exptrackpm.ui.screens.addexpense

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(viewModel: AddExpenseViewModel = viewModel(),navController: NavController) {

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var receiptUrl by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) } // Flag to show loading state
    val categories = listOf("Food", "Travel", "Entertainment", "Other")
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val handleSubmit = {
        if (amount.isNotEmpty() && description.isNotEmpty() && category.isNotEmpty()) {
            val amountDouble = amount.toDoubleOrNull()
            if (amountDouble != null) {
                // Call the ViewModel to add the expense
                scope.launch {
                    isSubmitting = true
                    viewModel.addExpense(amountDouble, description, category, receiptUrl)
                    isSubmitting = false
                    Toast.makeText(context, "Expense added successfully", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

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

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown Arrow"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true } // makes the whole field clickable
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = receiptUrl,
                onValueChange = { receiptUrl = it },
                label = { Text("Receipt URL (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                //viewModel.addExpense(amount.toDouble(), description, category, receiptUrl)
                onClick = {handleSubmit()},
                modifier = Modifier.fillMaxWidth(),
                //enabled = !isSubmitting // Disable the button when submitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Add Expense")
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewAddExpenseScreen() {
    val navController = rememberNavController()
    AddExpenseScreen(navController = navController)
}