package com.yu_morph.smartbudget

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yu_morph.smartbudget.data.local.DisplayMode
import com.yu_morph.smartbudget.data.local.PreferenceStoreFactory
import com.yu_morph.smartbudget.ui.SmartBudgetApp
import com.yu_morph.smartbudget.ui.theme.SmartBudgetTheme
import com.blongho.country_data.World
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val languageTag = PreferenceStoreFactory.appSettings(newBase).read().languageTag
        val configuration = Configuration(newBase.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(languageTag))
        }
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        World.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            var displayMode by remember {
                mutableStateOf(PreferenceStoreFactory.appSettings(applicationContext).read().displayMode)
            }
            val useDarkTheme = when (displayMode) {
                DisplayMode.LIGHT -> false
                DisplayMode.DARK -> true
                DisplayMode.SYSTEM -> isSystemInDarkTheme()
            }
            SmartBudgetTheme(darkTheme = useDarkTheme) {
                SmartBudgetApp(
                    onDisplayModeChanged = { displayMode = it },
                    onLanguageChanged = ::recreate,
                    onExit = ::finish,
                )
            }
        }
    }
}
