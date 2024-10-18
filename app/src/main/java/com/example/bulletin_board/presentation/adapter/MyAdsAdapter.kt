package com.example.bulletin_board.presentation.adapter

import android.animation.Animator
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.databinding.AdListItemBinding
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.domain.model.AdUpdateEvent
import com.example.bulletin_board.domain.model.FavData
import com.example.bulletin_board.domain.model.ViewData
import com.example.bulletin_board.domain.ui.ad.AdItemClickListener
import com.example.bulletin_board.domain.ui.adapters.AppStateListener
import jakarta.inject.Inject
import timber.log.Timber
import java.util.Locale

class MyAdsAdapter
    @Inject
    constructor(
        appStateListener: AppStateListener,
    ) : PagingDataAdapter<Ad, MyAdsAdapter.AdHolder>(MyAdDiffCallback()) {
        init {
            appStateListener.onAppStateEvent { adEvent ->
                when (adEvent) {
                    is AdUpdateEvent.FavUpdated -> updateFav(adEvent.favData)
                    is AdUpdateEvent.ViewCountUpdated -> updateViewCount(adEvent.viewData)
                    is AdUpdateEvent.AdDeleted -> refresh()
                }
            }
        }

        private fun updateFav(favData: FavData) {
            val position = snapshot().items.indexOfFirst { it.key == favData.key }
            if (position != -1) {
                val adToUpdate = snapshot().items[position]
                adToUpdate.isFav = favData.isFav
                adToUpdate.favCounter = favData.favCounter
                notifyItemChanged(position)
            }
        }

        private fun updateViewCount(viewData: ViewData) {
            val position = snapshot().items.indexOfFirst { it.key == viewData.key }
            if (position != -1) {
                val adToUpdate = snapshot().items[position]
                adToUpdate.viewsCounter = viewData.viewsCounter
                notifyItemChanged(position)
            }
        }

        class AdHolder
            @Inject
            constructor(
                val binding: AdListItemBinding,
            ) : RecyclerView.ViewHolder(binding.root) {
                private val formatter = SimpleDateFormat("dd/MM/yyyy - hh:mm", Locale.getDefault())

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
                        val publishTimeLabel = binding.root.context.getString(R.string.publication_time)
                        val publishTime = "$publishTimeLabel: ${getTimeFromMillis(ad.time)}"
                        textViewData.text = publishTime

                        Glide
                            .with(binding.root)
                            .load(ad.mainImage)
                            .apply(RequestOptions().transform(RoundedCorners(20)))
                            .into(imageViewMainImage)

                        ad.uid?.let { isOwner(it) }?.let { showEditPanel(it) }

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
                                        (binding.root.context as? AdItemClickListener)?.onFavClick(
                                            FavData(
                                                ad.favCounter,
                                                ad.isFav,
                                                ad.key,
                                            ),
                                        )
                                        imageButtonFav1.removeAnimatorListener(this)
                                    }

                                    override fun onAnimationCancel(animation: Animator) {}

                                    override fun onAnimationRepeat(animation: Animator) {}
                                },
                            )
                            imageButtonFav1.playAnimation()
                        }

                        itemView.setOnClickListener {
                            (binding.root.context as? AdItemClickListener)?.onAdClick(
                                ad,
                            )
                        }

                        imageButtonEditAd.setOnClickListener {
                            (binding.root.context as? AdItemClickListener)?.onEditClick(ad)
                        }

                        imageButtonDeleteAd.setOnClickListener {
                            (binding.root.context as? AdItemClickListener)?.onDeleteClick(ad.key)
                        }
                    }

                private fun getTimeFromMillis(timeMillis: String): String {
                    val c = Calendar.getInstance()
                    c.timeInMillis = timeMillis.toLong()
                    return formatter.format(c.time)
                }

                private fun isOwner(adUid: String): Boolean? = (binding.root.context as? AdItemClickListener)?.isOwner(adUid)

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
            return AdHolder(binding)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onBindViewHolder(
            holder: AdHolder,
            position: Int,
        ) {
            val ad = getItem(position)
            if (ad != null) {
                holder.bind(ad)
            }
        }
    }

class MyAdDiffCallback : DiffUtil.ItemCallback<Ad>() {
    override fun areItemsTheSame(
        oldItem: Ad,
        newItem: Ad,
    ): Boolean = oldItem.key == newItem.key

    override fun areContentsTheSame(
        oldItem: Ad,
        newItem: Ad,
    ): Boolean = oldItem == newItem
}
