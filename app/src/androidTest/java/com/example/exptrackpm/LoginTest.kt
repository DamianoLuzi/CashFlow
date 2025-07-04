
import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.exptrackpm.ui.screens.login.LoginScreen
import org.junit.Rule
import org.junit.Test


class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysEmailField() {

        composeTestRule.setContent {
            val navController = rememberNavController()
            LoginScreen(navController = navController)
        }

        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don’t have an account? Sign Up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("mypassword")
        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()

    }


    @Test
    fun loginScreen_signUpButton_navigates() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "login"
            ) {
                composable("login") {
                    LoginScreen(navController)
                }
                composable("signup") {
                    Text("Signup Screen")
                }
                composable("overview") {
                    Text("Overview Screen")
                }
            }
        }

        composeTestRule.onNodeWithText("Don’t have an account? Sign Up")
            .assertIsDisplayed()
            .performClick()
    }


    @Test
    fun loginScreen_inputAndClickSignIn() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            LoginScreen(navController = navController)
        }

        composeTestRule.onNodeWithText("Email").performTextInput("user@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Sign In").performClick()
    }

    @Test
    fun loginScreen_inputAndClickSignIn_showsLoading() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val navController = TestNavHostController(context)

        composeTestRule.setContent {
            LoginScreen(navController)
        }

        composeTestRule.onNodeWithText("Email").performTextInput("user@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        composeTestRule.onNodeWithText("Sign In")
            .performClick()
        composeTestRule.onNode(hasAnyChild(hasTestTag("CircularProgressIndicator")))
            .assertExists()
    }




}



