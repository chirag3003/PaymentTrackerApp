package codes.chirag.paymenttracker.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4
                    )
                    .build().also { INSTANCE = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS subscriptions (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        frequency TEXT NOT NULL,
                        nextDueDate TEXT NOT NULL,
                        category TEXT NOT NULL,
                        paymentMethod TEXT NOT NULL,
                        isActive INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE subscriptions ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE subscriptions ADD COLUMN lastProcessedDate TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}
