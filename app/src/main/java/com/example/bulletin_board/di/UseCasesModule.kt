package com.example.bulletin_board.di

import com.example.bulletin_board.domain.model.UseCases
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
import com.example.bulletin_board.domain.useCases.search.GetSearchResultsUseCase
import com.example.bulletin_board.domain.useCases.tokenManagement.SaveTokenUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCasesModule {
    @Provides
    fun provideSearchUseCases(getSearchResultsUseCase: GetSearchResultsUseCase): UseCases.Search = UseCases.Search(getSearchResultsUseCase)

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
    fun providePriceFiltersUseCases(getMinMaxPriceUseCase: GetMinMaxPriceUseCase): UseCases.PriceFilters =
        UseCases.PriceFilters(getMinMaxPriceUseCase)

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

    @Provides
    fun provideImageManagementUseCases(
        uploadUserImageUseCase: UploadUserImageUseCase,
        updateUserImageUseCase: UpdateUserImageUseCase,
        deleteUserImageUseCase: DeleteUserImageUseCase,
    ): UseCases.ImageManagement = UseCases.ImageManagement(uploadUserImageUseCase, updateUserImageUseCase, deleteUserImageUseCase)
}
