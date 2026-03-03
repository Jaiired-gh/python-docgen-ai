package com.github.pythondocgen.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class ClaudeApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private val GROQ_MODEL = "llama-3.3-70b-versatile"
    private val GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"

    data class ApiResult(
        val success: Boolean,
        val docstring: String = "",
        val error: String = ""
    )

    fun generateDocstring(functionCode: String, settings: DocGenSettings): ApiResult {
        if (settings.apiKey.isBlank()) {
            return ApiResult(false, error = "API key not configured. Go to Settings → Tools → Python DocGen AI")
        }

        val prompt = buildPrompt(functionCode, settings)

        val requestBody = gson.toJson(mapOf(
            "model" to GROQ_MODEL,
            "max_tokens" to 1024,
            "temperature" to 0.2,
            "messages" to listOf(
                mapOf(
                    "role" to "user",
                    "content" to prompt
                )
            )
        ))

        val request = Request.Builder()
            .url(GROQ_API_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${settings.apiKey}")
            .post(requestBody.toRequestBody(JSON))
            .build()

        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return ApiResult(false, error = "Groq API error (${response.code}): $body")
            }

            val json = gson.fromJson(body, JsonObject::class.java)
            val text = json
                .getAsJsonArray("choices")?.firstOrNull()?.asJsonObject
                ?.getAsJsonObject("message")
                ?.get("content")?.asString
                ?: return ApiResult(false, error = "Invalid Groq API response")

            ApiResult(true, docstring = cleanDocstring(text))

        } catch (e: IOException) {
            ApiResult(false, error = "Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult(false, error = "Unexpected error: ${e.message}")
        }
    }

    private fun buildPrompt(functionCode: String, settings: DocGenSettings): String {
        val formatDesc = when (settings.docstringFormat) {
            "numpy" -> "NumPy (with Parameters, Returns, Raises sections separated by '---')"
            "rst" -> "reStructuredText (with :param:, :type:, :returns:, :rtype:, :raises:)"
            else -> "Google Style (with indented Args, Returns, Raises sections)"
        }
        val lang = if (settings.language == "italian") "Italian" else "English"
        val examplesNote = if (settings.includeExamples) "Also include an 'Examples' section." else ""
        val raisesNote = if (settings.includeRaises) "If exceptions are raised, include the Raises section." else ""

        return "You are a Python expert. Analyze the following function and generate ONLY the docstring.\n\n" +
                "Format: $formatDesc\nLanguage: $lang\n$examplesNote\n$raisesNote\n\n" +
                "Rules:\n- Reply ONLY with the docstring (including triple quotes)\n" +
                "- Do NOT include the function definition\n" +
                "- Do NOT include markdown code blocks\n\n" +
                "Function:\n$functionCode"
    }

    private fun cleanDocstring(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.lines()
                .dropWhile { it.startsWith("```") }
                .dropLastWhile { it.startsWith("```") }
                .joinToString("\n").trim()
        }
        return cleaned
    }
}