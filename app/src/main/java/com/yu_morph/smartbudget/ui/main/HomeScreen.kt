@file:Suppress("MagicNumber")

package com.yu_morph.smartbudget.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import kotlin.math.roundToInt

private val ExpenseRed = Color(0xFFEA5A5A)
private val IncomeBlue = Color(0xFF4B88D8)
private val BudgetYellow = Color(0xFFF2C94C)
private val BudgetOrange = Color(0xFFF2994A)
private val BudgetRed = Color(0xFFEB5757)

@Composable
internal fun HomeScreen(
    bottomPadding: Dp,
    dashboardState: HomeDashboardUiState,
    displayName: String,
    newsState: HomeNewsUiState,
    onDashboardLoad: () -> Unit,
    onProfileLoad: () -> Unit,
    onNewsLoad: () -> Unit,
    onPreviousNewsPage: () -> Unit,
    onNextNewsPage: () -> Unit,
    onArticleClick: (NewsArticle) -> Unit,
) {
    LaunchedEffect(Unit) {
        onDashboardLoad()
        onProfileLoad()
        onNewsLoad()
    }
    MainTabContainer(bottomPadding = bottomPadding) {
        MonthlyOverviewCard(dashboardState)
        Spacer(Modifier.height(14.dp))
        MonthlyCashFlowCard(dashboardState)
        Spacer(Modifier.height(14.dp))
        SpendingTemperatureCard(
            temperature = dashboardState.spendingTemperature,
            displayName = displayName.ifBlank { stringResource(R.string.settings_profile_default_name) },
        )
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
    val remainingBudget = (state.budget - state.expense).coerceAtLeast(0)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.height(72.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.home_monthly_budget, state.month.monthValue),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    formatHomeAmount(remainingBudget),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            BudgetUsageIndicator(
                progress = progress,
                progressColor = progressColor,
            )
        }
    }
}

@Composable
private fun BudgetUsageIndicator(progress: Float, progressColor: Color) {
    val boundedProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = Modifier.size(72.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { boundedProgress },
            modifier = Modifier.size(72.dp),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 7.dp,
            strokeCap = StrokeCap.Butt,
        )
        Text(
            text = stringResource(R.string.home_budget_usage_percent, (progress * 100f).roundToInt()),
            color = progressColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SpendingTemperatureCard(temperature: Double, displayName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        SpendingTemperatureRow(temperature = temperature, displayName = displayName)
    }
}

@Composable
private fun MonthlyCashFlowCard(state: HomeDashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CashFlowAmount(
                label = stringResource(R.string.budget_book_income),
                amount = state.income,
                color = ExpenseRed,
                modifier = Modifier.weight(1f),
            )
            CashFlowAmount(
                label = stringResource(R.string.budget_book_expense),
                amount = state.expense,
                color = IncomeBlue,
                modifier = Modifier.weight(1f),
            )
            CashFlowAmount(
                label = stringResource(R.string.budget_book_total),
                amount = state.income - state.expense,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CashFlowAmount(label: String, amount: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = formatHomeAmount(amount),
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

internal fun budgetUsageColor(progress: Float): Color = when {
    progress <= 0.5f -> SmartBudgetGreen
    progress <= 0.7f -> BudgetYellow
    progress <= 0.9f -> BudgetOrange
    else -> BudgetRed
}

@Composable
private fun formatHomeAmount(amount: Int): String {
    val locale = LocalConfiguration.current.locales[0]
    return stringResource(
        R.string.settings_budget_history_amount,
        NumberFormat.getNumberInstance(locale).format(amount),
    )
}
