package com.example.exptrackpm.ui.screens.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.exptrackpm.data.users.UserRepository
import com.example.exptrackpm.domain.model.User
import com.example.exptrackpm.ui.screens.notifications.WeeklySummaryWorker
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
        UserRepository.getUser(userId!!) { fetchedUser ->
            _user.value = fetchedUser
            _loading.value = false
            // Schedule or cancel worker based on fetched user preferences
            fetchedUser?.notificationPreferences?.let { prefs ->
                scheduleOrCancelWeeklySummary(prefs.weeklySummaries)
            }
        }
    }

    fun updateUser(updatedUser: User) {
        _loading.value = true
        UserRepository.updateUser(updatedUser) { success ->
            _loading.value = false
            if (success) {
                _user.value = updatedUser // Update local state on success
                // Re-schedule or cancel worker if notification preferences changed
                scheduleOrCancelWeeklySummary(updatedUser.notificationPreferences.weeklySummaries)
                Log.d("ProfileViewModel", "User updated successfully. Preferences: ${updatedUser.notificationPreferences.weeklySummaries}")
            } else {
                Log.e("ProfileViewModel", "Failed to update user.")
            }
        }
    }

    private fun scheduleOrCancelWeeklySummary(enable: Boolean) {
        val workManager = WorkManager.getInstance(getApplication()) // Use getApplication() here
        val workTag = "WeeklySummaryWork"

        if (enable) {
            // Define the work request
            val weeklySummaryRequest = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(
                repeatInterval = 5, // Run every 7 days
                repeatIntervalTimeUnit = TimeUnit.MINUTES
                // For exact timing (e.g., every Monday at 9 AM), you'd need:
                // initialDelay = calculateInitialDelay(DayOfWeek.MONDAY, LocalTime.of(9, 0)),
                // initialDelayTimeUnit = TimeUnit.MILLISECONDS
            )
                .addTag(workTag) // Add a tag to uniquely identify this work
                .build()

            // Enqueue the unique periodic work. UPDATE replaces existing work with the same name.
            workManager.enqueueUniquePeriodicWork(
                workTag,
                ExistingPeriodicWorkPolicy.UPDATE,
                weeklySummaryRequest
            )
            Log.d("ProfileViewModel", "Weekly Summary Worker scheduled.")
        } else {
            // Cancel the work if notifications are disabled
            workManager.cancelUniqueWork(workTag)
            Log.d("ProfileViewModel", "Weekly Summary Worker cancelled.")
        }
    }

    // You would call this function when the user toggles notification preferences
    fun setWeeklySummaryPreference(enable: Boolean) {
        _user.value?.let { currentUser ->
            val updatedUser = currentUser.copy(
                notificationPreferences = currentUser.notificationPreferences.copy(weeklySummaries = enable)
            )
            updateUser(updatedUser) // Call updateUser to persist changes and re-schedule worker
        } ?: Log.e("ProfileViewModel", "Cannot set preference: No current user.")
    }

}






//class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
//
//    private val _user = MutableStateFlow<User?>(null)
//    val user: StateFlow<User?> = _user
//
//    private val _loading = MutableStateFlow(false)
//    val loading: StateFlow<Boolean> = _loading
//
//    fun loadUser(userId: String) {
//        _loading.value = true
//        userRepository.getUser(userId) { fetchedUser ->
//            _user.value = fetchedUser
//            _loading.value = false
//        }
//    }
//
//    fun updateUser(updatedUser: User) {
//        _loading.value = true
//        userRepository.updateUser(updatedUser) { success ->
//            _loading.value = false
//            if (success) {
//                _user.value = updatedUser
//            }
//            // else handle failure (show error etc)
//        }
//    }
//}
