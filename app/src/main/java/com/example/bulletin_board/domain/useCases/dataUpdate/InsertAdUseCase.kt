package com.example.bulletin_board.domain.useCases.dataUpdate

import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.domain.repository.AdRepository
import jakarta.inject.Inject

class InsertAdUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(ad: Ad): Result<Boolean> = adRepository.insertAd(ad)
    }
