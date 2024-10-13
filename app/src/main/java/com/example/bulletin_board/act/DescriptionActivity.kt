package com.example.bulletin_board.act

import android.content.ActivityNotFoundException
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
import com.example.bulletin_board.act.MainActivity.Companion.INTENT_AD_DETAILS
import com.example.bulletin_board.adapters.ImageAdapter
import com.example.bulletin_board.databinding.ActivityDescriptionBinding
import com.example.bulletin_board.domain.ThemeManager
import com.example.bulletin_board.domain.image.ImageManager
import com.example.bulletin_board.model.Ad
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding

    @Inject lateinit var imageManager: ImageManager

    @Inject lateinit var adapter: ImageAdapter

    private var ad: Ad? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.getSelectedTheme(PreferenceManager.getDefaultSharedPreferences(this)))
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        binding.buttonTel.setOnClickListener { startPhoneCall() }
        binding.buttonEmail.setOnClickListener { sendEmail() }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun init() {
        binding.viewPagerDescription.adapter = ImageAdapter()
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
            textViewTitleD.setText(ad.title)
            textViewDescription.setText(ad.description)
            textViewEmailDescription.setText(ad.email)
            textViewPriceDescription.setText(ad.price.toString())
            textViewTelDescription.setText(ad.tel)
            textViewCountryDescription.setText(ad.country)
            textViewCityDescription.setText(ad.city)
            textViewIndexDescription.setText(ad.index)
            textViewWithSendDescription.setText(
                if (ad.withSend.toBoolean()) {
                    getString(R.string.with_send_yes)
                } else {
                    getString(R.string.with_send_no)
                },
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
}
