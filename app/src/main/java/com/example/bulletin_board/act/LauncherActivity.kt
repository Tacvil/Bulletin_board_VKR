package com.example.bulletin_board.act

import android.animation.Animator
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.preference.PreferenceManager
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieListener
import com.example.bulletin_board.R
import com.example.bulletin_board.databinding.ActivityDescriptionBinding
import com.example.bulletin_board.databinding.ActivityLauncherBinding
import com.example.bulletin_board.settings.LocaleManager
import com.example.bulletin_board.settings.SettingsActivity

class LauncherActivity : AppCompatActivity() {
    lateinit var binding: ActivityLauncherBinding
    private lateinit var defPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        defPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme())
        val newLanguage = defPreferences.getString(SettingsActivity.LANGUAGE_KEY, "ru")
        if (newLanguage != null) {
            LocaleManager.setLocale(this, newLanguage)
        }
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.launcherAnim.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                binding.launcherAnim.removeAnimatorListener(this)
                initMainAct()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })

        // Запускаем анимацию
        binding.launcherAnim.playAnimation()
    }

    private fun initMainAct(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getSelectedTheme(): Int {
        return when (defPreferences.getString(SettingsActivity.THEME_KEY, SettingsActivity.DEFAULT_THEME)) {
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
}