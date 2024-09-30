package com.example.bulletin_board.useCase.dataUpdate

import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.Result
import jakarta.inject.Inject
import timber.log.Timber

class DeleteAdUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(adKey: String) {
            when (val result = adRepository.deleteAd(adKey)) {
                is Result.Success -> {}
                is Result.Error -> {
                    Timber.e(result.exception, "Error deleting ad: $adKey")
                }
            }
        }
    }
