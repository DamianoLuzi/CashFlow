package com.example.exptrackpm.ui.screens.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.exptrackpm.data.users.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NotificationSchedulerViewModel(private val applicationContext: Context) : ViewModel() {

    private val _userPreferences = MutableStateFlow(com.example.exptrackpm.domain.model.NotificationPreferences())
    val userPreferences: StateFlow<com.example.exptrackpm.domain.model.NotificationPreferences> = _userPreferences

    init {
        // Load initial preferences and observe changes
        loadAndObserveUserNotificationPreferences()
    }

    private fun loadAndObserveUserNotificationPreferences() {
        // Assuming you have a way to get the current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrBlank()) {
            // Handle not logged in state
            return
        }

        UserRepository.getUser { user ->
            user?.notificationPreferences?.let { preferences ->
                if (preferences != _userPreferences.value) { // Only update if changed
                    _userPreferences.value = preferences
                    scheduleOrCancelWeeklySummary(preferences.weeklySummaries)
                }
            }
        }
    }

    // Call this from your UI when notification preferences are updated
    fun updateNotificationPreference(newPreferences: com.example.exptrackpm.domain.model.NotificationPreferences) {
        // Assuming you have a UserRepository.updateUser method that takes a User object
        // and this would then trigger loadAndObserveUserNotificationPreferences
        // For now, directly update local state and schedule/cancel worker
        _userPreferences.value = newPreferences
        scheduleOrCancelWeeklySummary(newPreferences.weeklySummaries)

        // IMPORTANT: You need to save this to Firestore via UserRepository.updateUser(user.copy(notificationPreferences = newPreferences))
        // This example doesn't show the full user update flow.
    }


    private fun scheduleOrCancelWeeklySummary(enable: Boolean) {
        val workManager = WorkManager.getInstance(applicationContext)
        if (enable) {
            val weeklySummaryRequest = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(
                repeatInterval = 7, // Every 7 days
                repeatIntervalTimeUnit = TimeUnit.DAYS
                // If you want exact time, you'd need initial delay logic or more complex WorkManager constraints
            )
                .addTag("WeeklySummaryWorkerTag") // Add a tag to identify the work
                .build()

            // Enqueue the work, replacing any existing work with the same name
            workManager.enqueueUniquePeriodicWork(
                "WeeklySummaryWork",
                ExistingPeriodicWorkPolicy.UPDATE, // UPDATE or KEEP
                weeklySummaryRequest
            )
            Log.d("Scheduler", "Weekly Summary Work scheduled.")
        } else {
            workManager.cancelUniqueWork("WeeklySummaryWork")
            Log.d("Scheduler", "Weekly Summary Work cancelled.")
        }
    }
}
