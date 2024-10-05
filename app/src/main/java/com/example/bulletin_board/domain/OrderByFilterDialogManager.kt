package com.example.bulletin_board.domain

import android.content.Context
import com.example.bulletin_board.R
import com.example.bulletin_board.dialogs.DialogSpinnerHelper
import com.example.bulletin_board.dialogs.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.utils.FilterUpdater
import com.example.bulletin_board.utils.SortUtils.getSortOption
import com.google.android.material.textfield.TextInputEditText

interface FilterReader {
    fun getFilterValue(key: String): String?
}

class OrderByFilterDialogManager(
    private val resourceStringProvider: ResourceStringProvider,
    private val filterUpdater: FilterUpdater,
    private val filterReader: FilterReader,
    private val dialog: DialogSpinnerHelper = DialogSpinnerHelper(),
) {
    fun setupOrderByFilter(
        context: Context,
        autoComplete: TextInputEditText,
    ) {
        autoComplete.setOnClickListener {
            val listVariant: ArrayList<Pair<String, String>> = getFilterOptions()
            val onItemSelectedListener =
                object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                    override fun onItemSelected(item: String) {
                        filterUpdater.addToFilter("orderBy", getSortOption(context, item))
                    }
                }

            dialog.showSpinnerPopup(
                context,
                autoComplete,
                listVariant,
                autoComplete,
                onItemSelectedListener,
                false,
            )
        }
    }

    private fun getFilterOptions(): ArrayList<Pair<String, String>> =
        if (filterReader.getFilterValue("price_from")?.isNotEmpty() == true ||
            filterReader.getFilterValue("price_to")?.isNotEmpty() == true
        ) {
            arrayListOf(
                Pair(resourceStringProvider.getStringImpl(R.string.sort_by_ascending_price), "single"),
                Pair(resourceStringProvider.getStringImpl(R.string.sort_by_descending_price), "single"),
            )
        } else {
            arrayListOf(
                Pair(resourceStringProvider.getStringImpl(R.string.sort_by_newest), "single"),
                Pair(resourceStringProvider.getStringImpl(R.string.sort_by_popularity), "single"),
                Pair(resourceStringProvider.getStringImpl(R.string.sort_by_ascending_price), "single"),
                Pair(resourceStringProvider.getStringImpl(R.string.sort_by_descending_price), "single"),
            )
        }
}
