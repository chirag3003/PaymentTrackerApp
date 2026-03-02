package codes.chirag.paymenttracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import codes.chirag.paymenttracker.core.data.repository.PreferencesRepository
import codes.chirag.paymenttracker.core.data.repository.TransactionRepository
import codes.chirag.paymenttracker.core.data.repository.UserProfileRepository
import codes.chirag.paymenttracker.core.model.CategorySpending
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.model.TransactionType
import codes.chirag.paymenttracker.feature.home.components.WeeklyBarData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val userName: String = "",
    val balance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val safeToSpend: Double = 0.0,
    val dailyBudget: Double = 0.0,
    val spentToday: Double = 0.0,
    val weeklySpending: List<WeeklyBarData> = emptyList(),
    val categorySpending: List<CategorySpending> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList()
)

class HomeViewModel(
    txRepo: TransactionRepository,
    profileRepo: UserProfileRepository,
    private val prefsRepo: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        txRepo.allTransactions,
        profileRepo.profile
    ) { transactions, profile ->
        val monthlyBudget = profile?.monthlyBudget?.toDoubleOrNull() ?: 0.0
        val userName = profile?.name ?: ""

        val income  = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = income - expense

        val todayLabel = todayLabel()
        val spentToday = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date == todayLabel }
            .sumOf { it.amount }

        val daysInMonth = 30
        val dailyBudget = if (monthlyBudget > 0) monthlyBudget / daysInMonth else 0.0
        val safeToSpend = (dailyBudget - spentToday).coerceAtLeast(0.0)

        // Category spending — read budgets from SharedPreferences
        val categoryBudgets = prefsRepo.getCategoryBudgets()
        val categorySpending = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .map { (cat, txList) ->
                CategorySpending(
                    category = cat,
                    amount   = txList.sumOf { it.amount },
                    budget   = categoryBudgets[cat] ?: 0.0
                )
            }
            .sortedByDescending { it.amount }
            .take(6)

        val weeklySpending = buildWeeklyData(transactions)

        HomeUiState(
            userName           = userName.ifBlank { "there" },
            balance            = balance,
            monthlyIncome      = income,
            monthlyExpense     = expense,
            safeToSpend        = safeToSpend,
            dailyBudget        = dailyBudget,
            spentToday         = spentToday,
            weeklySpending     = weeklySpending,
            categorySpending   = categorySpending,
            recentTransactions = transactions.take(5)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun todayLabel(): String {
        val cal = java.util.Calendar.getInstance()
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        return "${months[cal.get(java.util.Calendar.MONTH)]} ${cal.get(java.util.Calendar.DAY_OF_MONTH)}, ${cal.get(java.util.Calendar.YEAR)}"
    }

    private fun buildWeeklyData(transactions: List<Transaction>): List<WeeklyBarData> {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dateGroups = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.date }

        val distinctDates = dateGroups.keys
            .sortedWith(Comparator { a, b -> compareDateStrings(a, b) })
            .takeLast(7)

        return distinctDates.mapIndexed { index, date ->
            WeeklyBarData(
                day    = days.getOrElse(index) { date.take(3) },
                amount = dateGroups[date]?.sumOf { it.amount } ?: 0.0
            )
        }
    }

    private fun compareDateStrings(a: String, b: String): Int {
        fun rank(s: String) = when (s) {
            "Today"     -> Int.MAX_VALUE
            "Yesterday" -> Int.MAX_VALUE - 1
            else        -> parseYYYYMMDD(s)
        }
        return rank(a).compareTo(rank(b))
    }

    private fun parseYYYYMMDD(s: String): Int {
        return try {
            val parts = s.replace(",", "").split(" ")
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            val m = months.indexOf(parts[0]) + 1
            val d = parts[1].toInt()
            val y = parts[2].toInt()
            y * 10000 + m * 100 + d
        } catch (_: Exception) { 0 }
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    companion object {
        fun factory(
            txRepo: TransactionRepository,
            profileRepo: UserProfileRepository,
            prefsRepo: PreferencesRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeViewModel(txRepo, profileRepo, prefsRepo) as T
            }
    }
}

