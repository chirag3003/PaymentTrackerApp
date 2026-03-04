package codes.chirag.paymenttracker.core.data.repository

import codes.chirag.paymenttracker.core.database.dao.SubscriptionDao
import codes.chirag.paymenttracker.core.database.entities.SubscriptionEntity
import codes.chirag.paymenttracker.core.model.BillingFrequency
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.Subscription
import codes.chirag.paymenttracker.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubscriptionRepository(private val dao: SubscriptionDao) {

    val all: Flow<List<Subscription>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun add(subscription: Subscription) =
        dao.insert(subscription.toEntity())

    suspend fun update(subscription: Subscription) =
        dao.update(subscription.toEntity())

    suspend fun delete(id: String) =
        dao.deleteById(id)

    suspend fun getById(id: String): Subscription? =
        dao.getById(id)?.toDomain()
}

// ── Mappers ───────────────────────────────────────────────────────────────────

private fun SubscriptionEntity.toDomain() = Subscription(
    id            = id,
    name          = name,
    amount        = amount,
    type          = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.EXPENSE),
    frequency     = runCatching { BillingFrequency.valueOf(frequency) }.getOrDefault(BillingFrequency.MONTHLY),
    nextDueDate   = nextDueDate,
    category      = category,
    paymentMethod = runCatching { PaymentMethod.valueOf(paymentMethod) }.getOrDefault(PaymentMethod.UPI),
    isActive      = isActive,
    lastProcessedDate = lastProcessedDate
)

private fun Subscription.toEntity() = SubscriptionEntity(
    id            = id,
    name          = name,
    amount        = amount,
    type          = type.name,
    frequency     = frequency.name,
    nextDueDate   = nextDueDate,
    category      = category,
    paymentMethod = paymentMethod.name,
    isActive      = isActive,
    lastProcessedDate = lastProcessedDate
)
