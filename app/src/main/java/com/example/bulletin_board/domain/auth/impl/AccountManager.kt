package com.example.bulletin_board.domain.auth.impl

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.MAIN_COLLECTION
import com.example.bulletin_board.domain.auth.AccountHelperProvider
import com.example.bulletin_board.domain.auth.AccountUiHandler
import com.example.bulletin_board.domain.auth.AuthCallback
import com.example.bulletin_board.domain.auth.AuthProvider
import com.example.bulletin_board.domain.auth.SignInAnonymouslyListenerCallback
import com.example.bulletin_board.domain.auth.SignInAnonymouslyProvider
import com.example.bulletin_board.domain.auth.SignUpInHandler
import com.example.bulletin_board.domain.auth.TokenHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject

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
        fun isSignedIn(): Boolean = firebaseAuth.currentUser != null

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
