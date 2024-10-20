package com.example.bulletin_board.di

import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.domain.listener.OnSettingsChangeListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object SettingsModule {
    @Provides
    fun provideAppStateListener(activity: FragmentActivity): OnSettingsChangeListener = activity as OnSettingsChangeListener
}
