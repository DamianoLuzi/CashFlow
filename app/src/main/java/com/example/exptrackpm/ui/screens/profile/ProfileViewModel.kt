package com.example.exptrackpm.ui.screens.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.exptrackpm.data.users.UserRepository
import com.example.exptrackpm.domain.model.User
import com.example.exptrackpm.ui.screens.notifications.SpendingSummaryWorker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit


class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = Firebase.auth
    val userId = auth.currentUser?.uid
    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadUser() {
        _loading.value = true
        UserRepository.getUser { fetchedUser ->
            _user.value = fetchedUser
            _loading.value = false
            fetchedUser?.notificationPreferences?.let { prefs ->
                scheduleOrCancelWeeklySummary(prefs.spendingSummaries)
            }
        }
    }

    fun updateUser(updatedUser: User) {
        _loading.value = true
        UserRepository.updateUser(updatedUser) { success ->
            _loading.value = false
            if (success) {
                _user.value = updatedUser
                scheduleOrCancelWeeklySummary(updatedUser.notificationPreferences.spendingSummaries)
                Log.d("ProfileViewModel", "User updated successfully. Preferences: ${updatedUser.notificationPreferences.spendingSummaries}")
            } else {
                Log.e("ProfileViewModel", "Failed to update user.")
            }
        }
    }

    private fun scheduleOrCancelWeeklySummary(enable: Boolean) {
        val workManager = WorkManager.getInstance(getApplication()) // Use getApplication() here
        val workTag = "WeeklySummaryWork"

        if (enable) {
            val weeklySummaryRequest = PeriodicWorkRequestBuilder<SpendingSummaryWorker>(
                repeatInterval = 5, // Run every 7 days
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .addTag(workTag) // Add a tag to uniquely identify this work
                .build()

            workManager.enqueueUniquePeriodicWork(
                workTag,
                ExistingPeriodicWorkPolicy.UPDATE,
                weeklySummaryRequest
            )
            Log.d("ProfileViewModel", "Weekly Summary Worker scheduled.")
        } else {
            workManager.cancelUniqueWork(workTag)
            Log.d("ProfileViewModel", "Weekly Summary Worker cancelled.")
        }
    }

    fun setSpendingSummaryPreference(enable: Boolean) {
        _user.value?.let { currentUser ->
            val updatedUser = currentUser.copy(
                notificationPreferences = currentUser.notificationPreferences.copy(spendingSummaries = enable)
            )
            updateUser(updatedUser)
        } ?: Log.e("ProfileViewModel", "Cannot set preference: No current user.")
    }

}