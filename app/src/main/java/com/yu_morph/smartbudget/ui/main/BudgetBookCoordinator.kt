package com.yu_morph.smartbudget.ui.main

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yu_morph.smartbudget.R
import com.yu_morph.smartbudget.data.remote.ApiResult
import com.yu_morph.smartbudget.data.remote.SmartBudgetApi
import com.yu_morph.smartbudget.data.remote.api.TransactionListQuery
import com.yu_morph.smartbudget.data.remote.model.TransactionData
import com.yu_morph.smartbudget.ui.showAppToast
import java.time.YearMonth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TRANSACTION_PAGE_LIMIT = 100

data class BudgetBookUiState(
    val month: YearMonth? = null,
    val transactions: List<TransactionData> = emptyList(),
    val isLoading: Boolean = false,
)

class BudgetBookCoordinator(context: Context, private val api: SmartBudgetApi, private val scope: CoroutineScope) {
    private val applicationContext = context.applicationContext
    private var loadJob: Job? = null

    var state by mutableStateOf(BudgetBookUiState())
        private set

    fun loadMonth(month: YearMonth) {
        loadJob?.cancel()
        loadJob = scope.launch {
            state = state.copy(month = month, transactions = emptyList(), isLoading = true)
            try {
                val transactions = loadAllTransactions(month) ?: return@launch
                state = state.copy(month = month, transactions = transactions)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                applicationContext.showAppToast(R.string.unknown_error)
            } finally {
                if (state.month == month) state = state.copy(isLoading = false)
            }
        }
    }

    private suspend fun loadAllTransactions(month: YearMonth): List<TransactionData>? {
        val transactions = mutableListOf<TransactionData>()
        var cursor: String? = null
        var succeeded = true
        do {
            when (
                val result =
                    api.transactions.list(
                        TransactionListQuery(
                            startDate = month.atDay(1).toString(),
                            endDate = month.atEndOfMonth().toString(),
                            limit = TRANSACTION_PAGE_LIMIT,
                            cursor = cursor,
                        ),
                    )
            ) {
                is ApiResult.Success -> {
                    result.data?.let { page ->
                        transactions += page.items
                        cursor = page.nextCursor
                    } ?: run { cursor = null }
                }

                else -> {
                    showFailure(result)
                    succeeded = false
                    cursor = null
                }
            }
        } while (cursor != null)
        return transactions.takeIf { succeeded }
    }

    private fun showFailure(result: ApiResult<*>) {
        val message =
            when (result) {
                is ApiResult.NetworkFailure -> R.string.network_error

                is ApiResult.DecodeFailure -> R.string.response_error

                is ApiResult.HttpFailure,
                is ApiResult.NotImplemented,
                -> R.string.service_unavailable_error

                is ApiResult.Success -> return
            }
        applicationContext.showAppToast(message)
    }
}
