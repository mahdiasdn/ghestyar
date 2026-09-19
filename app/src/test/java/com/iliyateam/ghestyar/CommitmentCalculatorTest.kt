package com.iliyateam.ghestyar

import com.iliyateam.ghestyar.data.Installment
import com.iliyateam.ghestyar.util.JalaliDate
import com.iliyateam.ghestyar.util.toJalali
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CommitmentCalculatorTest {

    @Test
    fun testCommitmentCalculationFiltersOnlyCurrentMonthAndUnpaid() {
        val today = LocalDate.of(2026, 8, 26)
        val todayJ = today.toJalali() // Shahrivar 1405

        val thisMonthEpoch = today.toEpochDay()
        val nextMonthEpoch = today.plusDays(40).toEpochDay()
        val prevMonthEpoch = today.minusDays(40).toEpochDay()

        val list = listOf(
            // Active, this month -> should be counted (1,000,000)
            Installment(
                id = 1,
                title = "وام ۱",
                amount = 1_000_000L,
                startEpochDay = thisMonthEpoch,
                dueEpochDay = thisMonthEpoch,
                totalSessions = 10,
                paidSessions = 2,
                isPaid = false
            ),
            // Paid, this month -> should NOT be counted
            Installment(
                id = 2,
                title = "وام ۲",
                amount = 2_000_000L,
                startEpochDay = thisMonthEpoch,
                dueEpochDay = thisMonthEpoch,
                totalSessions = 10,
                paidSessions = 3,
                isPaid = true
            ),
            // Active, next month -> should NOT be counted in this month
            Installment(
                id = 3,
                title = "وام ۳",
                amount = 3_000_000L,
                startEpochDay = nextMonthEpoch,
                dueEpochDay = nextMonthEpoch,
                totalSessions = 10,
                paidSessions = 0,
                isPaid = false
            ),
            // Active, previous month -> should NOT be counted in this month
            Installment(
                id = 4,
                title = "وام ۴",
                amount = 4_000_000L,
                startEpochDay = prevMonthEpoch,
                dueEpochDay = prevMonthEpoch,
                totalSessions = 10,
                paidSessions = 1,
                isPaid = false
            ),
            // Active, this month, but all sessions paid -> should NOT be counted
            Installment(
                id = 5,
                title = "وام ۵",
                amount = 500_000L,
                startEpochDay = thisMonthEpoch,
                dueEpochDay = thisMonthEpoch,
                totalSessions = 5,
                paidSessions = 5,
                isPaid = false
            )
        )

        val total = calculateThisMonthInstallmentsCommitment(list, todayJ)
        assertEquals(1_000_000L, total)
    }
}
