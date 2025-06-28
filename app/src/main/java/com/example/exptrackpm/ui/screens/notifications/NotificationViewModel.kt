//package com.example.exptrackpm.ui.screens.notifications
//
//import android.content.Context
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.work.ExistingPeriodicWorkPolicy
//import androidx.work.PeriodicWorkRequestBuilder
//import androidx.work.WorkManager
//import com.example.exptrackpm.data.users.UserRepository
//import com.google.firebase.auth.FirebaseAuth
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import java.util.concurrent.TimeUnit
//
//class NotificationSchedulerViewModel(private val applicationContext: Context) : ViewModel() {
//
//    private val _userPreferences = MutableStateFlow(com.example.exptrackpm.domain.model.NotificationPreferences())
//    val userPreferences: StateFlow<com.example.exptrackpm.domain.model.NotificationPreferences> = _userPreferences
//
//    init {
//        loadAndObserveUserNotificationPreferences()
//    }
//
//    private fun loadAndObserveUserNotificationPreferences() {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId.isNullOrBlank()) {
//            return
//        }
//
//        UserRepository.getUser(
//            //userId
//            ){ user ->
//            user?.notificationPreferences?.let { preferences ->
//                if (preferences != _userPreferences.value) { // Only update if changed
//                    _userPreferences.value = preferences
//                    scheduleOrCancelWeeklySummary(preferences.spendingSummaries)
//                }
//            }
//        }
//    }
//
//    fun updateNotificationPreference(newPreferences: com.example.exptrackpm.domain.model.NotificationPreferences) {
//        _userPreferences.value = newPreferences
//        scheduleOrCancelWeeklySummary(newPreferences.spendingSummaries)
//
//        // IMPORTANT: You need to save this to Firestore via UserRepository.updateUser(user.copy(notificationPreferences = newPreferences))
//    }
//
//
//    private fun scheduleOrCancelWeeklySummary(enable: Boolean) {
//        val workManager = WorkManager.getInstance(applicationContext)
//        if (enable) {
//            val weeklySummaryRequest = PeriodicWorkRequestBuilder<SpendingSummaryWorker>(
//                repeatInterval = 7, // Every 7 days
//                repeatIntervalTimeUnit = TimeUnit.DAYS
//                // If you want exact time, you'd need initial delay logic or more complex WorkManager constraints
//            )
//                .addTag("WeeklySummaryWorkerTag") // Add a tag to identify the work
//                .build()
//
//            // Enqueue the work, replacing any existing work with the same name
//            workManager.enqueueUniquePeriodicWork(
//                "WeeklySummaryWork",
//                ExistingPeriodicWorkPolicy.UPDATE, // UPDATE or KEEP
//                weeklySummaryRequest
//            )
//            Log.d("Scheduler", "Weekly Summary Work scheduled.")
//        } else {
//            workManager.cancelUniqueWork("WeeklySummaryWork")
//            Log.d("Scheduler", "Weekly Summary Work cancelled.")
//        }
//    }
//}
