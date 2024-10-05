package com.example.bulletin_board.di

import com.example.bulletin_board.domain.AccountHelper
import com.example.bulletin_board.domain.AccountHelperProvider
import com.example.bulletin_board.domain.AccountManager
import com.example.bulletin_board.domain.AccountUiHandler
import com.example.bulletin_board.domain.AccountUiManager
import com.example.bulletin_board.domain.ResourceStringProvider
import com.example.bulletin_board.domain.SignInAnonymouslyProvider
import com.example.bulletin_board.domain.ToastHelper
import com.example.bulletin_board.domain.TokenHandler
import com.example.bulletin_board.domain.TokenManager
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object AccountManagerModule {
    @Provides
    fun provideAccountManager(
        firebaseAuth: FirebaseAuth,
        accountUiHandler: AccountUiHandler,
        accountHelperProvider: AccountHelperProvider,
        tokenHandler: TokenHandler,
        signInAnonymouslyProvider: SignInAnonymouslyProvider,
    ): AccountManager =
        AccountManager(
            firebaseAuth,
            accountUiHandler,
            accountHelperProvider,
            tokenHandler,
            signInAnonymouslyProvider,
        )
}

@Module
@InstallIn(ActivityComponent::class)
abstract class AccountHelperBindingModule {
    @Binds
    @ActivityScoped
    abstract fun bindAccountHelperProvider(accountHelper: AccountHelper): AccountHelperProvider

    @Binds
    @ActivityScoped
    abstract fun bindSignInAnonymouslyProvider(accountHelper: AccountHelper): SignInAnonymouslyProvider
}

@Module
@InstallIn(ActivityComponent::class)
object ResourceProviderModule {
    @Provides
    @ActivityScoped
    fun provideToastHelper(androidResourceProvider: AndroidResourceProvider): ToastHelper = androidResourceProvider

    @Provides
    @ActivityScoped
    fun provideResourceStringProvider(androidResourceProvider: AndroidResourceProvider): ResourceStringProvider = androidResourceProvider
}

@Module
@InstallIn(ActivityComponent::class)
abstract class AccountUiHandlerModule {
    @Binds
    @ActivityScoped
    abstract fun bindAccountUiHandler(accountUiManager: AccountUiManager): AccountUiHandler
}

@Module
@InstallIn(ActivityComponent::class)
abstract class TokenHandlerModule {
    @Binds
    @ActivityScoped
    abstract fun bindTokenHandler(tokenManager: TokenManager): TokenHandler
}
