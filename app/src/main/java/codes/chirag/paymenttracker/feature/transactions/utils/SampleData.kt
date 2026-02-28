package codes.chirag.paymenttracker.feature.transactions.utils

import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.model.TransactionType

/**
 * Hardcoded sample transactions used during UI development.
 * TODO: Remove once the Room database and repository layer are wired up.
 */
fun getSampleTransactions(): List<Transaction> = listOf(
    // ── Today ──────────────────────────────────────────────────────────────
    Transaction(
        id            = "1",
        title         = "Starbucks",
        amount        = 320.0,
        type          = TransactionType.EXPENSE,
        category      = "Food",
        date          = "Today",
        paymentMethod = PaymentMethod.UPI,
        notes         = "Morning coffee"
    ),
    Transaction(
        id            = "2",
        title         = "Monthly Allowance",
        amount        = 15000.0,
        type          = TransactionType.INCOME,
        category      = "Income",
        date          = "Today",
        paymentMethod = PaymentMethod.CASH
    ),

    // ── Yesterday ──────────────────────────────────────────────────────────
    Transaction(
        id            = "3",
        title         = "McDonald's",
        amount        = 480.0,
        type          = TransactionType.EXPENSE,
        category      = "Food",
        date          = "Yesterday",
        paymentMethod = PaymentMethod.UPI
    ),
    Transaction(
        id            = "4",
        title         = "Amazon",
        amount        = 1299.0,
        type          = TransactionType.EXPENSE,
        category      = "Shopping",
        date          = "Yesterday",
        paymentMethod = PaymentMethod.CARD,
        notes         = "Earphones"
    ),

    // ── Feb 26, 2026 ───────────────────────────────────────────────────────
    Transaction(
        id            = "5",
        title         = "Metro Card Recharge",
        amount        = 200.0,
        type          = TransactionType.EXPENSE,
        category      = "Transport",
        date          = "Feb 26, 2026",
        paymentMethod = PaymentMethod.UPI
    ),
    Transaction(
        id            = "6",
        title         = "Netflix",
        amount        = 649.0,
        type          = TransactionType.EXPENSE,
        category      = "Subscription",
        date          = "Feb 26, 2026",
        paymentMethod = PaymentMethod.CARD
    ),
    Transaction(
        id            = "7",
        title         = "Grocery Store",
        amount        = 1840.0,
        type          = TransactionType.EXPENSE,
        category      = "Groceries",
        date          = "Feb 26, 2026",
        paymentMethod = PaymentMethod.UPI
    ),

    // ── Feb 25, 2026 ───────────────────────────────────────────────────────
    Transaction(
        id            = "8",
        title         = "Movie Tickets",
        amount        = 600.0,
        type          = TransactionType.EXPENSE,
        category      = "Entertainment",
        date          = "Feb 25, 2026",
        paymentMethod = PaymentMethod.UPI,
        tags          = listOf("Weekend")
    ),
    Transaction(
        id            = "9",
        title         = "Uber",
        amount        = 240.0,
        type          = TransactionType.EXPENSE,
        category      = "Transport",
        date          = "Feb 25, 2026",
        paymentMethod = PaymentMethod.UPI
    ),

    // ── Feb 24, 2026 ───────────────────────────────────────────────────────
    Transaction(
        id            = "10",
        title         = "Restaurant",
        amount        = 950.0,
        type          = TransactionType.EXPENSE,
        category      = "Dining",
        date          = "Feb 24, 2026",
        paymentMethod = PaymentMethod.CARD,
        tags          = listOf("Weekend")
    ),
    Transaction(
        id            = "11",
        title         = "Freelance Project",
        amount        = 8000.0,
        type          = TransactionType.INCOME,
        category      = "Freelance",
        date          = "Feb 24, 2026",
        paymentMethod = PaymentMethod.UPI,
        notes         = "Logo design project"
    ),
    Transaction(
        id            = "12",
        title         = "Spotify",
        amount        = 119.0,
        type          = TransactionType.EXPENSE,
        category      = "Subscription",
        date          = "Feb 24, 2026",
        paymentMethod = PaymentMethod.CARD
    )
)
