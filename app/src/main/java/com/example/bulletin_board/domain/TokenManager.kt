package com.example.bulletin_board.domain

import jakarta.inject.Inject

interface TokenSaveHandler {
    fun saveToken(token: String)
}

class TokenManager
    @Inject
    constructor(
        private val tokenSaveHandler: TokenSaveHandler,
    ) : TokenHandler {
        override fun saveToken(token: String) {
            tokenSaveHandler.saveToken(token)
        }
    }
