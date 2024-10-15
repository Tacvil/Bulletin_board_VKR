package com.example.bulletin_board.domain.auth

import com.example.bulletin_board.domain.SignInAnonymouslyListenerCallback
import com.google.firebase.auth.FirebaseUser

interface AccountUiHandler {
    fun updateUi(
        user: FirebaseUser?,
        callback: SignInAnonymouslyListenerCallback,
    )

    fun initializeUi()
}
