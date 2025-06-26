package com.example.exptrackpm.ui.screens.notifications

import Transaction
import TransactionType
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.exptrackpm.data.users.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.coroutines.resume

class WeeklySummaryWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrBlank()) {
            // User not logged in or invalid, no summary to generate
            return Result.success()
        }

        // Fetch user's notification preferences
        val userPreferences = suspendCancellableCoroutine<com.example.exptrackpm.domain.model.NotificationPreferences?> { continuation ->
            UserRepository.getUser { user ->
                continuation.resume(user?.notificationPreferences)
            }
        }

        if (userPreferences?.weeklySummaries != true) {
            // User has not enabled weekly summaries
            return Result.success()
        }

        // Define the current week (example: Monday to Sunday)
        val now = LocalDate.now(ZoneId.systemDefault())
        val weekFields = WeekFields.of(Locale.getDefault())
        val startOfWeek = now.with(weekFields.dayOfWeek(), 1L) // Monday
        val endOfWeek = startOfWeek.plusDays(6) // Sunday

        // Fetch transactions for the last week (requires a TransactionRepository with date filtering)
        // This is a placeholder. You need to implement actual data fetching from your TransactionRepository
        val weeklyTransactions = suspendCancellableCoroutine<List<Transaction>> { continuation ->
            // Replace with your actual TransactionRepository.getTransactionsByDateRange
            // For now, returning empty list or dummy data
            // Example:
            // TransactionRepository.getTransactionsByDateRange(userId, startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), endOfWeek.atEndOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()) { transactions ->
            //     continuation.resume(transactions)
            // }
            continuation.resume(emptyList()) // Placeholder: Replace with actual transaction data
        }

        val totalIncome = weeklyTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val totalExpenses = weeklyTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val netBalance = totalIncome - totalExpenses

        val formatter = DecimalFormat("0.00")
        val summaryText = """
                Weekly Summary (${startOfWeek} to ${endOfWeek}):
                Total Income: €${formatter.format(totalIncome)}
                Total Expenses: €${formatter.format(totalExpenses)}
                Net Balance: €${formatter.format(netBalance)}
            """.trimIndent()

        NotificationHelper.showWeeklySummaryNotification(applicationContext, summaryText)

        return Result.success()
    }
}
