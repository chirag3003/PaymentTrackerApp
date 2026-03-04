package codes.chirag.paymenttracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import codes.chirag.paymenttracker.core.data.repository.PreferencesRepository
import codes.chirag.paymenttracker.core.data.repository.TransactionRepository
import codes.chirag.paymenttracker.core.data.repository.SubscriptionRepository
import codes.chirag.paymenttracker.core.data.repository.UserProfileRepository
import codes.chirag.paymenttracker.core.model.BalancePeriod
import codes.chirag.paymenttracker.core.model.CategorySpending
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.model.TransactionType
import codes.chirag.paymenttracker.feature.home.components.WeeklyBarData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class HomeUiState(
    val userName: String = "",
    // Balance card values — scoped to balancePeriod
    val balance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val balancePeriod: BalancePeriod = BalancePeriod.Monthly,
    // Daily budget
    val safeToSpend: Double = 0.0,
    val dailyBudget: Double = 0.0,
    val spentToday: Double = 0.0,
    // Home bar chart
    val weeklySpending: List<WeeklyBarData> = emptyList(),
    val homeWeekLabel: String = "",
    val homeWeekOffset: Int = 0,
    // Insights bar chart (independent offset)
    val insightsWeeklySpending: List<WeeklyBarData> = emptyList(),
    val insightsWeekLabel: String = "",
    val insightsWeekOffset: Int = 0,
    // Category + recent
    val categorySpending: List<CategorySpending> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList()
)

class HomeViewModel(
    txRepo: TransactionRepository,
    profileRepo: UserProfileRepository,
    private val prefsRepo: PreferencesRepository,
    subscriptionRepo: SubscriptionRepository
) : ViewModel() {

    // ── Public mutable state ─────────────────────────────────────────────────

    val balancePeriod = MutableStateFlow<BalancePeriod>(BalancePeriod.Monthly)

    private val _homeWeekOffset = MutableStateFlow(0)
    private val _insightsWeekOffset = MutableStateFlow(0)

    // ── UI state ─────────────────────────────────────────────────────────────

    private val txAndSubs = combine(
        txRepo.allTransactions,
        subscriptionRepo.all
    ) { transactions, subscriptions ->
        transactions to subscriptions
    }

    val uiState: StateFlow<HomeUiState> = combine(
        txAndSubs,
        profileRepo.profile,
        balancePeriod,
        _homeWeekOffset,
        _insightsWeekOffset
    ) { (transactions, subscriptions), profile, period, homeOffset, insightsOffset ->
        val monthlyBudget = profile?.monthlyBudget?.toDoubleOrNull() ?: 0.0
        val userName = profile?.name ?: ""

        // ── Balance card — filtered by period ──────────────────────────────
        val periodTxns = filterByPeriod(transactions, period)
        val income  = periodTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = periodTxns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = income - expense

        // ── Daily budget ──────────────────────────────────────────────────
        val todayLbl = todayLabel()
        val spentToday = transactions
            .filter { it.type == TransactionType.EXPENSE && it.date == todayLbl }
            .sumOf { it.amount }
        val cal = Calendar.getInstance()
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val todayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val remainingDays = (daysInMonth - todayOfMonth + 1).coerceAtLeast(1)

        // Monthly expense so far
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        val monthlyExpensesSoFar = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .filter { tx ->
                val rank = resolveDateRank(tx.date)
                val txCal = rankToCalendar(rank) ?: return@filter false
                txCal.get(Calendar.MONTH) == currentMonth &&
                txCal.get(Calendar.YEAR) == currentYear
            }
            .sumOf { it.amount }

        // Subtract future subscription expenses due later this month
        val futureSubscriptionExpenses = subscriptions
            .filter { it.isActive && it.type == TransactionType.EXPENSE }
            .filter { sub ->
                val subCal = parseDateLabel(sub.nextDueDate) ?: return@filter false
                val isSameMonth = subCal.get(Calendar.MONTH) == currentMonth &&
                    subCal.get(Calendar.YEAR) == currentYear
                val isFuture = subCal.get(Calendar.DAY_OF_MONTH) > todayOfMonth
                isSameMonth && isFuture
            }
            .sumOf { it.amount }

        val remainingBudget = (monthlyBudget - monthlyExpensesSoFar - futureSubscriptionExpenses)
        val dailyBudget = if (monthlyBudget > 0) remainingBudget / remainingDays else 0.0
        val safeToSpend = (dailyBudget - spentToday).coerceAtLeast(0.0)

        // ── Category spending (always all-time) ────────────────────────────
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

        // ── Weekly charts ──────────────────────────────────────────────────
        val (homeWeekData, homeWeekLabel) = buildWeeklyData(transactions, homeOffset)
        val (insightsWeekData, insightsWeekLabel) = buildWeeklyData(transactions, insightsOffset)

        HomeUiState(
            userName              = userName.ifBlank { "there" },
            balance               = balance,
            monthlyIncome         = income,
            monthlyExpense        = expense,
            balancePeriod         = period,
            safeToSpend           = safeToSpend,
            dailyBudget           = dailyBudget,
            spentToday            = spentToday,
            weeklySpending        = homeWeekData,
            homeWeekLabel         = homeWeekLabel,
            homeWeekOffset        = homeOffset,
            insightsWeeklySpending = insightsWeekData,
            insightsWeekLabel     = insightsWeekLabel,
            insightsWeekOffset    = insightsOffset,
            categorySpending      = categorySpending,
            recentTransactions    = transactions.take(5)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    // ── Period selector ───────────────────────────────────────────────────────

    fun setBalancePeriod(period: BalancePeriod) {
        balancePeriod.value = period
    }

    // ── Week navigation ───────────────────────────────────────────────────────

    fun homeWeekPrev()  { _homeWeekOffset.value -= 1 }
    fun homeWeekNext()  { if (_homeWeekOffset.value < 0) _homeWeekOffset.value += 1 }

    fun insightsWeekPrev()  { _insightsWeekOffset.value -= 1 }
    fun insightsWeekNext()  { if (_insightsWeekOffset.value < 0) _insightsWeekOffset.value += 1 }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun todayLabel(): String {
        val cal = Calendar.getInstance()
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
    }

    /**
     * Filter transactions to only those belonging to the given [BalancePeriod].
     */
    private fun filterByPeriod(
        transactions: List<Transaction>,
        period: BalancePeriod
    ): List<Transaction> {
        return when (period) {
            is BalancePeriod.AllTime -> transactions

            is BalancePeriod.Monthly -> {
                val cal = Calendar.getInstance()
                val currentMonth = cal.get(Calendar.MONTH)
                val currentYear  = cal.get(Calendar.YEAR)
                transactions.filter { tx ->
                    val rank = resolveDateRank(tx.date)
                    val txCal = rankToCalendar(rank) ?: return@filter false
                    txCal.get(Calendar.MONTH) == currentMonth &&
                    txCal.get(Calendar.YEAR)  == currentYear
                }
            }

            is BalancePeriod.FromDate -> {
                val startRank = parseYYYYMMDD(period.startLabel)
                if (startRank == 0) transactions
                else transactions.filter { tx ->
                    val rank = resolveDateRank(tx.date)
                    rank >= startRank
                }
            }
        }
    }

    /**
     * Resolve a transaction date string ("Today", "Yesterday", or "MMM d, yyyy") to an
     * integer in YYYYMMDD format for easy comparison.
     */
    private fun resolveDateRank(dateStr: String): Int {
        return when (dateStr) {
            "Today" -> {
                val cal = Calendar.getInstance()
                dateToRank(cal)
            }
            "Yesterday" -> {
                val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, -1) }
                dateToRank(cal)
            }
            else -> parseYYYYMMDD(dateStr)
        }
    }

    private fun dateToRank(cal: Calendar): Int {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return y * 10000 + m * 100 + d
    }

    private fun rankToCalendar(rank: Int): Calendar? {
        if (rank == 0) return null
        val y = rank / 10000
        val m = (rank / 100) % 100 - 1
        val d = rank % 100
        return Calendar.getInstance().also {
            it.set(y, m, d, 0, 0, 0)
            it.set(Calendar.MILLISECOND, 0)
        }
    }

    /**
     * Builds a true Sun–Sat weekly bar chart for the week at [offset] weeks from now.
     * offset=0 → current week, offset=-1 → last week, etc.
     *
     * Returns a [Pair] of the bar data list and a display label like "Feb 23 – Mar 1".
     */
    private fun buildWeeklyData(
        transactions: List<Transaction>,
        offset: Int
    ): Pair<List<WeeklyBarData>, String> {
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

        // Find Sunday of the target week
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, offset)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Build 7 day slots: Sun Mon Tue Wed Thu Fri Sat
        val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dayRanks = (0..6).map { i ->
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_MONTH, i)
            dateToRank(dayCal)
        }

        // Week label (e.g. "Feb 23 – Mar 1")
        val startCal = cal.clone() as Calendar
        val endCal   = (cal.clone() as Calendar).also { it.add(Calendar.DAY_OF_MONTH, 6) }
        val weekLabel = buildString {
            append(months[startCal.get(Calendar.MONTH)])
            append(" ${startCal.get(Calendar.DAY_OF_MONTH)}")
            append(" – ")
            append(months[endCal.get(Calendar.MONTH)])
            append(" ${endCal.get(Calendar.DAY_OF_MONTH)}")
        }

        // Sum expenses per day slot
        val expenseTxns = transactions.filter { it.type == TransactionType.EXPENSE }
        val sumByRank: Map<Int, Double> = expenseTxns
            .groupBy { resolveDateRank(it.date) }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        val bars = dayRanks.mapIndexed { i, rank ->
            WeeklyBarData(
                day    = dayLabels[i],
                amount = sumByRank[rank] ?: 0.0
            )
        }
        return Pair(bars, weekLabel)
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

    private fun parseDateLabel(label: String): Calendar? {
        return try {
            val parts = label.replace(",", "").split(" ")
            if (parts.size != 3) return null
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            val m = months.indexOf(parts[0])
            val d = parts[1].toInt()
            val y = parts[2].toInt()
            if (m == -1) return null
            Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (_: Exception) { null }
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    companion object {
        fun factory(
            txRepo: TransactionRepository,
            profileRepo: UserProfileRepository,
            prefsRepo: PreferencesRepository,
            subscriptionRepo: SubscriptionRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeViewModel(txRepo, profileRepo, prefsRepo, subscriptionRepo) as T
            }
    }
}
