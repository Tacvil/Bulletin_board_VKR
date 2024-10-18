package com.example.bulletin_board.di

import android.app.Activity
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.R
import com.example.bulletin_board.data.auth.AuthRepository
import com.example.bulletin_board.domain.auth.AccountHelperProvider
import com.example.bulletin_board.domain.auth.AccountUiHandler
import com.example.bulletin_board.domain.auth.SignInAnonymouslyProvider
import com.example.bulletin_board.domain.auth.TokenHandler
import com.example.bulletin_board.domain.auth.impl.AccountManager
import com.example.bulletin_board.domain.auth.impl.TokenManager
import com.example.bulletin_board.domain.ui.account.AccountUiViewsProvider
import com.example.bulletin_board.domain.utils.ResourceStringProvider
import com.example.bulletin_board.domain.utils.ToastHelper
import com.example.bulletin_board.presentation.account.AccountView
import com.google.android.material.navigation.NavigationView
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
    abstract fun bindAccountHelperProvider(authRepository: AuthRepository): AccountHelperProvider

    @Binds
    @ActivityScoped
    abstract fun bindSignInAnonymouslyProvider(authRepository: AuthRepository): SignInAnonymouslyProvider
}

@Module
@InstallIn(ActivityComponent::class)
object ResourceProviderModule {
    @Provides
    fun provideToastHelper(activity: FragmentActivity): ToastHelper =
        if (activity is ToastHelper) {
            activity
        } else {
            object : ToastHelper {
                override fun showToast(
                    message: String,
                    duration: Int,
                ) {
                    Toast.makeText(activity, message, duration).show()
                }
            }
        }

    @Provides
    fun provideResourceStringProvider(activity: Activity): ResourceStringProvider =
        if (activity is ResourceStringProvider) {
            activity
        } else {
            object : ResourceStringProvider {
                override fun getStringImpl(resId: Int): String = activity.getString(resId)
            }
        }
}

@Module
@InstallIn(ActivityComponent::class)
abstract class AccountUiHandlerBindingModule {
    @Binds
    @ActivityScoped
    abstract fun bindAccountUiHandler(accountView: AccountView): AccountUiHandler
}

@Module
@InstallIn(ActivityComponent::class)
object AccountUiHandlerModule {
    @Provides
    fun provideAccountUiViewsProvider(activity: Activity): AccountUiViewsProvider {
        return if (activity is AccountUiViewsProvider) {
            activity
        } else {
            object : AccountUiViewsProvider {
                override fun getTextViewAccount(): TextView {
                    // Здесь нужно получить TextView из layout activity
                    // Например, если TextView находится в NavigationView:
                    val navigationView = activity.findViewById<NavigationView>(R.id.navigation_view)
                    return navigationView.getHeaderView(0).findViewById(R.id.text_view_account_email)
                }

                override fun getImageViewAccount(): ImageView {
                    // Здесь нужно получить ImageView из layout activity
                    // Например, если ImageView находится в NavigationView:
                    val navigationView = activity.findViewById<NavigationView>(R.id.navigation_view)
                    return navigationView.getHeaderView(0).findViewById(R.id.image_view_account_image)
                }
            }
        }
    }
}

@Module
@InstallIn(ActivityComponent::class)
abstract class TokenHandlerModule {
    @Binds
    @ActivityScoped
    abstract fun bindTokenHandler(tokenManager: TokenManager): TokenHandler
}
