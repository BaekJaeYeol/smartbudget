package com.yu_morph.smartbudget.ui.auth

import android.content.Context
import com.yu_morph.smartbudget.ui.launchAppCustomTab

private const val EXAMPLE_URL = "https://example.com"

internal fun openExampleCustomTab(context: Context) {
    launchAppCustomTab(context, EXAMPLE_URL)
}
