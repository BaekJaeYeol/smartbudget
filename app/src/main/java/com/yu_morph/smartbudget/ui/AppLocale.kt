package com.yu_morph.smartbudget.ui

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.annotation.StringRes
import com.yu_morph.smartbudget.data.local.PreferenceStoreFactory
import java.util.Locale

internal fun Context.getAppString(@StringRes resource: Int, vararg formatArgs: Any): String {
    val languageTag = PreferenceStoreFactory.appSettings(this).read().languageTag
    val configuration = Configuration(resources.configuration).apply {
        setLocale(Locale.forLanguageTag(languageTag))
    }
    return createConfigurationContext(configuration).getString(resource, *formatArgs)
}

internal fun Context.showAppToast(@StringRes resource: Int, vararg formatArgs: Any) {
    Toast.makeText(this, getAppString(resource, *formatArgs), Toast.LENGTH_SHORT).show()
}
