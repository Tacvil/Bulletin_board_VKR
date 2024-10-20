package com.example.bulletin_board.data.utils

import android.content.Context
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import com.example.bulletin_board.presentation.activity.SettingsActivity.Companion.LANGUAGE_EN
import com.example.bulletin_board.presentation.activity.SettingsActivity.Companion.LANGUAGE_KEY
import java.util.Locale

object LocaleManager {
    fun setLocale(context: Context): Context {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val language = sharedPrefs.getString(LANGUAGE_KEY, LANGUAGE_EN) ?: LANGUAGE_EN

        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
