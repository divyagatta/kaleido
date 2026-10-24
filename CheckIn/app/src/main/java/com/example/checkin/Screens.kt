package com.example.checkin

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class AppInfo(val label: String, val pkg: String)

fun launchableApps(ctx: Context): List<AppInfo> {
    val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return ctx.packageManager.queryIntentActivities(i, 0)
        .map { AppInfo(it.loadLabel(ctx.packageManager).toString(), it.activityInfo.packageName) }
        .filter { it.pkg != ctx.packageName }.distinctBy { it.pkg }.sortedBy { it.label.lowercase() }
}

@Composable
fun Page(title: String, body: String? = null, cta: String, ctaEnabled: Boolean = true, onCta: () -> Unit,
         content: @Composable ColumnScope.() -> Unit = {}) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        body?.let { Spacer(Modifier.height(8.dp)); Text(it) }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.weight(1f)) { content() }
        Button(onCta, enabled = ctaEnabled, modifier = Modifier.fillMaxWidth()) { Text(cta) }
    }
}

@Composable fun HowItWorksScreen(onNext: () -> Unit) = Page(
    "How it works",
    "Pick the apps you want to be more mindful of and how often to check in. After that much time in them, " +
        "the app pauses and asks a few questions about what you saw and how it made you feel. " +
        "Answer, and you're back in. The questions change every time and build on your earlier answers.",
    "Get started", onCta = onNext)

@Composable fun PermissionsScreen(usageGranted: Boolean, overlayGranted: Boolean,
    onUsage: () -> Unit, onOverlay: () -> Unit, onNext: () -> Unit) = Page(
    "Permissions", "CheckIn needs two permissions to notice when you're in a selected app and to show the check-in on top of it.",
    "Continue", ctaEnabled = usageGranted && overlayGranted, onCta = onNext) {
    Button(onUsage, enabled = !usageGranted) { Text(if (usageGranted) "Usage access ✓" else "Grant usage access") }
    Spacer(Modifier.height(8.dp))
    Button(onOverlay, enabled = !overlayGranted) { Text(if (overlayGranted) "Display over apps ✓" else "Allow display over apps") }
}

@Composable fun SelectAppsScreen(apps: List<AppInfo>, selected: Set<String>, onToggle: (String) -> Unit, onNext: () -> Unit) =
    Page("Choose apps", "Which apps should trigger check-ins?", "Next", selected.isNotEmpty(), onNext) {
        LazyColumn {
            items(apps) { a ->
                Row(Modifier.fillMaxWidth().clickable { onToggle(a.pkg) }.padding(vertical = 4.dp)) {
                    Checkbox(a.pkg in selected, { onToggle(a.pkg) }); Text(a.label, Modifier.padding(top = 12.dp))
                }
            }
        }
    }

@Composable fun FrequencyScreen(minutes: Int, onChange: (Int) -> Unit, onNext: () -> Unit) =
    Page("Check-in frequency", "Check in after this much time in your selected apps.", "Next", onCta = onNext) {
        Text("$minutes minutes", style = MaterialTheme.typography.titleLarge)
        Slider(minutes.toFloat(), { onChange((it / 5).toInt().coerceAtLeast(1) * 5) }, valueRange = 5f..120f)
    }

fun introQuestions(apps: List<String>): List<Question> {
    val a = apps.take(3).joinToString(" and ")
    return listOf(
        Question("What kind of content do you notice you typically consume on $a?", QType.OPEN),
        Question("How has this content helped you? (new hobbies, inspiration, positive changes…)", QType.OPEN),
        Question("How has it hurt you? (lost time, less creating, mood…)", QType.OPEN),
        Question("How do you usually feel after a long session?", QType.CHOICE,
            listOf("Energized", "Neutral", "Drained", "Anxious or irritated")),
    )
}

/** Shared by intro survey and check-ins. */
@Composable
fun SurveyScreen(title: String, questions: List<Question>, onDone: (List<QA>) -> Unit) {
    var idx by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateListOf<QA>() }
    var current by remember(idx) { mutableStateOf("") }
    val q = questions[idx]
    val valid = if (q.type == QType.OPEN) current.trim().length >= 10 else current.isNotBlank()
    val last = idx == questions.lastIndex
    Page(title, "Question ${idx + 1} of ${questions.size}", if (last) "Finish" else "Next", valid, {
        answers.add(QA(q.text, current.trim()))
        if (last) onDone(answers.toList()) else idx++
    }) {
        Text(q.text, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        if (q.type == QType.OPEN) OutlinedTextField(current, { current = it }, Modifier.fillMaxWidth(), minLines = 4)
        else q.options.forEach { o ->
            Row(Modifier.fillMaxWidth().clickable { current = o }) { RadioButton(current == o, { current = o }); Text(o, Modifier.padding(top = 12.dp)) }
        }
    }
}

@Composable fun HomeScreen(apps: List<String>, minutes: Int, count: Int) =
    Page("CheckIn is active", "Watching: ${apps.joinToString()}\nCheck-in every $minutes min\nReflections so far: $count",
        "OK", onCta = {})
