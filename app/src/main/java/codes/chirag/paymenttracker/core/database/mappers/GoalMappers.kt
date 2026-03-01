package codes.chirag.paymenttracker.core.database.mappers

import codes.chirag.paymenttracker.core.database.entities.GoalEntity
import codes.chirag.paymenttracker.core.model.Goal

fun GoalEntity.toDomain(): Goal = Goal(
    id           = id,
    name         = name,
    targetAmount = targetAmount,
    savedAmount  = savedAmount,
    targetDate   = targetDate
)

fun Goal.toEntity(): GoalEntity = GoalEntity(
    id           = id,
    name         = name,
    targetAmount = targetAmount,
    savedAmount  = savedAmount,
    targetDate   = targetDate
)
