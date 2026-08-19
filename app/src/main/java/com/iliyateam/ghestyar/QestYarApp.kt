// ═══ QestYarApp.kt ═══
package com.iliyateam.ghestyar

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class QestYarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("reminders", "یادآوری اقساط", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }
}