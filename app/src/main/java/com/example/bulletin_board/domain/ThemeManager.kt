package com.example.bulletin_board.domain

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.bulletin_board.R
import com.example.bulletin_board.settings.SettingsActivity

object ThemeManager {
    fun getSelectedTheme(defPreferences: SharedPreferences): Int =
        when (
            defPreferences.getString(
                SettingsActivity.THEME_KEY,
                SettingsActivity.DEFAULT_THEME,
            )
        ) {
            SettingsActivity.DEFAULT_THEME -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                R.style.Base_Theme_Bulletin_board_light
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                R.style.Base_Theme_Bulletin_board_dark
            }
        }
}
