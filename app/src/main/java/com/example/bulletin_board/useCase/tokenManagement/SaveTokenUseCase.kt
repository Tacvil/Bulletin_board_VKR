package com.example.bulletin_board.useCase.tokenManagement

import com.example.bulletin_board.packroom.AdRepository
import jakarta.inject.Inject

class SaveTokenUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(token: String) {
            adRepository.saveToken(token)
        }
    }
