package com.example.bulletin_board.useCase.priceFilters

import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.Result
import jakarta.inject.Inject

class GetMinPriceUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(category: String?): Result<Int> = adRepository.getMinPrice(category)
    }
