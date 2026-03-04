package codes.chirag.paymenttracker.core.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePreferencesRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_ai_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getActiveAiModel(): String {
        return prefs.getString(KEY_ACTIVE_MODEL, "GEMINI") ?: "GEMINI"
    }

    fun setActiveAiModel(model: String) {
        prefs.edit().putString(KEY_ACTIVE_MODEL, model).apply()
    }

    fun getGeminiApiKey(): String {
        return prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
    }

    fun setGeminiApiKey(key: String) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, key).apply()
    }

    fun getAnthropicApiKey(): String {
        return prefs.getString(KEY_ANTHROPIC_API_KEY, "") ?: ""
    }

    fun setAnthropicApiKey(key: String) {
        prefs.edit().putString(KEY_ANTHROPIC_API_KEY, key).apply()
    }

    companion object {
        private const val KEY_ACTIVE_MODEL = "active_ai_model"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_ANTHROPIC_API_KEY = "anthropic_api_key"
    }
}
