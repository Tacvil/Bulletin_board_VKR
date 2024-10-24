package com.example.bulletin_board.presentation.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.CATEGORY_FIELD
import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.domain.model.AdUpdateEvent
import com.example.bulletin_board.domain.model.AppState
import com.example.bulletin_board.domain.model.FavData
import com.example.bulletin_board.domain.model.UseCases
import com.example.bulletin_board.domain.model.ViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val useCasesDataRetrieval: UseCases.DataRetrieval,
        private val useCasesDataUpdate: UseCases.DataUpdate,
        private val useCasesTokenManagement: UseCases.TokenManagement,
        private val useCasesFilters: UseCases.Filters,
        private val useCasesSearch: UseCases.Search,
        private val useCasesPriceFilters: UseCases.PriceFilters,
        private val useCasesImageManagement: UseCases.ImageManagement,
    ) : ViewModel() {
        companion object {
            const val PAGE_SIZE = 2
        }

        private val _appState = MutableStateFlow(AppState())
        val appState: StateFlow<AppState> = _appState.asStateFlow()

        @OptIn(ExperimentalCoroutinesApi::class)
        val homeAdsData: Flow<PagingData<Ad>> =
            appState
                .map { it.filter }
                .filter { it.isNotEmpty() }
                .flatMapLatest { currentFilters ->
                    useCasesDataRetrieval.getHomeAdsUseCase(currentFilters)
                }.cachedIn(viewModelScope)

        val favoriteAds: Flow<PagingData<Ad>> =
            useCasesDataRetrieval.getFavoriteAdsUseCase().cachedIn(viewModelScope)

        val myAds: Flow<PagingData<Ad>> =
            useCasesDataRetrieval.getMyAdsUseCase().cachedIn(viewModelScope)

        suspend fun onFavClick(favData: FavData) {
            when (val result = useCasesDataUpdate.updateFavoriteAdUseCase(favData)) {
                is Result.Success -> {
                    _appState.value =
                        _appState.value.copy(adEvent = AdUpdateEvent.FavUpdated(result.data))
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Error updating favorites: ${favData.key}")
                }
            }
        }

        suspend fun adViewed(viewData: ViewData) {
            when (val result = useCasesDataUpdate.adViewedUseCase(viewData)) {
                is Result.Success -> {
                    _appState.value =
                        _appState.value.copy(adEvent = AdUpdateEvent.ViewCountUpdated(result.data))
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Error updating views counter for ad: ${viewData.key}")
                }
            }
        }

        suspend fun insertAd(ad: Ad): Boolean =
            when (val result = useCasesDataUpdate.insertAdUseCase(ad)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Timber.e(result.exception, "Error inserting announcement: $ad")
                    false
                }
            }

        suspend fun deleteAd(adKey: String) {
            when (val result = useCasesDataUpdate.deleteAdUseCase(adKey)) {
                is Result.Success ->
                    _appState.value =
                        _appState.value.copy(adEvent = AdUpdateEvent.AdDeleted)

                is Result.Error -> {
                    Timber.e(result.exception, "Error deleting ad: $adKey")
                }
            }
        }

        suspend fun getMinMaxPrice() {
            val category = appState.value.filter[CATEGORY_FIELD]
            when (val result = useCasesPriceFilters.getMinMaxPriceUseCase(category)) {
                is Result.Success -> {
                    _appState.value = _appState.value.copy(minMaxPrice = result.data)
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Error getting minMax price")
                }
            }
        }

        suspend fun fetchSearchResults(inputSearchQuery: String) {
            when (val result = useCasesSearch.getSearchResultsUseCase(inputSearchQuery)) {
                is Result.Success -> {
                    _appState.value = _appState.value.copy(searchResults = result.data)
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Error fetching search results")
                    _appState.value = _appState.value.copy(searchResults = emptyList())
                }
            }
        }

        suspend fun saveTokenFCM(token: String) {
            when (val result = useCasesTokenManagement.saveTokenUseCase(token)) {
                is Result.Success -> {
                    Timber.d("Token successful saved")
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Error saving token")
                }
            }
        }

        suspend fun uploadImage(byteArray: ByteArray): Uri? {
            when (val result = useCasesImageManagement.uploadUserImageUseCase(byteArray)) {
                is Result.Success -> return result.data
                is Result.Error -> {
                    Timber.e(result.exception, "Error uploading image")
                    return null
                }
            }
        }

        suspend fun updateImage(
            byteArray: ByteArray,
            url: String,
        ): Uri? {
            when (val result = useCasesImageManagement.updateUserImageUseCase(byteArray, url)) {
                is Result.Success -> return result.data
                is Result.Error -> {
                    Timber.e(result.exception, "Error updating image")
                    return null
                }
            }
        }

        suspend fun deleteImageByUrl(oldUrl: String) {
            when (val result = useCasesImageManagement.deleteUserImageUseCase(oldUrl)) {
                is Result.Success -> Timber.d("Success delete image")
                is Result.Error -> {
                    Timber.e(result.exception, "Error delete image")
                }
            }
        }

        fun addToFilter(
            key: String,
            value: String,
        ) {
            _appState.value =
                _appState.value.copy(
                    filter = useCasesFilters.addToFilterUseCase(appState.value.filter, key, value),
                )
        }

        fun getFilterValue(key: String): String? = useCasesFilters.getFilterValueUseCase(appState.value.filter, key)

        fun updateFilters(newFilters: Map<String, String>) {
            _appState.update { currentState ->
                currentState.copy(
                    filter = useCasesFilters.updateFiltersUseCase(currentState.filter, newFilters),
                )
            }
        }

        fun removeFromFilter(key: String) {
            _appState.value =
                _appState.value.copy(
                    filter = useCasesFilters.removeFromFilterUseCase(appState.value.filter, key),
                )
        }

        fun clearFilters() {
            _appState.value = _appState.value.copy(filter = useCasesFilters.clearFiltersUseCase())
        }
    }
