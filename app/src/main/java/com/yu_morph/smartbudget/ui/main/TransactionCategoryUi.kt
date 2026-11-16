package com.yu_morph.smartbudget.ui.main

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.yu_morph.smartbudget.R

internal data class TransactionCategoryUi(@param:StringRes val label: Int, val icon: ImageVector, val color: Color)

private val UnknownCategoryColor = Color(0xFF8A8A8A)

private val transactionCategories =
    mapOf(
        "food" to
            TransactionCategoryUi(R.string.transaction_category_food, Icons.Outlined.Restaurant, Color(0xFFF05D5E)),
        "transport" to
            TransactionCategoryUi(
                R.string.transaction_category_transport,
                Icons.Outlined.DirectionsBus,
                Color(0xFF3A86FF),
            ),
        "shopping" to
            TransactionCategoryUi(
                R.string.transaction_category_shopping,
                Icons.Outlined.ShoppingBag,
                Color(0xFF9B5DE5),
            ),
        "housing_utilities" to
            TransactionCategoryUi(
                R.string.transaction_category_housing_utilities,
                Icons.Outlined.Home,
                Color(0xFF00A896),
            ),
        "health" to
            TransactionCategoryUi(
                R.string.transaction_category_health,
                Icons.Outlined.MedicalServices,
                Color(0xFFEF476F),
            ),
        "leisure" to
            TransactionCategoryUi(
                R.string.transaction_category_leisure,
                Icons.Outlined.SportsEsports,
                Color(0xFFFF9F1C),
            ),
        "education" to
            TransactionCategoryUi(R.string.transaction_category_education, Icons.Outlined.School, Color(0xFF5E60CE)),
        "other_expense" to
            TransactionCategoryUi(
                R.string.transaction_category_other_expense,
                Icons.Outlined.MoreHoriz,
                Color(0xFF6C757D),
            ),
        "salary" to
            TransactionCategoryUi(R.string.transaction_category_salary, Icons.Outlined.Payments, Color(0xFF2A9D8F)),
        "side_income" to
            TransactionCategoryUi(R.string.transaction_category_side_income, Icons.Outlined.Work, Color(0xFF00B4D8)),
        "investment_income" to
            TransactionCategoryUi(
                R.string.transaction_category_investment_income,
                Icons.AutoMirrored.Outlined.TrendingUp,
                Color(0xFFD4A017),
            ),
        "other_income" to
            TransactionCategoryUi(
                R.string.transaction_category_other_income,
                Icons.Outlined.AccountBalanceWallet,
                Color(0xFFA26769),
            ),
    )

internal val transactionCategoryCodes: Set<String> = transactionCategories.keys

internal fun transactionCategoryUi(code: String): TransactionCategoryUi = transactionCategories[code]
    ?: TransactionCategoryUi(R.string.transaction_category_unknown, Icons.Outlined.MoreHoriz, UnknownCategoryColor)
