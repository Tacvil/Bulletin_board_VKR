package com.example.bulletin_board.fragments

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.databinding.SelectImageFragItemBinding
import com.example.bulletin_board.domain.ImageLoader
import com.example.bulletin_board.utils.ItemTouchMoveCallback
import java.util.Collections

interface ImageAdapterHandler {
    fun getSingleImages(editImagePos: Int)

    fun getTitle(position: Int): String

    fun chooseScaleType(bitmap: Bitmap): Boolean
}

class SelectImageRvAdapter(
    private val onItemDelete: () -> Unit,
    private val imageAdapterHandler: ImageAdapterHandler,
    private val imageLoader: ImageLoader,
) : RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(),
    ItemTouchMoveCallback.ItemTouchAdapter {
    val mainArray = ArrayList<Bitmap>()

    class ImageHolder(
        private val binding: SelectImageFragItemBinding,
        val adapter: SelectImageRvAdapter,
        private val imageAdapterHandler: ImageAdapterHandler,
        private val imageLoader: ImageLoader,
        private val onItemDelete: () -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setData(bitMap: Bitmap) {
            binding.imageButtonEditImage.setOnClickListener {
                imageAdapterHandler.getSingleImages(absoluteAdapterPosition)
            }

            binding.imageButtonDelete.setOnClickListener {
                adapter.mainArray.removeAt(absoluteAdapterPosition)
                adapter.notifyItemRemoved(absoluteAdapterPosition)
                adapter.notifyItemRangeChanged(absoluteAdapterPosition, adapter.mainArray.size - absoluteAdapterPosition)
                onItemDelete()
            }

            binding.textViewTitle.text = imageAdapterHandler.getTitle(absoluteAdapterPosition)

            val requestOptions =
                RequestOptions().transform(
                    if (imageAdapterHandler.chooseScaleType(bitMap)) CenterCrop() else FitCenter(),
                    RoundedCorners(20),
                )
            imageLoader.loadImage(binding.imageViewContent, bitMap, requestOptions)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ImageHolder {
        val binding =
            SelectImageFragItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(binding, this, imageAdapterHandler, imageLoader, onItemDelete)
    }

    override fun getItemCount(): Int = mainArray.size

    override fun onBindViewHolder(
        holder: ImageHolder,
        position: Int,
    ) {
        holder.setData(mainArray[position])
    }

    fun updateAdapter(
        newList: List<Bitmap>,
        needClear: Boolean,
    ) {
        val oldList = mainArray.toList()
        if (needClear) mainArray.clear()
        mainArray.addAll(newList)

        val diffResult = DiffUtil.calculateDiff(BitmapDiffCallback(oldList, mainArray))
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onMove(
        startPos: Int,
        targetPos: Int,
    ) {
        Collections.swap(mainArray, startPos, targetPos)
        notifyItemMoved(startPos, targetPos)
    }

    override fun onClear() {
        notifyItemRangeChanged(0, mainArray.size)
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
    ): Boolean = oldList[oldItemPosition] == newList[newItemPosition]

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean = oldList[oldItemPosition] == newList[newItemPosition]
}
