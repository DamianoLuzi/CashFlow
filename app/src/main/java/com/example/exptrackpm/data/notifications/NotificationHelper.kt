package com.example.exptrackpm.data.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.DecimalFormat
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.exptrackpm.MainActivity // Assuming MainActivity is the entry point
import com.example.exptrackpm.R

object NotificationHelper {

    const val OVER_BUDGET_CHANNEL_ID = "over_budget_channel"
    const val SPENDING_SUMMARY_CHANNEL_ID = "spending_summary_channel"

    const val OVER_BUDGET_NOTIFICATION_ID = 1001
    const val SPENDING_SUMMARY_NOTIFICATION_ID = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val overBudgetChannel = NotificationChannel(
                OVER_BUDGET_CHANNEL_ID,
                "Over Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when you go over budget in a category."
            }

            val weeklySummaryChannel = NotificationChannel(
                SPENDING_SUMMARY_CHANNEL_ID,
                "Spending Summaries",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Regular summaries of your spending and income."
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(overBudgetChannel)
            notificationManager.createNotificationChannel(weeklySummaryChannel)
        }
    }

    fun showOverBudgetNotification(context: Context, category: String, amountOver: Double) {
        // Check permission before attempting to show the notification
        // For API < 33, this permission is auto-granted.
        // For API >= 33, it must be requested at runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", "over_budget")
            putExtra("category", category)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            OVER_BUDGET_NOTIFICATION_ID, // Use a unique request code for this notification's intent
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val formatter = DecimalFormat("0.00")
        val formattedAmount = formatter.format(amountOver)

        val builder = NotificationCompat.Builder(context, OVER_BUDGET_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background) // Make sure this icon exists and is suitable
            .setContentTitle("Budget Alert: Over Spent!")
            .setContentText("You've exceeded your budget for '$category' by €$formattedAmount.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        NotificationManagerCompat.from(context).notify(OVER_BUDGET_NOTIFICATION_ID, builder.build())
    }

    fun showWeeklySummaryNotification(context: Context, summaryText: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", "weekly_summary")
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            SPENDING_SUMMARY_NOTIFICATION_ID, // Use a unique request code for this notification's intent
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, SPENDING_SUMMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Spending Summary")
            .setContentText(summaryText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summaryText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        NotificationManagerCompat.from(context).notify(SPENDING_SUMMARY_NOTIFICATION_ID, builder.build())
    }
}