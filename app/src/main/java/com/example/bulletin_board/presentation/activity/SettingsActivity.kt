package com.example.bulletin_board.presentation.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.example.bulletin_board.R
import com.example.bulletin_board.data.utils.LocaleManager
import com.example.bulletin_board.databinding.ActivitySettingsBinding
import com.example.bulletin_board.presentation.fragment.SettingsFragment

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var defPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        defPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme())
        applyLanguageFromPreferences() // Установка языка

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar2)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val callback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    isEnabled = true
                    startMainActivity()
                }
            }
        onBackPressedDispatcher.addCallback(this, callback)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.place_holder, SettingsFragment()).commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            startMainActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fragment_to_display", "fragment1")
        startActivity(intent)
    }

    private fun getSelectedTheme(): Int =
        when (defPreferences.getString(THEME_KEY, DEFAULT_THEME)) {
            DEFAULT_THEME -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                R.style.Base_Theme_Bulletin_board_light
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                R.style.Base_Theme_Bulletin_board_dark
            }
        }

    private fun applyLanguageFromPreferences() {
        val language = defPreferences.getString(LANGUAGE_KEY, "en") // По умолчанию "en" - английский язык
        if (language != null) {
            LocaleManager.setLocale(this, language)
        }
    }

    companion object {
        const val THEME_KEY = "theme_key"
        const val LANGUAGE_KEY = "LANGUAGE_KEY"
        const val DEFAULT_THEME = "Light theme"
    }
}
