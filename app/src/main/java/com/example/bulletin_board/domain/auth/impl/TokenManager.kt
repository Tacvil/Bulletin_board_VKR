package com.example.bulletin_board.domain.auth.impl

import com.example.bulletin_board.domain.auth.TokenHandler
import com.example.bulletin_board.domain.auth.TokenSaveHandler
import jakarta.inject.Inject

class TokenManager
    @Inject
    constructor(
        private val tokenSaveHandler: TokenSaveHandler,
    ) : TokenHandler {
        override fun saveToken(token: String) {
            tokenSaveHandler.saveToken(token)
        }
    }
