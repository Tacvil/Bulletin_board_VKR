package com.example.bulletin_board.presentation.adapters

import android.animation.Animator
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.databinding.AdListItemBinding
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.domain.model.FavData
import com.example.bulletin_board.domain.ui.ad.AdItemClickListener
import com.example.bulletin_board.presentation.account.AccountView.Companion.CORNER_RADIUS
import com.example.bulletin_board.presentation.utils.PagingAdDiffCallback
import java.util.Locale

abstract class BaseAdAdapter<VH : BaseAdAdapter.BaseAdViewHolder>(
    private val adItemClickListener: AdItemClickListener,
) : PagingDataAdapter<Ad, VH>(PagingAdDiffCallback) {
    abstract class BaseAdViewHolder(
        val binding: AdListItemBinding,
        private val adItemClickListener: AdItemClickListener,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val formatter = SimpleDateFormat("dd/MM/yyyy - hh:mm", Locale.getDefault())

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(ad: Ad) =
            with(binding) {
                textViewDescription.setText(ad.description)
                textViewPrice.text = ad.price.toString()
                adTitleEditText.setText(ad.title)
                textViewViewCounter.text = ad.viewsCounter.toString()
                textViewFav.text = ad.favCounter
                imageButtonFav1.isClickable = true
                val publishTimeLabel = binding.root.context.getString(R.string.publication_time)
                val publishTime = "$publishTimeLabel: ${getTimeFromMillis(ad.time)}"
                textViewData.text = publishTime

                Glide
                    .with(binding.root)
                    .load(ad.mainImage)
                    .apply(RequestOptions().transform(RoundedCorners(CORNER_RADIUS)))
                    .into(imageViewMainImage)

                ad.uid?.let { showEditPanel(adItemClickListener.isOwner(it)) }

                val (startProgress, endProgress, speed) =
                    when (ad.isFav) {
                        true -> Triple(0.38f, 0.38f, 0f)
                        false -> Triple(0.87f, 0.87f, 0f)
                    }

                imageButtonFav1.apply {
                    setupFavAnimation(startProgress, endProgress, speed)
                }

                imageButtonFav1.setOnClickListener {
                    val (startProgressFav, endProgressFav, speedFav) =
                        when (ad.isFav) {
                            true -> Triple(0.6f, 0.87f, 1.5f)
                            false -> Triple(0.0f, 0.38f, 1.5f)
                        }

                    imageButtonFav1.apply {
                        setupFavAnimation(startProgressFav, endProgressFav, speedFav)

                        addAnimatorListener(
                            object : Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {
                                    isClickable = false
                                }

                                override fun onAnimationEnd(animation: Animator) {
                                    adItemClickListener.onFavClick(
                                        FavData(
                                            ad.favCounter,
                                            ad.isFav,
                                            ad.key,
                                        ),
                                    )
                                    removeAnimatorListener(this)
                                }

                                override fun onAnimationCancel(animation: Animator) {}

                                override fun onAnimationRepeat(animation: Animator) {}
                            },
                        )
                        playAnimation()
                    }
                }

                itemView.setOnClickListener {
                    adItemClickListener.onAdClick(ad)
                }

                imageButtonEditAd.setOnClickListener {
                    adItemClickListener.onEditClick(ad)
                }

                imageButtonDeleteAd.setOnClickListener {
                    adItemClickListener.onDeleteClick(ad.key)
                }
            }

        private fun setupFavAnimation(
            startProgress: Float,
            endProgress: Float,
            speed: Float,
        ) {
            binding.imageButtonFav1.apply {
                pauseAnimation()
                cancelAnimation()
                setMinAndMaxProgress(startProgress, endProgress)
                this.speed = speed
            }
        }

        private fun getTimeFromMillis(timeMillis: String): String {
            val c = Calendar.getInstance()
            c.timeInMillis = timeMillis.toLong()
            return formatter.format(c.time)
        }

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
    ): VH {
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return getViewHolder(binding, adItemClickListener)
    }

    abstract fun getViewHolder(
        binding: AdListItemBinding,
        adItemClickListener: AdItemClickListener,
    ): VH

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: VH,
        position: Int,
    ) {
        val ad = getItem(position)
        if (ad != null) {
            holder.bind(ad)
        }
    }
}
