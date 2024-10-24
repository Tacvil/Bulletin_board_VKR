package com.example.bulletin_board.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.R
import com.example.bulletin_board.presentation.utils.ItemDiffCallback

class RcViewSearchSpinnerAdapter(
    var onItemSelectedListener: OnItemSelectedListener? = null,
) : RecyclerView.Adapter<RcViewSearchSpinnerAdapter.SearchSpinnerViewHolder>() {
    private val spinnerItems = ArrayList<Pair<String, String>>()

    fun interface OnItemSelectedListener {
        fun onItemSelected(item: String)
    }

    class SearchSpinnerViewHolder(
        itemView: View,
        var adapter: RcViewSearchSpinnerAdapter,
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private var itemTitle = ""

        fun bind(item: Pair<String, String>) {
            val spinnerTextView = itemView.findViewById<TextView>(R.id.text_view_spinner)
            val searchIcon = itemView.findViewById<ImageView>(R.id.image_view_search_icon)
            val linkIcon = itemView.findViewById<ImageView>(R.id.image_view_link_icon)
            spinnerTextView.text = item.first
            itemTitle = item.first

            if (!SEARCH.equals(item.second, ignoreCase = true)) {
                linkIcon.visibility = View.GONE
                searchIcon.visibility = View.GONE
            }

            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            adapter.onItemSelectedListener?.onItemSelected(itemTitle)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SearchSpinnerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_spinner_list_search, parent, false)
        return SearchSpinnerViewHolder(view, this)
    }

    override fun getItemCount(): Int = spinnerItems.size

    override fun onBindViewHolder(
        holder: SearchSpinnerViewHolder,
        position: Int,
    ) {
        holder.bind(spinnerItems[position])
    }

    fun updateItems(newList: List<Pair<String, String>>) {
        val diffCallback = ItemDiffCallback(spinnerItems, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        spinnerItems.clear()
        spinnerItems.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun clearItems() {
        val diffCallback = ItemDiffCallback(spinnerItems, emptyList())
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        spinnerItems.clear()
        diffResult.dispatchUpdatesTo(this)
    }

    companion object {
        const val SEARCH = "search"
    }
}
