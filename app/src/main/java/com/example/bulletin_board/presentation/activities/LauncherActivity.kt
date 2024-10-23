package com.example.bulletin_board.presentation.activities

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.bulletin_board.data.utils.LocaleManager
import com.example.bulletin_board.databinding.ActivityLauncherBinding
import com.example.bulletin_board.presentation.theme.ThemeManager

class LauncherActivity : AppCompatActivity() {
    lateinit var binding: ActivityLauncherBinding
    private lateinit var defPreferences: SharedPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initSettings()
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLauncherAnimation()
    }

    private fun initLauncherAnimation() {
        binding.lottieAnimationView.addAnimatorListener(
            object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    binding.lottieAnimationView.removeAnimatorListener(this)
                    initMainAct()
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            },
        )
        binding.lottieAnimationView.playAnimation()
    }

    private fun initSettings() {
        defPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(ThemeManager.getSelectedTheme(defPreferences))
    }

    private fun initMainAct() {
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }
}
