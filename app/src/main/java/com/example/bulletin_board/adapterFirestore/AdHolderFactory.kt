package com.example.bulletin_board.adapterFirestore

import android.icu.text.SimpleDateFormat
import com.example.bulletin_board.act.MainActivity
import com.example.bulletin_board.databinding.AdListItemBinding
import jakarta.inject.Inject

class AdHolderFactory
    @Inject
    constructor(
        private val act: MainActivity,
        private val formatter: SimpleDateFormat,
    ) {
        companion object { // Добавьте companion object

            @JvmStatic // Добавьте @JvmStatic для совместимости с Java
            @Inject // Добавьте @Inject в companion object
            fun create(
                act: MainActivity,
                formatter: SimpleDateFormat,
                binding: AdListItemBinding,
            ): AdsAdapter.AdHolder = AdsAdapter.AdHolder(binding, act, formatter)
        }
    }
