@file:Suppress("MagicNumber", "MatchingDeclarationName", "TooManyFunctions", "LongMethod")

package com.yu_morph.smartbudget.ui.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blongho.country_data.World
import com.yu_morph.smartbudget.R
import com.yu_morph.smartbudget.data.local.DisplayMode
import com.yu_morph.smartbudget.ui.auth.AuthTextField
import com.yu_morph.smartbudget.ui.auth.PrimaryAuthButton
import com.yu_morph.smartbudget.ui.auth.SignUpValidation
import com.yu_morph.smartbudget.ui.theme.SmartBudgetGreen
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class SettingsDetailDestination {
    PROFILE,
    BANK_CONNECTION,
    MONTHLY_BUDGET,
    LANGUAGE,
    NOTIFICATIONS,
    DISPLAY,
    WITHDRAWAL,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsDetailScreen(
    destination: SettingsDetailDestination,
    state: SettingsUiState,
    onBack: () -> Unit,
    onSaveProfile: (String, String, String) -> Unit,
    onLoadBudget: () -> Unit,
    onSaveBudget: (Int) -> Unit,
    onSyncBankTransactions: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onNotificationChanged: (NotificationSetting, Boolean) -> Unit,
    onDisplayModeChanged: (DisplayMode) -> Unit,
    onDeleteAccount: () -> Unit,
) {
    var saveAction by remember(destination) { mutableStateOf<SettingsSaveAction?>(null) }
    BackHandler(onBack = onBack)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                expandedHeight = 52.dp,
                title = {
                    Text(
                        text = stringResource(destination.titleResource()),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.navigate_back))
                    }
                },
                actions = {
                    saveAction?.let { action ->
                        IconButton(onClick = action.onSave, enabled = action.enabled && !state.isLoading) {
                            Icon(Icons.Rounded.Save, stringResource(R.string.settings_save_action))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        when (destination) {
            SettingsDetailDestination.PROFILE -> ProfileSettings(
                state = state,
                modifier = Modifier.padding(innerPadding),
                onSave = onSaveProfile,
                onSaveActionChanged = { saveAction = it },
            )

            SettingsDetailDestination.MONTHLY_BUDGET -> BudgetSettings(
                state = state,
                modifier = Modifier.padding(innerPadding),
                onLoad = onLoadBudget,
                onSave = onSaveBudget,
                onSaveActionChanged = { saveAction = it },
            )

            SettingsDetailDestination.BANK_CONNECTION -> BankConnectionScreen(
                isLoading = state.isBankSyncing,
                modifier = Modifier.padding(innerPadding),
                onConnect = onSyncBankTransactions,
            )

            SettingsDetailDestination.LANGUAGE -> LanguageSettings(
                selectedLanguage = state.appSettings.languageTag,
                modifier = Modifier.padding(innerPadding),
                onSelected = onLanguageSelected,
            )

            SettingsDetailDestination.NOTIFICATIONS -> NotificationSettings(
                state = state,
                modifier = Modifier.padding(innerPadding),
                onChanged = onNotificationChanged,
            )

            SettingsDetailDestination.DISPLAY -> DisplaySettings(
                selectedMode = state.appSettings.displayMode,
                modifier = Modifier.padding(innerPadding),
                onSelected = onDisplayModeChanged,
            )

            SettingsDetailDestination.WITHDRAWAL -> WithdrawalSettings(
                modifier = Modifier.padding(innerPadding),
                onDeleteAccount = onDeleteAccount,
            )
        }
    }
}

@Composable
private fun ProfileSettings(
    state: SettingsUiState,
    modifier: Modifier,
    onSave: (String, String, String) -> Unit,
    onSaveActionChanged: (SettingsSaveAction) -> Unit,
) {
    val context = LocalContext.current
    var displayName by rememberSaveable(state.displayName) { mutableStateOf(state.displayName) }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var passwordConfirmation by rememberSaveable { mutableStateOf("") }
    val passwordUnchanged = currentPassword.isBlank() && newPassword.isBlank() && passwordConfirmation.isBlank()
    val passwordValid =
        currentPassword.isNotBlank() &&
            SignUpValidation.isValidPassword(newPassword) &&
            newPassword == passwordConfirmation
    val hasChanges = displayName.trim() != state.displayName || !passwordUnchanged
    val saveAction =
        remember(displayName, currentPassword, newPassword, passwordConfirmation, state.displayName, onSave) {
            SettingsSaveAction(
                enabled = hasChanges,
                onSave = {
                    if (!SignUpValidation.isValidDisplayName(displayName) || (!passwordUnchanged && !passwordValid)) {
                        Toast.makeText(context, R.string.settings_profile_invalid_input, Toast.LENGTH_SHORT).show()
                    } else {
                        onSave(displayName.trim(), currentPassword, newPassword)
                    }
                },
            )
        }
    LaunchedEffect(saveAction) { onSaveActionChanged(saveAction) }
    SettingsColumn(modifier) {
        SettingsSection(title = stringResource(R.string.settings_name_tab)) {
            AuthTextField(
                value = displayName,
                onValueChange = { displayName = it },
                placeholder = stringResource(R.string.display_name_hint),
            )
            Text(
                text = stringResource(R.string.display_name_condition_label),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }
        SettingsSection(title = stringResource(R.string.settings_password_tab)) {
            PasswordField(currentPassword, { currentPassword = it }, R.string.settings_current_password)
            PasswordField(newPassword, { newPassword = it }, R.string.settings_new_password)
            PasswordField(passwordConfirmation, { passwordConfirmation = it }, R.string.password_confirm_hint)
            Text(
                text = stringResource(R.string.password_condition_label),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun BudgetSettings(
    state: SettingsUiState,
    modifier: Modifier,
    onLoad: () -> Unit,
    onSave: (Int) -> Unit,
    onSaveActionChanged: (SettingsSaveAction) -> Unit,
) {
    val context = LocalContext.current
    var amountText by rememberSaveable(state.budgetAmount) { mutableStateOf(state.budgetAmount.toString()) }
    val amount = amountText.toIntOrNull()
    val hasChanges = amountText != state.budgetAmount.toString()
    val saveAction =
        remember(amount, hasChanges, onSave) {
            SettingsSaveAction(
                enabled = hasChanges,
                onSave = {
                    if (amount == null || amount !in 0..10_000_000) {
                        Toast.makeText(context, R.string.settings_budget_invalid_input, Toast.LENGTH_SHORT).show()
                    } else {
                        onSave(amount)
                    }
                },
            )
        }
    LaunchedEffect(saveAction) { onSaveActionChanged(saveAction) }
    LaunchedEffect(Unit) { onLoad() }
    SettingsColumn(modifier) {
        SettingsSection(title = stringResource(R.string.settings_budget_setup_tab)) {
            AuthTextField(
                value = amountText,
                onValueChange = { value -> amountText = value.filter(Char::isDigit).take(8) },
                placeholder = stringResource(R.string.settings_budget_amount),
                suffix = { Text(stringResource(R.string.settings_won)) },
                keyboardType = KeyboardType.Number,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(500_000, 1_000_000, 2_000_000).forEach { amount ->
                    BudgetPresetButton(
                        text = stringResource(R.string.settings_budget_quick_amount, amount / 10_000),
                        onClick = { amountText = amount.toString() },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        SettingsSection(title = stringResource(R.string.settings_budget_history_tab)) {
            if (state.budgetHistory.isEmpty()) {
                Text(
                    stringResource(R.string.settings_budget_history_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                state.budgetHistory.forEach { item ->
                    SettingValueRow(
                        label = item.month.format(DateTimeFormatter.ofPattern("yyyy년 M월")),
                        value = stringResource(
                            R.string.settings_budget_history_amount,
                            NumberFormat.getNumberInstance(Locale.KOREA).format(item.amount),
                        ),
                    )
                }
            }
        }
    }
}

private data class SettingsSaveAction(val enabled: Boolean, val onSave: () -> Unit)

@Composable
private fun LanguageSettings(selectedLanguage: String, modifier: Modifier, onSelected: (String) -> Unit) {
    val languages =
        listOf(
            LanguageOption("en-US", "English", "us", R.string.settings_english_flag),
            LanguageOption("ko", "한국어", "kr", R.string.settings_korean_flag),
        ).sortedByDescending { it.tag }
    SettingsColumn(modifier) {
        languages.forEach { language ->
            SelectionRow(
                title = language.nativeName,
                selected = selectedLanguage == language.tag,
                onClick = { onSelected(language.tag) },
                leadingContent = {
                    Image(
                        painter = painterResource(World.getFlagOf(language.countryCode)),
                        contentDescription = stringResource(language.flagDescriptionResource),
                        modifier = Modifier.size(width = 34.dp, height = 24.dp).clip(RoundedCornerShape(5.dp)),
                    )
                },
            )
        }
    }
}

@Composable
private fun NotificationSettings(
    state: SettingsUiState,
    modifier: Modifier,
    onChanged: (NotificationSetting, Boolean) -> Unit,
) {
    SettingsColumn(modifier) {
        SettingsSection(title = stringResource(R.string.settings_notification_categories)) {
            SwitchRow(
                R.string.settings_budget_approaching_notification,
                state.appSettings.budgetApproachingNotification,
            ) { onChanged(NotificationSetting.BUDGET_APPROACHING, it) }
            SwitchRow(
                R.string.settings_budget_exceeded_notification,
                state.appSettings.budgetExceededNotification,
            ) { onChanged(NotificationSetting.BUDGET_EXCEEDED, it) }
        }
        SettingsSection(title = stringResource(R.string.settings_analysis_section)) {
            SwitchRow(R.string.settings_analysis_notification, state.appSettings.analysisNotification) {
                onChanged(NotificationSetting.ANALYSIS, it)
            }
        }
    }
}

@Composable
private fun DisplaySettings(selectedMode: DisplayMode, modifier: Modifier, onSelected: (DisplayMode) -> Unit) {
    SettingsColumn(modifier) {
        DisplayMode.entries.forEach { mode ->
            SelectionRow(
                title = stringResource(mode.labelResource()),
                selected = selectedMode == mode,
                onClick = { onSelected(mode) },
                leadingContent = {
                    Icon(
                        imageVector = mode.icon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun WithdrawalSettings(modifier: Modifier, onDeleteAccount: () -> Unit) {
    var showConfirmation by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(top = 20.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_withdraw_confirm_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.settings_withdraw_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.account_withdrawal_robot),
                contentDescription = stringResource(R.string.settings_withdraw_robot_description),
                modifier = Modifier.size(210.dp),
            )
        }
        PrimaryAuthButton(
            text = stringResource(R.string.settings_withdraw_action),
            onClick = { showConfirmation = true },
        )
    }
    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text(stringResource(R.string.settings_withdraw_confirm_title)) },
            text = { Text(stringResource(R.string.settings_withdraw_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = onDeleteAccount,
                    colors = ButtonDefaults.textButtonColors(contentColor = SmartBudgetGreen),
                ) {
                    Text(stringResource(R.string.settings_withdraw_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) { Text(stringResource(R.string.close_action)) }
            },
        )
    }
}

@Composable
private fun SettingsColumn(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
private fun BudgetPresetButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(9.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SmartBudgetGreen,
            ),
    ) {
        Text(text)
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        content()
    }
}

@Composable
private fun PasswordField(value: String, onValueChange: (String) -> Unit, label: Int) {
    AuthTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = stringResource(label),
        isPassword = true,
    )
}

@Composable
private fun SelectionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent?.let {
            it()
            androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
        }
        Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp)
        androidx.compose.foundation.layout.Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .indication(interactionSource, ripple(bounded = true, radius = 18.dp))
                    .background(if (selected) SmartBudgetGreen else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(label: Int, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onCheckedChange(!checked) },
                )
                .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(label), modifier = Modifier.weight(1f), fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.graphicsLayer(scaleX = 0.82f, scaleY = 0.82f),
        )
    }
}

@Composable
private fun SettingValueRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun SettingsDetailDestination.titleResource(): Int = when (this) {
    SettingsDetailDestination.PROFILE -> R.string.settings_edit_profile
    SettingsDetailDestination.BANK_CONNECTION -> R.string.settings_connect_bank
    SettingsDetailDestination.MONTHLY_BUDGET -> R.string.settings_monthly_budget
    SettingsDetailDestination.LANGUAGE -> R.string.settings_language
    SettingsDetailDestination.NOTIFICATIONS -> R.string.settings_notifications
    SettingsDetailDestination.DISPLAY -> R.string.settings_display
    SettingsDetailDestination.WITHDRAWAL -> R.string.settings_withdraw
}

private data class LanguageOption(
    val tag: String,
    val nativeName: String,
    val countryCode: String,
    val flagDescriptionResource: Int,
)

private fun DisplayMode.labelResource(): Int = when (this) {
    DisplayMode.LIGHT -> R.string.settings_theme_light
    DisplayMode.DARK -> R.string.settings_theme_dark
    DisplayMode.SYSTEM -> R.string.settings_theme_system
}

private fun DisplayMode.icon() = when (this) {
    DisplayMode.LIGHT -> Icons.Outlined.LightMode
    DisplayMode.DARK -> Icons.Outlined.DarkMode
    DisplayMode.SYSTEM -> Icons.Outlined.SettingsBrightness
}
