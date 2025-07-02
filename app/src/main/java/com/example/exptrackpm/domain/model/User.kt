package com.example.exptrackpm.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val currency: String = "EUR",
    val theme: String = "System default",
    val notificationPreferences: NotificationPreferences = NotificationPreferences()
)
