package com.example.exptrackpm.ui.screens.categories

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.exptrackpm.domain.model.Category
import com.example.exptrackpm.theme.ExpTrackPMTheme


@Composable
fun AddCategoryScreen(
    navController: NavController,
    viewModel: CategoryViewModel = viewModel()
) {
    var categoryName by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    val customCategories by viewModel.categories.collectAsStateWithLifecycle()
    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = categoryName,
            onValueChange = { categoryName = it },
            label = { Text("Category Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = icon,
            onValueChange = { icon = it },
            label = { Text("Icon (emoji)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (categoryName.isNotBlank()) {
                    viewModel.addCategory(categoryName, icon)
                    Toast.makeText(context, "Category added", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Save Category")
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(customCategories) { category ->
                CategoryItem(category = category) {
                    categoryToDelete = category
                    showDeleteDialog = true
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete the category '${categoryToDelete?.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        categoryToDelete?.let {
                            it.id?.let { id -> viewModel.deleteCategory(id) }
                        }
                        showDeleteDialog = false
                        categoryToDelete = null
                        Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    categoryToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CategoryItem(category: Category, onDeleteClick: (Category) -> Unit) {
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
            Text(text = "${category.icon ?: ""} ${category.name}")
            IconButton(onClick = { onDeleteClick(category) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Category")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AddCategoryScreenPreview() {
    val navController = rememberNavController()
    ExpTrackPMTheme {
        AddCategoryScreen(navController = navController)
    }
}
