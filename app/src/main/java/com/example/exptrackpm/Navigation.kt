package com.example.exptrackpm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.exptrackpm.auth.SessionManager
import com.example.exptrackpm.ui.screens.addexpense.AddExpenseScreen
import com.example.exptrackpm.ui.screens.expenselist.ExpenseListScreen
import com.example.exptrackpm.ui.screens.login.LoginScreen

// TODO: import and pass AuthViewModel as a param to each page
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val isUserLoggedIn by SessionManager.isUserLoggedIn.collectAsState()

    val startDestination = if (isUserLoggedIn) "expenselist" else "login"
    NavHost(navController= navController, startDestination = startDestination, builder = {
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