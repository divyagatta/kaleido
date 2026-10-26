package com.example.checkin

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class QType { CHOICE, OPEN }
data class Question(val text: String, val type: QType, val options: List<String> = emptyList())
data class QA(val question: String, val answer: String)
data class Reflection(val time: Long, val kind: String, val apps: List<String>, val answers: List<QA>)

fun Context.appLabel(pkg: String): String = try {
    packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
} catch (e: Exception) { pkg }

/** Simple SharedPreferences-backed store. Swap for Room/DataStore later if history grows. */
class Store(ctx: Context) {
    private val p = ctx.applicationContext.getSharedPreferences("checkin", Context.MODE_PRIVATE)

    var monitoredApps: Set<String>
        get() = p.getStringSet("apps", emptySet()) ?: emptySet()
        set(v) = p.edit().putStringSet("apps", v).apply()
    var intervalMin: Int
        get() = p.getInt("interval", 30); set(v) = p.edit().putInt("interval", v).apply()
    var onboarded: Boolean
        get() = p.getBoolean("onboarded", false); set(v) = p.edit().putBoolean("onboarded", v).apply()
    var currentStep: String
        get() = p.getString("step", "HOW") ?: "HOW"; set(v) = p.edit().putString("step", v).apply()
    var locked: Boolean
        get() = p.getBoolean("locked", false); set(v) = p.edit().putBoolean("locked", v).apply()
    var usedMs: Long
        get() = p.getLong("usedMs", 0); set(v) = p.edit().putLong("usedMs", v).apply()
    var lastApp: String?
        get() = p.getString("lastApp", null); set(v) = p.edit().putString("lastApp", v).apply()

    fun reflections(): List<Reflection> {
        val arr = JSONArray(p.getString("reflections", "[]"))
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Reflection(
                o.getLong("time"), o.getString("kind"),
                o.getJSONArray("apps").let { a -> (0 until a.length()).map(a::getString) },
                o.getJSONArray("qa").let { a -> (0 until a.length()).map {
                    j -> a.getJSONObject(j).let { QA(it.getString("q"), it.getString("a")) } } }
            )
        }
    }

    fun add(r: Reflection) {
        val arr = JSONArray(p.getString("reflections", "[]"))
        arr.put(JSONObject().apply {
            put("time", r.time); put("kind", r.kind); put("apps", JSONArray(r.apps))
            put("qa", JSONArray(r.answers.map { JSONObject().put("q", it.question).put("a", it.answer) }))
        })
        p.edit().putString("reflections", arr.toString()).apply()
    }
}
