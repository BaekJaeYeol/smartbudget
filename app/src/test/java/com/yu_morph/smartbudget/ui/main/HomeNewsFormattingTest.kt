package com.yu_morph.smartbudget.ui.main

import java.time.Instant
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeNewsFormattingTest {
    @Test
    fun `기사 시각은 한국 시간과 앱 언어 형식으로 표시한다`() {
        val publishedAt = Instant.parse("2026-11-17T14:32:00Z")

        assertEquals("11월 17일 오후 11:32", formatNewsPublishedAt(publishedAt, Locale.KOREAN))
        assertEquals("Nov 17, 11:32 PM", formatNewsPublishedAt(publishedAt, Locale.US))
    }
}
