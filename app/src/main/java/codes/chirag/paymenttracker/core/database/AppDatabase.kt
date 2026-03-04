package codes.chirag.paymenttracker.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import codes.chirag.paymenttracker.core.database.dao.GoalDao
import codes.chirag.paymenttracker.core.database.dao.SubscriptionDao
import codes.chirag.paymenttracker.core.database.dao.TransactionDao
import codes.chirag.paymenttracker.core.database.dao.UserProfileDao
import codes.chirag.paymenttracker.core.database.entities.GoalEntity
import codes.chirag.paymenttracker.core.database.entities.SubscriptionEntity
import codes.chirag.paymenttracker.core.database.entities.TransactionEntity
import codes.chirag.paymenttracker.core.database.entities.UserProfileEntity

@Database(
    entities = [TransactionEntity::class, GoalEntity::class, UserProfileEntity::class, SubscriptionEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun goalDao(): GoalDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "payment_tracker.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
    }
}
