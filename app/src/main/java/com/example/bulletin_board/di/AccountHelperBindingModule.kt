package com.example.bulletin_board.di

import com.example.bulletin_board.data.auth.AuthRepository
import com.example.bulletin_board.domain.auth.AccountHelperProvider
import com.example.bulletin_board.domain.auth.SignInAnonymouslyProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
abstract class AccountHelperBindingModule {
    @Binds
    @ActivityScoped
    abstract fun bindAccountHelperProvider(authRepository: AuthRepository): AccountHelperProvider

    @Binds
    @ActivityScoped
    abstract fun bindSignInAnonymouslyProvider(authRepository: AuthRepository): SignInAnonymouslyProvider
}
