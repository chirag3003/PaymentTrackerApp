package codes.chirag.paymenttracker.core.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import codes.chirag.paymenttracker.core.data.repository.SubscriptionRepository
import codes.chirag.paymenttracker.core.data.repository.TransactionRepository
import codes.chirag.paymenttracker.core.database.AppDatabase
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.utils.DateUtils
import codes.chirag.paymenttracker.core.utils.NotificationHelper
import codes.chirag.paymenttracker.core.utils.formatCurrency
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.UUID

class SubscriptionWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(appContext)
        val transactionRepo = TransactionRepository(database.transactionDao())
        val subscriptionRepo = SubscriptionRepository(database.subscriptionDao())

        NotificationHelper.createNotificationChannel(appContext)

        return try {
            val subscriptions = subscriptionRepo.all.first()
            val todayLabel = DateUtils.formatCalendarLabel(Calendar.getInstance())

            for (sub in subscriptions) {
                if (!sub.isActive) continue

                if (DateUtils.isDueOrPast(sub.nextDueDate)) {
                    // Create transaction for this subscription
                    val newTransaction = Transaction(
                        id = UUID.randomUUID().toString(),
                        title = sub.name,
                        amount = sub.amount,
                        type = sub.type,
                        category = sub.category,
                        date = todayLabel, // Record it as processed today
                        paymentMethod = sub.paymentMethod,
                        notes = "Auto-generated from subscription"
                    )
                    transactionRepo.add(newTransaction)

                    // Calculate next due date
                    var newDueDate = DateUtils.calculateNextDueDate(sub.nextDueDate, sub.frequency)
                    while (DateUtils.isDueOrPast(newDueDate)) {
                        newDueDate = DateUtils.calculateNextDueDate(newDueDate, sub.frequency)
                    }
                    val updatedSub = sub.copy(nextDueDate = newDueDate)
                    subscriptionRepo.update(updatedSub)

                    // Notify user
                    val formattedAmount = formatCurrency(sub.amount)
                    val displayAmount = if (sub.type == codes.chirag.paymenttracker.core.model.TransactionType.INCOME) {
                        "+$formattedAmount"
                    } else {
                        "-$formattedAmount"
                    }
                    NotificationHelper.showSubscriptionProcessedNotification(
                        context = appContext,
                        subscriptionName = sub.name,
                        amountFormatted = displayAmount,
                        category = sub.category,
                        paymentMethod = sub.paymentMethod.name
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
