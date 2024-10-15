package com.example.bulletin_board.domain.utils

import com.example.bulletin_board.presentation.adapter.RcViewDialogSpinnerAdapter.Companion.SINGLE
import java.util.Locale

object FilterCitiesHelper {
    fun filterCities(
        list: ArrayList<Pair<String, String>>,
        searchText: String?,
    ): ArrayList<Pair<String, String>> {
        val tempList = ArrayList<Pair<String, String>>()
        tempList.clear()

        if (searchText == null) {
            tempList.add(Pair("No result", ""))
            return tempList
        }

        for (selection: Pair<String, String> in list) {
            if (selection.first.lowercase(Locale.ROOT).startsWith(searchText.lowercase(Locale.ROOT))) {
                tempList.add(Pair(selection.first, SINGLE))
            }
        }
        if (tempList.size == 0) tempList.add(Pair("No result", ""))
        return tempList
    }
}
