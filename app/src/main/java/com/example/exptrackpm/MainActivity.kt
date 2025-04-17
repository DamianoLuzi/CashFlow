package com.example.exptrackpm

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.exptrackpm.theme.ExpTrackPMTheme
import com.example.exptrackpm.ui.screens.addexpense.AddExpenseScreen
import com.example.exptrackpm.ui.screens.expenselist.ExpenseListScreen

class MainActivity : ComponentActivity() {

    private lateinit var btnInsertData: Button
    private lateinit var btnFetchData: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpTrackPMTheme {
                //ExpenseListScreen()
                AddExpenseScreen()
            }
        }
    }


}