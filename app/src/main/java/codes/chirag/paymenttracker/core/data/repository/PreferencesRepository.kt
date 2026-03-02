package codes.chirag.paymenttracker.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import codes.chirag.paymenttracker.PREFS_NAME
import org.json.JSONObject

/**
 * Centralises SharedPreferences access for app-level preferences.
 * Used by both HomeViewModel and SettingsViewModel to avoid duplicating prefs keys.
 */
class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Custom categories ─────────────────────────────────────────────────────

    fun getCustomCategories(): List<String> {
        val csv = prefs.getString("custom_categories", "") ?: ""
        return if (csv.isBlank()) emptyList()
               else csv.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    fun saveCustomCategories(categories: List<String>) {
        prefs.edit().putString("custom_categories", categories.joinToString(",")).apply()
    }

    // ── Category budgets (stored as JSON: {"Food": 3000.0, "Transport": 1000.0}) ──

    fun getCategoryBudgets(): Map<String, Double> {
        val json = prefs.getString("category_budgets", null) ?: return emptyMap()
        return try {
            val obj = JSONObject(json)
            buildMap {
                obj.keys().forEach { key -> put(key, obj.getDouble(key)) }
            }
        } catch (_: Exception) { emptyMap() }
    }

    fun saveCategoryBudgets(budgets: Map<String, Double>) {
        val obj = JSONObject()
        budgets.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString("category_budgets", obj.toString()).apply()
    }

    fun setCategoryBudget(category: String, budget: Double) {
        val current = getCategoryBudgets().toMutableMap()
        if (budget <= 0.0) current.remove(category) else current[category] = budget
        saveCategoryBudgets(current)
    }
}
