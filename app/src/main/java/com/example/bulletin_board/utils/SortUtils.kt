package com.example.bulletin_board.utils

import android.content.Context
import com.example.bulletin_board.R
import com.example.bulletin_board.packroom.SortOption

object SortUtils {
    fun getSortOption(
        context: Context,
        item: String,
    ): String =
        when (item) {
            context.getString(R.string.sort_by_newest) -> SortOption.BY_NEWEST.id
            context.getString(R.string.sort_by_popularity) -> SortOption.BY_POPULARITY.id
            context.getString(R.string.sort_by_ascending_price) -> SortOption.BY_PRICE_ASC.id
            context.getString(R.string.sort_by_descending_price) -> SortOption.BY_PRICE_DESC.id
            context.getString(R.string.ad_car) -> SortOption.AD_CAR.id
            context.getString(R.string.ad_pc) -> SortOption.AD_PC.id
            context.getString(R.string.ad_smartphone) -> SortOption.AD_SMARTPHONE.id
            context.getString(R.string.ad_dm) -> SortOption.AD_DM.id
            else -> item
        }

    fun getSortOptionText(
        context: Context,
        sortOptionId: String,
    ): String =
        when (sortOptionId) {
            SortOption.BY_NEWEST.id -> context.getString(R.string.sort_by_newest)
            SortOption.BY_POPULARITY.id -> context.getString(R.string.sort_by_popularity)
            SortOption.BY_PRICE_ASC.id -> context.getString(R.string.sort_by_ascending_price)
            SortOption.BY_PRICE_DESC.id -> context.getString(R.string.sort_by_descending_price)
            else -> sortOptionId // Возвращаем исходное значение, если не найдено соответствие
        }
}
