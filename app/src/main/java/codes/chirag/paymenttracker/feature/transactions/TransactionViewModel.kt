package codes.chirag.paymenttracker.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import codes.chirag.paymenttracker.core.data.repository.TransactionRepository
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

enum class DateFilter(val label: String) {
    ALL("All"), TODAY("Today"), WEEK("This Week"), MONTH("This Month")
}

class TransactionViewModel(
    private val repo: TransactionRepository
) : ViewModel() {

    private val _all: StateFlow<List<Transaction>> = repo.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Filter state (public so UI can read & write) ──────────────────────────
    val searchQuery     = MutableStateFlow("")
    val activeFilter    = MutableStateFlow(DateFilter.ALL)
    val typeFilter      = MutableStateFlow<TransactionType?>(null)   // null = All
    val categoryFilter  = MutableStateFlow<String?>(null)             // null = All
    val dateRangeStart  = MutableStateFlow<String?>(null)             // "MMM d, yyyy" or null
    val dateRangeEnd    = MutableStateFlow<String?>(null)             // "MMM d, yyyy" or null

    /** True when any advanced filter is active. */
    val hasActiveFilters: StateFlow<Boolean> = combine(
        typeFilter, categoryFilter, dateRangeStart, dateRangeEnd
    ) { type, cat, start, end ->
        type != null || cat != null || start != null || end != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ── Derived filtered list ─────────────────────────────────────────────────

    val filtered: StateFlow<List<Transaction>> = combine(
        _all,
        searchQuery,
        activeFilter,
        typeFilter,
        categoryFilter
    ) { all, query, filter, type, cat ->
        // dateRangeStart/End are read directly inside the lambda via .value since
        // we can only combine up to 5 flows cleanly; date range is a secondary step.
        val startRank = dateRangeStart.value?.let { parseYYYYMMDD(it) }
        val endRank   = dateRangeEnd.value?.let { parseYYYYMMDD(it) }

        all.filter { tx ->
            // ── Date chip filter ──────────────────────────────────────────
            val matchesChip = when (filter) {
                DateFilter.ALL   -> true
                DateFilter.TODAY -> resolveDateRank(tx.date) == resolveDateRank("Today")
                DateFilter.WEEK  -> {
                    val txRank = resolveDateRank(tx.date)
                    val weekRanks = buildCalendarWeekRanks(0)
                    txRank in weekRanks
                }
                DateFilter.MONTH -> {
                    val txRank = resolveDateRank(tx.date)
                    val (monthStart, monthEnd) = currentMonthRanks()
                    txRank in monthStart..monthEnd
                }
            }

            // ── Search ────────────────────────────────────────────────────
            val matchesSearch = query.isBlank() ||
                tx.title.contains(query, ignoreCase = true) ||
                tx.category.contains(query, ignoreCase = true)

            // ── Advanced: Type ────────────────────────────────────────────
            val matchesType = type == null || tx.type == type

            // ── Advanced: Category ────────────────────────────────────────
            val matchesCat = cat == null || tx.category.equals(cat, ignoreCase = true)

            // ── Advanced: Date range ──────────────────────────────────────
            val txRank = resolveDateRank(tx.date)
            val matchesRange = (startRank == null || txRank >= startRank) &&
                               (endRank   == null || txRank <= endRank)

            matchesChip && matchesSearch && matchesType && matchesCat && matchesRange
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── CRUD ──────────────────────────────────────────────────────────────────

    fun add(
        title: String,
        amountStr: String,
        type: TransactionType,
        category: String,
        paymentMethod: PaymentMethod,
        notes: String,
        date: String = todayLabel()
    ) {
        val amount = amountStr.toDoubleOrNull() ?: return
        val tx = Transaction(
            id            = UUID.randomUUID().toString(),
            title         = title,
            amount        = amount,
            type          = type,
            category      = category,
            date          = date,
            paymentMethod = paymentMethod,
            notes         = notes
        )
        viewModelScope.launch { repo.add(tx) }
    }

    fun update(transaction: Transaction) {
        viewModelScope.launch { repo.update(transaction) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }

    suspend fun getById(id: String): Transaction? = repo.getById(id)

    // ── Filter helpers ────────────────────────────────────────────────────────

    fun clearAdvancedFilters() {
        typeFilter.value     = null
        categoryFilter.value = null
        dateRangeStart.value = null
        dateRangeEnd.value   = null
    }

    // ── Date helpers ──────────────────────────────────────────────────────────

    private fun todayLabel(): String {
        val cal = Calendar.getInstance()
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
    }

    private fun resolveDateRank(dateStr: String): Int {
        return when (dateStr) {
            "Today"     -> dateToRank(Calendar.getInstance())
            "Yesterday" -> dateToRank(Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, -1) })
            else        -> parseYYYYMMDD(dateStr)
        }
    }

    private fun dateToRank(cal: Calendar): Int {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return y * 10000 + m * 100 + d
    }

    /** Returns set of YYYYMMDD ints for the Sun–Sat week at [offset] weeks from now. */
    private fun buildCalendarWeekRanks(offset: Int): Set<Int> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, offset)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        return (0..6).map { i ->
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_MONTH, i)
            dateToRank(dayCal)
        }.toSet()
    }

    /** Returns (startRank, endRank) for the current calendar month. */
    private fun currentMonthRanks(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return Pair(y * 10000 + m * 100 + 1, y * 10000 + m * 100 + lastDay)
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

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        fun factory(repo: TransactionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TransactionViewModel(repo) as T
            }
    }
}
