package com.example.bulletin_board.domain

import android.widget.Toast
import com.example.bulletin_board.R
import com.example.bulletin_board.act.MainActivity
import com.example.bulletin_board.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.dialogs.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.utils.SortUtils.getSortOption
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import com.google.android.material.textfield.TextInputEditText

class OrderByFilterDialogManager(
    private val activity: MainActivity,
    private val viewModel: FirebaseViewModel,
    private val dialog: DialogSpinnerHelper = DialogSpinnerHelper(),
) {
    fun setupOrderByFilter(autoComplete: TextInputEditText) {
        autoComplete.setOnClickListener {
            val listVariant: ArrayList<Pair<String, String>> = getFilterOptions()
            val onItemSelectedListener =
                object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                    override fun onItemSelected(item: String) {
                        Toast.makeText(activity, "Item: $item", Toast.LENGTH_SHORT).show()
                        viewModel.addToFilter("orderBy", getSortOption(activity, item))
                    }
                }

            dialog.showSpinnerPopup(
                activity,
                autoComplete,
                listVariant,
                autoComplete,
                onItemSelectedListener,
                false,
            )
        }
    }

    private fun getFilterOptions(): ArrayList<Pair<String, String>> =
        if (viewModel.getFilterValue("price_from")?.isNotEmpty() == true ||
            viewModel.getFilterValue("price_to")?.isNotEmpty() == true
        ) {
            arrayListOf(
                Pair(activity.getStringAccountHelper(R.string.sort_by_ascending_price), "single"),
                Pair(activity.getStringAccountHelper(R.string.sort_by_descending_price), "single"),
            )
        } else {
            arrayListOf(
                Pair(activity.getStringAccountHelper(R.string.sort_by_newest), "single"),
                Pair(activity.getStringAccountHelper(R.string.sort_by_popularity), "single"),
                Pair(activity.getStringAccountHelper(R.string.sort_by_ascending_price), "single"),
                Pair(activity.getStringAccountHelper(R.string.sort_by_descending_price), "single"),
            )
        }
}
