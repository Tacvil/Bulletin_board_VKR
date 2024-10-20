package com.example.bulletin_board.presentation.adapters

import com.example.bulletin_board.databinding.AdListItemBinding
import com.example.bulletin_board.domain.model.AdUpdateEvent
import com.example.bulletin_board.domain.model.ViewData
import com.example.bulletin_board.domain.ui.ad.AdItemClickListener
import com.example.bulletin_board.domain.ui.adapters.AppStateListener
import jakarta.inject.Inject

class FavoriteAdsAdapter
    @Inject
    constructor(
        appStateListener: AppStateListener,
        adItemClickListener: AdItemClickListener,
    ) : BaseAdAdapter<FavoriteAdsAdapter.AdViewHolder>(adItemClickListener) {
        init {
            appStateListener.onAppStateEvent { adEvent ->
                when (adEvent) {
                    is AdUpdateEvent.FavUpdated -> refresh()
                    is AdUpdateEvent.ViewCountUpdated -> updateViewCount(adEvent.viewData)
                    is AdUpdateEvent.AdDeleted -> refresh()
                }
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

        class AdViewHolder(
            binding: AdListItemBinding,
            adItemClickListener: AdItemClickListener,
        ) : BaseAdViewHolder(binding, adItemClickListener)

        override fun getViewHolder(
            binding: AdListItemBinding,
            adItemClickListener: AdItemClickListener,
        ): AdViewHolder = AdViewHolder(binding, adItemClickListener)
    }
