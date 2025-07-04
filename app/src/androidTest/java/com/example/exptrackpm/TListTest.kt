package com.example.exptrackpm

import Transaction
import TransactionType
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.exptrackpm.ui.screens.transactions.TransactionFilterBar
import com.example.exptrackpm.ui.screens.transactions.TransactionItem
import com.example.exptrackpm.ui.screens.transactions.TransactionList
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class TransactionListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private val fakeTransactions = listOf(
        Transaction(id = "1", amount = 50.0, type = TransactionType.EXPENSE, category = "Food", description = "Lunch", date = Timestamp.now()),
        Transaction(id = "2", amount = 100.0, type = TransactionType.INCOME, category = "Salary", description = "Paycheck", date = Timestamp.now())
    )
    @Test
    fun transactionList_displaysTransactionsCorrectly() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            TransactionList(
                transactions = fakeTransactions,
                navController = navController,
                currentCurrencySymbol = "€"
            )
        }
        composeTestRule.onNodeWithText("-€50.00").assertExists()
        composeTestRule.onNodeWithText("+€100.00").assertExists()
        composeTestRule.onNodeWithText("Lunch").assertExists()
        composeTestRule.onNodeWithText("Paycheck").assertExists()
    }

    @Test
    fun filterBar_filtersCorrectly() {
        var selectedFilter: TransactionType? = null

        composeTestRule.setContent {
            TransactionFilterBar(
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it }
            )
        }

        composeTestRule.onNodeWithText("Income").performClick()
        assert(selectedFilter == TransactionType.INCOME)
    }

    @Test
    fun transactionItem_click_navigatesToDetails() {
        var clicked = false
        composeTestRule.setContent {
            TransactionItem(
                transaction = fakeTransactions[0],
                onClick = { clicked = true },
                currentCurrencySymbol = "$"
            )
        }

        composeTestRule.onNodeWithText("-$50.00").performClick()
        assert(clicked)
    }
}
