package com.example.bulletin_board.settings

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleManager {
    fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}