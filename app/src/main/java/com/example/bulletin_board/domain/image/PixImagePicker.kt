package com.example.bulletin_board.domain.image

import android.net.Uri
import com.example.bulletin_board.R
import com.example.bulletin_board.pix.models.Mode
import com.example.bulletin_board.pix.models.Options
import com.example.bulletin_board.pix.models.Ratio
import io.ak1.pix.helpers.PixEventCallback
import jakarta.inject.Inject
import timber.log.Timber

interface PixImagePickerActions {
    fun addPixToActivityImpl(
        placeholderId: Int,
        options: Options,
        callback: (PixEventCallback.Results) -> Unit,
    )

    fun showStatusBarImpl()

    fun openChooseImageFrag()

    fun showImageListFrag(uris: ArrayList<Uri>?)

    fun updateAdapter(uris: ArrayList<Uri>)

    fun setSingleImage(uri: Uri)
}

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
                        Timber.d("Success ${result.data}")
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
                    Timber.d("uris.isNotEmpty")
                    pixImagePickerActions.showImageListFrag(uris as ArrayList<Uri>)
                }
                else -> return
            }
/*            if (uris.size > 1 && edAct.chooseImageFrag == null) {
                edAct.openChooseImageFrag(uris as ArrayList<Uri>)
            } else if (uris.size == 1 && edAct.chooseImageFrag == null) {
                CoroutineScope(Dispatchers.Main).launch {
                    edAct.binding.progressBarLoad.visibility = View.VISIBLE
                    val bitMapArray = ImageManager.imageResize(uris as ArrayList<Uri>, edAct) as ArrayList<Bitmap>
                    edAct.binding.progressBarLoad.visibility = View.GONE
                    edAct.imageAdapter.update(bitMapArray)
                    closePixFrag(edAct)
                }
            }*/
        }

        private fun getOptions(imageCounter: Int): Options {
            val options =
                Options().apply {
                    count = imageCounter
                    isFrontFacing = false
                    mode = Mode.Picture
                    path = "/pix/images"
                    ratio = Ratio.RATIO_AUTO
                }
            return options
        }

        companion object {
            const val MAX_IMAGE_COUNT = 3
            const val REQUEST_CODE_GET_IMAGES = 999
            const val REQUEST_CODE_GET_SINGLE_IMAGE = 998
        }
    }
