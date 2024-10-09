package com.example.bulletin_board.di

import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.domain.AccountHelper
import com.example.bulletin_board.domain.AccountHelperProvider
import com.example.bulletin_board.domain.AccountManager
import com.example.bulletin_board.domain.AccountUiHandler
import com.example.bulletin_board.domain.AccountUiManager
import com.example.bulletin_board.domain.AccountUiViewsProvider
import com.example.bulletin_board.domain.ResourceStringProvider
import com.example.bulletin_board.domain.SignInAnonymouslyProvider
import com.example.bulletin_board.domain.ToastHelper
import com.example.bulletin_board.domain.TokenHandler
import com.example.bulletin_board.domain.TokenManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        firestore: FirebaseFirestore,
        accountUiHandler: AccountUiHandler,
        accountHelperProvider: AccountHelperProvider,
        tokenHandler: TokenHandler,
        signInAnonymouslyProvider: SignInAnonymouslyProvider,
    ): AccountManager =
        AccountManager(
            firebaseAuth,
            firestore,
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
    fun provideToastHelper(activity: FragmentActivity): ToastHelper = activity as ToastHelper

    @Provides
    fun provideResourceStringProvider(activity: FragmentActivity): ResourceStringProvider = activity as ResourceStringProvider
}

@Module
@InstallIn(ActivityComponent::class)
abstract class AccountUiHandlerBindingModule {
    @Binds
    @ActivityScoped
    abstract fun bindAccountUiHandler(accountUiManager: AccountUiManager): AccountUiHandler
}

@Module
@InstallIn(ActivityComponent::class)
object AccountUiHandlerModule {
    @Provides
    fun provideAccountUiViewsProvider(activity: FragmentActivity): AccountUiViewsProvider = activity as AccountUiViewsProvider
}

@Module
@InstallIn(ActivityComponent::class)
abstract class TokenHandlerModule {
    @Binds
    @ActivityScoped
    abstract fun bindTokenHandler(tokenManager: TokenManager): TokenHandler
}
