package com.example.bulletin_board.di

import com.example.bulletin_board.domain.images.ImageLoader
import com.example.bulletin_board.domain.images.impl.GlideImageLoader
import com.example.bulletin_board.domain.search.FilterUpdater
import com.example.bulletin_board.domain.search.InitSearchActionHelper
import com.example.bulletin_board.domain.search.InitSearchBarClickListenerHelper
import com.example.bulletin_board.domain.search.InitTextWatcher
import com.example.bulletin_board.domain.ui.search.SearchUi
import com.example.bulletin_board.presentation.search.SearchActionHandler
import com.example.bulletin_board.presentation.search.SearchBarClickHandler
import com.example.bulletin_board.presentation.search.SearchTextWatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object SearchModule {
    @Provides
    fun provideInitTextWatcher(searchUi: SearchUi): InitTextWatcher = SearchTextWatcher(searchUi)

    @Provides
    fun provideInitSearchActionHelper(
        searchUi: SearchUi,
        filterUpdater: FilterUpdater,
    ): InitSearchActionHelper = SearchActionHandler(searchUi, filterUpdater)

    @Provides
    fun provideInitSearchBarClickListenerHelper(searchUi: SearchUi): InitSearchBarClickListenerHelper = SearchBarClickHandler(searchUi)

    @Provides
    fun provideImageLoader(glideImageLoader: GlideImageLoader): ImageLoader = glideImageLoader
}
