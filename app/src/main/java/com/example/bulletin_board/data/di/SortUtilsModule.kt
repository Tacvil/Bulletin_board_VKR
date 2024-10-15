package com.example.bulletin_board.data.di

import com.example.bulletin_board.data.utils.SortUtils
import com.example.bulletin_board.domain.ResourceStringProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object SortUtilsModule {
    @Provides
    fun provideSortUtils(resourceStringProvider: ResourceStringProvider): SortUtils = SortUtils(resourceStringProvider)
}
