package com.example.exptrackpm.ui.screens.notifications


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

@Composable
fun NotificationPermissionScreen(navController: NavController) {
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
            Log.d("Permission", "POST_NOTIFICATIONS permission granted.")
            navController.navigate("overview") {
                popUpTo("notification_permission_screen") { inclusive = true }
            }
        } else {
            Toast.makeText(context, "Notification permission denied. You won't receive budget alerts.", Toast.LENGTH_LONG).show()
            Log.d("Permission", "POST_NOTIFICATIONS permission denied.")
            navController.navigate("overview") {
                popUpTo("notification_permission_screen") { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("Permission", "POST_NOTIFICATIONS permission already granted. Navigating to overview.")
                    navController.navigate("overview") {
                        popUpTo("notification_permission_screen") { inclusive = true }
                    }
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("Permission", "POST_NOTIFICATIONS not required for API < 33. Navigating to overview.")
            navController.navigate("overview") {
                popUpTo("notification_permission_screen") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to ExpTrackPM! We need your permission to send you budget alerts.")
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    Toast.makeText(context, "Notification permission is already granted.", Toast.LENGTH_SHORT).show()
                    navController.navigate("overview") { popUpTo("notification_permission_screen") { inclusive = true } }
                }
            } else {
                Toast.makeText(context, "Notifications are enabled by default on this Android version.", Toast.LENGTH_SHORT).show()
                navController.navigate("overview") { popUpTo("notification_permission_screen") { inclusive = true } }
            }
        }) {
            Text("Grant Notification Permission")
        }
    }
}