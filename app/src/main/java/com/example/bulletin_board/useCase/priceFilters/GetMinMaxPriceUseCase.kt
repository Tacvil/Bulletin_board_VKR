package com.example.bulletin_board.useCase.priceFilters

import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.Result
import jakarta.inject.Inject

class GetMinMaxPriceUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(category: String?): Result<Pair<Int?, Int?>> = adRepository.getMinMaxPrice(category)
    }
