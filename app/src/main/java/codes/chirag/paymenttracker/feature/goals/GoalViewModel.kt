package codes.chirag.paymenttracker.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import codes.chirag.paymenttracker.core.data.repository.GoalRepository
import codes.chirag.paymenttracker.core.model.Goal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class GoalViewModel(
    private val repo: GoalRepository
) : ViewModel() {

    val goals: StateFlow<List<Goal>> = repo.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addGoal(name: String, targetStr: String, targetDate: String) {
        val target = targetStr.toDoubleOrNull() ?: return
        val goal = Goal(
            id           = UUID.randomUUID().toString(),
            name         = name,
            targetAmount = target,
            savedAmount  = 0.0,
            targetDate   = targetDate
        )
        viewModelScope.launch { repo.add(goal) }
    }

    fun contribute(goalId: String, amount: Double) {
        viewModelScope.launch {
            val goal = repo.getById(goalId) ?: return@launch
            val newSaved = (goal.savedAmount + amount).coerceAtMost(goal.targetAmount)
            repo.update(goal.copy(savedAmount = newSaved))
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }

    suspend fun getById(id: String): Goal? = repo.getById(id)

    // ── Factory ──────────────────────────────────────────────────────────────

    companion object {
        fun factory(repo: GoalRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    GoalViewModel(repo) as T
            }
    }
}
