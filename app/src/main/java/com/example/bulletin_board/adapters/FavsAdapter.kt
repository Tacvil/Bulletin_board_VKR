package com.example.bulletin_board.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.R
import com.example.bulletin_board.model.Announcement

class FavsAdapter(
    val context: Context,
) : RecyclerView.Adapter<FavsAdapter.AdHolder>() {
    val mainList = ArrayList<Announcement>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AdHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.ad_list_item, parent, false)
        return AdHolder(view)
    }

    override fun onBindViewHolder(
        holder: AdHolder,
        position: Int,
    ) {
        holder.setData(mainList[position])
    }

    override fun getItemCount(): Int = mainList.size

    fun updateAdapter(newList: List<Announcement>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(mainList, newList))
        diffResult.dispatchUpdatesTo(this)
        mainList.clear()
        mainList.addAll(newList)
    }

    class AdHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        fun setData(announcement: Announcement) =
            with(itemView) {
                // ... ваш код для установки данных в элементы ad_item ...
            }
    }
}
