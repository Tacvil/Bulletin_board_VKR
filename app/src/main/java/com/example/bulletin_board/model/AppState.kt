package com.example.bulletin_board.model

data class AppState(
    val adEvent: AdUpdateEvent = AdUpdateEvent.Initial,
    val filter: MutableMap<String, String> = mutableMapOf(),
)
