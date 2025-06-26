package com.example.exptrackpm.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.exptrackpm.auth.AuthResponse
import com.example.exptrackpm.auth.AuthenticationManager
import com.example.exptrackpm.theme.ExpTrackPMTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val authManager = remember { AuthenticationManager(context) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome Back", style = MaterialTheme.typography.headlineMedium)
        Text("Please sign in to continue", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                errorMessage = null

                authManager
                    .logInWithEmail(email, password)
                    .onEach { response ->
                        isLoading = false
                        when (response) {
                            is AuthResponse.Success -> navController.navigate("overview") {
                                popUpTo(0) // removes everything from back stack
                                launchSingleTop = true
                            }
                            is AuthResponse.Error -> errorMessage = response.message
                        }
                    }
                    .launchIn(coroutineScope)
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Sign In")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("signup")
        }) {
            Text("Don’t have an account? Sign Up")
        }

    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    ExpTrackPMTheme {
    LoginScreen(navController = navController)
    }
}

