package com.example.bulletin_board.accounthelper

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.example.bulletin_board.R
import com.example.bulletin_board.constance.FirebaseAuthConstants
import com.example.bulletin_board.dialoghelper.SignUpInHandler
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
import timber.log.Timber

interface AccountHelperProvider {
    fun saveToken(token: String)

    fun showToast(
        message: String,
        duration: Int,
    )

    fun getStringAccountHelper(resId: Int): String

    val mAuth: FirebaseAuth
}

interface UserUiUpdate {
    fun updateUiImpl(user: FirebaseUser?)
}

class AccountHelper(
    private val accountHelperProvider: AccountHelperProvider,
    private val googleSignInClient: GoogleSignInClient,
    private val userUiUpdate: UserUiUpdate,
) : SignUpInHandler {
    private lateinit var signInClient: GoogleSignInClient

    fun signUpWithEmail(
        email: String,
        password: String,
    ) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            accountHelperProvider.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    accountHelperProvider.mAuth
                        .createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task1 ->

                            if (task1.isSuccessful) {
                                signUpWithEmailSuccessful(task1.result.user!!)
                                if (Firebase.auth.uid != null) {
                                    getFcmToken()
                                        .addOnSuccessListener { token ->
                                            accountHelperProvider.saveToken(token)
                                        }.addOnFailureListener { exception ->
                                            Log.d("Token", "Fetching FCM token failed", exception)
                                        }
                                } else {
                                    Log.d("Token", "uid = null")
                                }
                            } else {
                                signUpWithEmailException(task1.exception!!, email, password)
                            }
                        }
                }
            }
        }
    }

    private fun signUpWithEmailSuccessful(user: FirebaseUser) {
        sendEmailVerification(user)
        userUiUpdate.updateUiImpl(user)
    }

    private fun signUpWithEmailException(
        e: Exception,
        email: String,
        password: String,
    ) {
        // Toast.makeText(activity, activity.resources.getString(R.string.sign_up_error), Toast.LENGTH_LONG).show()

        if (e is FirebaseAuthUserCollisionException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
//                                Toast.makeText(
//                                    activity,
//                                    FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE,
//                                    Toast.LENGTH_LONG
//                                ).show()

                linkEmailToGoogle(email, password)
            }
        } else if (e is FirebaseAuthInvalidCredentialsException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                accountHelperProvider.showToast(FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG)
            }
        }
        if (e is FirebaseAuthWeakPasswordException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                accountHelperProvider.showToast(FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_LONG)
            }
        }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                accountHelperProvider.showToast(
                    accountHelperProvider.getStringAccountHelper(R.string.send_verification_email_done),
                    Toast.LENGTH_LONG,
                )
            } else {
                accountHelperProvider.showToast(
                    accountHelperProvider.getStringAccountHelper(R.string.send_verification_email_error),
                    Toast.LENGTH_LONG,
                )
            }
        }
    }

    fun signInWithEmail(
        email: String,
        password: String,
    ) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            accountHelperProvider.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    accountHelperProvider.mAuth
                        .signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
                                userUiUpdate.updateUiImpl(task1.result.user)
                                if (Firebase.auth.uid != null) {
                                    getFcmToken()
                                        .addOnSuccessListener { token ->
                                            accountHelperProvider.saveToken(token)
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
                accountHelperProvider.showToast(FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG)
            } else if (e.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                accountHelperProvider.showToast(FirebaseAuthConstants.ERROR_WRONG_PASSWORD, Toast.LENGTH_LONG)
            }
        } else if (e is FirebaseAuthInvalidUserException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                accountHelperProvider.showToast(FirebaseAuthConstants.ERROR_USER_NOT_FOUND, Toast.LENGTH_LONG)
            }
        }
    }

    private fun linkEmailToGoogle(
        email: String,
        password: String,
    ) {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (accountHelperProvider.mAuth.currentUser != null) {
            accountHelperProvider.mAuth.currentUser
                ?.linkWithCredential(credential)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        accountHelperProvider.showToast(accountHelperProvider.getStringAccountHelper(R.string.link_done), Toast.LENGTH_LONG)
                    }
                }
        } else {
            accountHelperProvider.showToast(accountHelperProvider.getStringAccountHelper(R.string.enter_to_google), Toast.LENGTH_LONG)
        }
    }

    fun signInWithGoogle(launcher: ActivityResultLauncher<Intent>) {
        signInClient = googleSignInClient
        val intent = signInClient.signInIntent
        launcher.launch(intent)
    }

    fun handleGoogleSignInResult(result: ActivityResult) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                signInFirebaseWithGoogle(account.idToken!!)
            }
        } catch (e: ApiException) {
            accountHelperProvider.showToast("Api exception: ${e.message}", Toast.LENGTH_LONG)
            Log.d("MyLog", "Api exception: ${e.message}")
        }
    }

    fun signOutGoogle() {
        signInClient.signOut()
    }

    private fun signInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        accountHelperProvider.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                accountHelperProvider.mAuth.signInWithCredential(credential).addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        accountHelperProvider.showToast("Sign in done", Toast.LENGTH_LONG)
                        Log.d("UID", "signInFirebaseWithGoogle - ${Firebase.auth.uid}")
                        userUiUpdate.updateUiImpl(task2.result.user)
                        if (Firebase.auth.uid != null) {
                            getFcmToken()
                                .addOnSuccessListener { token ->
                                    accountHelperProvider.saveToken(token)
                                }.addOnFailureListener { exception ->
                                    Log.d("Token", "Fetching FCM token failed", exception)
                                }
                        } else {
                            Log.d("Token", "uid = null")
                        }
                    } else {
                        accountHelperProvider.showToast("Google Sign In Exception: ${task2.exception}", Toast.LENGTH_LONG)
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

    override fun signInWithGoogleImpl(googleSignInLauncher: ActivityResultLauncher<Intent>) {
        signInWithGoogle(googleSignInLauncher)
    }

    override fun signUpWithEmailImpl(
        email: String,
        password: String,
    ) {
        signUpWithEmail(email, password)
    }

    override fun signInWithEmailImpl(
        email: String,
        password: String,
    ) {
        signInWithEmail(email, password)
    }

    fun signInAnonymously(onCompleteListener: () -> Unit) {
        accountHelperProvider.mAuth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onCompleteListener()
                accountHelperProvider.showToast("Вы вошли как Гость", Toast.LENGTH_SHORT)
            } else {
                accountHelperProvider.showToast("Не удалось войти как Гость", Toast.LENGTH_SHORT)
            }
        }
    }
}
