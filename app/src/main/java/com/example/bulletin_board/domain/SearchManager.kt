package com.example.bulletin_board.domain

import com.example.bulletin_board.dialogs.RcViewSearchSpinnerAdapter
import jakarta.inject.Inject
import timber.log.Timber

interface InitTextWatcher {
    fun initTextWatcherHelperl(callback: TextWatcherCallback)
}

interface TextWatcherCallback {
    fun onTextChanged(inputSearchQuery: String)

    fun clearSearchResults()
}

interface InitSearchActionHelper {
    fun setupSearchActionListener()
}

interface InitSearchBarClickListenerHelper {
    fun setupSearchBarClickListener()
}

interface SearchUiInitializer {
    fun initSearchAdapter(item: String)

    fun initRecyclerView(adapter: RcViewSearchSpinnerAdapter)
}

interface SearchQueryHandler {
    fun handleSearchQuery(
        inputSearchQuery: String,
        callback: SearchQueryHandlerCallback,
    )
}

interface SearchQueryHandlerCallback {
    fun onSearchResultsUpdated(results: List<Pair<String, String>>)
}

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
                        Timber.d("onSearchResultsUpdated() called with: results = $results")
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
