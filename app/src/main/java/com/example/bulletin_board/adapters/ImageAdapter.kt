package com.example.bulletin_board.adapters

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.R

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageHolder>() {

    val mainArray = ArrayList<Bitmap>()

    class ImageHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        lateinit var imItem : ImageView
        fun setData(bitmap: Bitmap){
            imItem = itemView.findViewById(R.id.image_view_item)
            imItem.setImageBitmap(bitmap)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageAdapter.ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_adapter_item, parent, false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: ImageAdapter.ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    fun update(newList: ArrayList<Bitmap>){
        mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }
}