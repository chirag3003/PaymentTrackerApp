package codes.chirag.paymenttracker.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amount: Double,
    val type: String,           // TransactionType.name
    val category: String,
    val date: String,
    val paymentMethod: String,  // PaymentMethod.name
    val notes: String,
    val tags: String            // CSV e.g. "Weekend,Trip"
)
