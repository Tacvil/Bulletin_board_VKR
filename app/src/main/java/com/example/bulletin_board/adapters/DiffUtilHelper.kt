package com.example.bulletin_board.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.bulletin_board.model.Ad

class DiffUtilHelper(
    val oldList: List<Ad>,
    val newList: List<Ad>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean = oldList[oldItemPosition].key == newList[newItemPosition].key

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean = oldList[oldItemPosition] == newList[newItemPosition]
}
