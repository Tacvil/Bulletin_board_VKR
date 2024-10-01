package com.example.bulletin_board.di

import com.example.bulletin_board.model.UseCases
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
import com.example.bulletin_board.useCase.priceFilters.GetMinMaxPriceUseCase
import com.example.bulletin_board.useCase.search.FormatSearchResultsUseCase
import com.example.bulletin_board.useCase.search.GetSearchResultsUseCase
import com.example.bulletin_board.useCase.tokenManagement.SaveTokenUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCasesModule {
    @Provides
    fun provideSearchUseCases(
        getSearchResultsUseCase: GetSearchResultsUseCase,
        formatSearchResultsUseCase: FormatSearchResultsUseCase,
    ): UseCases.Search = UseCases.Search(getSearchResultsUseCase, formatSearchResultsUseCase)

    @Provides
    fun provideDataRetrievalUseCases(
        getHomeAdsUseCase: GetHomeAdsUseCase,
        getFavoriteAdsUseCase: GetFavoriteAdsUseCase,
        getMyAdsUseCase: GetMyAdsUseCase,
    ): UseCases.DataRetrieval = UseCases.DataRetrieval(getHomeAdsUseCase, getFavoriteAdsUseCase, getMyAdsUseCase)

    @Provides
    fun provideDataUpdateUseCases(
        updateFavoriteAdUseCase: UpdateFavoriteAdUseCase,
        adViewedUseCase: AdViewedUseCase,
        insertAdUseCase: InsertAdUseCase,
        deleteAdUseCase: DeleteAdUseCase,
    ): UseCases.DataUpdate = UseCases.DataUpdate(updateFavoriteAdUseCase, adViewedUseCase, insertAdUseCase, deleteAdUseCase)

    @Provides
    fun providePriceFiltersUseCases(
        getMinMaxPriceUseCase: GetMinMaxPriceUseCase,
    ): UseCases.PriceFilters = UseCases.PriceFilters(getMinMaxPriceUseCase)

    @Provides
    fun provideTokenManagementUseCases(saveTokenUseCase: SaveTokenUseCase): UseCases.TokenManagement =
        UseCases.TokenManagement(saveTokenUseCase)

    @Provides
    fun provideFiltersUseCases(
        addToFilterUseCase: AddToFilterUseCase,
        getFilterValueUseCase: GetFilterValueUseCase,
        updateFiltersUseCase: UpdateFiltersUseCase,
        removeFromFilterUseCase: RemoveFromFilterUseCase,
        clearFiltersUseCase: ClearFiltersUseCase,
    ): UseCases.Filters =
        UseCases.Filters(addToFilterUseCase, getFilterValueUseCase, updateFiltersUseCase, removeFromFilterUseCase, clearFiltersUseCase)
}
