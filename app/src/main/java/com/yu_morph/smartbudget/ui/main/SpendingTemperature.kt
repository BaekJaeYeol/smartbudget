@file:Suppress("MagicNumber")

package com.yu_morph.smartbudget.ui.main

import com.yu_morph.smartbudget.data.remote.model.TransactionData
import com.yu_morph.smartbudget.data.remote.model.TransactionType
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.round

private const val NORMAL_TEMPERATURE = 36.5
private const val MINIMUM_TEMPERATURE = 28.0
private const val MAXIMUM_TEMPERATURE = 42.0
private const val EXPECTED_DISCRETIONARY_RATIO = 0.25
private const val RAPID_INCREASE_MULTIPLIER = 1.8
private const val LARGE_DAILY_BUDGET_RATIO = 0.1
private const val MEANINGFUL_DAILY_INCREASE_RATIO = 0.03

private val discretionaryCategories = setOf("shopping", "leisure", "other_expense")

internal enum class SpendingTemperatureLevel { BAD, NOT_GOOD, OKAY, GOOD, AMAZING }

internal fun spendingTemperatureLevel(temperature: Double): SpendingTemperatureLevel = when {
    temperature <= 32.0 -> SpendingTemperatureLevel.BAD
    temperature <= 35.0 -> SpendingTemperatureLevel.NOT_GOOD
    temperature <= 37.0 -> SpendingTemperatureLevel.OKAY
    temperature <= 39.0 -> SpendingTemperatureLevel.GOOD
    else -> SpendingTemperatureLevel.AMAZING
}

internal fun calculateSpendingTemperature(
    transactions: List<TransactionData>,
    budget: Int,
    month: YearMonth,
    today: LocalDate = LocalDate.now(),
): Double {
    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
    if (expenses.isEmpty() || budget <= 0) return NORMAL_TEMPERATURE

    val totalExpense = expenses.sumOf(TransactionData::amount)
    val daysElapsed = if (YearMonth.from(today) == month) today.dayOfMonth else month.lengthOfMonth()
    val expectedBudgetRatio = daysElapsed.toDouble() / month.lengthOfMonth()
    val usedBudgetRatio = totalExpense.toDouble() / budget
    val budgetPressure = ((usedBudgetRatio - expectedBudgetRatio).coerceAtLeast(0.0) * 4.5).coerceAtMost(5.0)
    val budgetReward = ((expectedBudgetRatio - usedBudgetRatio).coerceAtLeast(0.0) * 2.0).coerceAtMost(1.0)

    val discretionaryExpense = expenses.filter { it.category in discretionaryCategories }.sumOf(TransactionData::amount)
    val discretionaryRatio = discretionaryExpense.toDouble() / totalExpense
    val categoryPenalty =
        ((discretionaryRatio - EXPECTED_DISCRETIONARY_RATIO).coerceAtLeast(0.0) * 3.5).coerceAtMost(1.8)
    val categoryReward =
        ((EXPECTED_DISCRETIONARY_RATIO - discretionaryRatio).coerceAtLeast(0.0) * 1.5).coerceAtMost(0.4)

    val dailyExpenses = expenses.groupBy { LocalDate.parse(it.date) }.toSortedMap().mapValues { entry ->
        entry.value.sumOf(TransactionData::amount)
    }
    val rapidIncreaseDays = countRapidIncreaseDays(dailyExpenses.values.toList(), budget)
    val largeSpendingDays = dailyExpenses.values.count { it >= budget * LARGE_DAILY_BUDGET_RATIO }
    val rapidIncreasePenalty = (rapidIncreaseDays * 0.4).coerceAtMost(1.6)
    val largeSpendingPenalty = (largeSpendingDays * 0.45).coerceAtMost(1.8)

    val temperature =
        NORMAL_TEMPERATURE - budgetPressure + budgetReward - categoryPenalty + categoryReward -
            rapidIncreasePenalty - largeSpendingPenalty
    return (round(temperature.coerceIn(MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE) * 10.0) / 10.0)
}

private fun countRapidIncreaseDays(dailyExpenses: List<Int>, budget: Int): Int {
    var previousTotal = 0L
    var count = 0
    dailyExpenses.forEachIndexed { index, amount ->
        if (index > 0) {
            val previousAverage = previousTotal.toDouble() / index
            val increase = amount - previousAverage
            if (
                amount >= previousAverage * RAPID_INCREASE_MULTIPLIER &&
                increase >= budget * MEANINGFUL_DAILY_INCREASE_RATIO
            ) {
                count += 1
            }
        }
        previousTotal += amount
    }
    return count
}
