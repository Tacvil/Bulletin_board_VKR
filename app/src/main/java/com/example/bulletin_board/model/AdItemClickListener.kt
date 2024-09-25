package com.example.bulletin_board.model

interface AdItemClickListener {
    fun onAdClick(ad: Ad)

    fun onFavClick(favData: FavData)

    fun onDeleteClick(adKey: String)

    fun onEditClick(ad: Ad)

    fun isOwner(adUid: String): Boolean
}
