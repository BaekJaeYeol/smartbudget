@file:Suppress("MagicNumber")

package com.yu_morph.smartbudget.ui.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.yu_morph.smartbudget.R
import com.yu_morph.smartbudget.data.remote.news.NewsArticle
import com.yu_morph.smartbudget.ui.theme.SmartBudgetGreen
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
internal fun NewsCard(state: HomeNewsUiState, onShowMore: () -> Unit, onArticleClick: (NewsArticle) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            BreakingNewsTicker(
                articles = state.breakingNews,
                isLoading = state.isLoading,
                onArticleClick = onArticleClick,
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outlineVariant)
            EconomyNewsSection(
                state = state,
                onShowMore = onShowMore,
                onArticleClick = onArticleClick,
            )
        }
    }
}

@Composable
private fun BreakingNewsTicker(
    articles: List<NewsArticle>,
    isLoading: Boolean,
    onArticleClick: (NewsArticle) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_breaking_news),
            modifier = Modifier.padding(start = 18.dp, end = 8.dp),
            color = MaterialTheme.colorScheme.error,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
        when {
            articles.isNotEmpty() -> ArticleMarquee(articles, onArticleClick, Modifier.weight(1f))

            isLoading -> CircularProgressIndicator(
                modifier = Modifier.padding(start = 6.dp).size(17.dp),
                strokeWidth = 2.dp,
                color = SmartBudgetGreen,
            )

            else -> Text(
                text = stringResource(R.string.home_news_unavailable),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun ArticleMarquee(
    articles: List<NewsArticle>,
    onArticleClick: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current.density
    var cycleWidth by remember(articles) { mutableIntStateOf(0) }
    LaunchedEffect(articles, cycleWidth) {
        if (cycleWidth <= 0) return@LaunchedEffect
        while (true) {
            scrollState.scrollTo(0)
            delay(1_000L)
            val durationMillis = ((cycleWidth / density) / 28f * 1_000f).roundToInt().coerceAtLeast(8_000)
            scrollState.animateScrollTo(
                value = cycleWidth,
                animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing),
            )
        }
    }
    Row(
        modifier = modifier.clipToBounds().horizontalScroll(scrollState, enabled = false),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArticleTickerCycle(
            articles = articles,
            onArticleClick = onArticleClick,
            modifier = Modifier.onSizeChanged { cycleWidth = it.width },
        )
        ArticleTickerCycle(articles = articles, onArticleClick = onArticleClick)
    }
}

@Composable
private fun ArticleTickerCycle(
    articles: List<NewsArticle>,
    onArticleClick: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        articles.forEach { article ->
            Text(
                text = article.title,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onArticleClick(article) }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                fontSize = 13.sp,
                maxLines = 1,
                softWrap = false,
            )
            Text("·", color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun EconomyNewsSection(state: HomeNewsUiState, onShowMore: () -> Unit, onArticleClick: (NewsArticle) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
        Text(
            text = stringResource(R.string.home_economy_news),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        val visibleArticles = state.economyNews.take(state.visibleEconomyCount)
        if (visibleArticles.isEmpty()) {
            Text(
                text =
                    stringResource(
                        if (state.isLoading) R.string.home_news_loading else R.string.home_news_unavailable,
                    ),
                modifier = Modifier.padding(vertical = 18.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        } else {
            visibleArticles.forEachIndexed { index, article ->
                EconomyArticleRow(article = article, onClick = { onArticleClick(article) })
                if (index != visibleArticles.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                }
            }
        }
        if (state.visibleEconomyCount < state.economyNews.size) {
            TextButton(
                onClick = onShowMore,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_news_more),
                    color = SmartBudgetGreen,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun EconomyArticleRow(article: NewsArticle, onClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (article.thumbnailUrl != null) {
            AsyncImage(
                model = article.thumbnailUrl,
                contentDescription = stringResource(R.string.home_news_thumbnail, article.title),
                modifier =
                    Modifier
                        .size(width = 82.dp, height = 66.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .size(width = 82.dp, height = 66.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Article,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                text = article.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (article.description.isNotBlank()) {
                Text(
                    text = article.description,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
