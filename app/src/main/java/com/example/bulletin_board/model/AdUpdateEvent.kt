package com.example.bulletin_board.model

sealed class AdUpdateEvent {
    data object Initial : AdUpdateEvent()

    data class FavUpdated(
        val favData: FavData,
    ) : AdUpdateEvent()

    data class ViewCountUpdated(
        val viewData: ViewData,
    ) : AdUpdateEvent()

    data object AdDeleted : AdUpdateEvent()
}
