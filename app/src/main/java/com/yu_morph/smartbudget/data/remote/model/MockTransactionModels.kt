package com.yu_morph.smartbudget.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MockTransactionResponse(val count: Int, val transactions: List<MockTransaction>)

@Serializable
data class MockTransaction(
    @SerialName("transactionId") val transactionId: String,
    val date: String,
    val description: String,
    val category: String,
    val amount: Int,
    val type: String,
)
