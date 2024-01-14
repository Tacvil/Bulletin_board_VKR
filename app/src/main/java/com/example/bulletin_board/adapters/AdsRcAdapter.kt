package com.example.bulletin_board.adapters

import android.animation.Animator
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bulletin_board.R
import com.example.bulletin_board.act.EditAdsActivity
import com.example.bulletin_board.act.MainActivity
import com.example.bulletin_board.application.MyApplication
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.databinding.AdListItemBinding
import com.squareup.picasso.Picasso

class AdsRcAdapter(val act: MainActivity) : RecyclerView.Adapter<AdsRcAdapter.AdHolder>() {

    val adArray = ArrayList<Announcement>()

    class AdHolder(val binding: AdListItemBinding, val act: MainActivity) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(ad: Announcement) = with(binding) {
            textViewDescription.text = ad.description
            textViewPrice.text = ad.price
            textViewTitle.text = ad.title
            textViewViewCounter.text = ad.viewsCounter
            textViewFav.text = ad.favCounter

            val roundedCorners = RoundedCorners(20)
            val centerCrop = CenterCrop()
            val requestOptions = RequestOptions().transform(centerCrop, roundedCorners)

            Glide.with(binding.root)
                .load(ad.mainImage)
                .apply(requestOptions)
                .into(imageViewMainImage)

            //Picasso.get().load(ad.mainImage).into(imageViewMainImage)

//            if (ad.isFav) imageButtonFav.setImageResource(R.drawable.ic_favorite_pressed) else imageButtonFav.setImageResource(R.drawable.ic_favorite_normal)
            if (ad.isFav){
                imageButtonFav1.setMinAndMaxProgress(0.5f, 0.5f)
                imageButtonFav1.playAnimation()
            } else {
                imageButtonFav1.setMinAndMaxProgress(1.0f, 1.0f)
                imageButtonFav1.playAnimation()
            }

            showEditPanel(isOwner(ad))

            imageButtonFav1.setOnClickListener {
                // Обработка клика
                if (!ad.isFav){
                    imageButtonFav1.setMinAndMaxProgress(0.0f, 0.38f)
                    imageButtonFav1.speed = 1.5f
                } else {
                    imageButtonFav1.setMinAndMaxProgress(0.6f, 0.87f)
                    imageButtonFav1.speed = 1.5f
                }

                imageButtonFav1.playAnimation()

                //(act.application as MyApplication).isAnimationRunning = true

                imageButtonFav1.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        // Можно добавить логику при начале анимации, если необходимо
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        // Вызов метода по завершению анимации
                        //if(act.mAuth.currentUser?.isAnonymous == false)act.onFavClicked(ad)
                        act.onFavClicked(ad)
                       // (act.application as MyApplication).isAnimationRunning = false
                        // Удаление слушателя, чтобы избежать многократного вызова
                        imageButtonFav1.removeAnimatorListener(this)
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        //(act.application as MyApplication).isAnimationRunning = false
                        // Можно добавить логику при отмене анимации, если необходимо
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                        // Можно добавить логику при повторении анимации, если необходимо
                    }
                })
            }
            itemView.setOnClickListener {
                act.onAdViewed(ad)
            }
            imageButtonEditAd.setOnClickListener(onClickEdit(ad))
            imageButtonDeleteAd.setOnClickListener {
                act.onDeleteItem(ad)
            }
        }

        private fun onClickEdit(ad: Announcement): View.OnClickListener{
            return View.OnClickListener {
                val editIntent = Intent(act, EditAdsActivity::class.java).apply {
                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ADS_DATA, ad)
                }
                act.startActivity(editIntent)
            }
        }

        private fun isOwner(ad: Announcement): Boolean {
            return ad.uid == act.mAuth.uid
        }

        private fun showEditPanel(isOwner: Boolean) {
            if (isOwner) {
                binding.editPanel.visibility = View.VISIBLE
            } else {
                binding.editPanel.visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, act)
    }

    override fun getItemCount(): Int {
        return adArray.size
    }

    override fun onBindViewHolder(holder: AdHolder, position: Int) {
        holder.setData(adArray[position])
    }

    fun updateAdapter(newList: List<Announcement>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, newList))
        diffResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(newList)
    }

    interface Listener{
        fun onDeleteItem(ad: Announcement)
        fun onAdViewed(ad: Announcement)
        fun onFavClicked(ad: Announcement)
    }
}