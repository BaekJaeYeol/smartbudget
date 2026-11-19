@file:Suppress("MagicNumber")

package com.yu_morph.smartbudget.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        FinancialOverviewCard(
            state = dashboardState,
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
private fun FinancialOverviewCard(state: HomeDashboardUiState, displayName: String) {
    val progress = if (state.budget > 0) state.expense.toFloat() / state.budget else 0f
    val progressColor = budgetUsageColor(progress)
    val remainingBudget = (state.budget - state.expense).coerceAtLeast(0)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            GreetingSection(displayName)
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = dividerColor,
            )
            MonthlyCashFlowSection(state)
            MonthlyBudgetSection(state, remainingBudget, progress, progressColor)
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = dividerColor,
            )
            SpendingTemperatureRow(temperature = state.spendingTemperature)
        }
    }
}

@Composable
private fun GreetingSection(displayName: String) {
    Box(modifier = Modifier.fillMaxWidth().height(98.dp).clipToBounds()) {
        Text(
            text = stringResource(R.string.home_greeting, displayName),
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, end = 128.dp, bottom = 18.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Image(
            painter = painterResource(R.drawable.home_greeting_robot),
            contentDescription = stringResource(R.string.home_greeting_robot_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier.align(Alignment.BottomEnd).size(142.dp).offset(x = 4.dp, y = 37.dp),
        )
    }
}

@Composable
private fun MonthlyBudgetSection(
    state: HomeDashboardUiState,
    remainingBudget: Int,
    progress: Float,
    progressColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.height(72.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                stringResource(R.string.home_monthly_overview, state.month.monthValue),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.home_remaining_budget_amount, formatHomeAmount(remainingBudget)),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.home_total_budget_amount, formatHomeAmount(state.budget)),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
        BudgetUsageIndicator(
            progress = progress,
            progressColor = progressColor,
        )
    }
}

@Composable
private fun MonthlyCashFlowSection(state: HomeDashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 20.dp, end = 12.dp),
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
        Text(
            text = formatHomeAmount(amount),
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
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
            strokeWidth = 5.dp,
            strokeCap = StrokeCap.Butt,
        )
        Text(
            text = stringResource(R.string.home_budget_usage_percent, (progress * 100f).roundToInt()),
            color = progressColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 12.sp,
            textAlign = TextAlign.Center,
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
