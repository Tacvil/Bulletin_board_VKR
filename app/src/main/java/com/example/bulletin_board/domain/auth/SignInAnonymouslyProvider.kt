package com.example.bulletin_board.domain.auth

import com.google.firebase.auth.FirebaseAuth

interface SignInAnonymouslyProvider {
    fun signInAnonymously(auth: FirebaseAuth)
}
