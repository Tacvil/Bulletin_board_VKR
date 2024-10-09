package com.example.bulletin_board.domain

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import jakarta.inject.Inject
import timber.log.Timber
import kotlin.text.isNotEmpty

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

class TextWatcherHelper
    @Inject
    constructor(
        private val searchUi: SearchUi,
    ) : InitTextWatcher {
        override fun initTextWatcherHelperl(callback: TextWatcherCallback) {
            searchUi.addTextWatcher(createTextWatcher(callback))
        }

        private fun createTextWatcher(callback: TextWatcherCallback) =
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    val inputSearchQuery = s.toString().trimStart().replace(Regex("\\s{2,}"), " ")
                    Timber.tag("MActTextChanged").d("searchQuery = $inputSearchQuery, isEmpty = ${inputSearchQuery.isEmpty()}")

                    if (inputSearchQuery.isNotEmpty()) {
                        callback.onTextChanged(inputSearchQuery)
                    } else {
                        callback.clearSearchResults()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            }
    }
