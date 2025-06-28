package com.example.exptrackpm.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.exptrackpm.auth.SessionManager
import com.example.exptrackpm.domain.model.NotificationPreferences
import com.example.exptrackpm.theme.ExpTrackPMTheme

val currencyOptions = listOf("EUR", "USD", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "INR", "BRL")
val themeOptions = listOf("System default", "Light", "Dark")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    viewModel: ProfileViewModel = viewModel(),
    navController: NavController
) {
    val user by viewModel.user.collectAsState()
    Log.d("uid","profile")
    Log.d("uid",user.toString())
    val loading by viewModel.loading.collectAsState()
    var email by remember { mutableStateOf("") }
    var id by remember { mutableStateOf("") }
    var currencyExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    var displayName by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var currency by remember(user) { mutableStateOf(user?.currency ?: "EUR") }
    var theme by remember(user) { mutableStateOf(user?.theme ?: "System default") }
    var overBudgetAlerts by remember(user) { mutableStateOf(user?.notificationPreferences?.overBudgetAlerts ?: false) }
    var weeklySummaries by remember(user) { mutableStateOf(user?.notificationPreferences?.spendingSummaries ?: false) }
    val hasModifications by remember(user, displayName, currency, theme, overBudgetAlerts, weeklySummaries) {
        derivedStateOf {
            user?.let { originalUser ->
                val originalPrefs = originalUser.notificationPreferences
                !(displayName == originalUser.displayName &&
                        currency == originalUser.currency &&
                        theme == originalUser.theme &&
                        overBudgetAlerts == originalPrefs.overBudgetAlerts &&
                        weeklySummaries == originalPrefs.spendingSummaries)
            } ?: false // If user is null, there are no modifications to save
        }
    }
    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }

    LaunchedEffect(user) {
        user?.let {
            id = it.id
            displayName = it.displayName ?: ""
            email = it.email
            currency = it.currency
            theme = it.theme
        }
    }

        if (user == null || loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { SessionManager.logout() }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding() // Ensures padding for system navigation bars
            ) {
                Button(
                    onClick = {
                        user?.let { originalUser ->
                            val updatedPrefs = NotificationPreferences(
                                overBudgetAlerts = overBudgetAlerts,
                                spendingSummaries = weeklySummaries
                            )
                            val updatedUser = originalUser.copy(
                                displayName = displayName,
                                currency = currency,
                                theme = theme,
                                notificationPreferences = updatedPrefs
                            )
                            viewModel.updateUser(updatedUser)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasModifications
                ) {
                    Text("Save Changes")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { SessionManager.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out")
                }
            }
        }
    ) { paddingValues ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("User not loaded or not logged in.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply Scaffold's padding
                    .padding(horizontal = 16.dp) // Add specific horizontal padding for content
                    .verticalScroll(rememberScrollState()) // Make content scrollable
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = user!!.email,
                    onValueChange = { /* Email is read-only */ },
                    label = { Text("Email") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = id ,
                    onValueChange = {  },
                    label = { Text("ID") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { /* Read-only for direct input */ },
                        label = { Text("Currency") },
                        readOnly = true, // Make it read-only
                        trailingIcon = {
                            IconButton(onClick = { currencyExpanded = !currencyExpanded }) {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Currency")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currencyExpanded = true } // Make the whole field clickable
                    )
                    DropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false },
                        modifier = Modifier.fillMaxWidth() // Adjust width to match TextField
                    ) {
                        currencyOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    currency = option
                                    currencyExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = theme,
                        onValueChange = { /* Read-only for direct input */ },
                        label = { Text("Theme") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { themeExpanded = !themeExpanded }) {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Theme")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { themeExpanded = true }
                    )
                    DropdownMenu(
                        expanded = themeExpanded,
                        onDismissRequest = { themeExpanded = false },
                        modifier = Modifier.fillMaxWidth() // Adjust width to match TextField
                    ) {
                        themeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    theme = option
                                    themeExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notification Preferences", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                NotificationPreferenceItem(
                    label = "Over Budget Alerts",
                    checked = overBudgetAlerts,
                    onCheckedChange = { overBudgetAlerts = it }
                )
                NotificationPreferenceItem(
                    label = "Weekly Summaries",
                    checked = weeklySummaries,
                    onCheckedChange = {
                        weeklySummaries = it
                        viewModel.setSpendingSummaryPreference(true)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("setbudget") },
                    modifier = Modifier.padding(40.dp).fillMaxWidth()
                ) {
                    Text("Manage budgets")
                }

                Spacer(modifier = Modifier.height(32.dp)) // Spacer before the bottom bar area
            }
        }
    }
}

@Composable
fun NotificationPreferenceItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ExpTrackPMTheme {
        val navController = rememberNavController()
        Profile(navController = navController)
    }
}