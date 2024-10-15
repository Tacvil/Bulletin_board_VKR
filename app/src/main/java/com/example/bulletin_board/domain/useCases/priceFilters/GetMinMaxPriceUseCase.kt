package com.example.bulletin_board.domain.useCases.priceFilters

import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.domain.repository.AdRepository
import jakarta.inject.Inject

class GetMinMaxPriceUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(category: String?): Result<Pair<Int?, Int?>> = adRepository.getMinMaxPrice(category)
    }
