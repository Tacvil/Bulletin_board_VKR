package com.example.bulletin_board.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.KEYWORDS_FIELD
import timber.log.Timber
import kotlin.text.isNotEmpty
import kotlin.text.trim

interface SearchUi {
    fun clearSearchResults()

    fun updateSearchBar(query: String)

    fun hideSearchView()

    fun addTextWatcher(textWatcher: TextWatcher)

    fun setSearchActionListener(listener: () -> Boolean)

    fun setSearchBarClickListener(listener: View.OnClickListener)

    fun getQueryText(): String

    fun setQueryText(text: String)

    fun getSearchBarText(): String
}

interface SearchActions {
    fun addToFilter(
        key: String,
        value: String,
    )

    fun handleSearchQuery(query: String) // Добавляем функцию
}

class SearchHelper(
    private val searchActions: SearchActions,
    private val searchUi: SearchUi,
) {
    fun initSearchAdd() {
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
                    searchActions.handleSearchQuery(inputSearchQuery)
                } else {
                    searchUi.clearSearchResults()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

    private fun setupSearchActionListener() {
        searchUi.setSearchActionListener {
            val querySearch = searchUi.getQueryText().trim()
            if (querySearch.isNotEmpty()) {
                searchUi.updateSearchBar(querySearch)
                searchActions.addToFilter(KEYWORDS_FIELD, querySearch.split(" ").joinToString("-"))
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
