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
import jakarta.inject.Inject
import java.util.Collections

interface OnItemDeleteListener {
    fun onItemDelete()
}

interface ImageAdapterHandler {
    fun getSingleImages(editImagePos: Int)

    fun getTitle(position: Int): String

    fun chooseScaleType(bitmap: Bitmap): Boolean
}

class SelectImageRvAdapter
    @Inject
    constructor(
        private val onItemDeleteListener: OnItemDeleteListener,
        private val imageAdapterHandler: ImageAdapterHandler,
        private val imageLoader: ImageLoader,
    ) : RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(),
        ItemTouchMoveCallback.ItemTouchAdapter {
        val selectedImages = ArrayList<Bitmap>()

        class ImageHolder(
            private val binding: SelectImageFragItemBinding,
            val adapter: SelectImageRvAdapter,
            private val imageAdapterHandler: ImageAdapterHandler,
            private val imageLoader: ImageLoader,
            private val onItemDeleteListener: OnItemDeleteListener,
        ) : RecyclerView.ViewHolder(binding.root) {
            fun setData(bitMap: Bitmap) {
                binding.imageButtonEditImage.setOnClickListener {
                    imageAdapterHandler.getSingleImages(absoluteAdapterPosition)
                }

                binding.imageButtonDelete.setOnClickListener {
                    adapter.selectedImages.removeAt(absoluteAdapterPosition)
                    adapter.notifyItemRemoved(absoluteAdapterPosition)
                    adapter.notifyItemRangeChanged(absoluteAdapterPosition, adapter.selectedImages.size - absoluteAdapterPosition)
                    onItemDeleteListener.onItemDelete()
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
            return ImageHolder(binding, this, imageAdapterHandler, imageLoader, onItemDeleteListener)
        }

        override fun getItemCount(): Int = selectedImages.size

        override fun onBindViewHolder(
            holder: ImageHolder,
            position: Int,
        ) {
            holder.setData(selectedImages[position])
        }

        fun updateSelectedImages(
            newList: List<Bitmap>,
            needClear: Boolean,
        ) {
            val oldList = selectedImages.toList()
            if (needClear) selectedImages.clear()
            selectedImages.addAll(newList)

            val diffResult = DiffUtil.calculateDiff(BitmapDiffCallback(oldList, selectedImages))
            diffResult.dispatchUpdatesTo(this)
        }

        override fun onMove(
            startPos: Int,
            targetPos: Int,
        ) {
            Collections.swap(selectedImages, startPos, targetPos)
            notifyItemMoved(startPos, targetPos)
        }

        override fun onClear() {
            notifyItemRangeChanged(0, selectedImages.size)
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
