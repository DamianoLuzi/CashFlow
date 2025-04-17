package com.example.exptrackpm

import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.exptrackpm.ui.screens.addexpense.AddExpenseScreen
import com.example.exptrackpm.ui.screens.expenselist.ExpenseListScreen
import com.example.exptrackpm.ui.screens.login.LoginScreen

// TODO: import and pass AuthViewModel as a param to each page
@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController= navController, startDestination = "login", builder = {
        composable("login") {
            LoginScreen(navController)
        }
        composable("addexpense") {
          AddExpenseScreen(navController = navController)
        }
        composable("expenselist") {
            ExpenseListScreen(navController = navController)
        }

    })
}