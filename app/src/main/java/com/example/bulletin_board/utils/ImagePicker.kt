package com.example.bulletin_board.utils

import android.graphics.Bitmap
import android.hardware.camera2.CameraCaptureSession
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.bulletin_board.R
import com.example.bulletin_board.act.EditAdsActivity
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.helpers.showStatusBar
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImagePicker {
    const val MAX_IMAGE_COUNT = 3
    const val REQUEST_CODE_GET_IMAGES = 999
    const val REQUEST_CODE_GET_SINGLE_IMAGE = 998
    fun getOptions(imageCounter: Int): Options {
        val options = Options().apply {
            count = imageCounter
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
            ratio = Ratio.RATIO_AUTO
        }
        return options
    }

    fun getMultiImages(edAct: EditAdsActivity, imageCounter: Int) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectImages(edAct, result.data)
                    WindowCompat.setDecorFitsSystemWindows(edAct.window, true)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        edAct.window.attributes.layoutInDisplayCutoutMode =
                            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                    }
                    edAct.showStatusBar()
                }

                else -> {}
            }
        }
    }

    fun addImages(edAct: EditAdsActivity, imageCounter: Int) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edAct)
                    edAct.chooseImageFrag?.updateAdapter(result.data as ArrayList<Uri>, edAct)
                }

                else -> {}
            }
        }
    }

    fun getSingleImages(edAct: EditAdsActivity) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edAct)
                    singleImage(edAct, result.data[0])
                }

                else -> {}
            }
        }
    }

    private fun openChooseImageFrag(edAct: EditAdsActivity) {
        edAct.supportFragmentManager.beginTransaction().replace(R.id.place_holder, edAct.chooseImageFrag!!).commit()
    }

    private fun closePixFrag(edAct: EditAdsActivity) {
        val fList = edAct.supportFragmentManager.fragments
        fList.forEach {
            Log.d("It", "$it")
            if (it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    fun getMultiSelectImages(edAct: EditAdsActivity, uris: List<Uri>) {

        if (uris.size > 1 && edAct.chooseImageFrag == null) {

            edAct.openChooseImageFrag(uris as ArrayList<Uri>)

        } else if (uris.size == 1 && edAct.chooseImageFrag == null) {

            CoroutineScope(Dispatchers.Main).launch {
                edAct.binding.progressBarLoad.visibility = View.VISIBLE
                val bitMapArray =
                    ImageManager.imageResize(uris as ArrayList<Uri>, edAct) as ArrayList<Bitmap>
                edAct.binding.progressBarLoad.visibility = View.GONE
                edAct.imageAdapter.update(bitMapArray)
                closePixFrag(edAct)
            }

        }

    }

    fun singleImage(edAct: EditAdsActivity, uri: Uri) {
        edAct.chooseImageFrag?.setSingleImage(
            uri,
            edAct.editImagePos
        )
    }

}

