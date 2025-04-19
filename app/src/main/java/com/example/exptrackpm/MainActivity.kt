package com.example.exptrackpm

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.exptrackpm.theme.ExpTrackPMTheme
import com.example.exptrackpm.ui.screens.addexpense.AddExpenseScreen
import com.example.exptrackpm.ui.screens.expenselist.ExpenseListScreen
import com.example.exptrackpm.ui.screens.login.LoginScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpTrackPMTheme {
                Navigation()
            }
        }
    }


}