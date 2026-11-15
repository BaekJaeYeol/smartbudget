@file:Suppress("MagicNumber", "MatchingDeclarationName", "TooManyFunctions", "LongMethod")

package com.yu_morph.smartbudget.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onUpdateDisplayName: (String) -> Unit,
    onUpdatePassword: (String, String) -> Unit,
    onLoadBudget: () -> Unit,
    onSaveBudget: (Int) -> Unit,
    onSyncBankTransactions: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onNotificationChanged: (NotificationSetting, Boolean) -> Unit,
    onDisplayModeChanged: (DisplayMode) -> Unit,
    onVerifyWithdrawal: (String, () -> Unit) -> Unit,
    onDeleteAccount: () -> Unit,
) {
    BackHandler(onBack = onBack)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        when (destination) {
            SettingsDetailDestination.PROFILE -> ProfileSettings(
                state = state,
                modifier = Modifier.padding(innerPadding),
                onUpdateDisplayName = onUpdateDisplayName,
                onUpdatePassword = onUpdatePassword,
            )

            SettingsDetailDestination.MONTHLY_BUDGET -> BudgetSettings(
                state = state,
                modifier = Modifier.padding(innerPadding),
                onLoad = onLoadBudget,
                onSave = onSaveBudget,
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
                onVerify = onVerifyWithdrawal,
                onDeleteAccount = onDeleteAccount,
            )
        }
    }
}

@Composable
private fun ProfileSettings(
    state: SettingsUiState,
    modifier: Modifier,
    onUpdateDisplayName: (String) -> Unit,
    onUpdatePassword: (String, String) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var displayName by rememberSaveable(state.displayName) { mutableStateOf(state.displayName) }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var passwordConfirmation by rememberSaveable { mutableStateOf("") }
    SettingsColumn(modifier) {
        SettingsTabs(
            selectedTab = selectedTab,
            titles = listOf(R.string.settings_name_tab, R.string.settings_password_tab),
            onSelected = { selectedTab = it },
        )
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (selectedTab == 0) {
                AuthTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    placeholder = stringResource(R.string.display_name_hint),
                )
                PrimaryAuthButton(
                    text = stringResource(R.string.settings_save_action),
                    onClick = { onUpdateDisplayName(displayName.trim()) },
                    enabled = SignUpValidation.isValidDisplayName(displayName),
                )
            } else {
                PasswordField(currentPassword, { currentPassword = it }, R.string.settings_current_password)
                PasswordField(newPassword, { newPassword = it }, R.string.settings_new_password)
                PasswordField(passwordConfirmation, { passwordConfirmation = it }, R.string.password_confirm_hint)
                Text(
                    text = stringResource(R.string.password_condition_label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
                PrimaryAuthButton(
                    text = stringResource(R.string.settings_change_password),
                    onClick = { onUpdatePassword(currentPassword, newPassword) },
                    enabled =
                        currentPassword.isNotBlank() &&
                            SignUpValidation.isValidPassword(newPassword) &&
                            newPassword == passwordConfirmation,
                )
            }
        }
    }
}

@Composable
private fun BudgetSettings(state: SettingsUiState, modifier: Modifier, onLoad: () -> Unit, onSave: (Int) -> Unit) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var amountText by rememberSaveable(state.budgetAmount) { mutableStateOf(state.budgetAmount.toString()) }
    LaunchedEffect(Unit) { onLoad() }
    SettingsColumn(modifier) {
        SettingsTabs(
            selectedTab = selectedTab,
            titles = listOf(R.string.settings_budget_setup_tab, R.string.settings_budget_history_tab),
            onSelected = { selectedTab = it },
        )
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (selectedTab == 0) {
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
                PrimaryAuthButton(
                    text = stringResource(R.string.settings_save_budget),
                    onClick = { amountText.toIntOrNull()?.let(onSave) },
                    enabled = amountText.toIntOrNull() in 0..10_000_000,
                )
            } else {
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
}

@Composable
private fun LanguageSettings(selectedLanguage: String, modifier: Modifier, onSelected: (String) -> Unit) {
    val languages =
        listOf(
            LanguageOption("en-US", "English", R.drawable.flag_us, R.string.settings_english_flag),
            LanguageOption("ko", "한국어", R.drawable.flag_kr, R.string.settings_korean_flag),
        ).sortedByDescending { it.tag }
    SettingsColumn(modifier) {
        languages.forEach { language ->
            SelectionRow(
                title = language.nativeName,
                selected = selectedLanguage == language.tag,
                onClick = { onSelected(language.tag) },
                leadingContent = {
                    Image(
                        painter = painterResource(language.flagResource),
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
            )
        }
    }
}

@Composable
private fun WithdrawalSettings(
    modifier: Modifier,
    onVerify: (String, () -> Unit) -> Unit,
    onDeleteAccount: () -> Unit,
) {
    var password by rememberSaveable { mutableStateOf("") }
    var showConfirmation by rememberSaveable { mutableStateOf(false) }
    SettingsColumn(modifier) {
        SettingsSection(title = stringResource(R.string.settings_withdraw)) {
            Text(
                stringResource(R.string.settings_withdraw_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PasswordField(password, { password = it }, R.string.settings_current_password)
            PrimaryAuthButton(
                text = stringResource(R.string.settings_verify_password),
                onClick = { onVerify(password) { showConfirmation = true } },
                enabled = password.isNotBlank(),
            )
        }
    }
    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text(stringResource(R.string.settings_withdraw_confirm_title)) },
            text = { Text(stringResource(R.string.settings_withdraw_confirm_message)) },
            confirmButton = {
                TextButton(onClick = onDeleteAccount) {
                    Text(
                        stringResource(R.string.settings_withdraw_confirm_action),
                        color = MaterialTheme.colorScheme.error,
                    )
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
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
private fun SettingsTabs(selectedTab: Int, titles: List<Int>, onSelected: (Int) -> Unit) {
    PrimaryTabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = SmartBudgetGreen,
    ) {
        titles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onSelected(index) },
                text = { Text(stringResource(title), fontWeight = FontWeight.SemiBold) },
                selectedContentColor = SmartBudgetGreen,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
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
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent?.let {
            it()
            androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
        }
        Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp)
        if (selected) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(24.dp).background(SmartBudgetGreen, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
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
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .clickable { onCheckedChange(!checked) }
                .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(label), modifier = Modifier.weight(1f), fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
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
    val flagResource: Int,
    val flagDescriptionResource: Int,
)

private fun DisplayMode.labelResource(): Int = when (this) {
    DisplayMode.LIGHT -> R.string.settings_theme_light
    DisplayMode.DARK -> R.string.settings_theme_dark
    DisplayMode.SYSTEM -> R.string.settings_theme_system
}
