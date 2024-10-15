package com.example.bulletin_board.domain.auth

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

interface SignUpInHandler {
    fun signInWithGoogleImpl(googleSignInLauncher: ActivityResultLauncher<Intent>)

    fun signUpWithEmailImpl(
        email: String,
        password: String,
    )

    fun signInWithEmailImpl(
        email: String,
        password: String,
    )
}
