package com.example.bulletin_board.presentation.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.bulletin_board.R
import com.example.bulletin_board.domain.listener.OnSettingsChangeListener
import com.example.bulletin_board.presentation.activity.SettingsActivity.Companion.LANGUAGE_KEY
import com.example.bulletin_board.presentation.activity.SettingsActivity.Companion.THEME_KEY
import com.example.bulletin_board.presentation.preferences.ListPreference
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class SettingsFragment
    @Inject
    constructor() : PreferenceFragmentCompat() {
        @Inject
        lateinit var onSettingsChangeListener: OnSettingsChangeListener

        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            setPreferencesFromResource(R.xml.settings_preference, rootKey)

            val themePreference = findPreference(THEME_KEY) as? ListPreference
            if (themePreference != null) {
                themePreference.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _, _ ->
                        onSettingsChangeListener.onThemeChanged()
                        true
                    }
            }

            val languagePreference = findPreference(LANGUAGE_KEY) as? ListPreference
            languagePreference?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    onSettingsChangeListener.onLanguageChanged(newValue.toString())
                    true
                }
        }
    }
