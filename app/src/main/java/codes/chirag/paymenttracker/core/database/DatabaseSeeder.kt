package codes.chirag.paymenttracker.core.database

import codes.chirag.paymenttracker.core.data.repository.GoalRepository
import codes.chirag.paymenttracker.core.data.repository.TransactionRepository
import codes.chirag.paymenttracker.core.model.Goal
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.model.TransactionType

/**
 * Seeds the database with sample data on first install (when both tables are empty).
 * Must be called from a coroutine on the IO dispatcher.
 */
object DatabaseSeeder {

    suspend fun seedIfEmpty(
        txRepo: TransactionRepository,
        goalRepo: GoalRepository
    ) {
        if (txRepo.count() == 0) {
            seedTransactions(txRepo)
        }
        if (goalRepo.count() == 0) {
            seedGoals(goalRepo)
        }
    }

    private suspend fun seedTransactions(repo: TransactionRepository) {
        val samples = listOf(
            Transaction(
                id = "1", title = "Starbucks", amount = 320.0,
                type = TransactionType.EXPENSE, category = "Food",
                date = "Today", paymentMethod = PaymentMethod.UPI,
                notes = "Morning coffee"
            ),
            Transaction(
                id = "2", title = "Monthly Allowance", amount = 15000.0,
                type = TransactionType.INCOME, category = "Income",
                date = "Today", paymentMethod = PaymentMethod.CASH
            ),
            Transaction(
                id = "3", title = "McDonald's", amount = 480.0,
                type = TransactionType.EXPENSE, category = "Food",
                date = "Yesterday", paymentMethod = PaymentMethod.UPI
            ),
            Transaction(
                id = "4", title = "Amazon", amount = 1299.0,
                type = TransactionType.EXPENSE, category = "Shopping",
                date = "Yesterday", paymentMethod = PaymentMethod.CARD,
                notes = "Earphones"
            ),
            Transaction(
                id = "5", title = "Metro Card Recharge", amount = 200.0,
                type = TransactionType.EXPENSE, category = "Transport",
                date = "Feb 26, 2026", paymentMethod = PaymentMethod.UPI
            ),
            Transaction(
                id = "6", title = "Netflix", amount = 649.0,
                type = TransactionType.EXPENSE, category = "Subscription",
                date = "Feb 26, 2026", paymentMethod = PaymentMethod.CARD
            ),
            Transaction(
                id = "7", title = "Grocery Store", amount = 1840.0,
                type = TransactionType.EXPENSE, category = "Groceries",
                date = "Feb 26, 2026", paymentMethod = PaymentMethod.UPI
            ),
            Transaction(
                id = "8", title = "Movie Tickets", amount = 600.0,
                type = TransactionType.EXPENSE, category = "Entertainment",
                date = "Feb 25, 2026", paymentMethod = PaymentMethod.UPI,
                tags = listOf("Weekend")
            ),
            Transaction(
                id = "9", title = "Uber", amount = 240.0,
                type = TransactionType.EXPENSE, category = "Transport",
                date = "Feb 25, 2026", paymentMethod = PaymentMethod.UPI
            ),
            Transaction(
                id = "10", title = "Restaurant", amount = 950.0,
                type = TransactionType.EXPENSE, category = "Dining",
                date = "Feb 24, 2026", paymentMethod = PaymentMethod.CARD,
                tags = listOf("Weekend")
            ),
            Transaction(
                id = "11", title = "Freelance Project", amount = 8000.0,
                type = TransactionType.INCOME, category = "Freelance",
                date = "Feb 24, 2026", paymentMethod = PaymentMethod.UPI,
                notes = "Logo design project"
            ),
            Transaction(
                id = "12", title = "Spotify", amount = 119.0,
                type = TransactionType.EXPENSE, category = "Subscription",
                date = "Feb 24, 2026", paymentMethod = PaymentMethod.CARD
            )
        )
        samples.forEach { repo.add(it) }
    }

    private suspend fun seedGoals(repo: GoalRepository) {
        val samples = listOf(
            Goal(id = "1", name = "New Phone",      targetAmount = 25000.0, savedAmount = 14500.0, targetDate = "Jun 2026"),
            Goal(id = "2", name = "Goa Trip",       targetAmount = 18000.0, savedAmount = 6200.0,  targetDate = "Apr 2026"),
            Goal(id = "3", name = "Laptop Fund",    targetAmount = 60000.0, savedAmount = 22000.0, targetDate = "Dec 2026"),
            Goal(id = "4", name = "Emergency Fund", targetAmount = 50000.0, savedAmount = 50000.0, targetDate = ""),
            Goal(id = "5", name = "Books & Notes",  targetAmount = 5000.0,  savedAmount = 3400.0,  targetDate = "Mar 2026")
        )
        samples.forEach { repo.add(it) }
    }
}
