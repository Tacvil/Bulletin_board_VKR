package com.example.bulletin_board.model

import com.example.bulletin_board.useCase.dataRetrieval.GetFavoriteAdsUseCase
import com.example.bulletin_board.useCase.dataRetrieval.GetHomeAdsUseCase
import com.example.bulletin_board.useCase.dataRetrieval.GetMyAdsUseCase
import com.example.bulletin_board.useCase.dataUpdate.AdViewedUseCase
import com.example.bulletin_board.useCase.dataUpdate.DeleteAdUseCase
import com.example.bulletin_board.useCase.dataUpdate.InsertAdUseCase
import com.example.bulletin_board.useCase.dataUpdate.UpdateFavoriteAdUseCase
import com.example.bulletin_board.useCase.filters.AddToFilterUseCase
import com.example.bulletin_board.useCase.filters.ClearFiltersUseCase
import com.example.bulletin_board.useCase.filters.GetFilterValueUseCase
import com.example.bulletin_board.useCase.filters.RemoveFromFilterUseCase
import com.example.bulletin_board.useCase.filters.UpdateFiltersUseCase
import com.example.bulletin_board.useCase.priceFilters.GetMaxPriceUseCase
import com.example.bulletin_board.useCase.priceFilters.GetMinPriceUseCase
import com.example.bulletin_board.useCase.search.FormatSearchResultsUseCase
import com.example.bulletin_board.useCase.search.GetSearchResultsUseCase
import com.example.bulletin_board.useCase.tokenManagement.SaveTokenUseCase

sealed class UseCases {
    data class Search(
        val getSearchResultsUseCase: GetSearchResultsUseCase,
        val formatSearchResultsUseCase: FormatSearchResultsUseCase,
    ) : UseCases()

    data class DataRetrieval(
        val getHomeAdsUseCase: GetHomeAdsUseCase,
        val getFavoriteAdsUseCase: GetFavoriteAdsUseCase,
        val getMyAdsUseCase: GetMyAdsUseCase,
    ) : UseCases()

    data class DataUpdate(
        val updateFavoriteAdUseCase: UpdateFavoriteAdUseCase,
        val adViewedUseCase: AdViewedUseCase,
        val insertAdUseCase: InsertAdUseCase,
        val deleteAdUseCase: DeleteAdUseCase,
    ) : UseCases()

    data class PriceFilters(
        val getMinPriceUseCase: GetMinPriceUseCase,
        val getMaxPriceUseCase: GetMaxPriceUseCase,
    ) : UseCases()

    data class TokenManagement(
        val saveTokenUseCase: SaveTokenUseCase,
    ) : UseCases()

    data class Filters(
        val addToFilterUseCase: AddToFilterUseCase,
        val getFilterValueUseCase: GetFilterValueUseCase,
        val updateFiltersUseCase: UpdateFiltersUseCase,
        val removeFromFilterUseCase: RemoveFromFilterUseCase,
        val clearFiltersUseCase: ClearFiltersUseCase,
    ) : UseCases()
}
