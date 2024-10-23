package com.example.bulletin_board.presentation.dialogs

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
import com.example.bulletin_board.domain.auth.AuthProvider
import com.example.bulletin_board.domain.auth.SignUpInHandler
import com.example.bulletin_board.domain.utils.ToastHelper

class SignInDialogFragment(
    private val googleSignInLauncher: ActivityResultLauncher<Intent>,
    private val index: Int,
    private val signUpInHandler: SignUpInHandler,
    private val authProvider: AuthProvider,
    private val toastHelper: ToastHelper,
) : DialogFragment() {
    private var _binding: SignDialogBinding? = null
    val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        _binding = SignDialogBinding.inflate(layoutInflater)
        builder.setView(binding.root)

        setupDialogState()
        setupClickListeners()

        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupDialogState() {
        if (index == SIGN_UP_STATE) {
            binding.textViewSignTitle.text = getString(R.string.ac_sign_up)
            binding.buttonSignUpIn.text = getString(R.string.sign_up_action)
        } else {
            binding.textViewSignTitle.text = getString(R.string.ac_sign_in)
            binding.buttonSignUpIn.text = getString(R.string.sign_in_action)
            binding.buttonForgetPassword.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        binding.buttonSignUpIn.setOnClickListener { handleSignUpInClick() }
        binding.buttonForgetPassword.setOnClickListener { handleResetPasswordClick() }
        binding.googleSignInButton.setOnClickListener { handleGoogleSignInClick() }
    }

    private fun handleSignUpInClick() {
        if (index == SIGN_UP_STATE) {
            signUpInHandler.signUpWithEmailImpl(
                binding.editSignEmail.text.toString(),
                binding.editSignPassword.text.toString(),
            )
        } else {
            signUpInHandler.signInWithEmailImpl(
                binding.editSignEmail.text.toString(),
                binding.editSignPassword.text.toString(),
            )
        }
        dismiss()
    }

    private fun handleResetPasswordClick() {
        if (binding.editSignEmail.text?.isNotEmpty() == true) {
            authProvider.auth
                .sendPasswordResetEmail(binding.editSignEmail.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        toastHelper.showToast(getString(R.string.email_reset), Toast.LENGTH_LONG)
                    }
                }
            dismiss()
        } else {
            binding.textViewErrorEmail.visibility = View.VISIBLE
        }
    }

    private fun handleGoogleSignInClick() {
        signUpInHandler.signInWithGoogleImpl(googleSignInLauncher)
        dismiss()
    }

    companion object {
        const val SIGN_UP_STATE = 0
        const val SIGN_IN_STATE = 1
    }
}
