package com.example.bulletin_board.di

import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.domain.ui.ad.AdItemClickListener
import com.example.bulletin_board.domain.ui.adapters.AppStateListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object AppStateListenerModule {
    @Provides
    fun provideAppStateListener(activity: FragmentActivity): AppStateListener = activity as AppStateListener

    @Provides
    fun provideAdItemClickListener(activity: FragmentActivity): AdItemClickListener = activity as AdItemClickListener
}
