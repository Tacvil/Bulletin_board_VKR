package com.example.bulletin_board.presentation.utils

import android.graphics.Bitmap
import androidx.recyclerview.widget.DiffUtil

class ItemDiffCallback<T>(
    private val oldList: List<T>,
    private val newList: List<T>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return when {
            oldItem is Bitmap && newItem is Bitmap -> oldItem == newItem
            oldItem is Pair<*, *> && newItem is Pair<*, *> -> oldItem.first == newItem.first
            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return when {
            oldItem is Bitmap && newItem is Bitmap -> oldItem == newItem
            oldItem is Pair<*, *> && newItem is Pair<*, *> -> oldItem == newItem
            else -> false
        }
    }
}
