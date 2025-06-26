package com.example.exptrackpm

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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
import com.example.exptrackpm.ui.screens.budgets.BudgetScreen
import com.example.exptrackpm.ui.screens.categories.AddCategoryScreen
import com.example.exptrackpm.ui.screens.dashboard.Overview
import com.example.exptrackpm.ui.screens.dashboard.Pager
import com.example.exptrackpm.ui.screens.login.LoginScreen
import com.example.exptrackpm.ui.screens.profile.Profile
import com.example.exptrackpm.ui.screens.signup.SignUpScreen
import com.example.exptrackpm.ui.screens.transactions.AddTransactionScreen
import com.example.exptrackpm.ui.screens.transactions.TransactionDetailsScreen
import com.example.exptrackpm.ui.screens.transactions.TransactionListScreen

enum class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    Overview("overview", "Overview", Icons.Default.Home),
    Add("addtransaction", "Add", Icons.Default.AddCircle),
    Transactions("transactionlist", "Transactions", Icons.Default.List),
    Profile("profile", "Account",Icons.Default.AccountCircle)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val isUserLoggedIn by SessionManager.isUserLoggedIn.collectAsState()
    Log.d("auth", "user is logged in: ${isUserLoggedIn}")
    val startDestination = if (isUserLoggedIn) "overview" else "login"

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                BottomNavItem.entries.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        enabled = isUserLoggedIn,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
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
            composable("addtransaction") {
                AddTransactionScreen(navController = navController)
            }
            composable("transactionlist") {
              TransactionListScreen(navController = navController)
            }
            composable("overview") {
                Overview(navController = navController)
            }
            composable("addcategory") {
                AddCategoryScreen(navController = navController)
            }
            composable("pager") {
                Pager(navController = navController)
            }
            composable("transactionDetails/{transactionId}") { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
                TransactionDetailsScreen(transactionId = transactionId, navController = navController)
            }
            composable("profile") {
                Profile(navController = navController)
            }
            composable("setbudget") {
                BudgetScreen(navController = navController)
            }

        })
    }
}