package com.iliyateam.ghestyar.reminder

import org.junit.Assert.*
import org.junit.Test

class ReminderSchedulerTest {

    @Test
    fun testRequestCodesArePositiveAndBounded() {
        val testIds = listOf(1L, 2L, 99999L, Long.MAX_VALUE, 1_000_000_000L)
        for (id in testIds) {
            for (i in 0..2) {
                val code = ReminderScheduler.rc(id, i)
                assertTrue("Code should be positive and >= 90_000 for id=$id, i=$i", code >= 90_000)
                assertTrue("Code should be within reasonable bounds", code <= 1_090_000)
            }
        }
    }

    @Test
    fun testDistinctRequestCodesForSameIdDifferentOffsets() {
        val id = 12345L
        val codes = (0..2).map { ReminderScheduler.rc(id, it) }.toSet()
        assertEquals("Offsets for same installment must produce distinct request codes", 3, codes.size)
    }
}
