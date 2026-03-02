package codes.chirag.paymenttracker.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import codes.chirag.paymenttracker.core.data.repository.TransactionRepository
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.Transaction
import codes.chirag.paymenttracker.core.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModel(
    private val repo: TransactionRepository
) : ViewModel() {

    private val _all: StateFlow<List<Transaction>> = repo.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val searchQuery = MutableStateFlow("")
    val activeFilter = MutableStateFlow(DateFilter.ALL)

    val filtered: StateFlow<List<Transaction>> = combine(_all, searchQuery, activeFilter) { all, query, filter ->
        all.filter { tx ->
            val matchesSearch = query.isBlank() ||
                tx.title.contains(query, ignoreCase = true) ||
                tx.category.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                DateFilter.ALL   -> true
                DateFilter.TODAY -> tx.date == todayLabel()
                DateFilter.WEEK  -> {
                    val weekDates = buildWeekDateSet()
                    tx.date in weekDates || tx.date == "Today" || tx.date == "Yesterday"
                }
                DateFilter.MONTH -> true
            }
            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(
        title: String,
        amountStr: String,
        type: TransactionType,
        category: String,
        paymentMethod: PaymentMethod,
        notes: String
    ) {
        val amount = amountStr.toDoubleOrNull() ?: return
        val tx = Transaction(
            id            = UUID.randomUUID().toString(),
            title         = title,
            amount        = amount,
            type          = type,
            category      = category,
            date          = todayLabel(),
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

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun todayLabel(): String {
        val cal = Calendar.getInstance()
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
    }

    /** Returns a set of date label strings for the past 7 days (formatted as "MMM d, yyyy"). */
    private fun buildWeekDateSet(): Set<String> {
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        val cal = Calendar.getInstance()
        val dates = mutableSetOf<String>()
        repeat(7) {
            dates += "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        return dates
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    companion object {
        fun factory(repo: TransactionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TransactionViewModel(repo) as T
            }
    }
}
