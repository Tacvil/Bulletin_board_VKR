package com.example.bulletin_board.domain

import android.view.View
import com.airbnb.lottie.LottieDrawable

object LoadingAnimationManager {
    fun updateAnimationVisibility(
        itemCount: Int,
        adapterView: AdapterView,
    ) {
        if (itemCount == 0) {
            adapterView.nothinkWhiteAnim.visibility = View.VISIBLE
            adapterView.nothinkWhiteAnim.repeatCount = LottieDrawable.INFINITE
            adapterView.nothinkWhiteAnim.playAnimation()
        } else {
            adapterView.nothinkWhiteAnim.cancelAnimation()
            adapterView.nothinkWhiteAnim.visibility = View.GONE
        }
    }
}
