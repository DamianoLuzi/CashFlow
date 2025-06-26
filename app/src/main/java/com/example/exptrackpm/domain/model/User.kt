package com.example.exptrackpm.domain.model

data class User(
    val id: String = "", // This will be the Firebase Auth UID
    val email: String = "",
    val displayName: String? = null,
    val avatarUrl: String? = null, // Not directly used in this example, but good to have
    val currency: String = "EUR",
    val theme: String = "System default",
//    val notificationPreferences: Map<String, Boolean> = mapOf(
//        "overBudgetAlerts" to false,
//        "billReminders" to false,
//        "weeklySummaries" to false
//    )
    val notificationPreferences: NotificationPreferences = NotificationPreferences()
)
