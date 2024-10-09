package com.example.bulletin_board.domain

import android.view.View
import android.widget.TextView
import com.example.bulletin_board.R
import com.example.bulletin_board.dialogs.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.ORDER_BY_FIELD
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.PRICE_FROM_FIELD
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.PRICE_TO_FIELD
import com.example.bulletin_board.utils.SortUtils
import com.google.android.material.textfield.TextInputEditText
import jakarta.inject.Inject

interface FilterReader {
    fun getFilterValue(key: String): String?
}

interface OrderByFilterDialog {
    fun showSpinnerPopup(
        anchorView: View,
        list: ArrayList<Pair<String, String>>,
        tvSelection: TextView,
        onItemSelectedListener: RcViewDialogSpinnerAdapter.OnItemSelectedListener?,
        isSearchable: Boolean,
    )
}

class OrderByFilterDialogManager
    @Inject
    constructor(
        private val resourceStringProvider: ResourceStringProvider,
        private val filterUpdater: FilterUpdater,
        private val filterReader: FilterReader,
        private val sortUtils: SortUtils,
        private val orderDialog: OrderByFilterDialog,
    ) {
        fun setupOrderByFilter(autoComplete: TextInputEditText) {
            autoComplete.setOnClickListener {
                val listVariant: ArrayList<Pair<String, String>> = getFilterOptions()
                val onItemSelectedListener =
                    object : RcViewDialogSpinnerAdapter.OnItemSelectedListener {
                        override fun onItemSelected(item: String) {
                            filterUpdater.addToFilter(ORDER_BY_FIELD, sortUtils.getSortOption(item))
                        }
                    }

                orderDialog.showSpinnerPopup(
                    autoComplete,
                    listVariant,
                    autoComplete,
                    onItemSelectedListener,
                    false,
                )
            }
        }

        private fun getFilterOptions(): ArrayList<Pair<String, String>> =
            if (filterReader.getFilterValue(PRICE_FROM_FIELD)?.isNotEmpty() == true ||
                filterReader.getFilterValue(PRICE_TO_FIELD)?.isNotEmpty() == true
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
