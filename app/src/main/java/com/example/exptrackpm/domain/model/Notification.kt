package com.example.exptrackpm.domain.model

data class NotificationPreferences(
    val overBudgetAlerts: Boolean = false,
    val weeklySummaries: Boolean = false
)
