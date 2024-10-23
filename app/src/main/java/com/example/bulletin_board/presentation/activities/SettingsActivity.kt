package com.example.bulletin_board.presentation.activities

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.bulletin_board.R
import com.example.bulletin_board.data.utils.LocaleManager
import com.example.bulletin_board.databinding.ActivitySettingsBinding
import com.example.bulletin_board.domain.listener.OnSettingsChangeListener
import com.example.bulletin_board.presentation.fragment.SettingsFragment
import com.example.bulletin_board.presentation.theme.ThemeManager
import com.example.bulletin_board.presentation.viewModel.MainViewModel
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
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupOnBackPressedCallback() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    isEnabled = true
                    finish()
                }
            },
        )
    }

    private fun setupFragmentContainer(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_fragment_container, settingsFragment)
                .commit()
        }
    }

    override fun onLanguageChanged(newLanguage: String) {
        recreate()
    }

    override fun onThemeChanged() {
        recreate()
    }

    companion object {
        const val THEME_KEY = "THEME_KEY"
        const val LANGUAGE_KEY = "LANGUAGE_KEY"
        const val DEFAULT_THEME = "light_theme"
        const val LANGUAGE_RU = "ru"
        const val LANGUAGE_EN = "en"
    }
}
