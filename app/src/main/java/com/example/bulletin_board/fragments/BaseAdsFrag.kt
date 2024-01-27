package com.example.bulletin_board.fragments

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bulletin_board.utils.BillingManager
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.instream.MobileInstreamAds
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener


open class BaseAdsFrag: Fragment(), InterstitialAdListener {

    //
    var interstitialAd: InterstitialAd? = null
    //private val adInfoFragment get() = _adInfoFragment
    private val eventLogger = InterstitialAdEventLogger()
    //private var _adInfoFragment: ImageListFrag? = null
    //
    lateinit var mBannerAdView: BannerAdView
    private var pref: SharedPreferences? =null
    private var isPremiumUser:Boolean =false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = activity?.getSharedPreferences(BillingManager.MAIN_PREF, AppCompatActivity.MODE_PRIVATE)
        isPremiumUser = pref?.getBoolean(BillingManager.REMOVE_ADS_PREF, false)!!
        if (!isPremiumUser){
            initAds()
        }else{
            mBannerAdView.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //adInfoFragment?.onLoadClickListener = ::loadInterstitial



    }

    override fun onResume() {
        super.onResume()
        //binding.adView.resume()
    }

    override fun onPause() {
        super.onPause()
        //binding.adView.pause()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        mBannerAdView.destroy()
//    }

    private fun initAds(){

        // Инициализация рекламного SDK от Яндекс
        MobileAds.initialize(activity as Activity) {}
        // Включаем предзагрузку рекламы до ее показа
        MobileInstreamAds.setAdGroupPreloading(true)
        // Включаем логирование, чтобы следить за состоянием рекламы
        MobileAds.enableLogging(true)

        mBannerAdView.setAdUnitId("demo-banner-yandex")
        mBannerAdView.setAdSize(AdSize.BANNER_320x50)


        val adRequest = AdRequest.Builder().build()

        mBannerAdView.loadAd(adRequest);

//        MobileAds.initialize(activity as Activity) {}
//        val adRequest = AdRequest.Builder().build()
//        binding.adView.loadAd(adRequest)
    }

     fun loadInterstitial(){

        Log.d("MyLog", "This is loadInters///")

       // mInterstitialAd = InterstitialAd(context as Activity)
        //interstitialAd?.setAdUnitId("demo-interstitial-yandex")

        destroyInterstitial()
        createInterstitial()

        val adRequest = AdRequest.Builder().build()

        interstitialAd?.loadAd(adRequest)

    }

    private inner class InterstitialAdEventLogger : InterstitialAdEventListener {

        override fun onAdLoaded() {
            interstitialAd?.show()
        }

        override fun onAdFailedToLoad(error: AdRequestError) {

        }

        override fun onAdShown() {
        }

        override fun onAdDismissed() {
        }

        override fun onAdClicked() {
        }

        override fun onLeftApplication() {
        }

        override fun onReturnedToApplication() {
        }

        override fun onImpression(data: ImpressionData?) {
        }
    }

    override fun onClose() {

    }

    private fun destroyInterstitial() {
        interstitialAd?.destroy()
        interstitialAd = null
    }

//    override fun onDestroy() {
//        _adInfoFragment = null
//        destroyInterstitial()
//        super.onDestroy()
//    }

    private fun createInterstitial() {
        interstitialAd = InterstitialAd(requireActivity()).apply {
            setAdUnitId("demo-interstitial-yandex")
            setInterstitialAdEventListener(eventLogger)
        }
    }

}