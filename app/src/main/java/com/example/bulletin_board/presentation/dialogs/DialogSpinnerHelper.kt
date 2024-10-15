package com.example.bulletin_board.presentation.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletin_board.R
import com.example.bulletin_board.domain.utils.FilterCitiesHelper
import com.example.bulletin_board.presentation.adapter.RcViewDialogSpinnerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.search.SearchView
import com.google.android.material.search.SearchView.TransitionState

object DialogSpinnerHelper {
    fun showDialogSpinner(
        context: Context,
        anchorView: View,
        spinnerItems: ArrayList<Pair<String, String>>,
        targetTextView: TextView,
        onItemSelectedListener: RcViewDialogSpinnerAdapter.OnItemSelectedListener? = null,
        showSearchBar: Boolean,
    ) {
        val spinnerLayout = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null)
        val recyclerViewItems =
            spinnerLayout.findViewById<RecyclerView>(R.id.recycler_view_spinner_items).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = RcViewDialogSpinnerAdapter(targetTextView, null, onItemSelectedListener)
            }
        val recyclerViewSearchResults =
            spinnerLayout.findViewById<RecyclerView>(R.id.recycler_view_spinner_search_results).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = RcViewDialogSpinnerAdapter(targetTextView, null, onItemSelectedListener)
                visibility = View.GONE
            }

        val popupWindow =
            PopupWindow(
                spinnerLayout,
                if (showSearchBar) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                showSearchBar,
            ).apply {
                val margin = context.resources.getDimensionPixelSize(R.dimen.popup_offset_x)
                setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), margin, margin, margin, margin))
                isOutsideTouchable = true
                setOnDismissListener {
                    (recyclerViewItems.adapter as? RcViewDialogSpinnerAdapter)?.popupWindow = null
                    (recyclerViewSearchResults.adapter as? RcViewDialogSpinnerAdapter)?.popupWindow = null
                }
            }
        (recyclerViewItems.adapter as? RcViewDialogSpinnerAdapter)?.popupWindow = popupWindow
        (recyclerViewSearchResults.adapter as? RcViewDialogSpinnerAdapter)?.popupWindow = popupWindow

        if (showSearchBar) {
            val searchView = spinnerLayout.findViewById<SearchView>(R.id.search_view_spinner)
            val appBarLayout = spinnerLayout.findViewById<AppBarLayout>(R.id.app_bar_spinner)

            appBarLayout.visibility = View.VISIBLE
            searchView.visibility = View.VISIBLE

            searchView.editText.doOnTextChanged { text, _, _, _ ->
                val filteredList = FilterCitiesHelper.filterCities(spinnerItems, text.toString())

                (recyclerViewSearchResults.adapter as? RcViewDialogSpinnerAdapter)?.updateItems(filteredList)

                if (text.isNullOrEmpty()) {
                    recyclerViewItems.visibility = View.VISIBLE
                    recyclerViewSearchResults.visibility = View.GONE
                } else {
                    recyclerViewItems.visibility = View.GONE
                    recyclerViewSearchResults.visibility = View.VISIBLE
                }
            }

            searchView.addTransitionListener { _, _, newState ->
                if (newState == TransitionState.HIDDEN) {
                    (recyclerViewItems.adapter as? RcViewDialogSpinnerAdapter)?.updateItems(spinnerItems)
                    recyclerViewItems.visibility = View.VISIBLE
                    recyclerViewSearchResults.visibility = View.GONE
                }
            }
        }

        val yOffset = anchorView.resources.getDimensionPixelSize(R.dimen.popup_offset_y)
        popupWindow.showAsDropDown(anchorView, 0, yOffset)
        (recyclerViewItems.adapter as? RcViewDialogSpinnerAdapter)?.updateItems(spinnerItems)
    }
}
