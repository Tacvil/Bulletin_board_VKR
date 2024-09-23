package com.example.bulletin_board.model

sealed class AppEvent {
    sealed class AdUpdateEvent : AppEvent()Х
    data class FilterUpdateEvent(val filters: MutableMap<String, String>) : AppEvent()
}