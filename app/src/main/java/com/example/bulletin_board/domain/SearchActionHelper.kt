package com.example.bulletin_board.domain

import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.KEYWORDS_FIELD
import jakarta.inject.Inject

class SearchActionHelper
    @Inject
    constructor(
        private val searchUi: SearchUi,
        private val filterUpdater: FilterUpdater,
    ) : InitSearchActionHelper {
        override fun setupSearchActionListener() {
            searchUi.setSearchActionListener {
                val querySearch = searchUi.getQueryText().trim()
                if (querySearch.isNotEmpty()) {
                    searchUi.updateSearchBar(querySearch)
                    filterUpdater.addToFilter(KEYWORDS_FIELD, querySearch.split(" ").joinToString("-"))
                }
                searchUi.hideSearchView()
                false
            }
        }
    }
