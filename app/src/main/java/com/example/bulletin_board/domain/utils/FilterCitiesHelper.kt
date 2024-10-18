package com.example.bulletin_board.domain.utils

import com.example.bulletin_board.R
import com.example.bulletin_board.presentation.adapter.RcViewDialogSpinnerAdapter.Companion.SINGLE
import jakarta.inject.Inject
import java.util.Locale

class FilterCitiesHelper
    @Inject
    constructor(
        private val stringProvider: ResourceStringProvider,
    ) {
        fun filterCities(
            list: ArrayList<Pair<String, String>>,
            searchText: String?,
        ): ArrayList<Pair<String, String>> {
            val tempList = ArrayList<Pair<String, String>>()
            tempList.clear()

            if (searchText == null) {
                tempList.add(Pair(stringProvider.getStringImpl(R.string.no_result), ""))
                return tempList
            }

            for (selection: Pair<String, String> in list) {
                if (selection.first.lowercase(Locale.ROOT).startsWith(searchText.lowercase(Locale.ROOT))) {
                    tempList.add(Pair(selection.first, SINGLE))
                }
            }
            if (tempList.size == 0) tempList.add(Pair(stringProvider.getStringImpl(R.string.no_result), ""))
            return tempList
        }
    }
