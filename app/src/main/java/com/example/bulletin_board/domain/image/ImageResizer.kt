package com.example.bulletin_board.domain.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.result.launch
import com.example.bulletin_board.domain.image.ImageManager.Companion.MAX_IMAGE_SIZE
import com.squareup.picasso.Picasso
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageResizer
    @Inject
    constructor(
        private val contentResolverProvider: ContentResolverProvider,
    ) : ImageResizerHandler,
        ChooseScaleTypeHandler {
        private fun getImageSize(uri: Uri): List<Int> {
            val inStream = contentResolverProvider.getContentResolverAct().openInputStream(uri)
            val options =
                BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
            BitmapFactory.decodeStream(inStream, null, options)

            return listOf(options.outWidth, options.outHeight)
        }

        override fun chooseScaleType(bitmap: Bitmap): Boolean {
            val crop: Boolean = bitmap.width > bitmap.height
            return crop
        }

        override fun imageResize(
            uris: List<Uri>,
            callback: (List<Bitmap>) -> Unit,
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val resizedBitmaps =
                    uris.map { uri ->
                        val (width, height) = getImageSize(uri)
                        val (newWidth, newHeight) = calculateNewSize(width, height)
                        Picasso
                            .get()
                            .load(uri)
                            .resize(newWidth, newHeight)
                            .get()
                    }
                withContext(Dispatchers.Main) {
                    callback(resizedBitmaps)
                }
            }
        }

        private fun calculateNewSize(
            width: Int,
            height: Int,
        ): Pair<Int, Int> {
            val imageRatio = width.toFloat() / height.toFloat()
            return if (imageRatio > 1) {
                if (width > MAX_IMAGE_SIZE) {
                    Pair(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt())
                } else {
                    Pair(width, height)
                }
            } else {
                if (height > MAX_IMAGE_SIZE) {
                    Pair((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE)
                } else {
                    Pair(width, height)
                }
            }
        }
    }
