package com.example.bulletin_board.presentation.adapters

import com.example.bulletin_board.databinding.ItemAdListBinding
import com.example.bulletin_board.domain.model.AdUpdateEvent
import com.example.bulletin_board.domain.model.FavData
import com.example.bulletin_board.domain.model.ViewData
import com.example.bulletin_board.domain.ui.ad.AdItemClickListener
import com.example.bulletin_board.domain.ui.adapters.AppStateListener
import jakarta.inject.Inject

class MyAdsAdapter
    @Inject
    constructor(
        appStateListener: AppStateListener,
        adItemClickListener: AdItemClickListener,
    ) : BaseAdAdapter<MyAdsAdapter.AdViewHolder>(adItemClickListener) {
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

        class AdViewHolder(
            binding: ItemAdListBinding,
            adItemClickListener: AdItemClickListener,
        ) : BaseAdViewHolder(binding, adItemClickListener)

        override fun getViewHolder(
            binding: ItemAdListBinding,
            adItemClickListener: AdItemClickListener,
        ): AdViewHolder = AdViewHolder(binding, adItemClickListener)
    }
