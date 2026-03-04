package codes.chirag.paymenttracker.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import codes.chirag.paymenttracker.core.data.repository.SubscriptionRepository
import codes.chirag.paymenttracker.core.model.BillingFrequency
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.Subscription
import codes.chirag.paymenttracker.core.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class SubscriptionViewModel(
    private val repo: SubscriptionRepository
) : ViewModel() {

    val subscriptions: StateFlow<List<Subscription>> =
        repo.all.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(
        name: String,
        amount: String,
        type: TransactionType,
        frequency: BillingFrequency,
        nextDueDate: String,
        category: String,
        paymentMethod: PaymentMethod
    ) {
        val amt = amount.toDoubleOrNull() ?: return
        viewModelScope.launch {
            repo.add(
                Subscription(
                    id            = UUID.randomUUID().toString(),
                    name          = name.trim(),
                    amount        = amt,
                    type          = type,
                    frequency     = frequency,
                    nextDueDate   = nextDueDate.ifBlank { nextMonthLabel() },
                    category      = category,
                    paymentMethod = paymentMethod,
                    isActive      = true,
                    lastProcessedDate = ""
                )
            )
        }
    }

    fun updateSubscription(
        id: String,
        name: String,
        amount: String,
        type: TransactionType,
        frequency: BillingFrequency,
        nextDueDate: String,
        category: String,
        paymentMethod: PaymentMethod,
        isActive: Boolean,
        lastProcessedDate: String
    ) {
        val amt = amount.toDoubleOrNull() ?: return
        viewModelScope.launch {
            repo.update(
                Subscription(
                    id            = id,
                    name          = name.trim(),
                    amount        = amt,
                    type          = type,
                    frequency     = frequency,
                    nextDueDate   = nextDueDate.ifBlank { nextMonthLabel() },
                    category      = category,
                    paymentMethod = paymentMethod,
                    isActive      = isActive,
                    lastProcessedDate = lastProcessedDate
                )
            )
        }
    }

    fun toggleActive(subscription: Subscription) {
        viewModelScope.launch {
            repo.update(subscription.copy(isActive = !subscription.isActive))
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }

    private fun nextMonthLabel(): String {
        val cal = Calendar.getInstance().also { it.add(Calendar.MONTH, 1) }
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
    }

    companion object {
        fun factory(repo: SubscriptionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SubscriptionViewModel(repo) as T
            }
    }
}
