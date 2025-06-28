package com.example.exptrackpm.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String? = null,
    //val avatarUrl: String? = null, // Not directly used in this example, but good to have
    val currency: String = "EUR",
    val theme: String = "System default",
    val notificationPreferences: NotificationPreferences = NotificationPreferences()
)
