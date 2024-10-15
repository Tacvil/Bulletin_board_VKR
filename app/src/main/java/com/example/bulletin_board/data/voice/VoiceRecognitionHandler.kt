package com.example.bulletin_board.data.voice

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.example.bulletin_board.domain.utils.ToastHelper
import jakarta.inject.Inject
import timber.log.Timber

class VoiceRecognitionHandler
    @Inject
    constructor(
        private val listener: VoiceRecognitionListener,
        private val toastHelper: ToastHelper,
    ) {
        fun startVoiceRecognition(launcher: ActivityResultLauncher<Intent>) {
            val intent =
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                    )
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something")
                }
            try {
                launcher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                toastHelper.showToast("Голосовое распознавание не поддерживается на вашем устройстве", Toast.LENGTH_SHORT)
            }
        }

        fun handleRecognitionResult(result: ActivityResult) {
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
                if (!spokenText.isNullOrEmpty()) {
                    listener.onVoiceRecognitionResult(spokenText)
                } else {
                    Timber.d("Распознавание речи не дало результатов.")
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                toastHelper.showToast("Распознавание речи отменено.", Toast.LENGTH_SHORT)
            }
        }
    }

interface VoiceRecognitionListener {
    fun onVoiceRecognitionResult(spokenText: String)
}
