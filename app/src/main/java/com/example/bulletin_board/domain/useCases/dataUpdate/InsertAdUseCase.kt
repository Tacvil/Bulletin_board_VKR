package com.example.bulletin_board.domain.useCases.dataUpdate

import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.domain.repository.AdRepository
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
