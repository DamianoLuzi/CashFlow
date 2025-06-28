package com.example.exptrackpm.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.exptrackpm.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

object SessionManager {
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _isUserLoggedIn = MutableStateFlow(firebaseAuth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> get() = _isUserLoggedIn
    private var logoutJob: Job? = null
    init {
        firebaseAuth.addAuthStateListener {
            _isUserLoggedIn.value = it.currentUser != null
            if (it.currentUser != null) {
                startAutoLogoutTimer(30 * 60 * 1000) // user stays logged in for 30 minutes
            } else {
                //cancelAutoLogoutTimer()
            }
        }
    }

    fun startAutoLogoutTimer(timeoutMillis: Long) {
        logoutJob?.cancel()
        logoutJob = CoroutineScope(Dispatchers.Default).launch {
            delay(timeoutMillis)
            firebaseAuth.signOut()
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}

class AuthenticationManager(private val context: Context) {
    private val auth = Firebase.auth

    fun createAccountWithEmail(email:String, password: String): Flow<AuthResponse> = callbackFlow {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d("signup", "createUserWithEmail:success")
                    val user = auth.currentUser
                    val profile = User(
                        displayName =  user!!.displayName ?: user.email?.substringBefore("@") ?: "New User",
                        //avatarUrl = user.photoUrl?.toString(),
                        email = user.email ?: ""
                    )
                    Firebase.firestore.collection("users")
                        .document(user.uid)
                        .set(profile)
                        .addOnSuccessListener {
                            trySend(AuthResponse.Success)
                        }
                        .addOnFailureListener {
                            trySend(AuthResponse.Error("Failed to save profile: ${it.message}"))
                        }
                    trySend(AuthResponse.Success)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("signup", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    trySend(AuthResponse.Error(message=task.exception?.message ?: ""))
                }
            }
        awaitClose()
    }

    fun logInWithEmail (email:String, password: String) : Flow<AuthResponse> = callbackFlow {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResponse.Success)
                } else {
                    trySend(AuthResponse.Error(message=task.exception?.message ?: ""))
                }
            }

        awaitClose ()
    }
}

interface AuthResponse {
    data object Success: AuthResponse
    data class Error(val message: String): AuthResponse

}