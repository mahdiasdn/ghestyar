// ═══ reminder/ReminderReceiver.kt ═══
package com.iliyateam.ghestyar.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.iliyateam.ghestyar.MainActivity
import com.iliyateam.ghestyar.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: return
        val msg = intent.getStringExtra("msg").orEmpty()
        val notification = NotificationCompat.Builder(context, "reminders")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title).setContentText(msg)
            .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
            .setAutoCancel(true)
            .setContentIntent(PendingIntent.getActivity(context, 0,
                Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(intent.getIntExtra("nid", 1), notification)
    }
}