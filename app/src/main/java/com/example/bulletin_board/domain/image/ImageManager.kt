package com.example.bulletin_board.domain.image

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import com.example.bulletin_board.adapters.ImageAdapter
import com.example.bulletin_board.domain.ToastHelper
import com.example.bulletin_board.domain.image.ImageLoader.getBitmapFromUris
import com.example.bulletin_board.model.Ad
import jakarta.inject.Inject
import java.io.ByteArrayOutputStream

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

interface ContentResolverProvider {
    fun getContentResolverAct(): ContentResolver
}

interface GetMultiImagesProvider {
    fun getMultiImages(imageCounter: Int)
}

interface ImageResizerHandler {
    fun imageResize(
        uris: List<Uri>,
        callback: (List<Bitmap>) -> Unit,
    )
}

interface AddImageHandler {
    fun addImages(imageCounter: Int)
}

interface GetSingleImagesHandler {
    fun getSingleImages()
}

interface ChooseScaleTypeHandler {
    fun chooseScaleType(bitmap: Bitmap): Boolean
}

class ImageManager
    @Inject
    constructor(
        private val toastHelper: ToastHelper,
        private val viewModelHandler: ViewModelHandler,
        private val getMultiImagesProvider: GetMultiImagesProvider,
        private val imageResizer: ImageResizerHandler,
        private val addImageHandler: AddImageHandler,
        private val getSingleImagesHandler: GetSingleImagesHandler,
        private val chooseScaleTypeHandler: ChooseScaleTypeHandler,
    ) {
        fun getMultiImages(imageCounter: Int) {
            getMultiImagesProvider.getMultiImages(imageCounter)
        }

        fun chooseScaleType(bitmap: Bitmap): Boolean = chooseScaleTypeHandler.chooseScaleType(bitmap)

        fun addImages(imageCounter: Int) {
            addImageHandler.addImages(imageCounter)
        }

        fun getSingleImages() {
            getSingleImagesHandler.getSingleImages()
        }

        fun imageResize(
            uris: List<Uri>,
            callback: (List<Bitmap>) -> Unit,
        ) {
            imageResizer.imageResize(uris) { resizedBitmaps ->
                callback(resizedBitmaps)
            }
        }

        fun uploadImages(
            ad: Ad?,
            imageAdapter: ImageAdapter,
            imageIndex: Int,
            onUploadComplete: () -> Unit,
        ) {
            if (imageIndex == 3) {
                viewModelHandler.insertAd(ad!!) { result ->
                    if (result) {
                        toastHelper.showToast("Объявление отправлено на модерацию!", Toast.LENGTH_SHORT)
                    } else {
                        toastHelper.showToast("Ошибка отправки объявления!", Toast.LENGTH_SHORT)
                    }
                }
                onUploadComplete()
                return
            }

            val oldUrl = getUrlFromAd(ad, imageIndex)
            if (imageAdapter.mainArray.size > imageIndex) {
                val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
                if (oldUrl.startsWith("http")) {
                    viewModelHandler.updateImage(byteArray, oldUrl) { uri ->
                        nextImage(ad, uri.toString(), imageIndex, imageAdapter, onUploadComplete)
                    }
                } else {
                    viewModelHandler.uploadImage(byteArray) { uri ->
                        nextImage(ad, uri.toString(), imageIndex, imageAdapter, onUploadComplete)
                    }
                }
            } else {
                if (oldUrl.startsWith("http")) {
                    viewModelHandler.deleteImageByUrl(oldUrl)
                    nextImage(ad, "empty", imageIndex, imageAdapter, onUploadComplete)
                } else {
                    nextImage(ad, "empty", imageIndex, imageAdapter, onUploadComplete)
                }
            }
        }

        private fun nextImage(
            ad: Ad?,
            uri: String,
            imageIndex: Int,
            imageAdapter: ImageAdapter,
            onUploadComplete: () -> Unit,
        ) {
            setImageUriToAd(ad, uri, imageIndex)
            uploadImages(ad, imageAdapter, imageIndex + 1, onUploadComplete)
        }

        private fun setImageUriToAd(
            ad: Ad?,
            uri: String,
            imageIndex: Int,
        ) {
            when (imageIndex) {
                0 -> ad?.mainImage = uri
                1 -> ad?.image2 = uri
                2 -> ad?.image3 = uri
            }
        }

        private fun getUrlFromAd(
            ad: Ad?,
            imageIndex: Int,
        ): String = listOf(ad?.mainImage ?: "", ad?.image2 ?: "", ad?.image3 ?: "")[imageIndex]

        private fun prepareImageByteArray(bitmap: Bitmap): ByteArray {
            val outStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream)
            return outStream.toByteArray()
        }
        // ---------------------------------------------------------------------------------------

        fun fillImageArray(
            ad: Ad,
            adapter: ImageAdapter,
        ) {
            val listUris = listOf(ad.mainImage, ad.image2, ad.image3)

            getBitmapFromUris(listUris) { bitMapList ->
                adapter.update(bitMapList as ArrayList<Bitmap>)
            }
        }

        companion object {
            const val MAX_IMAGE_SIZE = 1000
        }
    }
