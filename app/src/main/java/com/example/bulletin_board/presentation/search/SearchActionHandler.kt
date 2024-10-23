package com.example.bulletin_board.presentation.search

import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.KEYWORDS_FIELD
import com.example.bulletin_board.domain.search.FilterUpdater
import com.example.bulletin_board.domain.search.InitSearchActionHelper
import com.example.bulletin_board.domain.ui.search.SearchUi
import jakarta.inject.Inject

class SearchActionHandler
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
                    filterUpdater.addToFilter(KEYWORDS_FIELD, querySearch.split(" ").joinToString("-").lowercase())
                }
                searchUi.hideSearchView()
                false
            }
        }
    }
