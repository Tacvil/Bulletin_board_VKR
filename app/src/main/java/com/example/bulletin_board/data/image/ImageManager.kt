package com.example.bulletin_board.data.image

import android.graphics.Bitmap
import android.net.Uri
import com.example.bulletin_board.data.image.ImageLoader.getBitmapFromUris
import com.example.bulletin_board.domain.images.AddImageHandler
import com.example.bulletin_board.domain.images.ChooseScaleTypeHandler
import com.example.bulletin_board.domain.images.GetMultiImagesProvider
import com.example.bulletin_board.domain.images.GetSingleImagesHandler
import com.example.bulletin_board.domain.images.ImageResizerHandler
import com.example.bulletin_board.domain.images.ViewModelHandler
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.presentation.adapters.ImageAdapter
import jakarta.inject.Inject
import java.io.ByteArrayOutputStream

class ImageManager
    @Inject
    constructor(
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
            onUploadComplete: (Boolean?) -> Unit,
        ) {
            if (imageIndex == 3) {
                viewModelHandler.insertAd(ad!!) { result ->
                    onUploadComplete(result)
                }
                return
            }

            val oldUrl = getUrlFromAd(ad, imageIndex)
            if (imageAdapter.imageBitmapList.size > imageIndex) {
                val byteArray = prepareImageByteArray(imageAdapter.imageBitmapList[imageIndex])
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
                    nextImage(ad, "", imageIndex, imageAdapter, onUploadComplete)
                } else {
                    nextImage(ad, "", imageIndex, imageAdapter, onUploadComplete)
                }
            }
        }

        private fun nextImage(
            ad: Ad?,
            uri: String,
            imageIndex: Int,
            imageAdapter: ImageAdapter,
            onUploadComplete: (Boolean?) -> Unit,
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
