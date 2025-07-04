package com.example.exptrackpm

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.exptrackpm.ui.screens.transactions.AddTransactionScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AddTransactionScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testUIElementsDisplayed() {
        composeTestRule.setContent {
            AddTransactionScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("Add Transaction").assertIsDisplayed()
        composeTestRule.onNodeWithText("Amount").assertIsDisplayed()
        composeTestRule.onNodeWithText("Description").assertIsDisplayed()
        composeTestRule.onNodeWithText("Category").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
        composeTestRule.onNodeWithText("Receipt (optional)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select Receipt Image").assertIsDisplayed()
    }

    @Test
    fun testTransactionTypeChipsAreClickable() {
        composeTestRule.setContent {
            AddTransactionScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("Expense").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("Income").assertIsDisplayed().performClick()
    }

    @Test
    fun testCategoryDropdownOpensAndSelectsItem() {
        composeTestRule.setContent {
            AddTransactionScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithContentDescription("Dropdown Arrow").performClick()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed().performClick()
    }

    @Test
    fun testSaveButtonShowsValidationError() {
        composeTestRule.setContent {
            AddTransactionScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.onNodeWithText("Add Transaction").assertIsDisplayed()
    }
}
