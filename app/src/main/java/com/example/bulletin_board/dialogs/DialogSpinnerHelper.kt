package com.example.bulletin_board.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.R
import com.example.bulletin_board.utils.CityHelper

class DialogSpinnerHelper {

    fun showSpinnerDialog(context: Context, list: ArrayList<String>, tvSelection: TextView){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        val binding = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null)
        val adapter = RcViewDialogSpinnerAdapter(tvSelection, dialog)
        val rcView = binding.findViewById<RecyclerView>(R.id.recycler_view_spinner)
        val sv = binding.findViewById<SearchView>(R.id.search_view_spinner)
        rcView.layoutManager = LinearLayoutManager(context)
        rcView.adapter = adapter
        dialog.setView(binding)
        setSearchView(adapter, list, sv)
        adapter.updateAdapter(list)
        dialog.show()
    }

    private fun setSearchView(adapter: RcViewDialogSpinnerAdapter, list: ArrayList<String>, sv: SearchView?) {
        sv?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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