package com.example.bulletin_board.presentation.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.example.bulletin_board.R
import com.example.bulletin_board.data.image.ImageManager
import com.example.bulletin_board.data.utils.LocaleManager
import com.example.bulletin_board.data.utils.SortUtils
import com.example.bulletin_board.databinding.ActivityDescriptionBinding
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.presentation.activities.MainActivity.Companion.INTENT_AD_DETAILS
import com.example.bulletin_board.presentation.adapters.ImageAdapter
import com.example.bulletin_board.presentation.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding

    @Inject lateinit var imageManager: ImageManager

    @Inject lateinit var adapter: ImageAdapter

    @Inject
    lateinit var sortUtils: SortUtils

    private var ad: Ad? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.getSelectedTheme(PreferenceManager.getDefaultSharedPreferences(this)))
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        binding.callPhoneButton.setOnClickListener { startPhoneCall() }
        binding.sendEmailButton.setOnClickListener { sendEmail() }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun init() {
        binding.imageViewPager.adapter = adapter
        getAdFromIntent()
        setupImageCounter()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getAdFromIntent() {
        intent.getParcelableExtra(INTENT_AD_DETAILS, Ad::class.java)?.let { ad ->
            this.ad = ad
            displayAdDetails(ad)
        }
    }

    private fun displayAdDetails(ad: Ad) {
        imageManager.fillImageArray(ad, adapter)
        populateAdDetails(ad)
    }

    private fun populateAdDetails(ad: Ad) =
        with(binding) {
            adTitleEditText.setText(ad.title)
            descriptionEditText.setText(ad.description)
            emailEditText.setText(ad.email)
            priceEditText.setText(ad.price.toString())
            phoneEditText.setText(ad.tel)
            countryEditText.setText(ad.country)
            cityEditText.setText(ad.city)
            indexEditText.setText(ad.index)
            shippingInfoEditText.setText(
                ad.withSend?.let { sortUtils.getSendOptionFromSortOption(it) }
            )
        }

    private fun startPhoneCall() {
        ad?.tel?.takeIf { it.isNotBlank() }?.let { tel ->
            val callUri = "tel:$tel"
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = callUri.toUri()
            startActivity(dialIntent)
        }
    }

    private fun sendEmail() {
        val emailIntent =
            Intent(Intent.ACTION_SENDTO).also {
                it.data = Uri.parse("mailto:")
                it.putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email))
                it.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.ad_subject))
                it.putExtra(Intent.EXTRA_TEXT, getString(R.string.ad_text))
            }
        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.open_with)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.no_app_for_sending), Toast.LENGTH_LONG).show()
        }
    }

    private fun setupImageCounter() {
        binding.imageViewPager.registerOnPageChangeCallback(
            object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val imageCounter = "${position + 1}/${binding.imageViewPager.adapter?.itemCount}"
                    binding.imageCounterTextView.text = imageCounter
                }
            },
        )
    }
}
