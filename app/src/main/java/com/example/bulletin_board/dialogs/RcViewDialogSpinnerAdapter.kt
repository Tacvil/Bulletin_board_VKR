package com.example.bulletin_board.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.R

class RcViewDialogSpinnerAdapter(
    var tvSelection: TextView,
    var popupWindow: PopupWindow?,
    var onItemSelectedListener: OnItemSelectedListener? = null
) : RecyclerView.Adapter<RcViewDialogSpinnerAdapter.SpViewHolder>() {

    private val mainList = ArrayList<String>()

    interface OnItemSelectedListener {
        fun onItemSelected(item: String)
    }

    class SpViewHolder(
        itemView: View,
        var tvSelection: TextView,
        var adapter: RcViewDialogSpinnerAdapter
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var itemText = ""

        fun setData(text: String) {
            val tvSpItem = itemView.findViewById<TextView>(R.id.text_view_spinner)
            tvSpItem.text = text
            itemText = text
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            tvSelection.text = itemText
            adapter.dismissDialog()  // Вызываем метод для закрытия диалога
            adapter.onItemSelectedListener?.onItemSelected(itemText)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.spinner_list_item, parent, false)
        return SpViewHolder(view, tvSelection, this)
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {
        holder.setData(mainList[position])
    }

    fun updateAdapter(list: ArrayList<String>) {
        mainList.clear()
        mainList.addAll(list)
        notifyDataSetChanged()
    }

    fun dismissDialog() {
        popupWindow?.dismiss()
    }
}
