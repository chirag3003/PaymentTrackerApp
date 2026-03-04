package codes.chirag.paymenttracker.feature.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.ShareCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import codes.chirag.paymenttracker.PREFS_NAME
import codes.chirag.paymenttracker.core.data.repository.PreferencesRepository
import codes.chirag.paymenttracker.core.data.repository.SecurePreferencesRepository
import codes.chirag.paymenttracker.core.data.repository.TransactionRepository
import codes.chirag.paymenttracker.core.data.repository.UserProfileRepository
import codes.chirag.paymenttracker.core.database.entities.UserProfileEntity
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsViewModel(
    private val profileRepo: UserProfileRepository,
    private val txRepo: TransactionRepository,
    private val prefs: SharedPreferences,
    private val prefsRepo: PreferencesRepository,
    private val securePrefs: SecurePreferencesRepository
) : ViewModel() {

    // ── Profile ───────────────────────────────────────────────────────────────

    val profile: StateFlow<UserProfileEntity?> = profileRepo.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── All transactions (for CSV export) ────────────────────────────────────

    val allTransactions: StateFlow<List<Transaction>> = txRepo.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Profile mutations ─────────────────────────────────────────────────────

    fun updateProfile(name: String, budget: String, method: PaymentMethod) {
        viewModelScope.launch {
            profileRepo.save(
                name            = name,
                monthlyBudget   = budget,
                preferredMethod = method.name
            )
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            val current = profile.value
            profileRepo.save(
                name            = name,
                monthlyBudget   = current?.monthlyBudget ?: "",
                preferredMethod = current?.preferredMethod ?: ""
            )
        }
    }

    fun updateBudget(budget: String) {
        viewModelScope.launch {
            val current = profile.value
            profileRepo.save(
                name            = current?.name ?: "",
                monthlyBudget   = budget,
                preferredMethod = current?.preferredMethod ?: ""
            )
        }
    }

    // ── Notification toggles (SharedPreferences) ──────────────────────────────

    fun getPushNotifications(): Boolean = prefs.getBoolean("push_notifs", true)
    fun getBillReminders(): Boolean     = prefs.getBoolean("bill_reminders", true)
    fun getBudgetAlerts(): Boolean      = prefs.getBoolean("budget_alerts", false)

    fun setToggle(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    // ── Biometric lock ────────────────────────────────────────────────────────

    fun getBiometricLock(): Boolean = prefs.getBoolean("biometric_lock", false)

    fun setBiometricLock(enabled: Boolean) {
        prefs.edit()
            .putBoolean("biometric_lock", enabled)
            .apply { if (!enabled) putBoolean("needs_unlock", false) }
            .apply()
    }

    // ── Custom categories (delegated to PreferencesRepository) ───────────────

    fun getCustomCategories(): List<String> = prefsRepo.getCustomCategories()

    fun saveCustomCategories(categories: List<String>) = prefsRepo.saveCustomCategories(categories)

    // ── Category budgets ──────────────────────────────────────────────────────

    fun getCategoryBudgets(): Map<String, Double> = prefsRepo.getCategoryBudgets()

    fun setCategoryBudget(category: String, budget: Double) =
        prefsRepo.setCategoryBudget(category, budget)

    // ── AI Configuration ──────────────────────────────────────────────────────

    fun getActiveAiModel(): String = securePrefs.getActiveAiModel()
    fun setActiveAiModel(model: String) = securePrefs.setActiveAiModel(model)

    fun getGeminiApiKey(): String = securePrefs.getGeminiApiKey()
    fun setGeminiApiKey(key: String) = securePrefs.setGeminiApiKey(key)

    fun getAnthropicApiKey(): String = securePrefs.getAnthropicApiKey()
    fun setAnthropicApiKey(key: String) = securePrefs.setAnthropicApiKey(key)

    // ── CSV export ────────────────────────────────────────────────────────────

    fun exportCsv(context: Context) {
        val transactions = allTransactions.value
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "transactions_$timestamp.csv"
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val file = File(dir, fileName)

        file.bufferedWriter().use { writer ->
            writer.write("ID,Title,Amount,Type,Category,Date,PaymentMethod,Notes,Tags\n")
            transactions.forEach { tx ->
                val tags  = tx.tags.joinToString("|")
                val notes = tx.notes.replace(",", ";").replace("\n", " ")
                writer.write("${tx.id},${tx.title},${tx.amount},${tx.type.name},${tx.category},${tx.date},${tx.paymentMethod.name},$notes,$tags\n")
            }
        }

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        ShareCompat.IntentBuilder(context)
            .setType("text/csv")
            .setStream(uri)
            .setChooserTitle("Export Transactions")
            .startChooser()
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        fun factory(
            profileRepo: UserProfileRepository,
            txRepo: TransactionRepository,
            context: Context,
            prefsRepo: PreferencesRepository? = null
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val repo = prefsRepo ?: PreferencesRepository(context)
                    val secureRepo = SecurePreferencesRepository(context)
                    return SettingsViewModel(profileRepo, txRepo, prefs, repo, secureRepo) as T
                }
            }
    }
}

