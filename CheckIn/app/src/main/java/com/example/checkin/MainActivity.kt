package com.example.checkin

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

enum class Step { HOW, PERMS, APPS, FREQ, INTRO, HOME }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Store(this)
        setContent { MaterialTheme { Flow(store) } }
    }

    private fun usageGranted(): Boolean {
        val ops = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED
    }

    @Composable
    private fun Flow(store: Store) {
        var step by remember { mutableStateOf(if (store.onboarded) Step.HOME else Step.HOW) }
        var selected by remember { mutableStateOf(store.monitoredApps) }
        var minutes by remember { mutableIntStateOf(store.intervalMin) }
        val apps = remember { launchableApps(this) }
        var usage by remember { mutableStateOf(usageGranted()) }
        var overlay by remember { mutableStateOf(Settings.canDrawOverlays(this)) }

        // Re-check permissions when returning from system settings.
        val owner = LocalLifecycleOwner.current
        DisposableEffect(owner) {
            val o = LifecycleEventObserver { _, e ->
                if (e == Lifecycle.Event.ON_RESUME) { usage = usageGranted(); overlay = Settings.canDrawOverlays(this@MainActivity) }
            }
            owner.lifecycle.addObserver(o); onDispose { owner.lifecycle.removeObserver(o) }
        }

        when (step) {
            Step.HOW -> HowItWorksScreen { step = Step.PERMS }
            Step.PERMS -> PermissionsScreen(
                usageGranted = usage,
                overlayGranted = overlay,
                onUsage = { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                onOverlay = { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) },
                onBack = { step = Step.HOW },
                onNext = { step = Step.APPS }
            )
            Step.APPS -> SelectAppsScreen(
                apps = apps,
                selected = selected,
                onToggle = { selected = if (it in selected) selected - it else selected + it },
                onBack = { step = Step.PERMS },
                onNext = { store.monitoredApps = selected; step = Step.FREQ }
            )
            Step.FREQ -> FrequencyScreen(
                minutes = minutes,
                onChange = { minutes = it },
                onBack = { step = Step.APPS },
                onNext = { store.intervalMin = minutes; step = Step.INTRO }
            )
            Step.INTRO -> {
                val names = selected.map { appLabel(it) }
                SurveyScreen("Getting to know you", remember { introQuestions(names) }) { answers ->
                    store.add(Reflection(System.currentTimeMillis(), "intro", names, answers))
                    store.onboarded = true; step = Step.HOME
                }
            }
            Step.HOME -> {
                LaunchedEffect(Unit) { startForegroundService(Intent(this@MainActivity, UsageMonitorService::class.java)) }
                HomeScreen(store.monitoredApps.map { appLabel(it) }, store.intervalMin, store.reflections().size)
            }
        }
    }
}
