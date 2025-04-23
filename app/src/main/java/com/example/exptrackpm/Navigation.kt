package com.example.exptrackpm

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.exptrackpm.auth.SessionManager
import com.example.exptrackpm.ui.screens.addexpense.AddExpenseScreen
import com.example.exptrackpm.ui.screens.dashboard.Dashboard
import com.example.exptrackpm.ui.screens.expenselist.ExpenseListScreen
import com.example.exptrackpm.ui.screens.login.LoginScreen
import com.example.exptrackpm.ui.screens.signup.SignUpScreen

enum class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    Dashboard("dashboard", "Dashboard", Icons.Default.Home),
    Expenses("expenselist", "Expenses", Icons.Default.List),
    Add("addexpense", "Add", Icons.Default.AddCircle),
}


// TODO: import and pass AuthViewModel as a param to each page
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val isUserLoggedIn by SessionManager.isUserLoggedIn.collectAsState()
    Log.d("auth", "user is logged in: ${isUserLoggedIn}")
    val startDestination = if (isUserLoggedIn) "dashboard" else "login"

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                BottomNavItem.entries.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    // Avoid building up a big backstack
                                    popUpTo(startDestination) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) {
        NavHost(navController= navController, startDestination = startDestination, builder = {
            composable("login") {
                LoginScreen(navController)
            }
            composable("signup") {
                SignUpScreen(navController)
            }
            composable("addexpense") {
                AddExpenseScreen(navController = navController)
            }
            composable("expenselist") {
                ExpenseListScreen(navController = navController)
            }
            composable("dashboard") {
               Dashboard(navController = navController)
            }
        })
    }
}