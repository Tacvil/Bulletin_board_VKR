package com.example.bulletin_board.di

import com.example.bulletin_board.domain.ResourceStringProvider
import com.example.bulletin_board.utils.SortUtils
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
