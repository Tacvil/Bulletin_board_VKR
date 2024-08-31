package com.example.bulletin_board.domain

import com.example.bulletin_board.Room.AdRepository
import com.example.bulletin_board.model.Ad
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAdsUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        operator fun invoke(): Flow<List<Ad>> = adRepository.getAllAds()
    }
