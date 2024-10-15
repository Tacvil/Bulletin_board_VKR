package com.example.bulletin_board.data.image

import android.net.Uri
import com.example.bulletin_board.R
import com.example.bulletin_board.domain.images.AddImageHandler
import com.example.bulletin_board.domain.images.GetMultiImagesProvider
import com.example.bulletin_board.domain.images.GetSingleImagesHandler
import com.example.bulletin_board.domain.images.PixImagePickerActions
import com.example.bulletin_board.pix.models.Mode
import com.example.bulletin_board.pix.models.Options
import com.example.bulletin_board.pix.models.Ratio
import io.ak1.pix.helpers.PixEventCallback
import jakarta.inject.Inject

class PixImagePicker
    @Inject
    constructor(
        private val pixImagePickerActions: PixImagePickerActions,
    ) : GetMultiImagesProvider,
        AddImageHandler,
        GetSingleImagesHandler {
        override fun getMultiImages(imageCounter: Int) {
            pixImagePickerActions.addPixToActivityImpl(R.id.place_holder, getOptions(imageCounter)) { result ->
                when (result.status) {
                    PixEventCallback.Status.SUCCESS -> {
                        getMultiSelectImages(result.data)
                        pixImagePickerActions.showStatusBarImpl()
                    }
                    else -> {}
                }
            }
        }

        override fun addImages(imageCounter: Int) {
            pixImagePickerActions.addPixToActivityImpl(R.id.place_holder, getOptions(imageCounter)) { result ->
                when (result.status) {
                    PixEventCallback.Status.SUCCESS -> {
                        pixImagePickerActions.openChooseImageFrag()
                        pixImagePickerActions.updateAdapter(result.data as ArrayList<Uri>)
                        pixImagePickerActions.showStatusBarImpl()
                    }

                    else -> {}
                }
            }
        }

        override fun getSingleImages() {
            pixImagePickerActions.addPixToActivityImpl(R.id.place_holder, getOptions(1)) { result ->
                when (result.status) {
                    PixEventCallback.Status.SUCCESS -> {
                        pixImagePickerActions.openChooseImageFrag()
                        pixImagePickerActions.setSingleImage(result.data[0])
                        pixImagePickerActions.showStatusBarImpl()
                    }

                    else -> {}
                }
            }
        }

        private fun getMultiSelectImages(uris: List<Uri>) {
            when {
                uris.isNotEmpty() -> {
                    pixImagePickerActions.showImageListFrag(uris as ArrayList<Uri>)
                }
                else -> return
            }
        }

        private fun getOptions(imageCounter: Int): Options {
            val options =
                Options().apply {
                    count = imageCounter
                    isFrontFacing = false
                    mode = Mode.Picture
                    path = PATH
                    ratio = Ratio.RATIO_AUTO
                }
            return options
        }

        companion object {
            const val PATH = "/pix/images"
            const val MAX_IMAGE_COUNT = 3
        }
    }
