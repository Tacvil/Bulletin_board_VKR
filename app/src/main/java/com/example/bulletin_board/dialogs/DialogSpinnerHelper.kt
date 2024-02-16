package com.example.bulletin_board.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.R
import com.example.bulletin_board.utils.CityHelper

class DialogSpinnerHelper {

    fun showSpinnerPopup(context: Context, anchorView: View, list: ArrayList<String>, tvSelection: TextView, onItemSelectedListener: RcViewDialogSpinnerAdapter.OnItemSelectedListener? = null) {
        val binding = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null)
        val popupWindow = PopupWindow(
            binding,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        val adapter = RcViewDialogSpinnerAdapter(tvSelection, popupWindow, onItemSelectedListener)
        val rcView = binding.findViewById<RecyclerView>(R.id.recycler_view_spinner)
        //val sv = binding.findViewById<SearchView>(R.id.search_view_spinner)
        rcView.layoutManager = LinearLayoutManager(context)
        rcView.adapter = adapter

        popupWindow.isOutsideTouchable = true

        //setSearchView(adapter, list, sv)

        anchorView.setOnClickListener {
            popupWindow.showAsDropDown(anchorView)
        }

        adapter.updateAdapter(list)
    }

    private fun setSearchView(adapter: RcViewDialogSpinnerAdapter, list: ArrayList<String>, sv: SearchView?) {
        sv?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tempList = CityHelper.filterListData(list, newText)
                adapter.updateAdapter(tempList)
                return true
            }
        })
    }
}