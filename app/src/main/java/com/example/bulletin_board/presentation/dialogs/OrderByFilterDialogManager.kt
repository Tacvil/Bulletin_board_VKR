package com.example.bulletin_board.presentation.dialogs

import com.example.bulletin_board.R
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.ORDER_BY_FIELD
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.PRICE_FROM_FIELD
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.PRICE_TO_FIELD
import com.example.bulletin_board.data.utils.SortUtils
import com.example.bulletin_board.domain.dialog.OrderByFilterDialog
import com.example.bulletin_board.domain.filter.FilterReader
import com.example.bulletin_board.domain.search.FilterUpdater
import com.example.bulletin_board.domain.utils.ResourceStringProvider
import com.example.bulletin_board.presentation.adapter.RcViewDialogSpinnerAdapter
import com.example.bulletin_board.presentation.adapter.RcViewDialogSpinnerAdapter.Companion.SINGLE
import com.google.android.material.textfield.TextInputEditText
import jakarta.inject.Inject

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
                    Pair(resourceStringProvider.getStringImpl(R.string.sort_by_ascending_price), SINGLE),
                    Pair(resourceStringProvider.getStringImpl(R.string.sort_by_descending_price), SINGLE),
                )
            } else {
                arrayListOf(
                    Pair(resourceStringProvider.getStringImpl(R.string.sort_by_newest), SINGLE),
                    Pair(resourceStringProvider.getStringImpl(R.string.sort_by_popularity), SINGLE),
                    Pair(resourceStringProvider.getStringImpl(R.string.sort_by_ascending_price), SINGLE),
                    Pair(resourceStringProvider.getStringImpl(R.string.sort_by_descending_price), SINGLE),
                )
            }
    }
