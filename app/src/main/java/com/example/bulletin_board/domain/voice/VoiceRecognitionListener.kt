package com.example.bulletin_board.domain.voice

interface VoiceRecognitionListener {
    fun onVoiceRecognitionResult(spokenText: String)
}
