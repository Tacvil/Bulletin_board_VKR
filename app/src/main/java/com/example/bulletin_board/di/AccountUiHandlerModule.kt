package com.example.bulletin_board.di

import android.app.Activity
import android.widget.ImageView
import android.widget.TextView
import com.example.bulletin_board.R
import com.example.bulletin_board.domain.ui.account.AccountUiViewsProvider
import com.google.android.material.navigation.NavigationView
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

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
                    val navigationView = activity.findViewById<NavigationView>(R.id.navigation_view)
                    return navigationView.getHeaderView(0).findViewById(R.id.text_view_account_email)
                }

                override fun getImageViewAccount(): ImageView {
                    val navigationView = activity.findViewById<NavigationView>(R.id.navigation_view)
                    return navigationView.getHeaderView(0).findViewById(R.id.image_view_account_image)
                }
            }
        }
    }
}
