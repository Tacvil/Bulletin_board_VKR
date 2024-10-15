package com.example.bulletin_board.domain.auth

import com.google.firebase.auth.FirebaseUser

interface AuthCallback {
    fun onAuthComplete(user: FirebaseUser?)

    fun onSaveToken(token: String)
}
