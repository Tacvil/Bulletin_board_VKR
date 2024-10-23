package com.example.bulletin_board.presentation.utils

import android.view.View
import com.airbnb.lottie.LottieDrawable
import com.example.bulletin_board.domain.ui.adapters.AdapterView
import com.example.bulletin_board.presentation.adapters.AdapterManager

object EmptyStateView {
    fun updateAnimationVisibility(
        adapterView: AdapterView,
    ) {
        val currentAdapter = AdapterManager.getCurrentAdapter()
        if (currentAdapter?.itemCount == 0) {
            adapterView.nothinkWhiteAnim.visibility = View.VISIBLE
            adapterView.nothinkWhiteAnim.repeatCount = LottieDrawable.INFINITE
            adapterView.nothinkWhiteAnim.playAnimation()
        } else {
            adapterView.nothinkWhiteAnim.cancelAnimation()
            adapterView.nothinkWhiteAnim.visibility = View.GONE
        }
    }
}
