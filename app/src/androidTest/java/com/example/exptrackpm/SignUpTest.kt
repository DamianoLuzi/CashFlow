package com.example.exptrackpm

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.exptrackpm.ui.screens.signup.SignUpScreen
import org.junit.Rule
import org.junit.Test


class SignUpTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signUpScreen_showsErrorWhenPasswordsDoNotMatch() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        composeTestRule.setContent {
            SignUpScreen(navController)
        }
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("password321")
        composeTestRule.onNodeWithText("Create Account").performClick()
        composeTestRule.onNodeWithText("Passwords do not match").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_showsErrorForInvalidEmailOrPassword() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        composeTestRule.setContent {
            SignUpScreen(navController)
        }

        composeTestRule.onNodeWithText("Email").performTextInput("")
        composeTestRule.onNodeWithText("Password").performTextInput("123")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("123")
        composeTestRule.onNodeWithText("Create Account").performClick()
        composeTestRule.onNodeWithText("Enter valid email and password (min 6 chars)")
            .assertIsDisplayed()
    }


}