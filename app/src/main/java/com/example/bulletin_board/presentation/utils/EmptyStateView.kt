package com.example.bulletin_board.presentation.utils

import android.view.View
import com.airbnb.lottie.LottieDrawable
import com.example.bulletin_board.domain.ui.adapters.AdapterView

object EmptyStateView {
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
