package com.example.bulletin_board.domain

import com.example.bulletin_board.dialogs.RcViewSearchSpinnerAdapter
import com.example.bulletin_board.utils.ClearSearchResults
import com.example.bulletin_board.utils.SearchQueryHandler
import jakarta.inject.Inject

interface InitSearchAdd {
    fun initSearchAddImpl()
}

interface SearchUiInitializer {
    fun initSearchAdapter(item: String)

    fun initRecyclerView(adapter: RcViewSearchSpinnerAdapter)
}

interface SearchAdapterUpdateCallback {
    fun onAdapterUpdated(results: List<Pair<String, String>>)
}

interface SearchAdapterUpdater {
    fun updateAdapter(
        query: String,
        callback: SearchAdapterUpdateCallback,
    )
}

class SearchManager
    @Inject
    constructor(
        private val initSearchAdd: InitSearchAdd,
        private val searchUiInitializer: SearchUiInitializer,
        private val searchQueryHandler: SearchAdapterUpdater,
    ) : SearchQueryHandler,
        ClearSearchResults {
        private lateinit var adapterSearch: RcViewSearchSpinnerAdapter

        init {
            initSearchAdapter()
            initRecyclerView()
        }

        fun initSearchAdd() {
            initSearchAdd.initSearchAddImpl()
        }

        private fun initSearchAdapter() {
            val onItemSelectedListener =
                RcViewSearchSpinnerAdapter.OnItemSelectedListener { item ->
                    searchUiInitializer.initSearchAdapter(item)
                }
            adapterSearch = RcViewSearchSpinnerAdapter(onItemSelectedListener)
        }

        private fun initRecyclerView() {
            searchUiInitializer.initRecyclerView(adapterSearch)
        }

        override fun clearSearchResults() {
            adapterSearch.clearAdapter()
        }

        override fun handleSearchQuery(query: String) {
            searchQueryHandler.updateAdapter(
                query,
                object : SearchAdapterUpdateCallback {
                    override fun onAdapterUpdated(results: List<Pair<String, String>>) {
                        adapterSearch.updateAdapter(results)
                    }
                },
            )
        }
    }
