package com.example.bulletin_board.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.R
import com.example.bulletin_board.utils.CityHelper
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.search.SearchView
import com.google.android.material.search.SearchView.TransitionState

object DialogSpinnerHelper {
    fun showSpinnerPopup(
        context: Context,
        anchorView: View,
        list: ArrayList<Pair<String, String>>,
        tvSelection: TextView,
        onItemSelectedListener: RcViewDialogSpinnerAdapter.OnItemSelectedListener? = null,
        isSearchable: Boolean,
    ) {
        val binding = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null)

        if (isSearchable) {
            val searchView =
                binding.findViewById<com.google.android.material.search.SearchView>(R.id.search_view_spinner)
            val appBarL = binding.findViewById<AppBarLayout>(R.id.appBarLayout_spinner)

            appBarL.visibility = View.VISIBLE
            searchView.visibility = View.VISIBLE

            val popupWindow =
                PopupWindow(
                    binding,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true,
                )

            val marginInPixels = context.resources.getDimensionPixelSize(R.dimen.popup_offset_x)
            val inset =
                InsetDrawable(
                    ColorDrawable(Color.TRANSPARENT),
                    marginInPixels,
                    marginInPixels,
                    marginInPixels,
                    marginInPixels,
                )
            popupWindow.setBackgroundDrawable(inset)

            val adapter =
                RcViewDialogSpinnerAdapter(tvSelection, popupWindow, onItemSelectedListener)
            val rcView = binding.findViewById<RecyclerView>(R.id.recycler_view_spinner)
            val rcView1 = binding.findViewById<RecyclerView>(R.id.recycler_view_spinner1)

            rcView.layoutManager = LinearLayoutManager(context)
            rcView.adapter = adapter

            rcView1.layoutManager = LinearLayoutManager(context)
            rcView1.adapter = adapter

            popupWindow.isOutsideTouchable = true

            setSearchView(adapter, list, searchView)

            searchView.addTransitionListener {
                    _: com.google.android.material.search.SearchView?,
                    _: TransitionState?,
                    newState: TransitionState,
                ->
                if (newState == TransitionState.HIDDEN) {
                    adapter.updateAdapter(list)
                }
            }

            val yOffset = anchorView.resources.getDimensionPixelSize(R.dimen.popup_offset_y)
            val xOffset = 0

            popupWindow.showAsDropDown(anchorView, xOffset, yOffset)

            adapter.updateAdapter(list)
        } else {
            val popupWindow =
                PopupWindow(
                    binding,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    false,
                )

            val adapter = RcViewDialogSpinnerAdapter(tvSelection, popupWindow, onItemSelectedListener)
            val rcView1 = binding.findViewById<RecyclerView>(R.id.recycler_view_spinner1)

            rcView1.layoutManager = LinearLayoutManager(context)
            rcView1.adapter = adapter

            popupWindow.isOutsideTouchable = true

            val yOffset = anchorView.resources.getDimensionPixelSize(R.dimen.popup_offset_y)
            val xOffset = 0

            popupWindow.showAsDropDown(anchorView, xOffset, yOffset)

            adapter.updateAdapter(list)
        }
    }

    private fun setSearchView(
        adapter: RcViewDialogSpinnerAdapter,
        list: ArrayList<Pair<String, String>>,
        sv: SearchView,
    ) {
        sv.editText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    val tempList = CityHelper.filterListData(list, s.toString())
                    Log.d("Dialog", "tempList: $s.toString()")
                    adapter.updateAdapter(tempList)
                }

                override fun afterTextChanged(s: Editable?) {
                }
            },
        )
    }
}
