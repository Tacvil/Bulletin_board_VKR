package com.example.bulletin_board.presentation.search

import com.example.bulletin_board.domain.search.InitSearchBarClickListenerHelper
import com.example.bulletin_board.domain.ui.search.SearchUi
import jakarta.inject.Inject

class SearchBarClickHandler
    @Inject
    constructor(
        private val searchUi: SearchUi,
    ) : InitSearchBarClickListenerHelper {
        override fun setupSearchBarClickListener() {
            searchUi.setSearchBarClickListener {
                searchUi.setQueryText(searchUi.getSearchBarText())
            }
        }
    }
