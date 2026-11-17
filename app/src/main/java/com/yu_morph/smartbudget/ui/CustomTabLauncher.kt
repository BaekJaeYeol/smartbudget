package com.yu_morph.smartbudget.ui

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

internal fun launchAppCustomTab(context: Context, url: String): Boolean {
    val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
    return if (uri.scheme !in setOf("http", "https")) {
        false
    } else {
        runCatching {
            CustomTabsIntent
                .Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(context, uri)
        }.isSuccess
    }
}
