package com.yu_morph.smartbudget.ui.main

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal val HomeSectionDividerThickness = 0.5.dp

@Composable
internal fun homeSectionDividerColor(): Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
