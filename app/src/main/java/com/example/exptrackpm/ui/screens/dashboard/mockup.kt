package com.example.exptrackpm.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockProfile(
    viewModel: ProfileViewModel = viewModel(),
    navController: NavController
) {
    val user by viewModel.user.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var email by remember { mutableStateOf("") }
    var id by remember { mutableStateOf("") }
    var currencyExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    var currency by remember(user) { mutableStateOf(user?.currency ?: "EUR") }
    var theme by remember(user) { mutableStateOf(user?.theme ?: "System default") }
    var overBudgetAlerts by remember(user) { mutableStateOf(user?.notificationPreferences?.overBudgetAlerts ?: false) }
    var weeklySummaries by remember(user) { mutableStateOf(user?.notificationPreferences?.spendingSummaries ?: false) }

    val hasModifications by remember(user, currency, theme, overBudgetAlerts, weeklySummaries) {
        derivedStateOf {
            user?.let { original ->
                val prefs = original.notificationPreferences
                currency != original.currency ||
                        theme != original.theme ||
                        overBudgetAlerts != prefs.overBudgetAlerts ||
                        weeklySummaries != prefs.spendingSummaries
            } ?: false
        }
    }

    LaunchedEffect(Unit) { viewModel.loadUser() }

    LaunchedEffect(user) {
        user?.let {
            id = it.id
            email = it.email
            currency = it.currency
            theme = it.theme
        }
    }

    if (loading || user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", style = MaterialTheme.typography.titleLarge) },
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
        floatingActionButton = {
            if (hasModifications) {
                Button(
                    onClick = {
                        user?.let {
                            val updatedPrefs = NotificationPreferences(
                                overBudgetAlerts = overBudgetAlerts,
                                spendingSummaries = weeklySummaries
                            )
                            val updatedUser = it.copy(
                                currency = currency,
                                theme = theme,
                                notificationPreferences = updatedPrefs
                            )
                            viewModel.updateUser(updatedUser)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Save Changes")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Account", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(Modifier.padding(16.dp)) {
                    MockProfileField("Email", email)

                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Preferences", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(Modifier.padding(16.dp)) {
                    MockDropdownField(
                        label = "Currency",
                        value = currency,
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = it },
                        options = currencyOptions,
                        onSelect = { currency = it }
                    )

                    Spacer(Modifier.height(16.dp))

                    MockDropdownField(
                        label = "Theme",
                        value = theme,
                        expanded = themeExpanded,
                        onExpandedChange = { themeExpanded = it },
                        options = themeOptions,
                        onSelect = { theme = it }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Notifications", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(Modifier.padding(16.dp)) {
                    NotificationPreferenceItem("Over Budget Alerts", overBudgetAlerts) {
                        overBudgetAlerts = it
                    }
                    NotificationPreferenceItem("Weekly Summaries", weeklySummaries) {
                        weeklySummaries = it
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("setbudget") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                enabled = true
            ) {
                Text("Manage Budgets")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun MockProfileField(label: String, value: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun MockDropdownField(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { onExpandedChange(!expanded) }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(true) }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelect(it)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MocknPreview() {
    val navController = rememberNavController()
    ExpTrackPMTheme {
        MockProfile(navController = navController)
    }
}

