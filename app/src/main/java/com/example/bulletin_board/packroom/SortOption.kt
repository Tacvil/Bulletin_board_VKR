package com.example.bulletin_board.packroom

enum class SortOption(
    val id: String,
) {
    BY_NEWEST("byNewest"),
    BY_POPULARITY("byPopularity"),
    BY_PRICE_ASC("byPriceAsc"),
    BY_PRICE_DESC("byPriceDesc"),
    WITH_SEND("with_send"),
    WITHOUT_SEND("without_send"),
    AD_CAR("cars"),
    AD_PC("pc"),
    AD_SMARTPHONE("smartphones"),
    AD_DM("dm"),
}
