package com.example.exptrackpm.ui.screens.addexpense

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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

@Composable
fun AddExpenseScreen(viewModel: AddExpenseViewModel = viewModel(),navController: NavController) {

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var receiptUrl by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) } // Flag to show loading state

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

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Field
            TextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Description Field
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            // Category Field
            TextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )

            // Receipt URL (optional) Field
            TextField(
                value = receiptUrl,
                onValueChange = { receiptUrl = it },
                label = { Text("Receipt URL (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Submit Button
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


//package com.example.exptrackpm.ui.screens.addexpense
//
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.Button
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.exptrackpm.ui.screens.expenselist.ExpenseItem
//import com.example.exptrackpm.ui.screens.expenselist.ExpenseListViewModel
//
//@Composable
//fun AddExpenseScreen(viewModel: AddExpenseViewModel = viewModel()) {
//    Scaffold { paddingValues ->
//        LazyColumn(
//            contentPadding = PaddingValues(16.dp),
//            modifier = Modifier.padding(paddingValues),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            item {
//                Button(onClick = {viewModel.addExpense()}) {
//                    Text(text="Add new expense")
//                }
//            }
//        }
//    }
//
//}