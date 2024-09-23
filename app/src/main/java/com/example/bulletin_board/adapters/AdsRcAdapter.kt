package com.example.bulletin_board.adapters

import android.animation.Animator
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.act.EditAdsActivity
import com.example.bulletin_board.act.MainActivity
import com.example.bulletin_board.databinding.AdListItemBinding
import com.example.bulletin_board.model.Ad
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdsRcAdapter(
    val act: MainActivity,
) : RecyclerView.Adapter<AdsRcAdapter.AdHolder>() {
    val adArray = ArrayList<Ad>()
    private var timeFormatter: SimpleDateFormat? = null

    init {
        timeFormatter = SimpleDateFormat("dd/MM/yyyy - hh:mm", Locale.getDefault())
    }

    class AdHolder(
        val binding: AdListItemBinding,
        val act: MainActivity,
        val formatter: SimpleDateFormat,
        val adArray: ArrayList<Ad>,
    ) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun setData(ad: Ad) =
            with(binding) {
                textViewDescription.setText(ad.description)
                textViewPrice.text = ad.price.toString()
                textViewTitleD.setText(ad.title)
                textViewViewCounter.text = ad.viewsCounter.toString()
                textViewFav.text = ad.favCounter
                Log.d("ADSRCADAPTER", "favCounter = ${ad.favCounter} | uids = ${ad.favUids}")
                imageButtonFav1.isClickable = true
                val publishTimeLabel = act.getString(R.string.publication_time)
                val publishTime = "$publishTimeLabel: ${getTimeFromMillis(ad.time)}"
                textViewData.text = publishTime
                // val roundedCorners = RoundedCorners(20)
                // val centerCrop = CenterCrop()
                // val requestOptions = RequestOptions().transform(centerCrop, roundedCorners)

                Glide
                    .with(binding.root)
                    .load(ad.mainImage)
                    .apply(RequestOptions().transform(RoundedCorners(20)))
                    .into(imageViewMainImage)

                // Picasso.get().load(ad.mainImage).into(imageViewMainImage)

//            if (ad.isFav) imageButtonFav.setImageResource(R.drawable.ic_favorite_pressed) else imageButtonFav.setImageResource(R.drawable.ic_favorite_normal)

                showEditPanel(isOwner(ad))
                if (ad.isFav) {
                    imageButtonFav1.pauseAnimation()
                    imageButtonFav1.cancelAnimation()
                    imageButtonFav1.setMinAndMaxProgress(0.38f, 0.38f)
                } else {
                    imageButtonFav1.pauseAnimation()
                    imageButtonFav1.cancelAnimation()
                    imageButtonFav1.setMinAndMaxProgress(0.87f, 0.87f)
                }
                imageButtonFav1.setOnClickListener {
                    // Обработка клика
                    if (!ad.isFav) {
                        imageButtonFav1.pauseAnimation()
                        imageButtonFav1.cancelAnimation()
                        imageButtonFav1.setMinAndMaxProgress(0.0f, 0.38f)
                        imageButtonFav1.speed = 1.5f
                    } else {
                        imageButtonFav1.pauseAnimation()
                        imageButtonFav1.cancelAnimation()
                        imageButtonFav1.setMinAndMaxProgress(0.6f, 0.87f)
                        imageButtonFav1.speed = 1.5f
                    }
                    // (act.application as MyApplication).isAnimationRunning = true
                    imageButtonFav1.addAnimatorListener(
                        object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                imageButtonFav1.isClickable = false
                                // Можно добавить логику при начале анимации, если необходимо
                            }

                            override fun onAnimationEnd(animation: Animator) {
                                // Вызов метода по завершению анимации
                                // if(act.mAuth.currentUser?.isAnonymous == false)act.onFavClicked(ad)
                                // act.onFavClicked(ad)
                                // act.onFavClicked(ad, adArray)
                                // (act.application as MyApplication).isAnimationRunning = false
                                // Удаление слушателя, чтобы избежать многократного вызова
                                imageButtonFav1.removeAnimatorListener(this)
                            }

                            override fun onAnimationCancel(animation: Animator) {
                                // (act.application as MyApplication).isAnimationRunning = false
                                // Можно добавить логику при отмене анимации, если необходимо
                            }

                            override fun onAnimationRepeat(animation: Animator) {
                                // Можно добавить логику при повторении анимации, если необходимо
                            }
                        },
                    )
                    imageButtonFav1.playAnimation()
                }
                itemView.setOnClickListener {
                    // act.onAdViewed(ad)
                }
                imageButtonEditAd.setOnClickListener(onClickEdit(ad))
                imageButtonDeleteAd.setOnClickListener {
                    // act.onDeleteItem(ad)
                }
            }

        private fun getTimeFromMillis(timeMillis: String): String {
            val c = Calendar.getInstance()
            c.timeInMillis = timeMillis.toLong()
            return formatter.format(c.time)
        }

        private fun onClickEdit(ad: Ad): View.OnClickListener =
            View.OnClickListener {
                val editIntent =
                    Intent(act, EditAdsActivity::class.java).apply {
                        putExtra(MainActivity.EDIT_STATE, true)
                        putExtra(MainActivity.ADS_DATA, ad)
                    }
                act.startActivity(editIntent)
            }

        private fun isOwner(ad: Ad): Boolean = ad.uid == act.mAuth.uid

        private fun showEditPanel(isOwner: Boolean) {
            if (isOwner) {
                binding.editPanel.visibility = View.VISIBLE
            } else {
                binding.editPanel.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AdHolder {
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, act, timeFormatter!!, adArray)
    }

    override fun getItemCount(): Int = adArray.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: AdHolder,
        position: Int,
    ) {
        holder.setData(adArray[position])
    }

    fun updateAdapter(newList: List<Ad>) {
        val tempArray = ArrayList<Ad>()
        tempArray.addAll(adArray)
        tempArray.addAll(newList)

        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, tempArray))
        diffResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(tempArray)
    }

    fun updateAdapterWithClear(newList: List<Ad>) {
        Log.d("adapter", "newList = $newList")
        val tempArray = ArrayList<Ad>()
        tempArray.addAll(newList)
/*        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, tempArray))
        diffResult.dispatchUpdatesTo(this)*/
        adArray.clear()
        adArray.addAll(tempArray)
        Log.d("adapter", "adArray = $adArray")
        notifyDataSetChanged()
    }
}
