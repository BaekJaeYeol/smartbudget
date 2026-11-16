package com.yu_morph.smartbudget.ui.main

import com.yu_morph.smartbudget.data.remote.ApiResult
import java.io.IOException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BankSyncRetryPolicyTest {
    @Test
    fun `재연동 중 일시적인 실패는 재시도한다`() {
        assertTrue(ApiResult.NetworkFailure(IOException()).isTransientBankSyncFailure())
        assertTrue(ApiResult.DecodeFailure("", IllegalArgumentException()).isTransientBankSyncFailure())
        assertTrue(httpFailure(503).isTransientBankSyncFailure())
        assertTrue(httpFailure(409, "IDEMPOTENCY_REQUEST_IN_PROGRESS").isTransientBankSyncFailure())
    }

    @Test
    fun `입력 오류와 인증 오류는 재시도하지 않는다`() {
        assertFalse(httpFailure(400).isTransientBankSyncFailure())
        assertFalse(httpFailure(401).isTransientBankSyncFailure())
    }

    private fun httpFailure(status: Int, code: String = "ERROR") =
        ApiResult.HttpFailure(status, code, "error", retryAfterSeconds = null)
}
