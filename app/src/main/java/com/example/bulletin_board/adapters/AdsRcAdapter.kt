package com.example.bulletin_board.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.model.Announcement
import com.example.bulletin_board.databinding.AdListItemBinding
import com.google.firebase.auth.FirebaseAuth

class AdsRcAdapter(val auth: FirebaseAuth) : RecyclerView.Adapter<AdsRcAdapter.AdHolder>() {

    val adArray = ArrayList<Announcement>()

    class AdHolder(val binding: AdListItemBinding, val auth: FirebaseAuth): RecyclerView.ViewHolder(binding.root){

        fun setData(ad: Announcement){

            binding.apply {
                textViewDescription.text = ad.description
                textViewPrice.text = ad.price
                textViewTitle.text = ad.title
            }
            showEditPanel(isOwner(ad))
        }

        private fun isOwner(ad: Announcement): Boolean{
            return ad.uid == auth.uid
        }

        private fun showEditPanel(isOwner: Boolean){
            if (isOwner){
                binding.editPanel.visibility = View.VISIBLE
            }else{
                binding.editPanel.visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, auth)
    }

    override fun getItemCount(): Int {
        return adArray.size
    }

    override fun onBindViewHolder(holder: AdHolder, position: Int) {
        holder.setData(adArray[position])
    }

    fun updateAdapter(newList: List<Announcement>){
        adArray.clear()
        adArray.addAll(newList)
        notifyDataSetChanged()
    }

}