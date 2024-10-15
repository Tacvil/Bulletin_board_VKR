package com.example.bulletin_board.data.auth

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.example.bulletin_board.R
import com.example.bulletin_board.data.network.FirebaseAuthConstants
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
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.currentUser?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        auth
                            .createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task1 ->

                                if (task1.isSuccessful) {
                                    signUpWithEmailSuccessful(task1.result.user!!, callback)
                                    if (Firebase.auth.uid != null) {
                                        getFcmToken()
                                            .addOnSuccessListener { token ->
                                                callback.onSaveToken(token)
                                            }.addOnFailureListener { exception ->
                                                Log.d("Token", "Fetching FCM token failed", exception)
                                            }
                                    } else {
                                        Log.d("Token", "uid = null")
                                    }
                                } else {
                                    signUpWithEmailException(task1.exception!!, email, password, auth)
                                }
                            }
                    }
                }
            }
        }

        private fun signUpWithEmailSuccessful(
            user: FirebaseUser,
            callback: AuthCallback,
        ) {
            sendEmailVerification(user)
            callback.onAuthComplete(user)
        }

        private fun signUpWithEmailException(
            e: Exception,
            email: String,
            password: String,
            auth: FirebaseAuth,
        ) {
            // Toast.makeText(activity, activity.resources.getString(R.string.sign_up_error), Toast.LENGTH_LONG).show()

            if (e is FirebaseAuthUserCollisionException) {
                if (e.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
//                                Toast.makeText(
//                                    activity,
//                                    FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE,
//                                    Toast.LENGTH_LONG
//                                ).show()

                    linkEmailToGoogle(email, password, auth)
                }
            } else if (e is FirebaseAuthInvalidCredentialsException) {
                if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                    toastHelper.showToast(
                        FirebaseAuthConstants.ERROR_INVALID_EMAIL,
                        Toast.LENGTH_LONG,
                    )
                }
            }
            if (e is FirebaseAuthWeakPasswordException) {
                if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                    toastHelper.showToast(
                        FirebaseAuthConstants.ERROR_WEAK_PASSWORD,
                        Toast.LENGTH_LONG,
                    )
                }
            }
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
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.currentUser?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        auth
                            .signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task1 ->
                                if (task1.isSuccessful) {
                                    callback.onAuthComplete(task1.result.user)
                                    if (Firebase.auth.uid != null) {
                                        getFcmToken()
                                            .addOnSuccessListener { token ->
                                                callback.onSaveToken(token)
                                            }.addOnFailureListener { exception ->
                                                Log.d("Token", "Fetching FCM token failed", exception)
                                            }
                                    }
                                } else {
                                    signInWithEmailException(task1.exception!!, email, password)
                                }
                            }
                    } else {
                        Timber.d("task = ${task.exception}")
                    }
                }
            }
        }

        private fun signInWithEmailException(
            e: Exception,
            email: String,
            password: String,
        ) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                    toastHelper.showToast(
                        FirebaseAuthConstants.ERROR_INVALID_EMAIL,
                        Toast.LENGTH_LONG,
                    )
                } else if (e.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                    toastHelper.showToast(
                        FirebaseAuthConstants.ERROR_WRONG_PASSWORD,
                        Toast.LENGTH_LONG,
                    )
                }
            } else if (e is FirebaseAuthInvalidUserException) {
                if (e.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                    toastHelper.showToast(
                        FirebaseAuthConstants.ERROR_USER_NOT_FOUND,
                        Toast.LENGTH_LONG,
                    )
                }
            }
        }

        private fun linkEmailToGoogle(
            email: String,
            password: String,
            auth: FirebaseAuth,
        ) {
            val credential = EmailAuthProvider.getCredential(email, password)
            if (auth.currentUser != null) {
                auth.currentUser
                    ?.linkWithCredential(credential)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            toastHelper.showToast(
                                resourceStringProvider.getStringImpl(R.string.link_done),
                                Toast.LENGTH_LONG,
                            )
                        }
                    }
            } else {
                toastHelper.showToast(
                    resourceStringProvider.getStringImpl(R.string.enter_to_google),
                    Toast.LENGTH_LONG,
                )
            }
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
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    signInFirebaseWithGoogle(account.idToken!!, callback, auth)
                }
            } catch (e: ApiException) {
                toastHelper.showToast("Api exception: ${e.message}", Toast.LENGTH_LONG)
                Log.d("MyLog", "Api exception: ${e.message}")
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
            auth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth
                        .signInWithCredential(credential)
                        .addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                toastHelper.showToast("Sign in done", Toast.LENGTH_LONG)
                                Log.d("UID", "signInFirebaseWithGoogle - ${Firebase.auth.uid}")
                                callback.onAuthComplete(task2.result.user)
                                if (Firebase.auth.uid != null) {
                                    getFcmToken()
                                        .addOnSuccessListener { token ->
                                            callback.onSaveToken(token)
                                        }.addOnFailureListener { exception ->
                                            Log.d("Token", "Fetching FCM token failed", exception)
                                        }
                                } else {
                                    Log.d("Token", "uid = null")
                                }
                            } else {
                                toastHelper.showToast(
                                    "Google Sign In Exception: ${task2.exception}",
                                    Toast.LENGTH_LONG,
                                )
                                Log.d("MyLog", "Google Sign In Exception: ${task2.exception}")
                            }
                        }
                }
            }
        }

        private fun getFcmToken(): Task<String> {
            val taskCompletionSource = TaskCompletionSource<String>()

            FirebaseMessaging
                .getInstance()
                .token
                .addOnSuccessListener { token ->
                    Log.d("TAG TOKEN", "token = $token")
                    taskCompletionSource.setResult(token)
                }.addOnFailureListener { exception ->
                    Log.d("TAG Token", "Fetching FCM registration token failed", exception)
                    taskCompletionSource.setException(exception)
                }

            return taskCompletionSource.task
        }

        override fun signInAnonymously(auth: FirebaseAuth) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toastHelper.showToast("Вы вошли как Гость", Toast.LENGTH_SHORT)
                } else {
                    toastHelper.showToast("Не удалось войти как Гость", Toast.LENGTH_SHORT)
                }
            }
        }
    }
