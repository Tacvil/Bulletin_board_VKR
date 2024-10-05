package com.example.bulletin_board.domain

import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseUser
import jakarta.inject.Inject

interface ImageLoader {
    fun loadImage(
        imageView: ImageView,
        imageUrl: Uri?,
        requestOptions: RequestOptions,
    )
}

class AccountUiManager
    @Inject
    constructor(
        private val imageLoader: ImageLoader,
        private val resourceStringProvider: ResourceStringProvider,
    ) : AccountUiHandler {
        private lateinit var textViewAccount: TextView
        private lateinit var imageViewAccount: ImageView

        override fun initViews(binding: ActivityMainBinding) {
            textViewAccount = binding.navigationView.getHeaderView(0).findViewById(R.id.text_view_account_email)
            imageViewAccount = binding.navigationView.getHeaderView(0).findViewById(R.id.image_view_account_image)
        }

        override fun updateUi(
            user: FirebaseUser?,
            callback: SignInAnonymouslyListenerCallback,
        ) {
            when {
                user == null || user.isAnonymous -> {
                    callback.signInAnonymously()
                    textViewAccount.text = resourceStringProvider.getStringImpl(R.string.guest)
                    imageViewAccount.setImageResource(R.drawable.ic_my_ads)
                }
                else -> {
                    textViewAccount.text = user.email
                    imageLoader.loadImage(imageViewAccount, user.photoUrl, RequestOptions().transform(RoundedCorners(20)))
                }
            }
        }
    }
