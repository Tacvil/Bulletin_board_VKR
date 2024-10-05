package com.example.bulletin_board.di

import android.app.Activity
import com.example.bulletin_board.domain.TokenSaveHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object TokenSaveHandlerModule {
    @Provides
    @ActivityScoped
    fun provideTokenSaveHandler(activity: Activity): TokenSaveHandler = activity as TokenSaveHandler
}
