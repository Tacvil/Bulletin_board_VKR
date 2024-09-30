package com.example.bulletin_board.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.launch
import androidx.lifecycle.viewModelScope
import com.example.bulletin_board.viewmodel.FirebaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.text.isNotEmpty
import kotlin.text.split
import kotlin.text.trim

interface SearchUi {
    fun updateSearchResults(results: List<Pair<String, String>>)

    fun clearSearchResults()

    fun updateSearchBar(query: String)

    fun hideSearchView()

    fun addTextWatcher(textWatcher: TextWatcher)

    fun setSearchActionListener(listener: () -> Boolean)

    fun setSearchBarClickListener(listener: View.OnClickListener)

    fun setToolbarClickListener(listener: View.OnClickListener)

    fun getQueryText(): String

    fun setQueryText(text: String)

    fun getSearchBarText(): String
}

class SearchHelper(
    private val viewModel: FirebaseViewModel,
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
                var inputSearchQuery = s.toString()
                Timber.tag("MActTextChanged").d("searchQuery = $inputSearchQuery, isEmpty = ${inputSearchQuery.isEmpty()}")

                if (inputSearchQuery.isNotEmpty()) {
                    inputSearchQuery = inputSearchQuery.trimStart().replace(Regex("\\s{2,}"), " ")
                    Timber.tag("MActTextChanged").d("searchQueryAfterValid = $inputSearchQuery")
                    handleSearchQuery(inputSearchQuery)
                } else {
                    searchUi.clearSearchResults()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

    private fun handleSearchQuery(query: String) {
        if (query.isEmpty()) {
            searchUi.clearSearchResults()
            return
        }

        Timber.d("searchQuery = $query")
        viewModel.viewModelScope.launch {
            viewModel.fetchSearchResults(query)
            viewModel.appState.collectLatest { appState ->
                if (appState.searchResults.isNotEmpty()) {
                    val formattedResults = viewModel.formatSearchResults(appState.searchResults, query)
                    searchUi.updateSearchResults(formattedResults)
                }
            }
        }
    }

    private fun setupSearchActionListener() {
        searchUi.setSearchActionListener {
            val querySearch = searchUi.getQueryText().trim()
            if (querySearch.isNotEmpty()) {
                searchUi.updateSearchBar(querySearch)
                viewModel.addToFilter("keyWords", validateQuery(querySearch))
            }
            searchUi.hideSearchView()
            false
        }
    }

    private fun validateQuery(query: String): String =
        query.split(" ").joinToString("-").also {
            Timber.d("validateData = $it")
        }

    private fun setupSearchBarClickListener() {
        searchUi.setSearchBarClickListener {
            searchUi.setQueryText(searchUi.getSearchBarText())
        }
    }
}
