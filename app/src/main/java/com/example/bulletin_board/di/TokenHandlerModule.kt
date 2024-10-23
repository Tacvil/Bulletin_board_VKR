package com.example.bulletin_board.di

import com.example.bulletin_board.domain.auth.TokenHandler
import com.example.bulletin_board.domain.auth.impl.TokenManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
abstract class TokenHandlerModule {
    @Binds
    @ActivityScoped
    abstract fun bindTokenHandler(tokenManager: TokenManager): TokenHandler
}
