package com.example.exptrackpm

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
import com.example.exptrackpm.theme.ExpTrackPMTheme
import com.example.exptrackpm.ui.screens.notifications.NotificationHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create notification channels here, once for the app's lifecycle
        NotificationHelper.createNotificationChannels(applicationContext)
        setContent {
            ExpTrackPMTheme {
                Navigation()
            }
        }
    }
}




@SuppressLint("ContextCastToActivity")
@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    val activity = LocalContext.current as? ComponentActivity
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted! You can now send notifications.
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
            Log.d("Permission", "POST_NOTIFICATIONS permission granted.")
            // If you deferred showing a notification, you could now attempt it.
        } else {
            // Permission denied.
            Toast.makeText(
                context,
                "Notification permission denied. You won't receive budget alerts.",
                Toast.LENGTH_LONG
            ).show()
            Log.d("Permission", "POST_NOTIFICATIONS permission denied.")
            // Consider showing a custom dialog here explaining the importance
            // and guiding the user to app settings if they've permanently denied.
        }
    }

    LaunchedEffect(Unit) { // This block runs once when the composable enters the composition
        // Only ask for permission on Android 13 (API 33) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted, nothing to do here for now
                    Log.d("Permission", "POST_NOTIFICATIONS permission already granted.")
                }
                // Check if we should show a rationale (user denied once, but not permanently)
                // This 'shouldShowRequestPermissionRationale' must be called from an Activity.
                // If you use LocalContext.current.packageManager.shouldShowRequestPermissionRationale,
                // it might not work as expected or requires a specific activity reference.
                // A better way in Composables is to pass the Activity or use a custom ViewModel
                // that handles this by injecting the Activity. For simplicity here, we'll
                // directly launch the request, but in a real app, a dialog is recommended.
                // activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == true -> {
                //    // Show an explanation dialog here, then call requestPermissionLauncher.launch(...)
                // }
                else -> {
                    // Directly request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
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
                    Toast.makeText(
                        context,
                        "Notification permission is already granted.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    "Notifications are enabled by default on this Android version.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }) {
            Text("Request Notification Permission")
        }
    }
}