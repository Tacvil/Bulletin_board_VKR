package com.example.bulletin_board.domain.auth

interface TokenSaveHandler {
    fun saveToken(token: String)
}
