package com.example.bulletin_board.domain.auth

interface TokenHandler {
    fun saveToken(token: String)
}
