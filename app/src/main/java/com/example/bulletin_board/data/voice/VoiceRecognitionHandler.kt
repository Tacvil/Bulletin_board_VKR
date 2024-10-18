package com.example.bulletin_board.data.voice

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.example.bulletin_board.R
import com.example.bulletin_board.domain.utils.ResourceStringProvider
import com.example.bulletin_board.domain.utils.ToastHelper
import com.example.bulletin_board.domain.voice.VoiceRecognitionListener
import jakarta.inject.Inject
import timber.log.Timber

class VoiceRecognitionHandler
    @Inject
    constructor(
        private val listener: VoiceRecognitionListener,
        private val toastHelper: ToastHelper,
        private val stringProvider: ResourceStringProvider,
    ) {
        fun startVoiceRecognition(launcher: ActivityResultLauncher<Intent>) {
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                    )
                    putExtra(
                        RecognizerIntent.EXTRA_PROMPT,
                        stringProvider.getStringImpl(R.string.speak_something),
                    )
                }.also { intent ->
                    try {
                        launcher.launch(intent)
                    } catch (e: ActivityNotFoundException) {
                        toastHelper.showToast(
                            stringProvider.getStringImpl(R.string.voice_recognition_not_supported),
                            Toast.LENGTH_SHORT,
                        )
                    }
                }
        }

        fun handleRecognitionResult(result: ActivityResult) {
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    result.data
                        ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        ?.firstOrNull()
                        ?.let { spokenText ->
                            listener.onVoiceRecognitionResult(spokenText)
                        }
                        ?: Timber.d(stringProvider.getStringImpl(R.string.voice_recognition_no_results))
                }

                Activity.RESULT_CANCELED ->
                    toastHelper.showToast(
                        stringProvider.getStringImpl(R.string.voice_recognition_canceled),
                        Toast.LENGTH_SHORT,
                    )
            }
        }
    }
