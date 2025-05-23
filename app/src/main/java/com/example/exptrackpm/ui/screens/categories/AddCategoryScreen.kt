package com.example.exptrackpm.ui.screens.categories

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.exptrackpm.theme.ExpTrackPMTheme


@Composable
fun AddCategoryScreen(
    navController: NavController,
    viewModel: CategoryViewModel = viewModel()
) {
    var categoryName by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    val context = LocalContext.current

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
        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (categoryName.isNotBlank()) {
                    viewModel.addCategory(categoryName, icon, color)
                    Toast.makeText(context, "Category added", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Save Category")
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
