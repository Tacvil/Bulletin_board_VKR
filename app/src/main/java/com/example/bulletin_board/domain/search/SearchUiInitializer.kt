package com.example.bulletin_board.domain.search

import com.example.bulletin_board.presentation.adapters.RcViewSearchSpinnerAdapter

interface SearchUiInitializer {
    fun initSearchAdapter(item: String)

    fun initRecyclerView(adapter: RcViewSearchSpinnerAdapter)
}
