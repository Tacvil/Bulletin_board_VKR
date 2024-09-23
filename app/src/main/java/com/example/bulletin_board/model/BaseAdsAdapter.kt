package com.example.bulletin_board.model

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdsAdapter<T : Any, VH : RecyclerView.ViewHolder>(diffCallback: DiffUtil.ItemCallback<T>) :
    PagingDataAdapter<T, VH>(diffCallback) {

    var onAdClickListener: ((T, Int) -> Unit)? = null
    var onFavClickListener: ((FavData, Int) -> Unit)? = null
    var onDeleteClickListener: ((T) -> Unit)? = null
    var onEditClickListener: ((T) -> Unit)? = null

    // ... (другие общие методы, если необходимо)
}