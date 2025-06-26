package com.example.exptrackpm.domain.model

data class NotificationPreferences(
    val overBudgetAlerts: Boolean = false,
    //val billReminders: Boolean = false,
    val weeklySummaries: Boolean = false
)
