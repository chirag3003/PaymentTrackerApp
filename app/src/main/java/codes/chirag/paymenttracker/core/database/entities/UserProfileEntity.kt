package codes.chirag.paymenttracker.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val monthlyBudget: String,
    val preferredMethod: String  // PaymentMethod.name
)
