package com.example.bulletin_board.data.auth

import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.example.bulletin_board.R
import com.example.bulletin_board.domain.auth.AccountHelperProvider
import com.example.bulletin_board.domain.auth.AuthCallback
import com.example.bulletin_board.domain.auth.SignInAnonymouslyProvider
import com.example.bulletin_board.domain.utils.ResourceStringProvider
import com.example.bulletin_board.domain.utils.ToastHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import jakarta.inject.Inject
import timber.log.Timber

class AuthRepository
    @Inject
    constructor(
        private val googleSignInClient: GoogleSignInClient,
        private val toastHelper: ToastHelper,
        private val resourceStringProvider: ResourceStringProvider,
    ) : AccountHelperProvider,
        SignInAnonymouslyProvider {
        override fun signUpWithEmail(
            email: String,
            password: String,
            callback: AuthCallback,
            auth: FirebaseAuth,
        ) {
            if (email.isNotBlank() && password.isNotBlank()) {
                auth.currentUser?.delete()?.addOnSuccessListener {
                    auth
                        .createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { result ->
                            signUpWithEmailSuccessful(result.user!!, callback)
                            getFcmTokenAndSave(callback)
                        }.addOnFailureListener { exception ->
                            handleAuthWithEmailException(exception, email, password, auth)
                        }
                }
            }
        }

        private fun getFcmTokenAndSave(callback: AuthCallback) {
            Firebase.auth.uid?.let { uid ->
                getFcmToken()
                    .addOnSuccessListener { token ->
                        callback.onSaveToken(token)
                    }.addOnFailureListener { exception ->
                        Timber.e(exception, "Fetching FCM token failed for user $uid")
                    }
            } ?: Timber.w("User UID is null, FCM token might not be fetched")
        }

        private fun handleAuthWithEmailException(
            e: Exception,
            email: String? = null,
            password: String? = null,
            auth: FirebaseAuth? = null,
        ) {
            when (e) {
                is FirebaseAuthUserCollisionException -> {
                    when (e.errorCode) {
                        ERROR_EMAIL_ALREADY_IN_USE -> {
                            if (email != null && password != null && auth != null) {
                                linkEmailToGoogle(email, password, auth)
                            } else {
                                Timber.e(e, "Missing parameters for linking email to Google")
                            }
                        }

                        else -> showToastForErrorCode(e.errorCode)
                    }
                }

                is FirebaseAuthWeakPasswordException -> showToastForErrorCode(e.errorCode)
                is FirebaseAuthInvalidCredentialsException -> showToastForErrorCode(e.errorCode)
                is FirebaseAuthInvalidUserException -> showToastForErrorCode(e.errorCode)
                else -> Timber.e(e, "Unexpected exception during authentication with email")
            }
        }

        private fun showToastForErrorCode(errorCode: String) {
            when (errorCode) {
                ERROR_WEAK_PASSWORD ->
                    toastHelper.showToast(
                        ERROR_WEAK_PASSWORD,
                        Toast.LENGTH_LONG,
                    )

                ERROR_INVALID_EMAIL ->
                    toastHelper.showToast(
                        ERROR_INVALID_EMAIL,
                        Toast.LENGTH_LONG,
                    )

                ERROR_WRONG_PASSWORD ->
                    toastHelper.showToast(
                        ERROR_WRONG_PASSWORD,
                        Toast.LENGTH_LONG,
                    )

                ERROR_USER_NOT_FOUND ->
                    toastHelper.showToast(
                        ERROR_USER_NOT_FOUND,
                        Toast.LENGTH_LONG,
                    )

                else -> Timber.e("Unknown error code: $errorCode")
            }
        }

        private fun signUpWithEmailSuccessful(
            user: FirebaseUser,
            callback: AuthCallback,
        ) {
            sendEmailVerification(user)
            callback.onAuthComplete(user)
        }

        private fun sendEmailVerification(user: FirebaseUser) {
            user.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toastHelper.showToast(
                        resourceStringProvider.getStringImpl(R.string.send_verification_email_done),
                        Toast.LENGTH_LONG,
                    )
                } else {
                    toastHelper.showToast(
                        resourceStringProvider.getStringImpl(R.string.send_verification_email_error),
                        Toast.LENGTH_LONG,
                    )
                }
            }
        }

        override fun signInWithEmail(
            email: String,
            password: String,
            callback: AuthCallback,
            auth: FirebaseAuth,
        ) {
            if (email.isNotBlank() && password.isNotBlank()) {
                auth.currentUser
                    ?.delete()
                    ?.addOnSuccessListener {
                        auth
                            .signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener { result ->
                                callback.onAuthComplete(result.user)
                                getFcmTokenAndSave(callback)
                            }.addOnFailureListener { exception ->
                                handleAuthWithEmailException(exception)
                            }
                    }?.addOnFailureListener { exception ->
                        Timber.e(exception, "signInWithEmail() failed: ${exception.message}")
                    }
            }
        }

        private fun linkEmailToGoogle(
            email: String,
            password: String,
            auth: FirebaseAuth,
        ) {
            val credential = EmailAuthProvider.getCredential(email, password)
            auth.currentUser?.linkWithCredential(credential)?.addOnSuccessListener {
                toastHelper.showToast(
                    resourceStringProvider.getStringImpl(R.string.link_done),
                    Toast.LENGTH_LONG,
                )
            } ?: toastHelper.showToast(
                resourceStringProvider.getStringImpl(R.string.enter_to_google),
                Toast.LENGTH_LONG,
            )
        }

        override fun signInWithGoogle(
            launcher: ActivityResultLauncher<Intent>,
            callback: AuthCallback,
        ) {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }

        override fun handleGoogleSignInResult(
            result: ActivityResult,
            callback: AuthCallback,
            auth: FirebaseAuth,
        ) {
            runCatching {
                GoogleSignIn
                    .getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
            }.onSuccess { account ->
                account?.let {
                    signInFirebaseWithGoogle(
                        it.idToken!!,
                        callback,
                        auth,
                    )
                }
            }.onFailure { e ->
                toastHelper.showToast(
                    resourceStringProvider.getStringImpl(R.string.google_sign_in_exception),
                    Toast.LENGTH_LONG,
                )
                Timber.e(e, "Google Sign In API exception")
            }
        }

        override fun signOutGoogle() {
            googleSignInClient.signOut()
        }

        private fun signInFirebaseWithGoogle(
            token: String,
            callback: AuthCallback,
            auth: FirebaseAuth,
        ) {
            val credential = GoogleAuthProvider.getCredential(token, null)
            auth.currentUser?.delete()?.addOnSuccessListener {
                auth
                    .signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        toastHelper.showToast(
                            resourceStringProvider.getStringImpl(R.string.sign_in_done),
                            Toast.LENGTH_LONG,
                        )
                        Timber.i("signInFirebaseWithGoogle: uid = ${Firebase.auth.uid}")
                        callback.onAuthComplete(result.user)
                        getFcmTokenAndSave(callback)
                    }.addOnFailureListener { exception ->
                        toastHelper.showToast(
                            resourceStringProvider.getStringImpl(R.string.google_sign_in_exception),
                            Toast.LENGTH_LONG,
                        )
                        Timber.e(exception, "Google Sign In Exception")
                    }
            }
        }

        private fun getFcmToken(): Task<String> {
            val taskCompletionSource = TaskCompletionSource<String>()

            FirebaseMessaging
                .getInstance()
                .token
                .addOnSuccessListener { token ->
                    taskCompletionSource.setResult(token)
                }.addOnFailureListener { exception ->
                    taskCompletionSource.setException(exception)
                }

            return taskCompletionSource.task
        }

        override fun signInAnonymously(auth: FirebaseAuth) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toastHelper.showToast(
                        resourceStringProvider.getStringImpl(R.string.sign_in_as_guest_done),
                        Toast.LENGTH_SHORT,
                    )
                } else {
                    toastHelper.showToast(
                        resourceStringProvider.getStringImpl(R.string.sign_in_as_guest_error),
                        Toast.LENGTH_SHORT,
                    )
                }
            }
        }

        companion object {
            const val ERROR_EMAIL_ALREADY_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE"
            const val ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL"
            const val ERROR_WRONG_PASSWORD = "ERROR_WRONG_PASSWORD"
            const val ERROR_WEAK_PASSWORD = "ERROR_WEAK_PASSWORD"
            const val ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"
        }
    }
