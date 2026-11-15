package com.yu_morph.smartbudget.ui.main

import org.junit.Assert.assertEquals
import org.junit.Test

class CompactBudgetFormatterTest {
    @Test
    fun `한국어 금액은 만원과 억원 단위로 표시한다`() {
        assertEquals("102만원", formatCompactBudget(1_020_000, "ko"))
        assertEquals("1038만원", formatCompactBudget(10_380_000, "ko"))
        assertEquals("2억원", formatCompactBudget(200_000_000, "ko"))
    }

    @Test
    fun `영어 금액은 축약 단위로 표시한다`() {
        assertEquals("1.2M￦", formatCompactBudget(1_200_000, "en"))
    }
}
