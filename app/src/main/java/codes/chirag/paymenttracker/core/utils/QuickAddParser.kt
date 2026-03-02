package codes.chirag.paymenttracker.core.utils

import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.TransactionType

data class ParsedTransaction(
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val paymentMethod: PaymentMethod
)

/**
 * Heuristic parser for quick-add text input.
 * Extracts amount, category, type and payment method from natural language.
 * e.g. "Had tea for 20" → ParsedTransaction(title="Had tea for 20", amount=20, category="Food", ...)
 * Will be replaced by LLM inference in the AI phase.
 */
object QuickAddParser {

    private val amountRegex = Regex("""(?:rs\.?|₹)?\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)

    // Category keyword map — order matters (first match wins)
    private val categoryKeywords: List<Pair<String, List<String>>> = listOf(
        "Food"          to listOf("food", "lunch", "dinner", "breakfast", "tea", "chai", "coffee",
                                   "zomato", "swiggy", "meal", "snack", "pizza", "burger", "biryani",
                                   "thali", "dosa", "idli", "sandwich", "juice", "eat", "hotel",
                                   "restaurant", "cafe", "ccd", "mcd", "kfc", "dominos"),
        "Transport"     to listOf("cab", "uber", "ola", "auto", "rickshaw", "bus", "metro", "train",
                                   "ticket", "petrol", "fuel", "rapido", "bike", "taxi", "travel",
                                   "commute", "ride"),
        "Shopping"      to listOf("shopping", "clothes", "shirt", "jeans", "shoes", "amazon", "flipkart",
                                   "myntra", "meesho", "buy", "bought", "purchase", "amazon", "watch",
                                   "bag", "accessories"),
        "Groceries"     to listOf("grocery", "groceries", "vegetables", "fruits", "milk", "bread",
                                   "eggs", "supermarket", "blinkit", "zepto", "dmart", "bigbasket",
                                   "provisions", "ration"),
        "Entertainment" to listOf("movie", "cinema", "netflix", "hotstar", "prime", "show", "concert",
                                   "event", "game", "gaming", "theatre", "pvr", "inox", "bookmyshow",
                                   "party", "club", "bowling", "sports"),
        "Subscription"  to listOf("subscription", "spotify", "youtube", "icloud", "google one",
                                   "linkedin", "coursera", "udemy", "recharge", "plan", "prepaid",
                                   "postpaid", "wifi", "internet", "broadband"),
        "Health"        to listOf("medicine", "doctor", "hospital", "pharmacy", "medical", "health",
                                   "gym", "fitness", "yoga", "tablet", "injection", "test", "scan",
                                   "apollo", "netmeds", "1mg"),
        "Education"     to listOf("book", "books", "stationery", "pen", "course", "exam", "fees",
                                   "tuition", "coaching", "class", "college", "university", "study",
                                   "notebook", "printing", "xerox"),
    )

    // Income keywords
    private val incomeKeywords = listOf(
        "salary", "stipend", "income", "received", "got", "earned", "allowance",
        "transfer received", "paid me", "refund", "cashback", "return", "won"
    )

    // Payment method keywords
    private val paymentMethodKeywords: List<Pair<PaymentMethod, List<String>>> = listOf(
        PaymentMethod.UPI  to listOf("upi", "gpay", "phonepe", "paytm", "bhim", "neft", "imps",
                                      "transfer", "online", "digital"),
        PaymentMethod.CARD to listOf("card", "credit card", "debit card", "visa", "mastercard",
                                      "swipe", "tap", "contactless"),
        PaymentMethod.CASH to listOf("cash", "notes", "coins", "physical", "hand"),
        PaymentMethod.WALLET to listOf("wallet", "amazon pay", "mobikwik", "freecharge", "sodexo"),
    )

    fun parse(input: String): ParsedTransaction {
        val text = input.trim()
        val lower = text.lowercase()

        // Extract amount — take the last number found (most likely to be the price)
        val amount = amountRegex.findAll(lower)
            .map { it.groupValues[1].toDoubleOrNull() ?: 0.0 }
            .lastOrNull() ?: 0.0

        // Detect type
        val type = if (incomeKeywords.any { lower.contains(it) }) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }

        // Detect category
        val category = categoryKeywords.firstOrNull { (_, keywords) ->
            keywords.any { lower.contains(it) }
        }?.first ?: "Other"

        // Detect payment method
        val method = paymentMethodKeywords.firstOrNull { (_, keywords) ->
            keywords.any { lower.contains(it) }
        }?.first ?: PaymentMethod.UPI

        // Title: use original input, capitalised, max 50 chars
        val title = text.replaceFirstChar { it.uppercaseChar() }.take(50)

        return ParsedTransaction(
            title         = title,
            amount        = amount,
            type          = type,
            category      = category,
            paymentMethod = method
        )
    }
}
