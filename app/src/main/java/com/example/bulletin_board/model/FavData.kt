package com.example.bulletin_board.model

data class FavData(
    val favUids: List<String> = emptyList(),
    var favCounter: String = "0",
    var isFav: Boolean = false,
    var key: String = "",
)
