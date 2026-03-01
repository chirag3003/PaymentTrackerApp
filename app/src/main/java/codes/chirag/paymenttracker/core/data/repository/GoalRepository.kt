package codes.chirag.paymenttracker.core.data.repository

import codes.chirag.paymenttracker.core.database.dao.GoalDao
import codes.chirag.paymenttracker.core.database.mappers.toDomain
import codes.chirag.paymenttracker.core.database.mappers.toEntity
import codes.chirag.paymenttracker.core.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepository(private val dao: GoalDao) {

    val allGoals: Flow<List<Goal>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: String): Goal? =
        dao.getById(id)?.toDomain()

    suspend fun add(goal: Goal) =
        dao.insert(goal.toEntity())

    suspend fun update(goal: Goal) =
        dao.update(goal.toEntity())

    suspend fun delete(id: String) =
        dao.deleteById(id)

    suspend fun count(): Int = dao.count()
}
