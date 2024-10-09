package com.example.bulletin_board.di

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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object ImageModule {
    @Provides
    fun provideViewModelHandler(activity: FragmentActivity): ViewModelHandler = activity as ViewModelHandler

    @Provides
    fun provideContentResolverProvider(activity: FragmentActivity): ContentResolverProvider = activity as ContentResolverProvider

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
    fun providePixImagePickerActions(activity: FragmentActivity): PixImagePickerActions = activity as PixImagePickerActions

    @Provides
    fun providePixImagePicker(pixImagePickerActions: PixImagePickerActions): PixImagePicker = PixImagePicker(pixImagePickerActions)
}
