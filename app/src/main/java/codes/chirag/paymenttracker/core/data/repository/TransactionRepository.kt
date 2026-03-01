package codes.chirag.paymenttracker.core.data.repository

import codes.chirag.paymenttracker.core.database.dao.TransactionDao
import codes.chirag.paymenttracker.core.database.mappers.toDomain
import codes.chirag.paymenttracker.core.database.mappers.toEntity
import codes.chirag.paymenttracker.core.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val dao: TransactionDao) {

    val allTransactions: Flow<List<Transaction>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: String): Transaction? =
        dao.getById(id)?.toDomain()

    suspend fun add(transaction: Transaction) =
        dao.insert(transaction.toEntity())

    suspend fun update(transaction: Transaction) =
        dao.update(transaction.toEntity())

    suspend fun delete(id: String) =
        dao.deleteById(id)

    suspend fun count(): Int = dao.count()
}
