package com.example.bulletin_board.domain.model

import com.example.bulletin_board.domain.useCases.dataRetrieval.GetFavoriteAdsUseCase
import com.example.bulletin_board.domain.useCases.dataRetrieval.GetHomeAdsUseCase
import com.example.bulletin_board.domain.useCases.dataRetrieval.GetMyAdsUseCase
import com.example.bulletin_board.domain.useCases.dataUpdate.AdViewedUseCase
import com.example.bulletin_board.domain.useCases.dataUpdate.DeleteAdUseCase
import com.example.bulletin_board.domain.useCases.dataUpdate.InsertAdUseCase
import com.example.bulletin_board.domain.useCases.dataUpdate.UpdateFavoriteAdUseCase
import com.example.bulletin_board.domain.useCases.filters.AddToFilterUseCase
import com.example.bulletin_board.domain.useCases.filters.ClearFiltersUseCase
import com.example.bulletin_board.domain.useCases.filters.GetFilterValueUseCase
import com.example.bulletin_board.domain.useCases.filters.RemoveFromFilterUseCase
import com.example.bulletin_board.domain.useCases.filters.UpdateFiltersUseCase
import com.example.bulletin_board.domain.useCases.imageManagement.DeleteUserImageUseCase
import com.example.bulletin_board.domain.useCases.imageManagement.UpdateUserImageUseCase
import com.example.bulletin_board.domain.useCases.imageManagement.UploadUserImageUseCase
import com.example.bulletin_board.domain.useCases.priceFilters.GetMinMaxPriceUseCase
import com.example.bulletin_board.domain.useCases.search.FormatSearchResultsUseCase
import com.example.bulletin_board.domain.useCases.search.GetSearchResultsUseCase
import com.example.bulletin_board.domain.useCases.tokenManagement.SaveTokenUseCase

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
        val getMinMaxPriceUseCase: GetMinMaxPriceUseCase,
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

    data class ImageManagement(
        val uploadUserImageUseCase: UploadUserImageUseCase,
        val updateUserImageUseCase: UpdateUserImageUseCase,
        val deleteUserImageUseCase: DeleteUserImageUseCase,
    ) : UseCases()
}
