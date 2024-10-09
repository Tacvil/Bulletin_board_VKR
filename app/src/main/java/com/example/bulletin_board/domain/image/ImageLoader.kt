package com.example.bulletin_board.domain.image

import android.graphics.Bitmap
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ImageLoader {
    fun getBitmapFromUris(
        uris: List<String?>,
        callback: (List<Bitmap>) -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmapList = ArrayList<Bitmap>()

            for (i in uris.indices) {
                kotlin.runCatching {
                    val bitmap = Picasso.get().load(uris[i]).get()
                    bitmapList.add(bitmap)
                }
            }

            withContext(Dispatchers.Main) {
                callback(bitmapList)
            }
        }
    }
}
