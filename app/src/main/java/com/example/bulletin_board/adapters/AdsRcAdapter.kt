package com.example.bulletin_board.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.act.EditAdsActivity
import com.example.bulletin_board.act.MainActivity
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.databinding.AdListItemBinding

class AdsRcAdapter(val act: MainActivity) : RecyclerView.Adapter<AdsRcAdapter.AdHolder>() {

    val adArray = ArrayList<Announcement>()

    class AdHolder(val binding: AdListItemBinding, val act: MainActivity) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(ad: Announcement) = with(binding) {
            textViewDescription.text = ad.description
            textViewPrice.text = ad.price
            textViewTitle.text = ad.title
            textViewViewCounter.text = ad.viewsCounter
            showEditPanel(isOwner(ad))
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
    }
}