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

class SpendingSummaryWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrBlank()) {
            return Result.success()
        }
        val userPreferences = suspendCancellableCoroutine { continuation ->
            UserRepository.getUser { user ->
                continuation.resume(user?.notificationPreferences)
            }
        }

        if (userPreferences?.spendingSummaries != true) {
            return Result.success()
        }
        val now = LocalDate.now(ZoneId.systemDefault())
        val endOfLastWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val startOfLastWeek = endOfLastWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val startDateTimestamp = Timestamp(startOfLastWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000, 0)
        val endDateTimestamp = Timestamp(endOfLastWeek.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000, 0)

        Log.d("spendingSummaryWorker", "Fetching transactions for last week: $startOfLastWeek to $endOfLastWeek")

        val spendingTransactions = suspendCancellableCoroutine<List<Transaction>> { continuation ->
            TransactionService.getTransactionsByDateRange(userId, startDateTimestamp, endDateTimestamp) { transactions ->
                continuation.resume(transactions)
            }
        }
        Log.d("summary",userId)
        Log.d("summary",spendingTransactions.toString())
        val totalIncome = spendingTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        Log.d("summary",startDateTimestamp.toString())
        Log.d("summary",endDateTimestamp.toString())

        val totalExpenses = spendingTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        Log.d("summary",spendingTransactions.toString())

        val netBalance = totalIncome - totalExpenses

        val formatter = DecimalFormat("0.00")
        val summaryText = """
                Spending Summary (${startOfLastWeek.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())} ${startOfLastWeek.dayOfMonth} - ${endOfLastWeek.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())} ${endOfLastWeek.dayOfMonth}):
                Total Income: €${formatter.format(totalIncome)}
                Total Expenses: €${formatter.format(totalExpenses)}
                Net Balance: €${formatter.format(netBalance)}
            """.trimIndent()

        NotificationHelper.showWeeklySummaryNotification(applicationContext, summaryText)

        return Result.success()
    }
}
