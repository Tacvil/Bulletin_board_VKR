package com.example.bulletin_board.domain

import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.bulletin_board.databinding.ActivityMainBinding

object AdapterViewManager : AdapterView {
    private lateinit var _nothinkWhiteAnim: LottieAnimationView
    private lateinit var _recyclerViewMainContent: RecyclerView

    fun initViews(binding: ActivityMainBinding) {
        _nothinkWhiteAnim = binding.mainContent.nothinkWhiteAnim
        _recyclerViewMainContent = binding.mainContent.recyclerViewMainContent
    }

    override val nothinkWhiteAnim: LottieAnimationView
        get() = _nothinkWhiteAnim

    override val recyclerViewMainContent: RecyclerView
        get() = _recyclerViewMainContent
}
