package com.example.bulletin_board.presentation.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.R
import jakarta.inject.Inject

class ImageAdapter
    @Inject
    constructor() : RecyclerView.Adapter<ImageAdapter.ImageHolder>() {
        val imageBitmapList = ArrayList<Bitmap>()

        class ImageHolder(
            itemView: View,
        ) : RecyclerView.ViewHolder(itemView) {
            private lateinit var imItem: ImageView

            fun setData(bitmap: Bitmap) {
                imItem = itemView.findViewById(R.id.image_view_item)
                imItem.setImageBitmap(bitmap)
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ImageHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.image_adapter_item, parent, false)
            return ImageHolder(view)
        }

        override fun onBindViewHolder(
            holder: ImageHolder,
            position: Int,
        ) {
            holder.setData(imageBitmapList[position])
        }

        override fun getItemCount(): Int = imageBitmapList.size

        fun update(newList: ArrayList<Bitmap>) {
            val diffResult = DiffUtil.calculateDiff(BitmapDiffCallback(imageBitmapList, newList))
            imageBitmapList.clear()
            imageBitmapList.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }
    }

class BitmapDiffCallback(
    private val oldList: List<Bitmap>,
    private val newList: List<Bitmap>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean = oldList[oldItemPosition] === newList[newItemPosition]

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean = oldList[oldItemPosition].sameAs(newList[newItemPosition])
}
