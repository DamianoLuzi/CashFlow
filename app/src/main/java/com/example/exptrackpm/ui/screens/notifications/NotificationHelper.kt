package com.example.exptrackpm.ui.screens.notifications

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
import com.example.exptrackpm.MainActivity
import com.example.exptrackpm.R

object NotificationHelper {

    const val OVER_BUDGET_CHANNEL_ID = "over_budget_channel"
    const val WEEKLY_SUMMARY_CHANNEL_ID = "weekly_summary_channel"
    // Add more channel IDs as needed

    const val OVER_BUDGET_NOTIFICATION_ID = 1001
    const val WEEKLY_SUMMARY_NOTIFICATION_ID = 1002
    // Add more notification IDs

    /**
     * Creates notification channels. Call this once when your app starts (e.g., in Application class).
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Over Budget Channel
            val overBudgetChannel = NotificationChannel(
                OVER_BUDGET_CHANNEL_ID,
                "Over Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH // High importance for immediate alerts
            ).apply {
                description = "Notifications when you go over budget in a category."
            }

            // Weekly Summary Channel
            val weeklySummaryChannel = NotificationChannel(
                WEEKLY_SUMMARY_CHANNEL_ID,
                "Weekly Summaries",
                NotificationManager.IMPORTANCE_LOW // Lower importance for summaries
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
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // You can add extras here to navigate to a specific part of the app
            putExtra("notification_type", "over_budget")
            putExtra("category", category)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val formatter = DecimalFormat("0.00")
        val formattedAmount = formatter.format(amountOver)

        val builder = NotificationCompat.Builder(context, OVER_BUDGET_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background) // You'll need to create this icon
            .setContentTitle("Budget Alert: Over Spent!")
            .setContentText("You've exceeded your budget for '$category' by €$formattedAmount.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Set the intent to open the app
            .setAutoCancel(true) // Dismiss notification when tapped
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on lock screen

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(OVER_BUDGET_NOTIFICATION_ID, builder.build())
        }
    }

    fun showWeeklySummaryNotification(context: Context, summaryText: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", "weekly_summary")
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, WEEKLY_SUMMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background) // You'll need to create this icon
            .setContentTitle("Weekly Spending Summary")
            .setContentText(summaryText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summaryText)) // Allow long text
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(WEEKLY_SUMMARY_NOTIFICATION_ID, builder.build())
        }
    }
}
