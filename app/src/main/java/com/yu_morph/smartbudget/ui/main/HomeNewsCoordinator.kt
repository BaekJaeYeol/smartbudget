package com.yu_morph.smartbudget.ui.main

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yu_morph.smartbudget.data.remote.news.NewsArticle
import com.yu_morph.smartbudget.data.remote.news.NewsFeedRepository
import java.io.Closeable
import kotlin.math.ceil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

private const val NEWS_PAGE_SIZE = 5

data class HomeNewsUiState(
    val breakingNews: List<NewsArticle> = emptyList(),
    val economyNews: List<NewsArticle> = emptyList(),
    val economyPage: Int = 0,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
) {
    val economyPageCount: Int
        get() = ceil(economyNews.size.toDouble() / NEWS_PAGE_SIZE).toInt()

    val economyPageArticles: List<NewsArticle>
        get() = economyNews.drop(economyPage * NEWS_PAGE_SIZE).take(NEWS_PAGE_SIZE)
}

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
                        economyPage = 0,
                        hasError = breaking.isEmpty() && economy.isEmpty(),
                    )
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun showPreviousEconomyPage() {
        if (state.economyPage > 0) state = state.copy(economyPage = state.economyPage - 1)
    }

    fun showNextEconomyPage() {
        if (state.economyPage + 1 < state.economyPageCount) {
            state = state.copy(economyPage = state.economyPage + 1)
        }
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
