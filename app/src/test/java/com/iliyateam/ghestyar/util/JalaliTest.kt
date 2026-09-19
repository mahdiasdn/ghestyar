package com.iliyateam.ghestyar.util

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class JalaliTest {

    @Test
    fun testGregorianToJalaliKnownDates() {
        // 2026-03-21 -> 1405-01-01
        val date1 = LocalDate.of(2026, 3, 21)
        val j1 = date1.toJalali()
        assertEquals(1405, j1.jy)
        assertEquals(1, j1.jm)
        assertEquals(1, j1.jd)

        // 2024-03-20 -> 1403-01-01
        val date2 = LocalDate.of(2024, 3, 20)
        val j2 = date2.toJalali()
        assertEquals(1403, j2.jy)
        assertEquals(1, j2.jm)
        assertEquals(1, j2.jd)
    }

    @Test
    fun testJalaliToGregorianRoundTrip() {
        val original = LocalDate.of(2025, 8, 26)
        val jalali = original.toJalali()
        val roundTrip = jalali.toLocalDate()
        assertEquals(original, roundTrip)
    }

    @Test
    fun testMonthLengths() {
        // First 6 months have 31 days
        for (m in 1..6) {
            assertEquals(31, Jalali.monthLength(1403, m))
        }
        // Months 7 to 11 have 30 days
        for (m in 7..11) {
            assertEquals(30, Jalali.monthLength(1403, m))
        }
        // 1403 is a leap year (Esfand has 30 days)
        assertEquals(30, Jalali.monthLength(1403, 12))
        // 1404 is not a leap year (Esfand has 29 days)
        assertEquals(29, Jalali.monthLength(1404, 12))
    }

    @Test
    fun testPlusMonths() {
        val j = JalaliDate(1404, 11, 15)
        val nextMonth = j.plusMonths(2)
        assertEquals(1405, nextMonth.jy)
        assertEquals(1, nextMonth.jm)
        assertEquals(15, nextMonth.jd)
    }
}
