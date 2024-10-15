package com.example.bulletin_board.domain.images

import android.net.Uri
import com.example.bulletin_board.pix.models.Options
import io.ak1.pix.helpers.PixEventCallback

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
