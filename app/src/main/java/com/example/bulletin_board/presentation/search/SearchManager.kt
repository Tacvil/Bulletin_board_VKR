package com.example.bulletin_board.presentation.search

import com.example.bulletin_board.domain.search.InitSearchActionHelper
import com.example.bulletin_board.domain.search.InitSearchBarClickListenerHelper
import com.example.bulletin_board.domain.search.InitTextWatcher
import com.example.bulletin_board.domain.search.SearchQueryHandler
import com.example.bulletin_board.domain.search.SearchQueryHandlerCallback
import com.example.bulletin_board.domain.search.SearchUiInitializer
import com.example.bulletin_board.domain.search.TextWatcherCallback
import com.example.bulletin_board.presentation.adapter.RcViewSearchSpinnerAdapter
import jakarta.inject.Inject

class SearchManager
    @Inject
    constructor(
        private val initTextWatcher: InitTextWatcher,
        private val initSearchActionHelper: InitSearchActionHelper,
        private val initSearchBarClickListenerHelper: InitSearchBarClickListenerHelper,
        private val searchUiInitializer: SearchUiInitializer,
        private val searchQueryHandler: SearchQueryHandler,
    ) {
        private lateinit var adapterSearch: RcViewSearchSpinnerAdapter

        fun initializeSearchFunctionality() {
            initSearchAdapter()
            initRecyclerView()
            setupSearchListeners()
        }

        private fun setupSearchListeners() {
            initTextWatcher.initTextWatcherHelperl(
                object : TextWatcherCallback {
                    override fun onTextChanged(inputSearchQuery: String) {
                        handleSearchQuery(inputSearchQuery)
                    }

                    override fun clearSearchResults() {
                        clearSearchResultsAdapter()
                    }
                },
            )
            initSearchActionHelper.setupSearchActionListener()
            initSearchBarClickListenerHelper.setupSearchBarClickListener()
        }

        fun handleSearchQuery(inputSearchQuery: String) {
            searchQueryHandler.handleSearchQuery(
                inputSearchQuery,
                object : SearchQueryHandlerCallback {
                    override fun onSearchResultsUpdated(results: List<Pair<String, String>>) {
                        adapterSearch.updateItems(results)
                    }
                },
            )
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

        fun clearSearchResultsAdapter() {
            adapterSearch.clearItems()
        }
    }
