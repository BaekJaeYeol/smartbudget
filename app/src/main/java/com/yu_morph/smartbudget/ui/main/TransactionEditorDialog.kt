@file:Suppress("LongMethod", "MagicNumber", "MatchingDeclarationName")

package com.yu_morph.smartbudget.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yu_morph.smartbudget.R
import com.yu_morph.smartbudget.data.remote.model.CreateTransactionRequest
import com.yu_morph.smartbudget.data.remote.model.PatchValue
import com.yu_morph.smartbudget.data.remote.model.TransactionData
import com.yu_morph.smartbudget.data.remote.model.TransactionSource
import com.yu_morph.smartbudget.data.remote.model.TransactionType
import com.yu_morph.smartbudget.data.remote.model.UpdateTransactionRequest
import com.yu_morph.smartbudget.ui.theme.SmartBudgetGreen
import java.time.LocalDate

internal data class TransactionEditorValue(
    val type: TransactionType,
    val category: String,
    val storeName: String,
    val note: String,
    val amount: Int,
)

internal fun TransactionEditorValue.toCreateRequest(date: LocalDate) = CreateTransactionRequest(
    date = date.toString(),
    type = type,
    source = TransactionSource.MANUAL,
    category = category,
    storeName = storeName.ifBlank { null },
    note = note.ifBlank { null },
    amount = amount,
)

internal fun TransactionEditorValue.toUpdateRequest(date: LocalDate) = UpdateTransactionRequest(
    date = date.toString(),
    type = type,
    category = category,
    storeName = PatchValue.Present(storeName.ifBlank { null }),
    note = PatchValue.Present(note.ifBlank { null }),
    amount = amount,
)

@Composable
internal fun TransactionEditorDialog(
    transaction: TransactionData?,
    isSaving: Boolean,
    onSave: (TransactionEditorValue) -> Unit,
    onDismiss: () -> Unit,
) {
    var type by rememberSaveable(transaction?.id) { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var category by rememberSaveable(transaction?.id) {
        mutableStateOf(transaction?.category ?: expenseTransactionCategoryCodes.first())
    }
    var storeName by rememberSaveable(transaction?.id) { mutableStateOf(transaction?.storeName.orEmpty()) }
    var note by rememberSaveable(transaction?.id) { mutableStateOf(transaction?.note.orEmpty()) }
    var amountText by rememberSaveable(transaction?.id) { mutableStateOf(transaction?.amount?.toString().orEmpty()) }
    val amount = amountText.toIntOrNull()
    val categories = if (type ==
        TransactionType.INCOME
    ) {
        incomeTransactionCategoryCodes
    } else {
        expenseTransactionCategoryCodes
    }
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                stringResource(
                    if (transaction ==
                        null
                    ) {
                        R.string.budget_book_add_transaction
                    } else {
                        R.string.budget_book_edit_transaction
                    },
                ),
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TransactionTypeSelector(type) { selectedType ->
                    type = selectedType
                    category =
                        if (selectedType == TransactionType.INCOME) {
                            incomeTransactionCategoryCodes.first()
                        } else {
                            expenseTransactionCategoryCodes.first()
                        }
                }
                Text(stringResource(R.string.budget_book_editor_category), fontSize = 12.sp)
                categories.chunked(2).forEach { rowCategories ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowCategories.forEach { code ->
                            CategoryOption(
                                code = code,
                                selected = category == code,
                                modifier = Modifier.weight(1f),
                                onClick = { category = code },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.budget_book_editor_source_place)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.budget_book_editor_note)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.budget_book_amount)) },
                    suffix = { Text(stringResource(R.string.settings_won)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.border(1.dp, editorBorderColor(), RoundedCornerShape(12.dp)),
                    ) {
                        Icon(Icons.Outlined.CameraAlt, stringResource(R.string.budget_book_ocr_not_available))
                    }
                    Button(
                        onClick = {
                            onSave(
                                TransactionEditorValue(
                                    type = type,
                                    category = category,
                                    storeName = storeName,
                                    note = note,
                                    amount = checkNotNull(amount),
                                ),
                            )
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        enabled = amount != null && amount > 0 && !isSaving,
                    ) {
                        Text(stringResource(R.string.budget_book_save))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text(stringResource(R.string.budget_book_cancel)) }
        },
    )
}

@Composable
private fun TransactionTypeSelector(type: TransactionType, onSelect: (TransactionType) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(TransactionType.EXPENSE, TransactionType.INCOME).forEach { option ->
            val selected = option == type
            Surface(
                modifier = Modifier.weight(1f).clickable { onSelect(option) },
                shape = RoundedCornerShape(18.dp),
                color = if (selected) SmartBudgetGreen else editorMutedBackground(),
            ) {
                Text(
                    text = stringResource(
                        if (option ==
                            TransactionType.EXPENSE
                        ) {
                            R.string.budget_book_expense
                        } else {
                            R.string.budget_book_income
                        },
                    ),
                    modifier = Modifier.padding(vertical = 11.dp),
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CategoryOption(code: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val category = transactionCategoryUi(code)
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) category.color.copy(alpha = 0.16f) else editorMutedBackground(),
        border = if (selected) BorderStroke(1.dp, category.color) else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(category.icon, null, modifier = Modifier.size(16.dp), tint = category.color)
            Text(stringResource(category.label), fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
internal fun DeleteTransactionDialog(isDeleting: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text(stringResource(R.string.budget_book_delete_confirm_title)) },
        text = { Text(stringResource(R.string.budget_book_delete_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isDeleting) {
                Text(stringResource(R.string.budget_book_delete_action), color = Color(0xFFD94A4A))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text(
                    stringResource(R.string.budget_book_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
private fun editorMutedBackground(): Color =
    if (MaterialTheme.colorScheme.background.luminance() > 0.5f) Color(0xFFF3F3F3) else Color(0xFF252525)

@Composable
private fun editorBorderColor(): Color =
    if (MaterialTheme.colorScheme.background.luminance() > 0.5f) Color(0xFFECECEC) else Color(0xFF343434)
