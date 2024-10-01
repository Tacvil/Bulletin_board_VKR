package com.example.bulletin_board.domain

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import timber.log.Timber

class VoiceRecognitionHandler(
    private val listener: VoiceRecognitionListener,
    private val launcher: ActivityResultLauncher<Intent>,
) {
    fun startVoiceRecognition() {
        val intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something")
            }

        try {
            launcher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            listener.onVoiceRecognitionError("Голосовое распознавание не поддерживается на вашем устройстве")
        }
    }

    fun handleRecognitionResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            if (!results.isNullOrEmpty()) {
                val spokenText = results[0]
                Timber.d("Распознанный текст: $spokenText")
                listener.onVoiceRecognitionResult(spokenText)
            } else {
                Timber.d("Распознавание речи не дало результатов.")
            }
        }
    }
}

interface VoiceRecognitionListener {
    fun onVoiceRecognitionResult(spokenText: String)

    fun onVoiceRecognitionError(message: String)
}
