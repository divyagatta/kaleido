package com.example.checkin

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.*

/**
 * Polls the foreground app. Accumulates time in monitored apps; when the interval is hit,
 * sets locked=true and shows CheckInActivity. While locked, re-opens it whenever a monitored app
 * comes to the foreground.
 */
class UsageMonitorService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var store: Store
    private var lastPkg: String? = null

    override fun onBind(i: Intent?): IBinder? = null
    override fun onStartCommand(i: Intent?, f: Int, id: Int) = START_STICKY

    override fun onCreate() {
        super.onCreate()
        store = Store(this)
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("monitor", "Check-in monitoring", NotificationManager.IMPORTANCE_MIN))
        val n = NotificationCompat.Builder(this, "monitor")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("CheckIn is running").build()
        ServiceCompat.startForeground(this, 1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        scope.launch { loop() }
    }

    private suspend fun loop() {
        var last = System.currentTimeMillis()
        while (currentCoroutineContext().isActive) {
            delay(2_000)
            val now = System.currentTimeMillis(); val dt = now - last; last = now
            val pkg = foregroundPackage()
            if (pkg == null || pkg !in store.monitoredApps) continue
            store.lastApp = pkg
            if (store.locked) { showCheckIn(); continue }
            store.usedMs += dt
            if (store.usedMs >= store.intervalMin * 60_000L) { store.locked = true; showCheckIn() }
        }
    }

    @Suppress("DEPRECATION")
    private fun foregroundPackage(): String? {
        val um = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val ev = um.queryEvents(now - 10_000, now); val e = UsageEvents.Event()
        while (ev.hasNextEvent()) {
            ev.getNextEvent(e)
            if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) lastPkg = e.packageName
            if (e.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND && e.packageName == lastPkg) lastPkg = null
        }
        return lastPkg
    }

    // Background activity launch requires the "display over other apps" permission (granted in onboarding).
    private fun showCheckIn() = startActivity(
        Intent(this, CheckInActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    )

    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
