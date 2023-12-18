package com.example.bulletin_board.accounthelper

import android.util.Log
import android.widget.Toast
import com.example.bulletin_board.act.MainActivity
import com.example.bulletin_board.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseUser
import com.example.bulletin_board.constance.FirebaseAuthConstants
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider

class AccountHelper(private val activity: MainActivity) {

    val signInRequestCode = 111
    private lateinit var signInClient: GoogleSignInClient

    fun signUpWithEmail(email: String, password: String) {

        if (email.isNotEmpty() && password.isNotEmpty()) {
            activity.mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        sendEmailVerification(task.result.user!!)
                        activity.uiUpdate(task.result.user)
                    } else {
                        //Toast.makeText(activity, activity.resources.getString(R.string.sign_up_error), Toast.LENGTH_LONG).show()

                        if (task.exception is FirebaseAuthUserCollisionException) {
                            val exception = task.exception as FirebaseAuthUserCollisionException
                            if (exception.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
//                                Toast.makeText(
//                                    activity,
//                                    FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE,
//                                    Toast.LENGTH_LONG
//                                ).show()

                                linkEmailToGoogle(email, password)

                            }

                        } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            val exception =
                                task.exception as FirebaseAuthInvalidCredentialsException
                            if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                                Toast.makeText(
                                    activity,
                                    FirebaseAuthConstants.ERROR_INVALID_EMAIL,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        if (task.exception is FirebaseAuthWeakPasswordException) {
                            val exception = task.exception as FirebaseAuthWeakPasswordException
                            if (exception.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                                Toast.makeText(
                                    activity,
                                    FirebaseAuthConstants.ERROR_WEAK_PASSWORD,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
        }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    activity,
                    activity.resources.getString(R.string.send_verification_email_done),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    activity,
                    activity.resources.getString(R.string.send_verification_email_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {

        if (email.isNotEmpty() && password.isNotEmpty()) {
            activity.mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        activity.uiUpdate(task.result.user)
                    } else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            val exception =
                                task.exception as FirebaseAuthInvalidCredentialsException
                            if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                                Toast.makeText(
                                    activity,
                                    FirebaseAuthConstants.ERROR_INVALID_EMAIL,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (exception.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                                Toast.makeText(
                                    activity,
                                    FirebaseAuthConstants.ERROR_WRONG_PASSWORD,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else if (task.exception is FirebaseAuthInvalidUserException) {
                            val exception = task.exception as FirebaseAuthInvalidUserException
                            if (exception.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                                Toast.makeText(
                                    activity,
                                    FirebaseAuthConstants.ERROR_USER_NOT_FOUND,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
        }
    }

    private fun linkEmailToGoogle(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (activity.mAuth.currentUser != null) {
            activity.mAuth.currentUser?.linkWithCredential(credential)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            activity,
                            activity.resources.getString(R.string.link_done),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } else {
            Toast.makeText(
                activity,
                activity.resources.getString(R.string.enter_to_google),
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id)).requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun signInWithGoogle() {
        signInClient = getSignInClient()
        val intent = signInClient.signInIntent
        activity.startActivityForResult(intent, RESULT_CODE_SUCCESS)
    }

    fun signOutGoogle() {
        getSignInClient().signOut()
    }

    fun signInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        activity.mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(activity, "Sign is done", Toast.LENGTH_SHORT).show()
                activity.uiUpdate(task.result.user)
            } else {
                Log.d("MyLog", "Google Sign In Exception: ${task.exception}")
            }
        }
    }

    companion object {
        const val RESULT_CODE_SUCCESS = 1111
        const val RESULT_CODE_FAILURE = 2222
        // другие константы
    }

}