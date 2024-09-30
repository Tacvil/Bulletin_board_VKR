package com.example.bulletin_board.useCase.dataUpdate

import com.example.bulletin_board.model.ViewData
import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.Result
import jakarta.inject.Inject

class AdViewedUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(viewData: ViewData): Result<ViewData> = adRepository.adViewed(viewData)
    }
