package com.example.bulletin_board.dialoghelper

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.example.bulletin_board.act.MainActivity
import com.example.bulletin_board.R
import com.example.bulletin_board.accounthelper.AccountHelper
import com.example.bulletin_board.databinding.SignDialogBinding

class DialogHelper(private val activity: MainActivity) {

    val accHelper = AccountHelper(activity)

    fun createSignDialog(index: Int) {
        val builder = AlertDialog.Builder(activity)
        val binding = SignDialogBinding.inflate(activity.layoutInflater)
        builder.setView(binding.root)
        setDialogState(index, binding)

        val dialog = builder.create()
        binding.buttonSignUpIn.setOnClickListener {
            setOnClickSignUpIn(index, binding, dialog)
        }
        binding.buttonForgetPassword.setOnClickListener {
            setOnClickResetPassword(binding, dialog)
        }
        binding.buttonGoogleSigIn.setOnClickListener {
            accHelper.signInWithGoogle()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setOnClickResetPassword(binding: SignDialogBinding, dialog: AlertDialog?) {
        if (binding.editSignEmail.text?.isNotEmpty() == true) {
            activity.mAuth.sendPasswordResetEmail(binding.editSignEmail.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(activity, R.string.email_reset, Toast.LENGTH_LONG).show()
                    }
                }
            dialog?.dismiss()
        } else {
            binding.textViewError.visibility = View.VISIBLE
        }
    }

    private fun setOnClickSignUpIn(index: Int, binding: SignDialogBinding, dialog: AlertDialog?) {
        dialog?.dismiss()

        if (index == DialogConst.SIGN_UP_STATE) {
            accHelper.signUpWithEmail(
                binding.editSignEmail.text.toString(),
                binding.editSignPassword.text.toString()
            )
        } else {
            accHelper.signInWithEmail(
                binding.editSignEmail.text.toString(),
                binding.editSignPassword.text.toString()
            )
        }
    }

    private fun setDialogState(index: Int, binding: SignDialogBinding) {
        if (index == DialogConst.SIGN_UP_STATE) {
            binding.textViewSignTitle.text = activity.resources.getString(R.string.ac_sign_up)
            binding.buttonSignUpIn.text = activity.resources.getString(R.string.sign_up_action)
        } else {
            binding.textViewSignTitle.text = activity.resources.getString(R.string.ac_sign_in)
            binding.buttonSignUpIn.text = activity.resources.getString(R.string.sign_in_action)
            binding.buttonForgetPassword.visibility = View.VISIBLE
        }
    }
}