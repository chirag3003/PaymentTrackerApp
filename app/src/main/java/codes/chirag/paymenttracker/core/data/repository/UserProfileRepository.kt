package codes.chirag.paymenttracker.core.data.repository

import codes.chirag.paymenttracker.core.database.dao.UserProfileDao
import codes.chirag.paymenttracker.core.database.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val dao: UserProfileDao) {

    val profile: Flow<UserProfileEntity?> = dao.get()

    suspend fun save(name: String, monthlyBudget: String, preferredMethod: String) {
        dao.upsert(
            UserProfileEntity(
                id              = 1,
                name            = name,
                monthlyBudget   = monthlyBudget,
                preferredMethod = preferredMethod
            )
        )
    }
}
