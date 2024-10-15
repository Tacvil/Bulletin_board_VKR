package com.example.bulletin_board.data.di

import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.domain.SearchUi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object SearchUiModule {
    @Provides
    fun provideSearchUi(activity: FragmentActivity): SearchUi = activity as SearchUi
}
