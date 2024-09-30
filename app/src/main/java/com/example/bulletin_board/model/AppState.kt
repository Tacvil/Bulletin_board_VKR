package com.example.bulletin_board.model

data class AppState(
    val adEvent: AdUpdateEvent? = null,
    val filter: MutableMap<String, String> = mutableMapOf(),
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val searchResults: List<String> = emptyList(),
)
