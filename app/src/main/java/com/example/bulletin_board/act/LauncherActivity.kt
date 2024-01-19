package com.example.bulletin_board.act

import android.animation.Animator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieListener
import com.example.bulletin_board.R
import com.example.bulletin_board.databinding.ActivityDescriptionBinding
import com.example.bulletin_board.databinding.ActivityLauncherBinding

class LauncherActivity : AppCompatActivity() {
    lateinit var binding: ActivityLauncherBinding
    override fun onCreate(savedInstanceState: Bundle?) {
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
}