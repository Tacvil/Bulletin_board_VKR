package com.example.bulletin_board.data.di

import android.content.ContentResolver
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.data.image.AddImageHandler
import com.example.bulletin_board.data.image.ChooseScaleTypeHandler
import com.example.bulletin_board.data.image.ContentResolverProvider
import com.example.bulletin_board.data.image.GetMultiImagesProvider
import com.example.bulletin_board.data.image.GetSingleImagesHandler
import com.example.bulletin_board.data.image.ImageResizer
import com.example.bulletin_board.data.image.ImageResizerHandler
import com.example.bulletin_board.data.image.PixImagePicker
import com.example.bulletin_board.data.image.PixImagePickerActions
import com.example.bulletin_board.data.image.ViewModelHandler
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.pix.models.Options
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.ak1.pix.helpers.PixEventCallback

@Module
@InstallIn(ActivityComponent::class)
object ImageModule {
    @Provides
    fun provideViewModelHandler(activity: FragmentActivity): ViewModelHandler =
        activity as? ViewModelHandler ?: object : ViewModelHandler {
            override fun insertAd(
                ad: Ad,
                onResult: (Boolean) -> Unit,
            ) {}

            override fun updateImage(
                byteArray: ByteArray,
                oldUrl: String,
                onResult: (Uri) -> Unit,
            ) {}

            override fun uploadImage(
                byteArray: ByteArray,
                onResult: (Uri) -> Unit,
            ) {}

            override fun deleteImageByUrl(url: String) {}
        }

    @Provides
    fun provideContentResolverProvider(activity: FragmentActivity): ContentResolverProvider =
        activity as? ContentResolverProvider ?: object : ContentResolverProvider {
            override fun getContentResolverAct(): ContentResolver = activity.contentResolver
        }

    @Provides
    fun provideGetMultiImagesProvider(pixImagePicker: PixImagePicker): GetMultiImagesProvider = pixImagePicker

    @Provides
    fun provideImageResizerHandler(imageResizer: ImageResizer): ImageResizerHandler = imageResizer

    @Provides
    fun provideAddImageHandler(pixImagePicker: PixImagePicker): AddImageHandler = pixImagePicker

    @Provides
    fun provideGetSingleImagesHandler(pixImagePicker: PixImagePicker): GetSingleImagesHandler = pixImagePicker

    @Provides
    fun provideChooseScaleTypeHandler(imageResizer: ImageResizer): ChooseScaleTypeHandler = imageResizer

    @Provides
    fun providePixImagePickerActions(activity: FragmentActivity): PixImagePickerActions =
        activity as? PixImagePickerActions ?: object : PixImagePickerActions {
            override fun addPixToActivityImpl(
                placeholderId: Int,
                options: Options,
                callback: (PixEventCallback.Results) -> Unit,
            ) {}

            override fun showStatusBarImpl() {}

            override fun openChooseImageFrag() {}

            override fun showImageListFrag(uris: ArrayList<Uri>?) {}

            override fun updateAdapter(uris: ArrayList<Uri>) {}

            override fun setSingleImage(uri: Uri) {}
        }

    @Provides
    fun providePixImagePicker(pixImagePickerActions: PixImagePickerActions): PixImagePicker = PixImagePicker(pixImagePickerActions)
}
