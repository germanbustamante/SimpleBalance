package com.germandebustamante.simplebalance.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

object LocaleManager {

    fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

        // Recreate activity to apply changes immediately
        (context as? Activity)?.recreate()
    }

    @Composable
    fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

    @Composable
    fun currentLanguageCode(): String = currentLocale().language

    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
