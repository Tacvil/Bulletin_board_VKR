package com.example.bulletin_board.domain

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.example.bulletin_board.dialoghelper.SignUpInHandler
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.MAIN_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject

interface AccountUiHandler {
    fun updateUi(
        user: FirebaseUser?,
        callback: SignInAnonymouslyListenerCallback,
    )

    fun initializeUi()
}

interface SignInAnonymouslyProvider {
    fun signInAnonymously(auth: FirebaseAuth)
}

interface SignInAnonymouslyListenerCallback {
    fun signInAnonymously()
}

interface AuthCallback {
    fun onAuthComplete(user: FirebaseUser?)

    fun onSaveToken(token: String)
}

interface TokenHandler {
    fun saveToken(token: String)
}

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

class AccountManager
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
        private val firestore: FirebaseFirestore,
        private val accountUiHandler: AccountUiHandler,
        private val accountHelperProvider: AccountHelperProvider,
        private val tokenHandler: TokenHandler,
        private val signInAnonymouslyProvider: SignInAnonymouslyProvider,
    ) : SignUpInHandler,
        AuthProvider {
        fun signOut() = firebaseAuth.signOut()

        fun isAnonymous(): Boolean = firebaseAuth.currentUser?.isAnonymous == true

        override val auth: FirebaseAuth
            get() = firebaseAuth

        fun generateAdId(): String =
            firestore
                .collection(MAIN_COLLECTION)
                .document()
                .id

        fun init() {
            accountUiHandler.initializeUi()
        }

        fun updateUi(user: FirebaseUser?) {
            accountUiHandler.updateUi(
                user,
                object : SignInAnonymouslyListenerCallback {
                    override fun signInAnonymously() {
                        signInAnonymouslyProvider.signInAnonymously(auth)
                    }
                },
            )
        }

        fun saveToken(token: String) {
            tokenHandler.saveToken(token)
        }

        override fun signInWithGoogleImpl(googleSignInLauncher: ActivityResultLauncher<Intent>) {
            accountHelperProvider.signInWithGoogle(
                googleSignInLauncher,
                object : AuthCallback {
                    override fun onAuthComplete(user: FirebaseUser?) {
                        updateUi(user)
                    }

                    override fun onSaveToken(token: String) {
                        saveToken(token)
                    }
                },
            )
        }

        override fun signUpWithEmailImpl(
            email: String,
            password: String,
        ) {
            accountHelperProvider.signUpWithEmail(
                email,
                password,
                object : AuthCallback {
                    override fun onAuthComplete(user: FirebaseUser?) {
                        updateUi(user)
                    }

                    override fun onSaveToken(token: String) {
                        saveToken(token)
                    }
                },
                auth,
            )
        }

        override fun signInWithEmailImpl(
            email: String,
            password: String,
        ) {
            accountHelperProvider.signInWithEmail(
                email,
                password,
                object : AuthCallback {
                    override fun onAuthComplete(user: FirebaseUser?) {
                        updateUi(user)
                    }

                    override fun onSaveToken(token: String) {
                        saveToken(token)
                    }
                },
                auth,
            )
        }

        fun handleGoogleSignInResult(
            result: ActivityResult,
            callback: AuthCallback,
        ) {
            accountHelperProvider.handleGoogleSignInResult(result, callback, auth)
        }

        fun signOutGoogle() {
            accountHelperProvider.signOutGoogle()
        }
    }
