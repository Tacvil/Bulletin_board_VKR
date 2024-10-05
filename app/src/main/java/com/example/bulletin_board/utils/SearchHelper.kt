package com.example.bulletin_board.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.example.bulletin_board.domain.InitSearchAdd
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.KEYWORDS_FIELD
import jakarta.inject.Inject
import timber.log.Timber
import kotlin.text.isNotEmpty
import kotlin.text.trim

interface ClearSearchResults {
    fun clearSearchResults()
}

interface SearchUi {
    fun updateSearchBar(query: String)

    fun hideSearchView()

    fun addTextWatcher(textWatcher: TextWatcher)

    fun setSearchActionListener(listener: () -> Boolean)

    fun setSearchBarClickListener(listener: View.OnClickListener)

    fun getQueryText(): String

    fun setQueryText(text: String)

    fun getSearchBarText(): String
}

interface FilterUpdater {
    fun addToFilter(
        key: String,
        value: String,
    )
}

interface SearchQueryHandler {
    fun handleSearchQuery(query: String)
}

class SearchHelper
    @Inject
    constructor(
        private val filterUpdater: FilterUpdater,
        private val searchQueryHandler: SearchQueryHandler,
        private val searchUi: SearchUi,
        private val clearSearchResults: ClearSearchResults,
    ) : InitSearchAdd {
        override fun initSearchAddImpl() {
            searchUi.addTextWatcher(createTextWatcher())
            setupSearchActionListener()
            setupSearchBarClickListener()
        }

        private fun createTextWatcher() =
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    val inputSearchQuery = s.toString().trimStart().replace(Regex("\\s{2,}"), " ")
                    Timber.tag("MActTextChanged").d("searchQuery = $inputSearchQuery, isEmpty = ${inputSearchQuery.isEmpty()}")

                    if (inputSearchQuery.isNotEmpty()) {
                        Timber.tag("MActTextChanged").d("searchQueryAfterValid = $inputSearchQuery")
                        searchQueryHandler.handleSearchQuery(inputSearchQuery)
                    } else {
                        clearSearchResults.clearSearchResults()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            }

        private fun setupSearchActionListener() {
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

        private fun setupSearchBarClickListener() {
            searchUi.setSearchBarClickListener {
                searchUi.setQueryText(searchUi.getSearchBarText())
            }
        }
    }
