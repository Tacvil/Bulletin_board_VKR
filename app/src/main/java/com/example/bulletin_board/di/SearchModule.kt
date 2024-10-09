package com.example.bulletin_board.di

import android.app.Activity
import com.example.bulletin_board.domain.FilterUpdater
import com.example.bulletin_board.domain.ImageLoader
import com.example.bulletin_board.domain.InitSearchActionHelper
import com.example.bulletin_board.domain.InitSearchBarClickListenerHelper
import com.example.bulletin_board.domain.InitTextWatcher
import com.example.bulletin_board.domain.SearchActionHelper
import com.example.bulletin_board.domain.SearchBarClickListenerHelper
import com.example.bulletin_board.domain.SearchUi
import com.example.bulletin_board.domain.TextWatcherHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object SearchModule {
    @Provides
    fun provideInitTextWatcher(searchUi: SearchUi): InitTextWatcher = TextWatcherHelper(searchUi) // Реализация в TextWatcherHelper

    @Provides
    fun provideInitSearchActionHelper(
        searchUi: SearchUi,
        filterUpdater: FilterUpdater,
    ): InitSearchActionHelper = SearchActionHelper(searchUi, filterUpdater) // Реализация в SearchActionHelper

    @Provides
    fun provideInitSearchBarClickListenerHelper(searchUi: SearchUi): InitSearchBarClickListenerHelper =
        SearchBarClickListenerHelper(searchUi) // Реализация в SearchBarClickListenerHelper

    @Provides
    fun provideImageLoader(activity: Activity): ImageLoader = activity as ImageLoader
}
