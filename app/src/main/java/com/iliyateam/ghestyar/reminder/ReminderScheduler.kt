// ═══ reminder/ReminderScheduler.kt ═══
package com.iliyateam.ghestyar.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.iliyateam.ghestyar.data.Installment
import java.time.LocalDate
import java.time.ZoneId

object ReminderScheduler {
    private fun am(ctx: Context) = ctx.getSystemService(AlarmManager::class.java)
    private fun rc(id: Long, i: Int) = (id * 10 + i).toInt() + 90_000

    private fun pi(ctx: Context, code: Int, title: String, msg: String, nid: Int): PendingIntent {
        val intent = Intent(ctx, ReminderReceiver::class.java)
            .putExtra("title", title).putExtra("msg", msg).putExtra("nid", nid)
        return PendingIntent.getBroadcast(ctx, code, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun schedule(ctx: Context, item: Installment, profileName: String = "", customHour: Int = -1) {
        if (!item.remind) return
        cancel(ctx, item)
        val due = LocalDate.ofEpochDay(item.dueEpochDay)
        val prefix = if (profileName.isNotBlank()) "[$profileName] " else ""
        val prefs = ctx.getSharedPreferences("ghestyar_settings", Context.MODE_PRIVATE)
        val notifHour = if (customHour in 0..23) customHour else prefs.getInt("notif_hour", 9)
        val alarmManager = am(ctx)

        listOf(
            3 to "${prefix}۳ روز تا سررسید «${item.title}»",
            1 to "${prefix}فردا سررسید «${item.title}» است",
            0 to "${prefix}امروز سررسید «${item.title}» 🔔"
        ).forEachIndexed { i, (days, msg) ->
            val t = due.minusDays(days.toLong()).atTime(notifHour, 0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            if (t > System.currentTimeMillis()) {
                val pendingIntent = pi(ctx, rc(item.id, i), "${prefix}${item.title}", msg, rc(item.id, i))
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pendingIntent)
                        } else {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pendingIntent)
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pendingIntent)
                    }
                } catch (_: SecurityException) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pendingIntent)
                } catch (_: Exception) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pendingIntent)
                }
            }
        }
    }

    fun cancel(ctx: Context, item: Installment) {
        for (i in 0..2) {
            try {
                am(ctx).cancel(pi(ctx, rc(item.id, i), "", "", rc(item.id, i)))
            } catch (_: Exception) {}
        }
    }
}