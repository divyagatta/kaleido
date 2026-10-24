package com.example.checkin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

interface QuestionGenerator {
    suspend fun checkInQuestions(apps: List<String>, history: List<Reflection>): List<Question>
}

/**
 * Calls a backend proxy that forwards the body to Anthropic's /v1/messages and returns the raw response.
 * Never ship an API key inside the APK.
 */
class ClaudeQuestionGenerator(
    private val proxyUrl: String = BuildConfig.PROXY_URL,
    private val model: String = "claude-sonnet-5-5",
) : QuestionGenerator {

    override suspend fun checkInQuestions(apps: List<String>, history: List<Reflection>): List<Question> =
        withContext(Dispatchers.IO) {
            if (proxyUrl.isBlank()) return@withContext Fallback.questions(apps)
            try { call(apps, history) } catch (e: Exception) { Fallback.questions(apps) }
        }

    private fun call(apps: List<String>, history: List<Reflection>): List<Question> {
        val system = """You write short mindful check-in surveys for someone who just spent time on: ${apps.joinToString()}.
Write 3-5 questions: a mix of CHOICE (3-4 options) and OPEN. Reference the user's earlier reflections
when relevant (patterns, contradictions, goals they stated). Never repeat earlier wording. Be warm, not preachy.
Reply with ONLY a JSON array: [{"text":"...","type":"CHOICE"|"OPEN","options":["..."]}]"""
        val past = history.joinToString("\n\n") { r ->
            r.answers.joinToString("\n") { "Q: ${it.question}\nA: ${it.answer}" }
        }
        val body = JSONObject().apply {
            put("model", model); put("max_tokens", 1000); put("system", system)
            put("messages", JSONArray().put(JSONObject().put("role", "user")
                .put("content", "Earlier reflections:\n$past\n\nGenerate today's check-in.")))
        }
        val conn = (URL(proxyUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; doOutput = true; connectTimeout = 10_000; readTimeout = 20_000
            setRequestProperty("Content-Type", "application/json")
        }
        conn.outputStream.use { it.write(body.toString().toByteArray()) }
        val text = JSONObject(conn.inputStream.bufferedReader().readText())
            .getJSONArray("content").getJSONObject(0).getString("text")
            .replace("```json", "").replace("```", "").trim()
        val arr = JSONArray(text)
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            val opts = o.optJSONArray("options")?.let { a -> (0 until a.length()).map(a::getString) } ?: emptyList()
            Question(o.getString("text"), if (o.getString("type") == "CHOICE") QType.CHOICE else QType.OPEN, opts)
        }
    }
}

/** Offline / error fallback so the lock never dead-ends. */
object Fallback {
    fun questions(apps: List<String>): List<Question> {
        val a = apps.joinToString(" and ").ifBlank { "your apps" }
        return listOf(
            Question("How do you feel right now compared to when you started on $a?", QType.CHOICE,
                listOf("Better", "About the same", "Drained", "Anxious or irritated")),
            Question("What was the most memorable thing you saw? Why did it stick?", QType.OPEN),
            Question("Did you open $a on purpose, or out of habit?", QType.CHOICE,
                listOf("On purpose", "Mostly habit", "Both")),
            Question("What could you do with the next 30 minutes that you'd feel good about later?", QType.OPEN),
        ).shuffled()
    }
}
