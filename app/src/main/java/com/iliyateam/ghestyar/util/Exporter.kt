// ═══ util/Exporter.kt ═══
package com.iliyateam.ghestyar.util

import android.content.Context
import android.net.Uri
import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.data.InstallmentCategories
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate

enum class SubscriptionTier(
    val id: String,
    val title: String,
    val priceFormatted: String,
    val durationText: String,
    val discountBadge: String? = null
) {
    MONTHLY("monthly", "اشتراک ۱ ماهه", "۴۹٬۰۰۰", "ماهانه"),
    QUARTERLY("quarterly", "اشتراک ۳ ماهه", "۱۱۹٬۰۰۰", "۳ ماهه", "۲۰٪ تخفیف"),
    YEARLY("yearly", "اشتراک ۱ ساله (پیشنهادی)", "۲۹۹٬۰۰۰", "سالانه", "۵۰٪ تخفیف ویژه 🔥"),
    LIFETIME("lifetime", "اشتراک مادام‌العمر VIP", "۵۹۰٬۰۰۰", "یک‌بار برای همیشه", "ارزش استثنایی")
}

object Premium {
    const val MAX_FREE_ACTIVE_INSTALLMENTS = 4

    private const val PREF_NAME = "ghestyar_pref"
    private const val KEY_PREMIUM = "is_premium"
    private const val KEY_PLAN_ID = "premium_plan_id"
    private const val KEY_UNLOCKED_AT = "premium_unlocked_at"

    fun isPremium(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_PREMIUM, false)

    fun setPremium(ctx: Context, enabled: Boolean, plan: SubscriptionTier = SubscriptionTier.YEARLY) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_PREMIUM, enabled)
            .putString(KEY_PLAN_ID, if (enabled) plan.id else "")
            .putLong(KEY_UNLOCKED_AT, if (enabled) System.currentTimeMillis() else 0L)
            .apply()
    }

    fun getActivePlan(ctx: Context): SubscriptionTier? {
        if (!isPremium(ctx)) return null
        val id = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_PLAN_ID, "")
        return SubscriptionTier.entries.firstOrNull { it.id == id } ?: SubscriptionTier.YEARLY
    }

    fun canAddMoreInstallments(ctx: Context, currentActiveCount: Int): Boolean {
        return isPremium(ctx) || currentActiveCount < MAX_FREE_ACTIVE_INSTALLMENTS
    }
}

object Exporter {
    /** خروجی CSV سازگار با اکسل (با BOM برای یونیکد فارسی) */
    fun csv(ctx: Context, uri: Uri, items: List<Installment>) {
        ctx.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            val sb = StringBuilder("عنوان,دسته‌بندی,مبلغ قسط (تومان),تعداد کل اقساط,اقساط پرداخت‌شده,مبلغ کل (تومان),مبلغ مانده (تومان),سررسید فعلی,وضعیت,تاریخ آخرین پرداخت,یادداشت\n")
            items.forEach { i ->
                val cleanTitle = i.title.replace(",", " - ").replace("\n", " ")
                val categoryName = InstallmentCategories.get(i.category).title
                val due = LocalDate.ofEpochDay(i.dueEpochDay).formatJalali()
                val paidDate = i.paidAtEpochDay?.let { LocalDate.ofEpochDay(it).formatJalali() } ?: "—"
                val status = if (i.isPaid) "تسویه‌شده" else "در جریان"
                val cleanNote = i.note.replace(",", " - ").replace("\n", " ")

                sb.append("$cleanTitle,$categoryName,${i.amount},${i.totalSessions},${i.paidSessions},${i.totalAmount},${i.remainingAmount},$due,$status,$paidDate,$cleanNote\n")
            }
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }
    }

    /** پشتیبان JSON */
    fun jsonBackup(ctx: Context, uri: Uri, items: List<Installment>) {
        ctx.contentResolver.openOutputStream(uri)?.use { out ->
            val root = JSONObject()
            root.put("version", 2)
            root.put("timestamp", System.currentTimeMillis())

            val arr = JSONArray()
            items.forEach { i ->
                val obj = JSONObject()
                    .put("title", i.title)
                    .put("amount", i.amount)
                    .put("startEpochDay", i.startEpochDay)
                    .put("dueEpochDay", i.dueEpochDay)
                    .put("totalSessions", i.totalSessions)
                    .put("paidSessions", i.paidSessions)
                    .put("isPaid", i.isPaid)
                    .put("paidAtEpochDay", i.paidAtEpochDay ?: JSONObject.NULL)
                    .put("colorIndex", i.colorIndex)
                    .put("category", i.category)
                    .put("remind", i.remind)
                    .put("note", i.note)
                arr.put(obj)
            }
            root.put("installments", arr)
            out.write(root.toString(2).toByteArray(Charsets.UTF_8))
        }
    }

    /** بازیابی از پشتیبان JSON */
    fun jsonRestore(ctx: Context, uri: Uri): List<Installment> {
        val result = mutableListOf<Installment>()
        ctx.contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val jsonString = reader.readText()
            val root = JSONObject(jsonString)
            val arr = root.optJSONArray("installments") ?: JSONArray(jsonString)

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                result.add(
                    Installment(
                        title = obj.optString("title", "بدون عنوان"),
                        amount = obj.optLong("amount", 0L),
                        startEpochDay = obj.optLong("startEpochDay", LocalDate.now().toEpochDay()),
                        dueEpochDay = obj.optLong("dueEpochDay", LocalDate.now().plusMonths(1).toEpochDay()),
                        totalSessions = obj.optInt("totalSessions", 1),
                        paidSessions = obj.optInt("paidSessions", 0),
                        isPaid = obj.optBoolean("isPaid", false),
                        paidAtEpochDay = if (obj.isNull("paidAtEpochDay")) null else obj.optLong("paidAtEpochDay"),
                        colorIndex = obj.optInt("colorIndex", 0),
                        category = obj.optString("category", "bank"),
                        remind = obj.optBoolean("remind", true),
                        note = obj.optString("note", "")
                    )
                )
            }
        }
        return result
    }
}