package com.example.bulletin_board.act

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.example.bulletin_board.R
import com.example.bulletin_board.adapters.ImageAdapter
import com.example.bulletin_board.databinding.ActivityDescriptionBinding
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.settings.SettingsActivity
import com.example.bulletin_board.utils.ImageManager.fillImageArray
import java.io.Serializable

class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter
    private var ad: Ad? = null
    private lateinit var defPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        defPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme())
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        binding.buttonTel.setOnClickListener { startPhoneCall() }
        binding.buttonEmail.setOnClickListener { sendEmail() }
    }

    private fun init() {
        adapter = ImageAdapter()
        binding.apply {
            viewPagerDescription.adapter = adapter
        }
        getIntentFromMainAct()
        imageChangeCounter()
    }

    private fun getIntentFromMainAct() {
        ad = intent.serializable<Ad>("AD")
        if (ad != null) updateUI(ad!!)
    }

    private fun updateUI(ad: Ad) {
        fillImageArray(ad, adapter)
        fillTextViews(ad)
    }

    private inline fun <reified T : Serializable> Intent.serializable(key: String): T? =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                getSerializableExtra(
                    key,
                    T::class.java,
                )

            else ->
                @Suppress("DEPRECATION")
                getSerializableExtra(key)
                    as? T
        }

    private fun fillTextViews(ad: Ad) =
        with(binding) {
            textViewTitleD.setText(ad.title)
            textViewDescription.setText(ad.description)
            textViewEmailDescription.setText(ad.email)
            textViewPriceDescription.setText(ad.price.toString())
            textViewTelDescription.setText(ad.tel)
            textViewCountryDescription.setText(ad.country)
            textViewCityDescription.setText(ad.city)
            textViewIndexDescription.setText(ad.index)
            textViewWithSendDescription.setText(isWithSent(ad.withSend.toBoolean()))
        }

    private fun isWithSent(withSent: Boolean): String = if (withSent) "Да" else "Нет"

    private fun startPhoneCall() {
        val callUri = "tel:${ad?.tel}"
        val iCall = Intent(Intent.ACTION_DIAL)
        iCall.data = callUri.toUri()
        startActivity(iCall)
    }

    private fun sendEmail() {
        val iSendEmail = Intent(Intent.ACTION_SENDTO)
        iSendEmail.data = Uri.parse("mailto:") // Устанавливаем схему "mailto:"
        // iSendEmail.type = "message/rfc822"
        iSendEmail.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email))
            putExtra(Intent.EXTRA_SUBJECT, "Объявления")
            putExtra(Intent.EXTRA_TEXT, "Меня интересует ваше объявление!")
        }
        try {
            startActivity(Intent.createChooser(iSendEmail, "Открыть с помощью"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Отсутствует приложение для отправки", Toast.LENGTH_LONG).show()
        }
    }

    private fun imageChangeCounter() {
        binding.viewPagerDescription.registerOnPageChangeCallback(
            object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val imageCounter = "${position + 1}/${binding.viewPagerDescription.adapter?.itemCount}"
                    binding.textViewCounter.text = imageCounter
                }
            },
        )
    }

    private fun getSelectedTheme(): Int =
        when (defPreferences.getString(SettingsActivity.THEME_KEY, SettingsActivity.DEFAULT_THEME)) {
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
