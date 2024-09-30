package com.example.bulletin_board.useCase.dataUpdate

import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.Result
import jakarta.inject.Inject
import timber.log.Timber

class InsertAdUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(ad: Ad): Boolean =
            when (val result = adRepository.insertAd(ad)) {
                is Result.Success -> true
                is Result.Error -> {
                    Timber.e(result.exception, "Error inserting announcement: $ad")
                    false
                }
            }
    }
