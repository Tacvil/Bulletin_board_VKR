package com.example.bulletin_board.domain.useCases.tokenManagement

import com.example.bulletin_board.domain.repository.AdRepository
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
