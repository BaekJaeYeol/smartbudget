package com.yu_morph.smartbudget.data.remote.api

import com.yu_morph.smartbudget.data.remote.ApiResult
import com.yu_morph.smartbudget.data.remote.ResponseSource
import com.yu_morph.smartbudget.data.remote.model.MockTransactionResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private const val HTTP_SUCCESS_MIN = 200
private const val HTTP_SUCCESS_MAX = 299

class MockTransactionApi(private val client: HttpClient, private val json: Json, baseUrl: String) {
    private val normalizedBaseUrl = baseUrl.trimEnd('/')

    suspend fun list(startDate: String, endDate: String): ApiResult<MockTransactionResponse> = try {
        val response = client.get("$normalizedBaseUrl/transactions") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }
        val rawBody = response.bodyAsText()
        if (response.status.value in HTTP_SUCCESS_MIN..HTTP_SUCCESS_MAX) {
            ApiResult.Success(
                data = json.decodeFromString(rawBody),
                status = response.status.value,
                code = "SUCCESS",
                message = "Mock transactions loaded.",
                source = ResponseSource.NETWORK,
            )
        } else {
            ApiResult.HttpFailure(
                status = response.status.value,
                code = "MOCK_API_ERROR",
                message = rawBody,
                retryAfterSeconds = null,
            )
        }
    } catch (error: SerializationException) {
        ApiResult.DecodeFailure("", error)
    } catch (error: IOException) {
        ApiResult.NetworkFailure(error)
    }
}
