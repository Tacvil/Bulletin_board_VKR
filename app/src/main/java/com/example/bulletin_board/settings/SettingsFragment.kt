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
import androidx.preference.PreferenceManager
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

        val languagePreference = findPreference(LANGUAGE_KEY) as? CustomListPreference
        Log.d("SettingsFragment", "languagePreference = $languagePreference")
        languagePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val newLanguage = newValue.toString()
            Log.d("SettingsFragment", "newLanguage = $newLanguage")

            // Установка нового языка
            setAppLanguage(newLanguage)
            // Пересоздание активити для применения нового языка
            requireActivity().recreate()

            true
        }
    }

    private fun setAppLanguage(newLanguage: String) {
        val locale = Locale(newLanguage)
        Locale.setDefault(locale)

        val resources = requireContext().resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        // Сохранение выбранного языка в SharedPreferences, если необходимо
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferences.edit().putString(LANGUAGE_KEY, newLanguage).apply()
    }

    private fun applyTheme(themeName: String) {
        val themeMode = if (themeName == getString(R.string.light_theme)) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
/*        if(themeMode == 2){
            activity?.setTheme(R.style.Base_Theme_Bulletin_board_dark)
        }else{
            activity?.setTheme(R.style.Base_Theme_Bulletin_board_light)
        }*/
        requireActivity().recreate() // Пересоздать активити для применения новой темы
    }
}