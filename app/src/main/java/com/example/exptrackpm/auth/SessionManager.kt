package com.example.exptrackpm.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _isUserLoggedIn = MutableStateFlow(firebaseAuth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> get() = _isUserLoggedIn

    init {
        firebaseAuth.addAuthStateListener {
            _isUserLoggedIn.value = it.currentUser != null
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
