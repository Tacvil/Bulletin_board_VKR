package com.example.bulletin_board.domain.images

import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions

interface ImageLoader {
    fun loadImage(
        imageView: ImageView,
        imageUrl: Any?,
        requestOptions: RequestOptions,
    )
}
