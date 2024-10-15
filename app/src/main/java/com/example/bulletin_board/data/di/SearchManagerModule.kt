package com.example.bulletin_board.data.di

import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.domain.SearchQueryHandler
import com.example.bulletin_board.domain.SearchUiInitializer
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
