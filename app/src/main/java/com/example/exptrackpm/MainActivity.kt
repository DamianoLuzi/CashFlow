package com.example.exptrackpm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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