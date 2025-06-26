package com.example.exptrackpm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.exptrackpm.theme.ExpTrackPMTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpTrackPMTheme {
                Navigation()
            }
        }
    }
//    override val workManagerConfiguration: ComponentProvider.Configuration
//        get() = Configuration.Builder()
//            .setMinimumLoggingLevel(Log.DEBUG)
//            .build()
}