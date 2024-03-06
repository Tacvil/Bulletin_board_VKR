package com.example.bulletin_board.settings

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.bulletin_board.R
import com.example.bulletin_board.settings.SettingsActivity.Companion.LANGUAGE_KEY
import com.example.bulletin_board.settings.SettingsActivity.Companion.THEME_KEY
import java.util.Locale

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

        (findPreference(LANGUAGE_KEY) as? CustomListPreference)?.setOnPreferenceChangeListener { _, newValue ->
            val newLanguage = newValue.toString()
            Log.d("SettingsFragment", "newLanguage = $newLanguage")
            com.example.bulletin_board.settings.LocaleManager.setLocale(requireContext(), newLanguage)
            requireActivity().recreate() // Пересоздать активити для применения нового языка
            true
        }
    }

    private fun applyTheme(themeName: String) {
        val themeMode = if (themeName == SettingsActivity.DEFAULT_THEME) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(themeMode)
/*        if(themeMode == 2){
            activity?.setTheme(R.style.Base_Theme_Bulletin_board_dark)
        }else{
            activity?.setTheme(R.style.Base_Theme_Bulletin_board_light)
        }*/
        requireActivity().recreate() // Пересоздать активити для применения новой темы
    }
}