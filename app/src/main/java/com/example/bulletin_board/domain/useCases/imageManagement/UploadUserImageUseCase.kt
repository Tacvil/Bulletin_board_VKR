package com.example.bulletin_board.domain.useCases.imageManagement

import android.net.Uri
import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.domain.repository.AdRepository
import jakarta.inject.Inject

class UploadUserImageUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(byteArray: ByteArray): Result<Uri> = adRepository.uploadImage(byteArray)
    }
