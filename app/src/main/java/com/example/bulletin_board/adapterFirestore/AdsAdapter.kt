package com.example.bulletin_board.adapterFirestore

import android.animation.Animator
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingDataAdapter
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
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

class AdsAdapter(
    private val viewModel: FirebaseViewModel,
    private val auth: FirebaseAuth,
) : PagingDataAdapter<Ad, AdsAdapter.AdHolder>(AdDiffCallback) {
    // Map для быстрого доступа к объявлениям по ключу
    private val adMap = mutableMapOf<String, Ad>()

    init {
        viewModel.viewModelScope.launch {
            viewModel.adUpdated.collectLatest { updatedAd ->
                updateAd(updatedAd)
            }
        }
    }

    fun updateAd(updatedAd: Ad) {
        adMap[updatedAd.key]?.let { ad ->
            ad.isFav = updatedAd.isFav
            ad.favCounter = updatedAd.favCounter
            val snapshot = snapshot()
            val index = snapshot.indexOfFirst { it?.key == updatedAd.key }
            if (index != -1) {
                notifyItemChanged(index)
            }
        }
    }

    class AdHolder(
        val binding: AdListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val formatter = SimpleDateFormat("dd/MM/yyyy - hh:mm", Locale.getDefault())

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(
            ad: Ad,
            auth: FirebaseAuth,
            viewModel: FirebaseViewModel,
        ) = with(binding) {
            textViewDescription.setText(ad.description)
            textViewPrice.text = ad.price.toString()
            textViewTitleD.setText(ad.title)
            textViewViewCounter.text = ad.viewsCounter.toString()
            textViewFav.text = ad.favCounter
            Timber
                .tag("ADSRCADAPTER")
                .d("ifFav = " + ad.isFav + "favCounter = " + ad.favCounter + " | uids = " + ad.favUids)
            imageButtonFav1.isClickable = true
            val publishTimeLabel = binding.root.context.getString(R.string.publication_time)
            val publishTime = "$publishTimeLabel: ${getTimeFromMillis(ad.time)}"
            textViewData.text = publishTime

            Glide
                .with(binding.root)
                .load(ad.mainImage)
                .apply(RequestOptions().transform(RoundedCorners(20)))
                .into(imageViewMainImage)

            showEditPanel(isOwner(ad, auth))
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

                imageButtonFav1.addAnimatorListener(
                    object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            imageButtonFav1.isClickable = false
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            viewModel.viewModelScope.launch {
                                viewModel.onFavClick(ad)
                            }
                            imageButtonFav1.removeAnimatorListener(this)
                        }

                        override fun onAnimationCancel(animation: Animator) {}

                        override fun onAnimationRepeat(animation: Animator) {}
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
                Intent(binding.root.context, EditAdsActivity::class.java).also {
                    it.putExtra(MainActivity.EDIT_STATE, true)
                    it.putExtra(MainActivity.ADS_DATA, ad)
                    binding.root.context.startActivity(it)
                }
            }

        private fun isOwner(
            ad: Ad,
            auth: FirebaseAuth,
        ): Boolean = ad.uid == auth.uid

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
        return AdHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: AdHolder,
        position: Int,
    ) {
        val ad = getItem(position)
        if (ad != null) {
            adMap[ad.key] = ad
            holder.bind(ad, auth, viewModel)
        }
    }
}

object AdDiffCallback : DiffUtil.ItemCallback<Ad>() {
    override fun areItemsTheSame(
        oldItem: Ad,
        newItem: Ad,
    ): Boolean = oldItem.key == newItem.key

    override fun areContentsTheSame(
        oldItem: Ad,
        newItem: Ad,
    ): Boolean = oldItem == newItem
    /*    @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: DocumentSnapshot,
            newItem: DocumentSnapshot,
        ): Boolean {
            val oldData = oldItem.data
            val newData = newItem.data

            if (oldData.isNullOrEmpty() || newData.isNullOrEmpty()) return false

            if (oldData.keys != newData.keys) return false

            for (key in oldData.keys) {
                val oldValue = oldData[key]
                val newValue = newData[key]

                if (oldValue == null && newValue == null) continue
                if (oldValue == null || newValue == null) return false
                if (oldValue::class != newValue::class) return false

                when (oldValue) {
                    is String -> if (oldValue != newValue as String) return false
                    is Int -> if (oldValue != newValue as Int) return false
                    is Boolean -> if (oldValue != newValue as Boolean) return false
                    is List<*> -> if (!areListsEqual(oldValue, newValue as List<*>)) return false
                    is Long -> if (oldValue != newValue as Long) return false // Добавлено для Long
                    // Добавьте другие типы данных из вашего дата-класса Ad (Double, Float, etc.)
                    else -> if (oldValue != newValue) return false
                }
            }

            return true
        }

        private fun areListsEqual(
            list1: List<*>,
            list2: List<*>,
        ): Boolean {
            if (list1.size != list2.size) return false
            for (i in list1.indices) {
                if (list1[i] != list2[i]) return false
            }
            return true
        }*/
}
