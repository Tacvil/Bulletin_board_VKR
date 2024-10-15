package com.example.bulletin_board.domain.useCases.imageManagement

import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.domain.repository.AdRepository
import jakarta.inject.Inject

class DeleteUserImageUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(oldUrl: String): Result<Boolean> = adRepository.deleteImageByUrl(oldUrl)
    }
