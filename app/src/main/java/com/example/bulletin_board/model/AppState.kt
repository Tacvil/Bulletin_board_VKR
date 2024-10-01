package com.example.bulletin_board.model

data class AppState(
    val adEvent: AdUpdateEvent? = null,
    val filter: MutableMap<String, String> = mutableMapOf(),
    val minMaxPrice: Pair<Int?, Int?>? = null,
    val searchResults: List<String> = emptyList(),
)
