package com.example.bulletin_board.domain.images.impl

import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.domain.images.ImageLoader
import jakarta.inject.Inject

class GlideImageLoader
    @Inject
    constructor() : ImageLoader {
        override fun loadImage(
            imageView: ImageView,
            imageUrl: Any?,
            requestOptions: RequestOptions,
        ) {
            when (imageUrl) {
                is Uri -> {
                    Glide
                        .with(imageView.context)
                        .load(imageUrl)
                        .apply(requestOptions)
                        .into(imageView)
                }

                is Bitmap -> {
                    Glide
                        .with(imageView.context)
                        .load(imageUrl)
                        .apply(requestOptions)
                        .into(imageView)
                }
            }
        }
    }
