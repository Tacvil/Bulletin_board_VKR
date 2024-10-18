package com.example.bulletin_board.domain.useCases.dataUpdate

import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.domain.repository.AdRepository
import jakarta.inject.Inject

class DeleteAdUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(adKey: String): Result<Boolean> = adRepository.deleteAd(adKey)
    }
