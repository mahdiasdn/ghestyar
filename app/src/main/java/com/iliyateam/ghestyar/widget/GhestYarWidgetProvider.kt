// ═══ widget/GhestYarWidgetProvider.kt ═══
package com.iliyateam.ghestyar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.iliyateam.ghestyar.MainActivity
import com.iliyateam.ghestyar.R
import com.iliyateam.ghestyar.data.AppDatabase
import com.iliyateam.ghestyar.util.faDigits
import com.iliyateam.ghestyar.util.money
import com.iliyateam.ghestyar.util.relativeLabel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class GhestYarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(context)
                val activeList = db.installmentDao().getAll().filter { !it.isPaid }.sortedBy { it.dueEpochDay }
                val nextItem = activeList.firstOrNull()

                val today = LocalDate.now().toEpochDay()
                val monthlyTotal = activeList.filter { it.dueEpochDay <= today + 30 }.sumOf { it.amount }

                for (widgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_ghestyar)
                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                    if (nextItem != null) {
                        val due = LocalDate.ofEpochDay(nextItem.dueEpochDay)
                        val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), due)
                        val daysText = due.relativeLabel()

                        views.setTextViewText(R.id.widget_installment_title, nextItem.title)
                        views.setTextViewText(R.id.widget_installment_amount, "${nextItem.amount.money()} تومان")
                        views.setTextViewText(R.id.widget_days_left, daysText)
                    } else {
                        views.setTextViewText(R.id.widget_installment_title, "خیالت آسوده ✨")
                        views.setTextViewText(R.id.widget_installment_amount, "همه اقساط تسویه شده است")
                        views.setTextViewText(R.id.widget_days_left, "تکمیل")
                    }

                    views.setTextViewText(
                        R.id.widget_monthly_total,
                        "تعهد این ماه: ${monthlyTotal.money()} تومان (${activeList.size.faDigits()} قسط)"
                    )

                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            } catch (_: Exception) { }
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val intent = Intent(context, GhestYarWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                    ComponentName(context, GhestYarWidgetProvider::class.java)
                )
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
