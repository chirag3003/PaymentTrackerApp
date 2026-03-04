package codes.chirag.paymenttracker.core.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import codes.chirag.paymenttracker.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "payment_tracker_subscriptions"
    private const val CHANNEL_NAME = "Subscriptions & Auto-Pay"
    private const val NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for automatic subscription transactions"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSubscriptionProcessedNotification(
        context: Context,
        subscriptionName: String,
        amountFormatted: String,
        category: String,
        paymentMethod: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            pendingIntentFlags
        )

        val methodLabel = paymentMethod.lowercase().replaceFirstChar { it.uppercase() }
        val content = "Auto-added $amountFormatted for $subscriptionName - $category - $methodLabel"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda) // Use default icon temporarily, ideally use app icon
            .setContentTitle("Subscription Processed")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID_BASE + subscriptionName.hashCode(), builder.build())
            } catch (e: SecurityException) {
                // Permission not granted for POST_NOTIFICATIONS, do nothing
            }
        }
    }
}
