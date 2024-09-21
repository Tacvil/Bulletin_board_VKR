package com.example.bulletin_board.model

sealed class AdUpdateEvent {
    data class FavUpdated(
        val favData: FavData,
        val position: Int,
    ) : AdUpdateEvent()

    data class ViewCountUpdated(
        val viewData: ViewData,
        val position: Int,
    ) : AdUpdateEvent()
}
