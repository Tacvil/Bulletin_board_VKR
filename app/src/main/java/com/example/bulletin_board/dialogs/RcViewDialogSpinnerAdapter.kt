package com.example.bulletin_board.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.R

class RcViewDialogSpinnerAdapter(
    var tvSelection: TextView,
    var popupWindow: PopupWindow?,
    var onItemSelectedListener: OnItemSelectedListener? = null,
) : RecyclerView.Adapter<RcViewDialogSpinnerAdapter.SpViewHolder>() {
    private val mainList = ArrayList<Pair<String, String>>()

    interface OnItemSelectedListener {
        fun onItemSelected(item: String)
    }

    class SpViewHolder(
        itemView: View,
        var tvSelection: TextView,
        var adapter: RcViewDialogSpinnerAdapter,
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private var itemText = ""

        fun setData(item: Pair<String, String>) {
            val tvSpItem = itemView.findViewById<TextView>(R.id.text_view_spinner)
            val imView1 = itemView.findViewById<ImageView>(R.id.image_view1)
            val imView2 = itemView.findViewById<ImageView>(R.id.image_view2)
            tvSpItem.text = item.first
            itemText = item.first

            if ("single".equals(item.second, ignoreCase = true)) {
                // Ваш код для выполнения действий, если условие соответствует
                imView2.visibility = View.GONE
            } else {
                imView2.visibility = View.GONE
                imView1.visibility = View.GONE
            }

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            tvSelection.text = itemText
            adapter.dismissDialog()
            adapter.onItemSelectedListener?.onItemSelected(itemText)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SpViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.spinner_list_item, parent, false)
        return SpViewHolder(view, tvSelection, this)
    }

    override fun getItemCount(): Int = mainList.size

    override fun onBindViewHolder(
        holder: SpViewHolder,
        position: Int,
    ) {
        holder.setData(mainList[position])
    }

    fun updateAdapter(list: ArrayList<Pair<String, String>>) {
        mainList.clear()
        mainList.addAll(list)
        notifyDataSetChanged()
    }

    fun dismissDialog() {
        popupWindow?.dismiss()
    }
}
