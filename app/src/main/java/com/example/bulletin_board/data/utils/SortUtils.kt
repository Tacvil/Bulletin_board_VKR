package com.example.bulletin_board.data.utils

import com.example.bulletin_board.R
import com.example.bulletin_board.domain.utils.ResourceStringProvider
import jakarta.inject.Inject

class SortUtils
@Inject
constructor(
    private val resourceStringProvider: ResourceStringProvider,
) {
    fun getSortOption(item: String): String =
        when (item) {
            resourceStringProvider.getStringImpl(R.string.sort_by_newest) -> SortOption.BY_NEWEST.id
            resourceStringProvider.getStringImpl(R.string.sort_by_popularity) -> SortOption.BY_POPULARITY.id
            resourceStringProvider.getStringImpl(R.string.sort_by_ascending_price) -> SortOption.BY_PRICE_ASC.id
            resourceStringProvider.getStringImpl(R.string.sort_by_descending_price) -> SortOption.BY_PRICE_DESC.id
            else -> item
        }

    fun getSortOptionText(sortOptionId: String): String =
        when (sortOptionId) {
            SortOption.BY_NEWEST.id -> resourceStringProvider.getStringImpl(R.string.sort_by_newest)
            SortOption.BY_POPULARITY.id -> resourceStringProvider.getStringImpl(R.string.sort_by_popularity)
            SortOption.BY_PRICE_ASC.id -> resourceStringProvider.getStringImpl(R.string.sort_by_ascending_price)
            SortOption.BY_PRICE_DESC.id -> resourceStringProvider.getStringImpl(R.string.sort_by_descending_price)
            else -> sortOptionId
        }

    fun getCategory(item: String): String =
        when (item) {
            resourceStringProvider.getStringImpl(R.string.ad_car) -> SortOption.AD_CAR.id
            resourceStringProvider.getStringImpl(R.string.ad_pc) -> SortOption.AD_PC.id
            resourceStringProvider.getStringImpl(R.string.ad_smartphone) -> SortOption.AD_SMARTPHONE.id
            resourceStringProvider.getStringImpl(R.string.ad_dm) -> SortOption.AD_DM.id
            else -> item
        }

    fun getCategoryFromSortOption(item: String): String =
        when (item) {
            SortOption.AD_CAR.id -> resourceStringProvider.getStringImpl(R.string.ad_car)
            SortOption.AD_PC.id -> resourceStringProvider.getStringImpl(R.string.ad_pc)
            SortOption.AD_SMARTPHONE.id -> resourceStringProvider.getStringImpl(R.string.ad_smartphone)
            SortOption.AD_DM.id -> resourceStringProvider.getStringImpl(R.string.ad_dm)
            else -> item
        }

    fun getSendOption(item: String): String =
        when (item) {
            resourceStringProvider.getStringImpl(R.string.no_matter) -> SortOption.DELIVERY_UNSPECIFIED.id
            resourceStringProvider.getStringImpl(R.string.with_sending) -> SortOption.WITH_SEND.id
            resourceStringProvider.getStringImpl(R.string.without_sending) -> SortOption.WITHOUT_SEND.id
            else -> item
        }

    fun getSendOptionFromSortOption(item: String): String =
        when (item) {
            SortOption.DELIVERY_UNSPECIFIED.id -> resourceStringProvider.getStringImpl(R.string.no_matter)
            SortOption.WITH_SEND.id -> resourceStringProvider.getStringImpl(R.string.with_sending)
            SortOption.WITHOUT_SEND.id -> resourceStringProvider.getStringImpl(R.string.without_sending)
            else -> item
        }

    fun getCategoryFromItem(itemId: Int): String =
        when (itemId) {
            R.id.id_car -> SortOption.AD_CAR.id
            R.id.id_pc -> SortOption.AD_PC.id
            R.id.id_smartphone -> SortOption.AD_SMARTPHONE.id
            R.id.id_dm -> SortOption.AD_DM.id
            else -> ""
        }
}
