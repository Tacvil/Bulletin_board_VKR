package com.example.bulletin_board.di

import android.content.ContentResolver
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.domain.image.AddImageHandler
import com.example.bulletin_board.domain.image.ChooseScaleTypeHandler
import com.example.bulletin_board.domain.image.ContentResolverProvider
import com.example.bulletin_board.domain.image.GetMultiImagesProvider
import com.example.bulletin_board.domain.image.GetSingleImagesHandler
import com.example.bulletin_board.domain.image.ImageResizer
import com.example.bulletin_board.domain.image.ImageResizerHandler
import com.example.bulletin_board.domain.image.PixImagePicker
import com.example.bulletin_board.domain.image.PixImagePickerActions
import com.example.bulletin_board.domain.image.ViewModelHandler
import com.example.bulletin_board.model.Ad
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
