package com.example.bulletin_board.domain.useCases.dataUpdate

import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.domain.model.ViewData
import com.example.bulletin_board.domain.repository.AdRepository
import jakarta.inject.Inject

class AdViewedUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(viewData: ViewData): Result<ViewData> = adRepository.adViewed(viewData)
    }
