package com.example.bulletin_board.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.R

class RcViewDialogSpinnerAdapter(
    private var targetTextView: TextView,
    var popupWindow: PopupWindow?,
    var onItemSelectedListener: OnItemSelectedListener? = null,
) : RecyclerView.Adapter<RcViewDialogSpinnerAdapter.SpinnerViewHolder>() {
    private val spinnerItems = ArrayList<Pair<String, String>>()

    interface OnItemSelectedListener {
        fun onItemSelected(item: String)
    }

    class SpinnerViewHolder(
        itemView: View,
        private var textViewSelection: TextView,
        var adapter: RcViewDialogSpinnerAdapter,
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private var itemTitle = ""

        fun bind(item: Pair<String, String>) {
            val spinnerTextView = itemView.findViewById<TextView>(R.id.text_view_spinner)
            val searchIcon = itemView.findViewById<ImageView>(R.id.image_view_search_icon)
            val linkIcon = itemView.findViewById<ImageView>(R.id.image_view_link_icon)
            spinnerTextView.text = item.first
            itemTitle = item.first

            if (SINGLE.equals(item.second, ignoreCase = true)) {
                searchIcon.visibility = View.GONE
            } else {
                searchIcon.visibility = View.GONE
                linkIcon.visibility = View.GONE
            }

            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            textViewSelection.text = itemTitle
            adapter.dismissPopupWindow()
            adapter.onItemSelectedListener?.onItemSelected(itemTitle)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SpinnerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.spinner_list_item, parent, false)
        return SpinnerViewHolder(view, targetTextView, this)
    }

    override fun getItemCount(): Int = spinnerItems.size

    override fun onBindViewHolder(
        holder: SpinnerViewHolder,
        position: Int,
    ) {
        holder.bind(spinnerItems[position])
    }

    fun updateItems(newList: ArrayList<Pair<String, String>>) {
        val diffCallback = SpinnerDiffUtilCallback(spinnerItems, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        spinnerItems.clear()
        spinnerItems.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun dismissPopupWindow() {
        popupWindow?.dismiss()
    }

    companion object {
        const val SINGLE = "single"
    }
}

class SpinnerDiffUtilCallback(
    private val oldList: List<Pair<String, String>>,
    private val newList: List<Pair<String, String>>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean = oldList[oldItemPosition].first == newList[newItemPosition].first

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean = oldList[oldItemPosition] == newList[newItemPosition]
}
