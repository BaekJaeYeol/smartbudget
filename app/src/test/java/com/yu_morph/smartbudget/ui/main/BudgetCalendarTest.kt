package com.yu_morph.smartbudget.ui.main

import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetCalendarTest {
    @Test
    fun `일요일에 시작하는 달은 첫 칸부터 날짜를 표시한다`() {
        val days = calendarDays(YearMonth.of(2026, 11))

        assertEquals(1, days.first().dayOfMonth)
    }

    @Test
    fun `월요일에 시작하는 달은 전월 마지막 일요일부터 표시한다`() {
        val days = calendarDays(YearMonth.of(2026, 6))

        assertEquals("2026-05-31", days.first().toString())
        assertEquals("2026-06-01", days[1].toString())
    }

    @Test
    fun `마지막 주의 빈 셀에는 익월 날짜를 표시한다`() {
        val days = calendarDays(YearMonth.of(2026, 11))

        assertEquals("2026-12-05", days.last().toString())
    }

    @Test
    fun `달력 전체 칸 수는 항상 주 단위이다`() {
        val days = calendarDays(YearMonth.of(2026, 2))

        assertTrue(days.size % 7 == 0)
    }

    @Test
    fun `큰 금액은 앱 언어에 맞는 짧은 단위로 표시한다`() {
        assertEquals("+3백만", formatCalendarAmount(3_676_400, "ko-KR"))
        assertEquals("-3M", formatCalendarAmount(-3_676_400, "en-US"))
    }
}
