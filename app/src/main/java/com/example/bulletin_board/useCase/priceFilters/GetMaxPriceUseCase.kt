package com.example.bulletin_board.useCase.priceFilters

import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.Result
import jakarta.inject.Inject

class GetMaxPriceUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(category: String?): Result<Int> = adRepository.getMaxPrice(category)
    }
