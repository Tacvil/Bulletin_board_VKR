package com.example.bulletin_board.useCase.dataUpdate

import com.example.bulletin_board.model.FavData
import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.Result
import jakarta.inject.Inject

class UpdateFavoriteAdUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(favData: FavData): Result<FavData> = adRepository.onFavClick(favData)
    }
