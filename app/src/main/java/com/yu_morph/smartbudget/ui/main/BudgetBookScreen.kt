@file:Suppress("LongMethod", "MagicNumber", "TooManyFunctions")

package com.yu_morph.smartbudget.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yu_morph.smartbudget.R
import com.yu_morph.smartbudget.ui.theme.SmartBudgetGreen
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val CalendarIncomeColor = Color(0xFF3FB284)
private val CalendarExpenseColor = Color(0xFFF1A443)
private val CalendarTransferColor = Color(0xFF4B88D8)

@Composable
internal fun BudgetBookRoute(bottomPadding: Dp, onOpenMonth: (YearMonth) -> Unit) =
    BudgetBookOverview(bottomPadding = bottomPadding, onOpenMonth = onOpenMonth)

@Composable
private fun BudgetBookOverview(bottomPadding: Dp, onOpenMonth: (YearMonth) -> Unit) {
    val currentMonth = YearMonth.now()
    MainTabContainer(bottomPadding = bottomPadding) {
        LedgerCard(
            modifier = Modifier.clickable { onOpenMonth(currentMonth) },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        stringResource(R.string.budget_book_current_month),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(monthTitle(currentMonth), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    stringResource(R.string.budget_book_view_all),
                    color = SmartBudgetGreen,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(16.dp))
            MonthCalendar(month = currentMonth, compact = true)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CalendarLegend(CalendarIncomeColor, stringResource(R.string.budget_book_income))
                CalendarLegend(CalendarExpenseColor, stringResource(R.string.budget_book_expense))
                CalendarLegend(CalendarTransferColor, stringResource(R.string.budget_book_fixed_cost))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.budget_book_previous_months), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        LedgerCard {
            for (offset in 1L..4L) {
                val month = currentMonth.minusMonths(offset)
                PreviousMonthRow(
                    month = month,
                    income = 3_200_000 - offset.toInt() * 120_000,
                    expense = 1_150_000 + offset.toInt() * 85_000,
                    onClick = { onOpenMonth(month) },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BudgetBookDetailScreen(month: YearMonth, onMonthChange: (YearMonth) -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                expandedHeight = 52.dp,
                title = {
                    Text(
                        text = stringResource(R.string.budget_book_detail),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.navigate_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            MonthSelector(month, onMonthChange)
            Spacer(Modifier.height(8.dp))
            MonthCalendar(
                month = month,
                compact = false,
                modifier = Modifier.fillMaxWidth().weight(1f),
                fillAvailableHeight = true,
            )
        }
    }
}

@Composable
private fun MonthCalendar(
    month: YearMonth,
    compact: Boolean,
    modifier: Modifier = Modifier,
    fillAvailableHeight: Boolean = false,
) {
    val days = remember(month) { calendarDays(month) }
    val cellHeight = if (compact) 42.dp else 72.dp
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(
                R.string.weekday_sunday,
                R.string.weekday_monday,
                R.string.weekday_tuesday,
                R.string.weekday_wednesday,
                R.string.weekday_thursday,
                R.string.weekday_friday,
                R.string.weekday_saturday,
            ).forEach { day ->
                Text(
                    stringResource(day),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        days.chunked(7).forEach { week ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .then(if (fillAvailableHeight) Modifier.weight(1f) else Modifier),
            ) {
                week.forEach { date -> CalendarDay(date, cellHeight, compact, fillAvailableHeight) }
            }
        }
    }
}

@Composable
private fun RowScope.CalendarDay(date: LocalDate?, height: Dp, compact: Boolean, fillAvailableHeight: Boolean) {
    Column(
        modifier =
            Modifier
                .weight(1f)
                .then(if (fillAvailableHeight) Modifier.fillMaxHeight() else Modifier.height(height))
                .padding(top = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (date != null) {
            val isToday = date == LocalDate.now()
            Box(
                modifier =
                    Modifier
                        .size(if (compact) 24.dp else 28.dp)
                        .background(if (isToday) SmartBudgetGreen else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    date.dayOfMonth.toString(),
                    color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                )
            }
            if (date.dayOfMonth % 3 == 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(Modifier.size(4.dp).background(CalendarExpenseColor, CircleShape))
                    Box(Modifier.size(4.dp).background(CalendarTransferColor, CircleShape))
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(month: YearMonth, onMonthChange: (YearMonth) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onMonthChange(month.minusMonths(1)) }) {
            Icon(Icons.Outlined.ChevronLeft, stringResource(R.string.budget_book_previous_month))
        }
        Text(monthTitle(month), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = { onMonthChange(month.plusMonths(1)) }) {
            Icon(Icons.Outlined.ChevronRight, stringResource(R.string.budget_book_next_month))
        }
    }
}

@Composable
private fun PreviousMonthRow(month: YearMonth, income: Int, expense: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(monthTitle(month), fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.budget_book_income_amount, income.withComma()),
                color = CalendarIncomeColor,
                fontSize = 13.sp,
            )
        }
        Text(stringResource(R.string.budget_book_expense_amount, expense.withComma()), fontWeight = FontWeight.SemiBold)
        Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CalendarLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
        Text(label, modifier = Modifier.padding(start = 5.dp), fontSize = 12.sp)
    }
}

@Composable
private fun LedgerCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), content = content)
    }
}

internal fun calendarDays(month: YearMonth): List<LocalDate?> {
    val leading = month.atDay(1).dayOfWeek.value % DayOfWeek.SUNDAY.value.coerceAtLeast(1)
    val normalizedLeading = if (month.atDay(1).dayOfWeek == DayOfWeek.SUNDAY) 0 else leading
    val result = MutableList<LocalDate?>(normalizedLeading) { null }
    for (day in 1..month.lengthOfMonth()) result += month.atDay(day)
    while (result.size % 7 != 0) result += null
    return result
}

@Composable
private fun monthTitle(month: YearMonth): String = stringResource(R.string.year_month, month.year, month.monthValue)

private fun Int.withComma(): String = String.format("%,d", this)
