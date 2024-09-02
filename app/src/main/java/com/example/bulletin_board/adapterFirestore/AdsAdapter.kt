package com.example.bulletin_board.adapterFirestore

import android.animation.Animator
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.act.EditAdsActivity
import com.example.bulletin_board.act.MainActivity
import com.example.bulletin_board.databinding.AdListItemBinding
import com.example.bulletin_board.model.Ad
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class AdsAdapter
    @Inject
    constructor(
        private val act: MainActivity,
        private val adHolderFactory: AdHolderFactory.Companion,
        options: FirestorePagingOptions<Ad>,
    ) : FirestorePagingAdapter<Ad, AdsAdapter.AdHolder>(options) {
        private var timeFormatter: SimpleDateFormat? = null

        init {
            timeFormatter = SimpleDateFormat("dd/MM/yyyy - hh:mm", Locale.getDefault())
        }

        class AdHolder
            @Inject
            constructor(
                val binding: AdListItemBinding,
                val act: MainActivity,
                private val formatter: SimpleDateFormat,
            ) : RecyclerView.ViewHolder(binding.root) {
                @RequiresApi(Build.VERSION_CODES.O)
                fun bind(ad: Ad) =
                    with(binding) {
                        textViewDescription.setText(ad.description)
                        textViewPrice.text = ad.price.toString()
                        textViewTitleD.setText(ad.title)
                        textViewViewCounter.text = ad.viewsCounter.toString()
                        textViewFav.text = ad.favCounter
                        Timber
                            .tag("ADSRCADAPTER")
                            .d("favCounter = " + ad.favCounter + " | uids = " + ad.favUids)
                        imageButtonFav1.isClickable = true
                        val publishTimeLabel = act.getString(R.string.publication_time)
                        val publishTime = "$publishTimeLabel: ${getTimeFromMillis(ad.time)}"
                        textViewData.text = publishTime

                        Glide
                            .with(binding.root)
                            .load(ad.mainImage)
                            .apply(RequestOptions().transform(RoundedCorners(20)))
                            .into(imageViewMainImage)

                        showEditPanel(isOwner(ad))
                        if (ad.isFav) {
                            imageButtonFav1.pauseAnimation()
                            imageButtonFav1.cancelAnimation()
                            imageButtonFav1.setMinAndMaxProgress(0.38f, 0.38f)
                        } else {
                            imageButtonFav1.pauseAnimation()
                            imageButtonFav1.cancelAnimation()
                            imageButtonFav1.setMinAndMaxProgress(0.87f, 0.87f)
                        }

                        imageButtonFav1.setOnClickListener {
                            if (!ad.isFav) {
                                imageButtonFav1.pauseAnimation()
                                imageButtonFav1.cancelAnimation()
                                imageButtonFav1.setMinAndMaxProgress(0.0f, 0.38f)
                                imageButtonFav1.speed = 1.5f
                            } else {
                                imageButtonFav1.pauseAnimation()
                                imageButtonFav1.cancelAnimation()
                                imageButtonFav1.setMinAndMaxProgress(0.6f, 0.87f)
                                imageButtonFav1.speed = 1.5f
                            }

                            imageButtonFav1.addAnimatorListener(
                                object : Animator.AnimatorListener {
                                    override fun onAnimationStart(animation: Animator) {
                                        imageButtonFav1.isClickable = false
                                    }

                                    override fun onAnimationEnd(animation: Animator) {
                                        imageButtonFav1.removeAnimatorListener(this)
                                    }

                                    override fun onAnimationCancel(animation: Animator) {
                                    }

                                    override fun onAnimationRepeat(animation: Animator) {
                                    }
                                },
                            )
                            imageButtonFav1.playAnimation()
                        }

                        itemView.setOnClickListener {
                            // act.onAdViewed(ad)
                        }

                        imageButtonEditAd.setOnClickListener(onClickEdit(ad))

                        imageButtonDeleteAd.setOnClickListener {
                            // act.onDeleteItem(ad)
                        }
                    }

                private fun getTimeFromMillis(timeMillis: String): String {
                    val c = Calendar.getInstance()
                    c.timeInMillis = timeMillis.toLong()
                    return formatter.format(c.time)
                }

                private fun onClickEdit(ad: Ad): View.OnClickListener =
                    View.OnClickListener {
                        val editIntent =
                            Intent(act, EditAdsActivity::class.java).apply {
                                putExtra(MainActivity.EDIT_STATE, true)
                                putExtra(MainActivity.ADS_DATA, ad)
                            }
                        act.startActivity(editIntent)
                    }

                private fun isOwner(ad: Ad): Boolean = ad.uid == act.mAuth.uid

                private fun showEditPanel(isOwner: Boolean) {
                    if (isOwner) {
                        binding.editPanel.visibility = View.VISIBLE
                    } else {
                        binding.editPanel.visibility = View.GONE
                    }
                }
            }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): AdHolder {
            val binding =
                AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return adHolderFactory.create(act, timeFormatter!!, binding)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onBindViewHolder(
            holder: AdHolder,
            position: Int,
            model: Ad,
        ) {
            holder.bind(model)
        }

        interface Listener {
            fun onDeleteItem(ad: Ad)

            fun onAdViewed(ad: Ad)

            fun onFavClicked(ad: Ad)
        }
    }
