package codes.chirag.paymenttracker.feature.transactions.utils

import androidx.compose.ui.graphics.Color
import codes.chirag.paymenttracker.feature.home.models.Transaction
import codes.chirag.paymenttracker.feature.home.models.TransactionType

/**
 * Generate sample transactions for development
 * TODO: Replace with actual data from repository/database
 */
fun getSampleTransactions(): List<Transaction> {
    return listOf(
        // Today
        Transaction(
            id = "1",
            title = "Starbucks",
            amount = 5.75,
            type = TransactionType.EXPENSE,
            category = "Food",
            date = "Today",
            color = Color(0xFFFF9A56)
        ),
        Transaction(
            id = "2",
            title = "Salary",
            amount = 2500.00,
            type = TransactionType.INCOME,
            category = "Salary",
            date = "Today",
            color = Color(0xFF4CAF50)
        ),

        // Yesterday
        Transaction(
            id = "3",
            title = "McDonald's",
            amount = 12.30,
            type = TransactionType.EXPENSE,
            category = "Food",
            date = "Yesterday",
            color = Color(0xFFFF9A56)
        ),
        Transaction(
            id = "4",
            title = "Amazon",
            amount = 49.99,
            type = TransactionType.EXPENSE,
            category = "Shopping",
            date = "Yesterday",
            color = Color(0xFF42A5F5)
        ),

        // October 28, 2023
        Transaction(
            id = "5",
            title = "Metro Ticket",
            amount = 2.75,
            type = TransactionType.EXPENSE,
            category = "Transport",
            date = "October 28, 2023",
            color = Color(0xFF9C27B0)
        ),
        Transaction(
            id = "6",
            title = "Netflix",
            amount = 15.99,
            type = TransactionType.EXPENSE,
            category = "Subscription",
            date = "October 28, 2023",
            color = Color(0xFFE91E63)
        ),
        Transaction(
            id = "7",
            title = "Grocery Store",
            amount = 67.40,
            type = TransactionType.EXPENSE,
            category = "Groceries",
            date = "October 28, 2023",
            color = Color(0xFF4CAF50)
        ),

        // October 27, 2023
        Transaction(
            id = "8",
            title = "Movie Tickets",
            amount = 24.00,
            type = TransactionType.EXPENSE,
            category = "Entertainment",
            date = "October 27, 2023",
            color = Color(0xFFFF5722)
        ),
        Transaction(
            id = "9",
            title = "Uber",
            amount = 18.50,
            type = TransactionType.EXPENSE,
            category = "Transport",
            date = "October 27, 2023",
            color = Color(0xFF9C27B0)
        ),

        // October 26, 2023
        Transaction(
            id = "10",
            title = "Restaurant",
            amount = 45.20,
            type = TransactionType.EXPENSE,
            category = "Dining",
            date = "October 26, 2023",
            color = Color(0xFFFF9A56)
        ),
        Transaction(
            id = "11",
            title = "Freelance Project",
            amount = 500.00,
            type = TransactionType.INCOME,
            category = "Income",
            date = "October 26, 2023",
            color = Color(0xFF4CAF50)
        ),
        Transaction(
            id = "12",
            title = "Gas Station",
            amount = 55.00,
            type = TransactionType.EXPENSE,
            category = "Transport",
            date = "October 26, 2023",
            color = Color(0xFF9C27B0)
        )
    )
}

