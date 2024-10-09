package com.example.bulletin_board.domain

import jakarta.inject.Inject

class SearchBarClickListenerHelper
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
