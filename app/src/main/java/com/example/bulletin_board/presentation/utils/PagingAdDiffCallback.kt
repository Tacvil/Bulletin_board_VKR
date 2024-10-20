package com.example.bulletin_board.presentation.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.bulletin_board.domain.model.Ad

object PagingAdDiffCallback : DiffUtil.ItemCallback<Ad>() {
    override fun areItemsTheSame(
        oldItem: Ad,
        newItem: Ad,
    ): Boolean = oldItem.key == newItem.key

    override fun areContentsTheSame(
        oldItem: Ad,
        newItem: Ad,
    ): Boolean = oldItem == newItem
}
