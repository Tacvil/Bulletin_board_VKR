package com.example.bulletin_board.domain.useCases.dataUpdate

import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.domain.repository.AdRepository
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
