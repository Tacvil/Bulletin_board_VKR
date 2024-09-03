package com.example.bulletin_board.casedomain

import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.packroom.AdRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAdsUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        operator fun invoke(): Flow<List<Ad>>? = null
    }
