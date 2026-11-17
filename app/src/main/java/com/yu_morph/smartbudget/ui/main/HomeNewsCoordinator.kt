package com.yu_morph.smartbudget.ui.main

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yu_morph.smartbudget.data.remote.news.NewsArticle
import com.yu_morph.smartbudget.data.remote.news.NewsFeedRepository
import java.io.Closeable
import kotlin.math.min
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

private const val NEWS_PAGE_SIZE = 4

data class HomeNewsUiState(
    val breakingNews: List<NewsArticle> = emptyList(),
    val economyNews: List<NewsArticle> = emptyList(),
    val visibleEconomyCount: Int = NEWS_PAGE_SIZE,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
)

class HomeNewsCoordinator(context: Context, private val scope: CoroutineScope) : Closeable {
    private val repository = NewsFeedRepository(context)
    private var loadJob: Job? = null
    private var hasRequested = false

    var state by mutableStateOf(HomeNewsUiState())
        private set

    fun load() {
        if (hasRequested || loadJob?.isActive == true) return
        hasRequested = true
        loadJob = scope.launch {
            state = state.copy(isLoading = true, hasError = false)
            try {
                val (breaking, economy) = supervisorScope {
                    val breakingRequest = async { loadSafely(repository::loadBreakingNews) }
                    val economyRequest = async { loadSafely(repository::loadEconomyNews) }
                    breakingRequest.await() to economyRequest.await()
                }
                state =
                    state.copy(
                        breakingNews = breaking,
                        economyNews = economy,
                        visibleEconomyCount = NEWS_PAGE_SIZE,
                        hasError = breaking.isEmpty() && economy.isEmpty(),
                    )
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun showMoreEconomyNews() {
        state =
            state.copy(
                visibleEconomyCount = min(state.visibleEconomyCount + NEWS_PAGE_SIZE, state.economyNews.size),
            )
    }

    override fun close() {
        loadJob?.cancel()
        repository.close()
    }

    private suspend fun loadSafely(request: suspend () -> List<NewsArticle>): List<NewsArticle> = try {
        request()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        emptyList()
    }
}
