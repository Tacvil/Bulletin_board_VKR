package com.example.bulletin_board.presentation.fragment

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.instream.MobileInstreamAds

open class BaseAdsFrag : Fragment() {
    lateinit var bannerAdView: BannerAdView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initAds()
    }

    private fun initAds() {
        MobileAds.initialize(activity as Activity) {}
        MobileInstreamAds.setAdGroupPreloading(true)
        MobileAds.enableLogging(true)
        bannerAdView.apply {
            setAdUnitId("demo-banner-yandex")
            setAdSize(AdSize.BANNER_320x50)
            loadAd(AdRequest.Builder().build())
        }
    }
}
