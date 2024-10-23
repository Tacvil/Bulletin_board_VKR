package com.example.bulletin_board.di

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.bulletin_board.domain.utils.ResourceStringProvider
import com.example.bulletin_board.domain.utils.ToastHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

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
