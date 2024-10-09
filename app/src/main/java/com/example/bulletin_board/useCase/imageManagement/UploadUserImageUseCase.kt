package com.example.bulletin_board.useCase.imageManagement

import android.net.Uri
import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.Result
import jakarta.inject.Inject

class UploadUserImageUseCase
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) {
        suspend operator fun invoke(byteArray: ByteArray): Result<Uri> = adRepository.uploadImage(byteArray)
    }
