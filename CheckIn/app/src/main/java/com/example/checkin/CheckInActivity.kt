package com.example.checkin


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


/** Full-screen lock. Back is disabled; only completing the survey clears the lock. */
class CheckInActivity : ComponentActivity() {
    private val generator: QuestionGenerator = ClaudeQuestionGenerator()
    // test comment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Store(this)
        val apps = store.monitoredApps.map { appLabel(it) }
        setContent {
            MaterialTheme {
                BackHandler(true) {}
                var qs by remember { mutableStateOf<List<Question>?>(null) }
                LaunchedEffect(Unit) {
                    val names = store.lastApp?.let { listOf(appLabel(it)) } ?: apps
                    qs = generator.checkInQuestions(names, store.reflections().takeLast(10))
                }
                val questions = qs
                if (questions == null) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(); Spacer(Modifier.height(12.dp)); Text("Preparing your check-in…")
                        }
                    }
                } else SurveyScreen("Time to check in", questions) { answers ->
                    store.add(Reflection(System.currentTimeMillis(), "checkin", apps, answers))
                    store.usedMs = 0; store.locked = false
                    finish()
                }
            }
        }
    }
}
