package com.example.bulletin_board.utils

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
import com.example.bulletin_board.R
import com.example.bulletin_board.act.EditAdsActivity
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
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
    private fun getOptions(imageCounter: Int): Options {
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
                }

                else -> {}
            }
        }
    }

    fun addImages(edAct: EditAdsActivity, imageCounter: Int) {
        val f = edAct.chooseImageFrag
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    edAct.chooseImageFrag = f
                    openChooseImageFrag(edAct, f!!)
                    edAct.chooseImageFrag?.updateAdapter(result.data as ArrayList<Uri>, edAct)
                }

                else -> {}
            }
        }
    }

    fun getSingleImages(edAct: EditAdsActivity) {
        val f = edAct.chooseImageFrag
        edAct.addPixToActivity(R.id.place_holder, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    edAct.chooseImageFrag = f
                    openChooseImageFrag(edAct, f!!)
                    singleImage(edAct, result.data[0])
                }

                else -> {}
            }
        }
    }

    private fun openChooseImageFrag(edAct: EditAdsActivity, f: Fragment) {
        edAct.supportFragmentManager.beginTransaction().replace(R.id.place_holder, f).commit()
    }

    private fun closePixFrag(edAct: EditAdsActivity) {
        val fList = edAct.supportFragmentManager.fragments
        fList.forEach {
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

