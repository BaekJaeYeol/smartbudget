@file:Suppress("MagicNumber")

package com.yu_morph.smartbudget.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yu_morph.smartbudget.R
import com.yu_morph.smartbudget.data.remote.news.NewsArticle
import com.yu_morph.smartbudget.ui.theme.SmartBudgetGreen
import java.text.NumberFormat

private val ExpenseRed = Color(0xFFEA5A5A)
private val IncomeBlue = Color(0xFF4B88D8)
private val BudgetYellow = Color(0xFFF2C94C)
private val BudgetOrange = Color(0xFFF2994A)
private val BudgetRed = Color(0xFFEB5757)

@Composable
internal fun HomeScreen(
    bottomPadding: Dp,
    dashboardState: HomeDashboardUiState,
    newsState: HomeNewsUiState,
    onDashboardLoad: () -> Unit,
    onNewsLoad: () -> Unit,
    onPreviousNewsPage: () -> Unit,
    onNextNewsPage: () -> Unit,
    onArticleClick: (NewsArticle) -> Unit,
) {
    LaunchedEffect(Unit) {
        onDashboardLoad()
        onNewsLoad()
    }
    MainTabContainer(bottomPadding = bottomPadding) {
        MonthlyOverviewCard(dashboardState)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                label = stringResource(R.string.home_monthly_income),
                amount = dashboardState.income,
                indicatorColor = ExpenseRed,
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                label = stringResource(R.string.home_monthly_expense),
                amount = dashboardState.expense,
                indicatorColor = IncomeBlue,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(14.dp))
        NewsCard(
            state = newsState,
            onPreviousPage = onPreviousNewsPage,
            onNextPage = onNextNewsPage,
            onArticleClick = onArticleClick,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MonthlyOverviewCard(state: HomeDashboardUiState) {
    val progress = if (state.budget > 0) state.expense.toFloat() / state.budget else 0f
    val progressColor = budgetUsageColor(progress)
    val percentFormat = NumberFormat.getPercentInstance(LocalConfiguration.current.locales[0]).apply {
        maximumFractionDigits = 1
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    stringResource(R.string.home_monthly_spending, state.month.monthValue),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                Text(formatHomeAmount(state.expense), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(
                        R.string.home_budget_progress,
                        formatHomeAmount(state.budget),
                        percentFormat.format(progress),
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(12.dp))
                BudgetUsageBar(progress = progress, progressColor = progressColor)
                Spacer(Modifier.height(9.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        stringResource(R.string.home_remaining_budget),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                    Text(
                        formatHomeAmount((state.budget - state.expense).coerceAtLeast(0)),
                        color = progressColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            )
            SpendingTemperatureRow(temperature = state.spendingTemperature)
        }
    }
}

@Composable
private fun BudgetUsageBar(progress: Float, progressColor: Color) {
    val boundedProgress = progress.coerceIn(0f, 1f)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
    ) {
        if (boundedProgress > 0f) {
            Box(Modifier.weight(boundedProgress).fillMaxHeight().background(progressColor))
        }
        if (boundedProgress < 1f) {
            Box(
                Modifier
                    .weight(1f - boundedProgress)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

internal fun budgetUsageColor(progress: Float): Color = when {
    progress <= 0.5f -> SmartBudgetGreen
    progress <= 0.7f -> BudgetYellow
    progress <= 0.9f -> BudgetOrange
    else -> BudgetRed
}

@Composable
private fun SummaryCard(label: String, amount: Int, indicatorColor: Color, modifier: Modifier = Modifier) {
    DashboardCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(9.dp).background(indicatorColor, CircleShape))
            Text(
                label,
                modifier = Modifier.padding(start = 7.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(formatHomeAmount(amount), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun formatHomeAmount(amount: Int): String {
    val locale = LocalConfiguration.current.locales[0]
    return stringResource(
        R.string.settings_budget_history_amount,
        NumberFormat.getNumberInstance(locale).format(amount),
    )
}

@Composable
private fun DashboardCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), content = content)
    }
}
