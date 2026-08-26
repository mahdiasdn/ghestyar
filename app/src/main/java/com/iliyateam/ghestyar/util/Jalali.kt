// ═══ util/Jalali.kt ═══
package com.iliyateam.ghestyar.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object Jalali {
    val months = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    fun isLeap(jy: Int): Boolean {
        val r = jy % 33
        return r == 1 || r == 5 || r == 9 || r == 13 || r == 17 || r == 22 || r == 26 || r == 30
    }

    fun monthLength(jy: Int, jm: Int): Int = when {
        jm in 1..6 -> 31
        jm in 7..11 -> 30
        else -> if (isLeap(jy)) 30 else 29
    }

    /** تبدیل دقیق شمسی به میلادی */
    fun toGregorian(jy: Int, jm: Int, jd: Int): LocalDate {
        val safeMonth = jm.coerceIn(1, 12)
        val safeDay = jd.coerceIn(1, monthLength(jy, safeMonth))

        val jy2 = jy - 979
        val jm2 = safeMonth - 1
        val jd2 = safeDay - 1

        var jDayNo = 365 * jy2 + (jy2 / 33) * 8 + ((jy2 % 33 + 3) / 4)
        for (i in 0 until jm2) {
            jDayNo += if (i < 6) 31 else 30
        }
        jDayNo += jd2

        var gDayNo = jDayNo + 79

        var gy2 = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy2 += 100 * (gDayNo / 36524)
            gDayNo %= 36524

            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy2 += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy2 += gDayNo / 365
            gDayNo %= 365
        }

        var i = 0
        val gDaysInMonth = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        while (gDayNo >= gDaysInMonth[i]) {
            gDayNo -= gDaysInMonth[i]
            i++
        }

        val gy = gy2
        val gm = i + 1
        val gd = gDayNo + 1

        return LocalDate.of(gy, gm, gd)
    }

    /** تبدیل دقیق میلادی به شمسی */
    fun fromGregorian(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        val gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 0 until gm2) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm2 > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var i = 0
        while (i < 11 && jDayNo >= jDaysInMonth[i]) {
            jDayNo -= jDaysInMonth[i]
            i++
        }
        val jm = i + 1
        val jd = jDayNo + 1

        return Triple(jy, jm, jd)
    }
}

data class JalaliDate(
    val jy: Int,
    val jm: Int,
    val jd: Int
) : java.io.Serializable {

    fun toLocalDate(): LocalDate =
        Jalali.toGregorian(jy, jm, jd)

    fun plusMonths(n: Int): JalaliDate {
        val totalMonths = jy * 12 + (jm - 1) + n
        val newYear = Math.floorDiv(totalMonths, 12)
        val newMonth = Math.floorMod(totalMonths, 12) + 1
        val maxDay = Jalali.monthLength(newYear, newMonth)
        return JalaliDate(newYear, newMonth, jd.coerceIn(1, maxDay))
    }

    fun minusMonths(n: Int): JalaliDate = plusMonths(-n)

    fun plusDays(days: Long): JalaliDate =
        toLocalDate().plusDays(days).toJalali()

    fun minusDays(days: Long): JalaliDate = plusDays(-days)

    companion object {
        fun today(): JalaliDate = LocalDate.now().toJalali()

        fun startOfNextMonth(): JalaliDate {
            val t = today()
            val nextMonth = t.plusMonths(1)
            return JalaliDate(nextMonth.jy, nextMonth.jm, 1)
        }
    }
}

fun LocalDate.toJalali(): JalaliDate {
    val (jy, jm, jd) = Jalali.fromGregorian(year, monthValue, dayOfMonth)
    return JalaliDate(jy, jm, jd)
}

// ─── فرمت‌های نمایشی و ارقام فارسی و انگلیسی ───
fun String.faDigits(): String = buildString {
    this@faDigits.forEach { c ->
        append(if (c in '0'..'9') ('۰'.code + (c - '0')).toChar() else c)
    }
}

fun String.enDigits(): String = buildString {
    this@enDigits.forEach { c ->
        append(
            when (c) {
                in '۰'..'۹' -> ('0'.code + (c - '۰')).toChar()
                in '٠'..'٩' -> ('0'.code + (c - '٠')).toChar()
                else -> c
            }
        )
    }
}

/** پاکسازی ورودی متنی و استخراج ارقام استاندارد */
fun String.cleanNumericDigits(maxDigits: Int = 14): String =
    enDigits().filter { it.isDigit() }.take(maxDigits)

fun Int.faDigits() = toString().faDigits()
fun Long.faDigits() = toString().faDigits()

fun Long.money(): String =
    java.text.DecimalFormat("#,###").format(this).replace(",", "٬").faDigits()

object PersianNumberHelper {
    private val yekan = arrayOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")
    private val dahgan = arrayOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val dahToNoozdah = arrayOf("ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده")
    private val sadgan = arrayOf("", "صد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد")
    private val scales = arrayOf("", "هزار", "میلیون", "میلیارد", "تریلیون")

    /** تبدیل مبلغ عددی به حروف فارسی (مثلاً ۲۵۰۰۰۰۰ -> دو میلیون و پانصد هزار تومان) */
    fun toWords(number: Long, unit: String = "تومان"): String {
        if (number == 0L) return if (unit.isBlank()) "صفر" else "صفر $unit"
        if (number < 0) return "منفی " + toWords(-number, unit)

        var n = number
        val parts = mutableListOf<String>()
        var scaleIndex = 0

        while (n > 0 && scaleIndex < scales.size) {
            val chunk = (n % 1000).toInt()
            if (chunk > 0) {
                val chunkWord = chunkToWords(chunk)
                val scaleWord = scales[scaleIndex]
                val part = if (scaleWord.isNotEmpty()) "$chunkWord $scaleWord" else chunkWord
                parts.add(0, part)
            }
            n /= 1000
            scaleIndex++
        }

        val text = parts.joinToString(" و ")
        return if (unit.isNotBlank()) "$text $unit" else text
    }

    private fun chunkToWords(chunk: Int): String {
        val s = chunk / 100
        val d = (chunk % 100) / 10
        val y = chunk % 10

        val words = mutableListOf<String>()
        if (s in 1..9) words.add(sadgan[s])

        if (d == 1) {
            words.add(dahToNoozdah[y])
        } else {
            if (d in 2..9) words.add(dahgan[d])
            if (y in 1..9) words.add(yekan[y])
        }

        return words.joinToString(" و ")
    }

    /** نمایش خلاصه و هوشمند مبالغ در کارت‌ها و نمودارها */
    fun toCompact(amount: Long, unit: String = "تومان"): String {
        return when {
            amount >= 1_000_000_000L -> {
                val b = amount.toDouble() / 1_000_000_000.0
                String.format(java.util.Locale.US, "%.1f", b).trimEnd('0').trimEnd('.').faDigits() + " میلیارد " + unit
            }
            amount >= 1_000_000L -> {
                val m = amount.toDouble() / 1_000_000.0
                String.format(java.util.Locale.US, "%.1f", m).trimEnd('0').trimEnd('.').faDigits() + " میلیون " + unit
            }
            amount >= 1_000L -> {
                val k = amount.toDouble() / 1_000.0
                String.format(java.util.Locale.US, "%.1f", k).trimEnd('0').trimEnd('.').faDigits() + " هزار " + unit
            }
            else -> amount.money() + " " + unit
        }
    }
}

/** تبدیل مستقیم عدد به حروف فارسی */
fun Long.toPersianWords(unit: String = "تومان"): String = PersianNumberHelper.toWords(this, unit)

/** فرمت خلاصه و شکیل مبالغ بزرگ */
fun Long.compactMoney(unit: String = "تومان"): String = PersianNumberHelper.toCompact(this, unit)

fun JalaliDate.format(): String {
    val safeMonth = jm.coerceIn(1, 12)
    val safeDay = jd.coerceIn(1, Jalali.monthLength(jy, safeMonth))
    return "${safeDay.faDigits()} ${Jalali.months[safeMonth - 1]} ${jy.faDigits()}"
}

fun JalaliDate.formatJalali(): String = format()

fun LocalDate.formatJalali(): String = toJalali().format()

fun LocalDate.formatJalaliWithWeekday(): String {
    val wd = when (dayOfWeek.toString()) {
        "SATURDAY" -> "شنبه"
        "SUNDAY" -> "یکشنبه"
        "MONDAY" -> "دوشنبه"
        "TUESDAY" -> "سه‌شنبه"
        "WEDNESDAY" -> "چهارشنبه"
        "THURSDAY" -> "پنجشنبه"
        else -> "جمعه"
    }
    return "$wd، ${formatJalali()}"
}

fun LocalDate.relativeLabel(): String {
    val days = ChronoUnit.DAYS.between(LocalDate.now(), this)
    return when (days) {
        0L -> "امروز"
        1L -> "فردا"
        -1L -> "دیروز"
        in 2..7 -> "${days.toInt().faDigits()} روز دیگر"
        in -7..-2 -> "${(-days).toInt().faDigits()} روز پیش"
        else -> formatJalali()
    }
}