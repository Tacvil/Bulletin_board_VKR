package com.example.bulletin_board.data.di

import android.app.Activity
import com.example.bulletin_board.data.voice.VoiceRecognitionHandler
import com.example.bulletin_board.data.voice.VoiceRecognitionListener
import com.example.bulletin_board.domain.ToastHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object VoiceRecognitionModule {
    @Provides
    fun provideVoiceRecognitionListener(activity: Activity): VoiceRecognitionListener {
        if (activity is VoiceRecognitionListener) {
            return activity
        } else {
            throw IllegalArgumentException("Activity must implement VoiceRecognitionListener")
        }
    }

    @Provides
    fun provideVoiceRecognitionHandler(
        listener: VoiceRecognitionListener,
        toastHelper: ToastHelper,
    ): VoiceRecognitionHandler = VoiceRecognitionHandler(listener, toastHelper)
}
