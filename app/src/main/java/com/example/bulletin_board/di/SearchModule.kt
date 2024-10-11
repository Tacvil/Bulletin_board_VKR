package com.example.bulletin_board.di

import com.example.bulletin_board.domain.FilterUpdater
import com.example.bulletin_board.domain.ImageLoader
import com.example.bulletin_board.domain.InitSearchActionHelper
import com.example.bulletin_board.domain.InitSearchBarClickListenerHelper
import com.example.bulletin_board.domain.InitTextWatcher
import com.example.bulletin_board.domain.SearchActionHelper
import com.example.bulletin_board.domain.SearchBarClickListenerHelper
import com.example.bulletin_board.domain.SearchUi
import com.example.bulletin_board.domain.TextWatcherHelper
import com.example.bulletin_board.domain.image.GlideImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object SearchModule {
    @Provides
    fun provideInitTextWatcher(searchUi: SearchUi): InitTextWatcher = TextWatcherHelper(searchUi)

    @Provides
    fun provideInitSearchActionHelper(
        searchUi: SearchUi,
        filterUpdater: FilterUpdater,
    ): InitSearchActionHelper = SearchActionHelper(searchUi, filterUpdater)

    @Provides
    fun provideInitSearchBarClickListenerHelper(searchUi: SearchUi): InitSearchBarClickListenerHelper =
        SearchBarClickListenerHelper(searchUi)

    @Provides
    fun provideImageLoader(glideImageLoader: GlideImageLoader): ImageLoader = glideImageLoader
}
