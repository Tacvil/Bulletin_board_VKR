package com.example.bulletin_board.presentation.activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.bulletin_board.R
import com.example.bulletin_board.data.utils.LocaleManager
import com.example.bulletin_board.databinding.ActivitySettingsBinding
import com.example.bulletin_board.domain.listener.OnSettingsChangeListener
import com.example.bulletin_board.presentation.fragment.SettingsFragment
import com.example.bulletin_board.presentation.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class SettingsActivity :
    AppCompatActivity(),
    OnSettingsChangeListener {
    private lateinit var binding: ActivitySettingsBinding

    @Inject
    lateinit var settingsFragment: SettingsFragment

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.getSelectedTheme(PreferenceManager.getDefaultSharedPreferences(this)))
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupOnBackPressedCallback()
        setupFragmentContainer(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(Activity.RESULT_OK)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar2)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupOnBackPressedCallback() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    isEnabled = true
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            },
        )
    }

    private fun setupFragmentContainer(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.place_holder, settingsFragment)
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onLanguageChanged(newLanguage: String) {
        recreate()
    }

    override fun onThemeChanged() {
        recreate()
    }

    companion object {
        const val THEME_KEY = "theme_key"
        const val LANGUAGE_KEY = "LANGUAGE_KEY"
        const val DEFAULT_THEME = "Light theme"
        const val LANGUAGE_RU = "ru"
        const val LANGUAGE_EN = "en"
    }
}
