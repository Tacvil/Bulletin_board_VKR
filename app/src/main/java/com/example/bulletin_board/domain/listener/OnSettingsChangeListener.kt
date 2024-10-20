package com.example.bulletin_board.domain.listener

interface OnSettingsChangeListener {
    fun onLanguageChanged(newLanguage: String)

    fun onThemeChanged()
}
