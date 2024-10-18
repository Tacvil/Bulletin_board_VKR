package com.example.bulletin_board.di

import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.domain.search.SearchQueryHandler
import com.example.bulletin_board.domain.search.SearchUiInitializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object SearchManagerModule {
    @Provides
    fun provideSearchUiInitializer(activity: FragmentActivity): SearchUiInitializer = activity as SearchUiInitializer

    @Provides
    fun provideSearchQueryHandler(activity: FragmentActivity): SearchQueryHandler = activity as SearchQueryHandler
}
