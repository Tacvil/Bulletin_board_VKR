package com.example.bulletin_board.presentation.account

import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.domain.auth.AccountUiHandler
import com.example.bulletin_board.domain.auth.SignInAnonymouslyListenerCallback
import com.example.bulletin_board.domain.images.ImageLoader
import com.example.bulletin_board.domain.ui.account.AccountUiViewsProvider
import com.example.bulletin_board.domain.utils.ResourceStringProvider
import com.google.firebase.auth.FirebaseUser
import jakarta.inject.Inject

class AccountView
    @Inject
    constructor(
        private val imageLoader: ImageLoader,
        private val resourceStringProvider: ResourceStringProvider,
        private val viewsProvider: AccountUiViewsProvider,
    ) : AccountUiHandler {
        private lateinit var textViewAccount: TextView
        private lateinit var imageViewAccount: ImageView

        override fun initializeUi() {
            textViewAccount = viewsProvider.getTextViewAccount()
            imageViewAccount = viewsProvider.getImageViewAccount()
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
                    imageLoader.loadImage(
                        imageViewAccount,
                        user.photoUrl,
                        RequestOptions().transform(RoundedCorners(20)),
                    )
                }
            }
        }
    }
