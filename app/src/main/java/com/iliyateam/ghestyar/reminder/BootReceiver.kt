// ═══ reminder/BootReceiver.kt ═══
package com.iliyateam.ghestyar.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iliyateam.ghestyar.data.AppDatabase
import com.iliyateam.ghestyar.widget.GhestYarWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * دریافت‌کننده سیگنال راه‌اندازی مجدد دستگاه (Boot Completed)
 * برای بازسازی خودکار تمام آلارم‌ها و یادآورهای اقساط فعال
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.get(context)
                    val profilesMap = db.userProfileDao().getAll().associateBy { it.id }
                    val activeInstallments = db.installmentDao().getAll().filter { !it.isPaid && it.remind }

                    for (item in activeInstallments) {
                        val profileName = profilesMap[item.profileId]?.name.orEmpty()
                        ReminderScheduler.schedule(context, item, profileName)
                    }

                    GhestYarWidgetProvider.updateAll(context)
                } catch (_: Exception) {
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
