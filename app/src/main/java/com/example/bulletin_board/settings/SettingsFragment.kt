package com.example.bulletin_board.settings

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.bulletin_board.R
import com.example.bulletin_board.settings.SettingsActivity.Companion.THEME_KEY

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)

        val themePreference = findPreference(THEME_KEY) as? CustomListPreference
        Log.d("SettingsFragment", "themePreference = $themePreference")
        if (themePreference != null) {
            themePreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    val newTheme = newValue.toString()
                    Log.d("SettingsFragment", "newTheme = $newTheme")
                    applyTheme(newTheme)
                    true
                }
        }
    }

    private fun applyTheme(themeName: String) {
        val themeMode = if (themeName == SettingsActivity.DEFAULT_THEME) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(themeMode)
        requireActivity().recreate() // Пересоздать активити для применения новой темы
    }
}