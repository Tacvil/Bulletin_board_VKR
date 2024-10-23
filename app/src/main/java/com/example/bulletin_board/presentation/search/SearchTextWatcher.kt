package com.example.bulletin_board.presentation.search

import android.text.Editable
import android.text.TextWatcher
import com.example.bulletin_board.domain.search.InitTextWatcher
import com.example.bulletin_board.domain.search.TextWatcherCallback
import com.example.bulletin_board.domain.ui.search.SearchUi
import jakarta.inject.Inject
import kotlin.text.isNotEmpty

class SearchTextWatcher
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
                    if (inputSearchQuery.isNotEmpty()) {
                        callback.onTextChanged(inputSearchQuery)
                    } else {
                        callback.clearSearchResults()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            }
    }
