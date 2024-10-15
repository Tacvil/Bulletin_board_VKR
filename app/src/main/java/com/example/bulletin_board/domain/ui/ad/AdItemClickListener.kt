package com.example.bulletin_board.domain.ui.ad

import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.domain.model.FavData

interface AdItemClickListener {
    fun onAdClick(ad: Ad)

    fun onFavClick(favData: FavData)

    fun onDeleteClick(adKey: String)

    fun onEditClick(ad: Ad)

    fun isOwner(adUid: String): Boolean
}
