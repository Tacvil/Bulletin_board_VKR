package com.example.bulletin_board.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.bulletin_board.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)

        val themePreference = findPreference("theme_key") as? ListPreference
        if (themePreference != null) {
            themePreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    val newTheme = newValue.toString()
                    applyTheme(newTheme)
                    true
                }
        }
    }

    private fun applyTheme(themeName: String) {
        val themeMode = if (themeName == "light") {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(themeMode)
        requireActivity().recreate() // Пересоздать активити для применения новой темы
    }
}