package com.example.bulletin_board.fragments

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.act.EditAdsActivity
import com.example.bulletin_board.databinding.SelectImageFragItemBinding
import com.example.bulletin_board.utils.AdapterCallback
import com.example.bulletin_board.utils.ImageManager
import com.example.bulletin_board.utils.ImagePicker
import com.example.bulletin_board.utils.ItemTouchMoveCallback

class SelectImageRvAdapter(val adapterCallback: AdapterCallback) :
    RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(),
    ItemTouchMoveCallback.ItemTouchAdapter {

    val mainArray = ArrayList<Bitmap>()

    class ImageHolder(
        private val binding: SelectImageFragItemBinding,
        val context: Context,
        val adapter: SelectImageRvAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(bitMap: Bitmap) {

            binding.imageButtonEditImage.setOnClickListener {
                ImagePicker.launcher(
                    context as EditAdsActivity,
                    context.launcherSingleSelectImage,
                    1
                )
                context.editImagePos = adapterPosition
            }

            binding.imageButtonDelete.setOnClickListener {

                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                for (n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
                adapter.adapterCallback.onItemDelete()
            }

            binding.textViewTitle.text =
                context.resources.getStringArray(R.array.title_array)[adapterPosition]

            val cropBool = ImageManager.chooseScaleType(binding.imageViewContent, bitMap)

            if (cropBool) {

                val roundedCorners = RoundedCorners(20)
                val centerCrop = CenterCrop()
                val requestOptions = RequestOptions().transform(centerCrop, roundedCorners)

                Glide.with(context)
                    .load(bitMap)
                    .apply(requestOptions)
                    .into(binding.imageViewContent)

            } else {
                val roundedCorners = RoundedCorners(20)
                val fitCenter = FitCenter()
                val requestOptions = RequestOptions().transform(fitCenter, roundedCorners)
                Glide.with(context)
                    .load(bitMap)
                    .apply(requestOptions)
                    .into(binding.imageViewContent)

            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val binding =
            SelectImageFragItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(binding, parent.context, this)
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean) {
        if (needClear) mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onMove(startPos: Int, targetPos: Int) {
        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        mainArray[startPos] = targetItem
        notifyItemMoved(startPos, targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }

}