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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class GhestYarWidgetProvider : AppWidgetProvider() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                updateWidgetsInternal(context, appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            action == AppWidgetManager.ACTION_APPWIDGET_ENABLED ||
            action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, GhestYarWidgetProvider::class.java)
            )
            if (ids != null && ids.isNotEmpty()) {
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        updateWidgetsInternal(context, appWidgetManager, ids)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private suspend fun updateWidgetsInternal(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        try {
            val db = AppDatabase.get(context)
            val prefs = context.getSharedPreferences("ghestyar_settings", Context.MODE_PRIVATE)
            val activeProfileId = prefs.getLong("active_profile_id", 1L)

            val allList = db.installmentDao().getAll()
            val activeList = allList.filter { !it.isPaid && it.profileId == activeProfileId }.sortedBy { it.dueEpochDay }
            val nextItem = activeList.firstOrNull()

            val monthlyTotal = com.iliyateam.ghestyar.calculateThisMonthInstallmentsCommitment(activeList)

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_ghestyar)
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                if (nextItem != null) {
                    val due = LocalDate.ofEpochDay(nextItem.dueEpochDay)
                    val daysText = due.relativeLabel()

                    views.setTextViewText(R.id.widget_header_title, "قسط‌یار • سررسید بعدی")
                    views.setTextViewText(R.id.widget_installment_title, nextItem.title)
                    views.setTextViewText(R.id.widget_installment_amount, "${nextItem.amount.money()} تومان")
                    views.setTextViewText(R.id.widget_days_left, daysText)
                } else {
                    views.setTextViewText(R.id.widget_header_title, "قسط‌یار • وضعیت مالی")
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

    companion object {
        fun updateAll(context: Context) {
            try {
                val intent = Intent(context, GhestYarWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                        ComponentName(context, GhestYarWidgetProvider::class.java)
                    )
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            } catch (_: Exception) { }
        }
    }
}
