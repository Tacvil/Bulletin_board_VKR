package com.example.bulletin_board.domain.useCases.dataUpdate

import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.domain.model.FavData
import com.example.bulletin_board.domain.repository.AdRepository
import jakarta.inject.Inject

class UpdateFavoriteAdUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(favData: FavData): Result<FavData> = adRepository.onFavClick(favData)
    }
