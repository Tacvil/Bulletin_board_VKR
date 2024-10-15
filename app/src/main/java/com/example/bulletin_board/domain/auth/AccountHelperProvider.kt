package com.example.bulletin_board.domain.auth

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.google.firebase.auth.FirebaseAuth

interface AccountHelperProvider {
    fun signInWithGoogle(
        launcher: ActivityResultLauncher<Intent>,
        callback: AuthCallback,
    )

    fun signUpWithEmail(
        email: String,
        password: String,
        callback: AuthCallback,
        auth: FirebaseAuth,
    )

    fun signInWithEmail(
        email: String,
        password: String,
        callback: AuthCallback,
        auth: FirebaseAuth,
    )

    fun handleGoogleSignInResult(
        result: ActivityResult,
        callback: AuthCallback,
        auth: FirebaseAuth,
    )

    fun signOutGoogle()
}
