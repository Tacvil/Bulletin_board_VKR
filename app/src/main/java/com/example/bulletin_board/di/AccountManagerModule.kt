package com.example.bulletin_board.di

import com.example.bulletin_board.domain.auth.AccountHelperProvider
import com.example.bulletin_board.domain.auth.AccountUiHandler
import com.example.bulletin_board.domain.auth.SignInAnonymouslyProvider
import com.example.bulletin_board.domain.auth.TokenHandler
import com.example.bulletin_board.domain.auth.impl.AccountManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

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
