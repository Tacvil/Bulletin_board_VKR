package com.example.bulletin_board.domain.images

import android.net.Uri
import com.example.bulletin_board.domain.model.Ad

interface ViewModelHandler {
    fun insertAd(
        ad: Ad,
        onResult: (Boolean) -> Unit,
    )

    fun updateImage(
        byteArray: ByteArray,
        oldUrl: String,
        onResult: (Uri) -> Unit,
    )

    fun uploadImage(
        byteArray: ByteArray,
        onResult: (Uri) -> Unit,
    )

    fun deleteImageByUrl(url: String)
}
