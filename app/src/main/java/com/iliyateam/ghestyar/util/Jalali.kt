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
    return try {
        val (jy, jm, jd) = Jalali.fromGregorian(year, monthValue, dayOfMonth)
        JalaliDate(jy, jm, jd)
    } catch (e: Exception) {
        JalaliDate(1404, 1, 1)
    }
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

fun Int.faDigits() = toString().faDigits()
fun Long.faDigits() = toString().faDigits()

fun Long.money(): String =
    java.text.DecimalFormat("#,###").format(this).replace(",", "٬").faDigits()

fun JalaliDate.format(): String {
    val safeMonth = jm.coerceIn(1, 12)
    val safeDay = jd.coerceIn(1, Jalali.monthLength(jy, safeMonth))
    return "${safeDay.faDigits()} ${Jalali.months[safeMonth - 1]} ${jy.faDigits()}"
}

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