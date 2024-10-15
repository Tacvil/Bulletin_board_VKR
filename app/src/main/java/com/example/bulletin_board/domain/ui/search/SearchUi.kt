package com.example.bulletin_board.domain.ui.search

import android.text.TextWatcher
import android.view.View

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
