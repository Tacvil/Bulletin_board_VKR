package com.example.bulletin_board.fragments

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.databinding.SelectImageFragItemBinding
import com.example.bulletin_board.domain.ImageLoader
import com.example.bulletin_board.utils.AdapterCallback
import com.example.bulletin_board.utils.ItemTouchMoveCallback

interface GetSingleImagesHandler {
    fun getSingleImages(editImagePos: Int)
}

interface TitleProvider {
    fun getTitle(position: Int): String
}

interface ChooseScaleTypeHandler {
    fun chooseScaleType(bitmap: Bitmap): Boolean
}

class SelectImageRvAdapter(
    val adapterCallback: AdapterCallback,
    private val getSingleImagesHandler: GetSingleImagesHandler,
    private val titleProvider: TitleProvider,
    private val chooseScaleTypeHandler: ChooseScaleTypeHandler,
    private val imageLoader: ImageLoader,
) : RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(),
    ItemTouchMoveCallback.ItemTouchAdapter {
    val mainArray = ArrayList<Bitmap>()

    class ImageHolder(
        private val binding: SelectImageFragItemBinding,
        val adapter: SelectImageRvAdapter,
        private val getSingleImagesHandler: GetSingleImagesHandler,
        private val titleProvider: TitleProvider,
        private val chooseScaleTypeHandler: ChooseScaleTypeHandler,
        private val imageLoader: ImageLoader,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setData(bitMap: Bitmap) {
            binding.imageButtonEditImage.setOnClickListener {
                getSingleImagesHandler.getSingleImages(adapterPosition)
            }

            binding.imageButtonDelete.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                for (n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
                adapter.adapterCallback.onItemDelete()
            }

            binding.textViewTitle.text = titleProvider.getTitle(adapterPosition)

            val cropBool = chooseScaleTypeHandler.chooseScaleType(bitMap)

            if (cropBool) {
                val roundedCorners = RoundedCorners(20)
                val centerCrop = CenterCrop()
                val requestOptions = RequestOptions().transform(centerCrop, roundedCorners)

                imageLoader.loadImage(binding.imageViewContent, bitMap, requestOptions)
            } else {
                val roundedCorners = RoundedCorners(20)
                val fitCenter = FitCenter()
                val requestOptions = RequestOptions().transform(fitCenter, roundedCorners)
                imageLoader.loadImage(binding.imageViewContent, bitMap, requestOptions)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ImageHolder {
        val binding =
            SelectImageFragItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(binding, this, getSingleImagesHandler, titleProvider, chooseScaleTypeHandler, imageLoader)
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
        if (needClear) mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onMove(
        startPos: Int,
        targetPos: Int,
    ) {
        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        mainArray[startPos] = targetItem
        notifyItemMoved(startPos, targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }
}
