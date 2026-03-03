package codes.chirag.paymenttracker.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Double,
    val type: String,            // TransactionType.name
    val frequency: String,       // BillingFrequency.name
    val nextDueDate: String,     // Display string e.g. "Mar 10, 2026"
    val category: String,
    val paymentMethod: String,   // PaymentMethod.name
    val isActive: Boolean
)
