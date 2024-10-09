package com.example.bulletin_board.useCase.imageManagement

import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.Result
import jakarta.inject.Inject

class DeleteUserImageUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(oldUrl: String): Result<Boolean> = adRepository.deleteImageByUrl(oldUrl)
    }
