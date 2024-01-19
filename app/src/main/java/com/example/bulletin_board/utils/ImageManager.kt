package com.example.bulletin_board.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.example.bulletin_board.R
import com.example.bulletin_board.adapters.ImageAdapter
import com.example.bulletin_board.model.Announcement
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.io.File
import java.io.InputStream

object ImageManager {

    private const val MAX_IMAGE_SIZE = 1000

    fun getImageSize(uri: Uri, act: Activity): List<Int> {
        val inStream = act.contentResolver.openInputStream(uri)
//        val fTemp = File(act.cacheDir, "temp.tmp")
//        if (inStream != null) {
//            fTemp.copyInStreamToFile(inStream)
//        }
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inStream, null, options)
//        return if (imageRotation(fTemp) == 90)
//            listOf(options.outHeight, options.outWidth)
//        else listOf(options.outWidth, options.outHeight)
        return listOf(options.outWidth, options.outHeight)
    }

//    private fun File.copyInStreamToFile(inStream: InputStream){
//        this.outputStream().use {
//            out -> inStream.copyTo(out)
//        }
//    }

//    private fun imageRotation(imageFile: File): Int {
//        val rotation: Int
//
//        val exif = ExifInterface(imageFile.absolutePath)
//        val orientation =
//            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
//
//        rotation =
//            if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
//                90
//            } else {
//                0
//            }
//
//        return rotation
//    }

    fun chooseScaleType(im: ImageView, bitmap: Bitmap): Boolean{
        var crop = true
        if (bitmap.width > bitmap.height){
            crop = true
        }else{
            crop = false
        }
        return crop
    }



    suspend fun imageResize(uris: List<Uri>, act: Activity) : List<Bitmap> = withContext(Dispatchers.IO) {
        val tempList = ArrayList<List<Int>>()
        val bitmapList = ArrayList<Bitmap>()

        for (n in uris.indices) {
            val size = getImageSize(uris[n], act)
            val imageRatio = size[0].toFloat() / size[1].toFloat()

            if (imageRatio > 1) {

                if (size[0] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))
                } else {
                    tempList.add(listOf(size[0], size[1]))
                }

            } else {

                if (size[1] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                } else {
                    tempList.add(listOf(size[0], size[1]))
                }
            }
        }

        for (i in uris.indices){
           val e = kotlin.runCatching {
               bitmapList.add(Picasso.get().load(uris[i]).resize(tempList[i][0], tempList[i][1]).get())
           }
           // Log.d("MyLog", "Bitmap load done: ${e.isSuccess}")
        }

        return@withContext bitmapList
    }

    private suspend fun getBitmapFromUris(uris: List<String?>) : List<Bitmap> = withContext(Dispatchers.IO) {
        val bitmapList = ArrayList<Bitmap>()

        for (i in uris.indices){
            val e = kotlin.runCatching {
                bitmapList.add(Picasso.get().load(uris[i]).get())
            }
            // Log.d("MyLog", "Bitmap load done: ${e.isSuccess}")
        }

        return@withContext bitmapList
    }

    fun fillImageArray(ad: Announcement, adapter: ImageAdapter) {
        val listUris = listOf(ad.mainImage, ad.image2, ad.image3)
        CoroutineScope(Dispatchers.Main).launch {
            val bitMapList = getBitmapFromUris(listUris)
            adapter.update(bitMapList as ArrayList<Bitmap>)
        }
    }

}