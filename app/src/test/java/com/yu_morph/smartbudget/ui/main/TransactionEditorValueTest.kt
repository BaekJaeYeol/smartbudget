package com.yu_morph.smartbudget.ui.main

import com.yu_morph.smartbudget.data.remote.model.PatchValue
import com.yu_morph.smartbudget.data.remote.model.TransactionSource
import com.yu_morph.smartbudget.data.remote.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionEditorValueTest {
    private val value =
        TransactionEditorValue(
            type = TransactionType.EXPENSE,
            category = "food",
            storeName = "식당",
            note = "점심",
            amount = 12_000,
        )

    @Test
    fun `추가 입력값을 수기 거래 요청으로 변환한다`() {
        val request = value.toCreateRequest(LocalDate.of(2026, 11, 17))

        assertEquals("2026-11-17", request.date)
        assertEquals(TransactionSource.MANUAL, request.source)
        assertEquals("food", request.category)
        assertEquals(12_000, request.amount)
    }

    @Test
    fun `수정 입력값은 비어 있는 선택 필드도 명시적으로 전달한다`() {
        val request = value.copy(storeName = "", note = "").toUpdateRequest(LocalDate.of(2026, 11, 17))

        assertEquals(PatchValue.Present(null), request.storeName)
        assertEquals(PatchValue.Present(null), request.note)
    }
}
