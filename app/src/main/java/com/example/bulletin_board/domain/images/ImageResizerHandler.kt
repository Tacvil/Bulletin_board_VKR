package com.example.bulletin_board.domain.images

import android.graphics.Bitmap
import android.net.Uri

interface ImageResizerHandler {
    fun imageResize(
        uris: List<Uri>,
        callback: (List<Bitmap>) -> Unit,
    )
}
