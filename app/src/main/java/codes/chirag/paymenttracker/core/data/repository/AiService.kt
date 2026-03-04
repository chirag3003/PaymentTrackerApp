package codes.chirag.paymenttracker.core.data.repository

import android.content.Context
import android.net.Uri
import codes.chirag.paymenttracker.core.model.PaymentMethod
import codes.chirag.paymenttracker.core.model.TransactionType
import codes.chirag.paymenttracker.core.utils.ImageUtils
import codes.chirag.paymenttracker.core.utils.ParsedTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiService(
    private val securePrefs: SecurePreferencesRepository
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val systemPrompt = """
        You are a highly intelligent financial assistant. Your task is to extract transaction details and return ONLY a valid JSON object. Do NOT include markdown formatting like ```json.
        Do NOT include any comments inside the JSON.
        The JSON must match this structure exactly:
        {
            "title": "Short descriptive title (max 50 chars)",
            "amount": 0.0,
            "type": "EXPENSE" | "INCOME",
            "category": "Food" | "Transport" | "Shopping" | "Entertainment" | "Groceries" | "Health" | "Education" | "Other",
            "paymentMethod": "UPI" | "CARD" | "CASH" | "WALLET"
        }
    """.trimIndent()

    suspend fun parseText(input: String): ParsedTransaction? = withContext(Dispatchers.IO) {
        val model = securePrefs.getActiveAiModel()
        if (model == "GEMINI") {
            val key = securePrefs.getGeminiApiKey()
            if (key.isBlank()) throw Exception("Gemini API key is not set")
            parseWithGeminiText(key, input)
        } else {
            val key = securePrefs.getAnthropicApiKey()
            if (key.isBlank()) throw Exception("Anthropic API key is not set")
            parseWithClaudeText(key, input)
        }
    }

    suspend fun parseReceipt(context: Context, uri: Uri): ParsedTransaction? = withContext(Dispatchers.IO) {
        val base64Image = ImageUtils.getCompressedBase64FromUri(context, uri) ?: throw Exception("Failed to process image")
        val model = securePrefs.getActiveAiModel()
        
        if (model == "GEMINI") {
            val key = securePrefs.getGeminiApiKey()
            if (key.isBlank()) throw Exception("Gemini API key is not set")
            parseWithGeminiVision(key, base64Image)
        } else {
            val key = securePrefs.getAnthropicApiKey()
            if (key.isBlank()) throw Exception("Anthropic API key is not set")
            parseWithClaudeVision(key, base64Image)
        }
    }

    // ── Gemini Implementations ───────────────────────────────────────────────────

    private fun parseWithGeminiText(apiKey: String, text: String): ParsedTransaction? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${apiKey}"
        
        val jsonPayload = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", org.json.JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply { put("text", "Extract details from: $text") })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonPayload.toString().toRequestBody(jsonMediaType))
            .build()

        return executeAndParse(request, ::extractGeminiResponse)
    }

    private fun parseWithGeminiVision(apiKey: String, base64Image: String): ParsedTransaction? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${apiKey}"
        
        val jsonPayload = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", org.json.JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply { put("text", "Extract transaction details from this receipt.") })
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonPayload.toString().toRequestBody(jsonMediaType))
            .build()

        return executeAndParse(request, ::extractGeminiResponse)
    }

    private fun extractGeminiResponse(responseString: String): String {
        val root = JSONObject(responseString)
        val candidates = root.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "")
            }
        }
        return ""
    }

    // ── Claude Implementations ───────────────────────────────────────────────────

    private fun parseWithClaudeText(apiKey: String, text: String): ParsedTransaction? {
        val url = "https://api.anthropic.com/v1/messages"
        
        val jsonPayload = JSONObject().apply {
            put("model", "claude-3-haiku-20240307")
            put("max_tokens", 1024)
            put("system", systemPrompt)
            put("messages", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "Extract details from: $text")
                })
            })
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(jsonPayload.toString().toRequestBody(jsonMediaType))
            .build()

        return executeAndParse(request, ::extractClaudeResponse)
    }

    private fun parseWithClaudeVision(apiKey: String, base64Image: String): ParsedTransaction? {
        val url = "https://api.anthropic.com/v1/messages"
        
        val jsonPayload = JSONObject().apply {
            put("model", "claude-3-haiku-20240307")
            put("max_tokens", 1024)
            put("system", systemPrompt)
            put("messages", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "image")
                            put("source", JSONObject().apply {
                                put("type", "base64")
                                put("media_type", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                        put(JSONObject().apply {
                            put("type", "text")
                            put("text", "Extract transaction details from this receipt.")
                        })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(jsonPayload.toString().toRequestBody(jsonMediaType))
            .build()

        return executeAndParse(request, ::extractClaudeResponse)
    }

    private fun extractClaudeResponse(responseString: String): String {
        val root = JSONObject(responseString)
        val contentArray = root.optJSONArray("content")
        if (contentArray != null && contentArray.length() > 0) {
            return contentArray.getJSONObject(0).optString("text", "")
        }
        return ""
    }

    // ── Common execution ─────────────────────────────────────────────────────────

    private fun executeAndParse(request: Request, responseExtractor: (String) -> String): ParsedTransaction? {
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: ""
            throw Exception("API Error ${response.code}: $errBody")
        }
        val bodyStr = response.body?.string() ?: throw Exception("Empty response body")
        val rawJson = responseExtractor(bodyStr)
        
        // Fallback parsing: find the first '{' and last '}' to handle unexpected LLM formatting
        val startIndex = rawJson.indexOf('{')
        val endIndex = rawJson.lastIndexOf('}')
        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            throw Exception("Failed to extract JSON from model response")
        }
        val cleanJson = rawJson.substring(startIndex, endIndex + 1)
        
        val jsonObj = JSONObject(cleanJson)
        return ParsedTransaction(
            title = jsonObj.optString("title", "Unknown"),
            amount = jsonObj.optDouble("amount", 0.0),
            type = runCatching { TransactionType.valueOf(jsonObj.optString("type", "EXPENSE").uppercase()) }.getOrDefault(TransactionType.EXPENSE),
            category = jsonObj.optString("category", "Other"),
            paymentMethod = runCatching { PaymentMethod.valueOf(jsonObj.optString("paymentMethod", "UPI").uppercase()) }.getOrDefault(PaymentMethod.UPI)
        )
    }
}
