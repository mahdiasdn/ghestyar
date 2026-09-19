// ═══ data/Premium.kt ═══
package com.iliyateam.ghestyar.data

import android.content.Context

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

    private const val PREF_NAME = "ghestyar_premium"
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
