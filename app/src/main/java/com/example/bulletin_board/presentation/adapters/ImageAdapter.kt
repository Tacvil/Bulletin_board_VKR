package com.example.bulletin_board.presentation.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.R
import com.example.bulletin_board.presentation.utils.ItemDiffCallback
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
            val diffResult = DiffUtil.calculateDiff(ItemDiffCallback(imageBitmapList, newList))
            imageBitmapList.clear()
            imageBitmapList.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }
    }
