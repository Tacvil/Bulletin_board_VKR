package com.example.bulletin_board.di

import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.fragments.FragmentCloseInterface
import com.example.bulletin_board.fragments.ImageAdapterHandler
import com.example.bulletin_board.fragments.OnItemDeleteListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object ChooseImageFragmentModule {
    @Provides
    fun provideFragmentCloseInterface(activity: FragmentActivity): FragmentCloseInterface = activity as FragmentCloseInterface

    @Provides
    fun provideOnItemDeleteListener(activity: FragmentActivity): OnItemDeleteListener = activity as OnItemDeleteListener

    @Provides
    fun provideImageAdapterHandler(activity: FragmentActivity): ImageAdapterHandler = activity as ImageAdapterHandler
}
