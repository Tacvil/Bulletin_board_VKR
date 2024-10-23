package com.example.bulletin_board.di

import com.example.bulletin_board.domain.auth.AccountUiHandler
import com.example.bulletin_board.presentation.account.AccountView
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
abstract class AccountUiHandlerBindingModule {
    @Binds
    @ActivityScoped
    abstract fun bindAccountUiHandler(accountView: AccountView): AccountUiHandler
}
