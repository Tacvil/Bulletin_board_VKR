package com.example.bulletin_board.di

import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.domain.FilterUpdater
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object FilterUpdaterModule {
    @Provides
    fun provideFilterUpdater(activity: FragmentActivity): FilterUpdater = activity as FilterUpdater
}
