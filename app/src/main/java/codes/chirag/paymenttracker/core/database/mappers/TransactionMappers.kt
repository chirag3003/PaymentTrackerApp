package codes.chirag.paymenttracker.core.database.mappers

import codes.chirag.paymenttracker.core.database.entities.TransactionEntity
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.model.TransactionType

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id            = id,
    title         = title,
    amount        = amount,
    type          = TransactionType.valueOf(type),
    category      = category,
    date          = date,
    paymentMethod = PaymentMethod.valueOf(paymentMethod),
    notes         = notes,
    tags          = if (tags.isBlank()) emptyList()
                    else tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id            = id,
    title         = title,
    amount        = amount,
    type          = type.name,
    category      = category,
    date          = date,
    paymentMethod = paymentMethod.name,
    notes         = notes,
    tags          = tags.filter { it.isNotBlank() }.joinToString(",")
)
