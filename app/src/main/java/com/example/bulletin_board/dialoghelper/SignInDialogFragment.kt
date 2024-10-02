package com.example.bulletin_board.dialoghelper

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.DialogFragment
import com.example.bulletin_board.R
import com.example.bulletin_board.databinding.SignDialogBinding
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

// Интерфейсы для взаимодействия
interface DialogHelperProvider {
    fun showToastImpl(
        message: String,
        duration: Int,
    )

    val mAuthImpl: FirebaseAuth
}

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

class SignInDialogFragment(
    private val googleSignInLauncher: ActivityResultLauncher<Intent>,
    private val index: Int,
    private val signUpInHandler: SignUpInHandler,
    private val dialogHelperProvider: DialogHelperProvider,
) : DialogFragment() {
    private var _binding: SignDialogBinding? = null
    val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        _binding = SignDialogBinding.inflate(layoutInflater)
        builder.setView(binding.root)

        setDialogState(index)

        // Настраиваем действия на кнопки
        binding.buttonSignUpIn.setOnClickListener {
            setOnClickSignUpIn(index)
        }

        binding.buttonForgetPassword.setOnClickListener {
            setOnClickResetPassword()
        }

        binding.buttonGoogleSigIn.setOnClickListener {
            signUpInHandler.signInWithGoogleImpl(googleSignInLauncher)
            dismiss() // Закрыть диалог
        }

        return builder.create()
    }

    private fun setOnClickResetPassword() {
        if (binding.editSignEmail.text?.isNotEmpty() == true) {
            dialogHelperProvider.mAuthImpl
                .sendPasswordResetEmail(binding.editSignEmail.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        dialogHelperProvider.showToastImpl(getString(R.string.email_reset), Toast.LENGTH_LONG)
                    }
                }
            dismiss()
        } else {
            binding.textViewError.visibility = View.VISIBLE
        }
    }

    private fun setOnClickSignUpIn(index: Int) {
        Timber.d("Sign state: $index")
        if (index == DialogConst.SIGN_UP_STATE) {
            signUpInHandler.signUpWithEmailImpl(
                binding.editSignEmail.text.toString(),
                binding.editSignPassword.text.toString(),
            )
        } else {
            Timber.d("Sign state: SDELAL")
            signUpInHandler.signInWithEmailImpl(
                binding.editSignEmail.text.toString(),
                binding.editSignPassword.text.toString(),
            )
        }
        dismiss()
    }

    private fun setDialogState(index: Int) {
        if (index == DialogConst.SIGN_UP_STATE) {
            binding.textViewSignTitle.text = getString(R.string.ac_sign_up)
            binding.buttonSignUpIn.text = getString(R.string.sign_up_action)
        } else {
            binding.textViewSignTitle.text = getString(R.string.ac_sign_in)
            binding.buttonSignUpIn.text = getString(R.string.sign_in_action)
            binding.buttonForgetPassword.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
