// ═══ ui/components/ReceiptShareHelper.kt ═══
package com.iliyateam.ghestyar.ui.components

import android.content.Context
import android.content.Intent
import com.iliyateam.ghestyar.data.ChequeOrDebt
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.util.faDigits
import com.iliyateam.ghestyar.util.formatJalali
import com.iliyateam.ghestyar.util.money
import java.time.LocalDate

object ReceiptShareHelper {

    fun shareInstallmentReceipt(context: Context, item: Installment) {
        val todayJalali = LocalDate.now().formatJalali()
        val dueJalali = LocalDate.ofEpochDay(item.dueEpochDay).formatJalali()

        val text = """
            🧾 رسید پرداخت قسط در قسط‌یار
            ━━━━━━━━━━━━━━━━━
            📌 عنوان: ${item.title}
            💵 مبلغ هر قسط: ${item.amount.money()} تومان
            📊 وضعیت: قسط ${(item.paidSessions).faDigits()} از ${item.totalSessions.faDigits()} تسویه شد ✅
            📅 تاریخ ثبت پرداخت: $todayJalali
            ⏰ سررسید بعدی: $dueJalali
            ━━━━━━━━━━━━━━━━━
            📱 ثبت و پیگیری شده توسط اپلیکیشن «قسط‌یار»
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "ارسال رسید پرداخت قسط"))
    }

    fun shareChequeReminder(context: Context, item: ChequeOrDebt) {
        val dueJalali = LocalDate.ofEpochDay(item.dueEpochDay).formatJalali()
        val typeTitle = if (item.isCheque) "چک صیادی" else "قرض مالی"
        val status = if (item.isReceivable) "طلب (دریافتی)" else "بدهی (پرداختی)"

        val text = """
            🔔 یادآوری سررسید $typeTitle
            ━━━━━━━━━━━━━━━━━
            👤 طرف حساب: ${item.personName}
            📌 بابت: ${item.title}
            💵 مبلغ: ${item.amount.money()} تومان
            🏷️ وضعیت: $status
            📅 موعد سررسید: $dueJalali
            ${if (item.chequeNumber.isNotBlank()) "🔢 شماره صیادی/چک: ${item.chequeNumber}" else ""}
            ${if (item.bankName.isNotBlank()) "🏦 بانک: ${item.bankName}" else ""}
            ━━━━━━━━━━━━━━━━━
            📱 مدیریت و یادآوری با اپلیکیشن «قسط‌یار»
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری یادآوری سررسید"))
    }
}
