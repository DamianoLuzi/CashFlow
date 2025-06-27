package com.example.exptrackpm.ui.screens.notifications

import Transaction
import TransactionService
import TransactionType
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.exptrackpm.data.users.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.DayOfWeek
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
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
            UserRepository.getUser(
                //userId
                ) { user ->
                continuation.resume(user?.notificationPreferences)
            }
        }

        if (userPreferences?.weeklySummaries != true) {
            // User has not enabled weekly summaries
            return Result.success()
        }

        // Define the date range for the *last full week*
        val now = LocalDate.now(ZoneId.systemDefault())
        // Adjust to the start of the current week (e.g., Monday)
        val endOfLastWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)) // Assuming week ends on Sunday
        val startOfLastWeek = endOfLastWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        // Convert LocalDate to Timestamp
        val startDateTimestamp = Timestamp(startOfLastWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000, 0)
        val endDateTimestamp = Timestamp(endOfLastWeek.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000, 0)

        Log.d("WeeklySummaryWorker", "Fetching transactions for last week: $startOfLastWeek to $endOfLastWeek")

        // Fetch transactions for the last week using the new TransactionService method
        val weeklyTransactions = suspendCancellableCoroutine<List<Transaction>> { continuation ->
            TransactionService.getTransactionsByDateRange(userId, startDateTimestamp, endDateTimestamp) { transactions ->
                continuation.resume(transactions)
            }
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
                Weekly Summary (${startOfLastWeek.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())} ${startOfLastWeek.dayOfMonth} - ${endOfLastWeek.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())} ${endOfLastWeek.dayOfMonth}):
                Total Income: €${formatter.format(totalIncome)}
                Total Expenses: €${formatter.format(totalExpenses)}
                Net Balance: €${formatter.format(netBalance)}
            """.trimIndent()

        NotificationHelper.showWeeklySummaryNotification(applicationContext, summaryText)

        return Result.success()
    }
}
