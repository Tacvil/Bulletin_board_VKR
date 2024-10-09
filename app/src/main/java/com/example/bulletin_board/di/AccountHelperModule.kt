package com.example.bulletin_board.di

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.bulletin_board.R
import com.example.bulletin_board.domain.AccountHelper
import com.example.bulletin_board.domain.ResourceStringProvider
import com.example.bulletin_board.domain.ToastHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
object AccountHelperModule {
    @Provides
    fun provideGso(activity: Activity): GoogleSignInOptions =
        GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

    @Provides
    fun provideGoogleSignInClient(
        activity: Activity,
        gso: GoogleSignInOptions,
    ): GoogleSignInClient = GoogleSignIn.getClient(activity, gso)

    @Provides
    fun provideAccountHelper(
        googleSignInClient: GoogleSignInClient,
        toastHelper: ToastHelper,
        resourceStringProvider: ResourceStringProvider,
    ): AccountHelper =
        AccountHelper(
            googleSignInClient,
            toastHelper,
            resourceStringProvider,
        )

    @Provides
    fun provideAppCompatActivity(
        @ActivityContext context: Context,
    ): AppCompatActivity = context as AppCompatActivity
}
