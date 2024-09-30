package com.example.bulletin_board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.AdUpdateEvent
import com.example.bulletin_board.model.AppState
import com.example.bulletin_board.model.FavData
import com.example.bulletin_board.model.UseCases
import com.example.bulletin_board.model.ViewData
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.CATEGORY_FIELD
import com.example.bulletin_board.packroom.Result
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
class FirebaseViewModel
    @Inject
    constructor(
        private val useCases: UseCases, // переделать
    ) : ViewModel() {
        init {
            Timber.d("ViewModel created: $this")
        }

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
                    when (useCases) {
                        is UseCases.DataRetrieval -> useCases.getHomeAdsUseCase(currentFilters)
                        else -> throw IllegalStateException("UseCases should be of type DataRetrieval")
                    }
                }.cachedIn(viewModelScope)

        val favoriteAds: Flow<PagingData<Ad>> =
            when (useCases) {
                is UseCases.DataRetrieval -> useCases.getFavoriteAdsUseCase().cachedIn(viewModelScope)
                else -> throw IllegalStateException("UseCases should be of type DataRetrieval")
            }

        val myAds: Flow<PagingData<Ad>> =
            when (useCases) {
                is UseCases.DataRetrieval -> useCases.getMyAdsUseCase().cachedIn(viewModelScope)
                else -> throw IllegalStateException("UseCases should be of type DataRetrieval")
            }

        suspend fun onFavClick(favData: FavData) {
            when (useCases) {
                is UseCases.DataUpdate -> {
                    when (val result = useCases.updateFavoriteAdUseCase(favData)) {
                        is Result.Success -> {
                            _appState.value =
                                _appState.value.copy(adEvent = AdUpdateEvent.FavUpdated(result.data))
                        }

                        is Result.Error -> {
                            Timber.e(result.exception, "Error updating favorites: ${favData.key}")
                        }
                    }
                }

                else -> throw IllegalStateException("UseCases should be of type DataUpdate")
            }
        }

        suspend fun adViewed(viewData: ViewData) {
            when (useCases) {
                is UseCases.DataUpdate -> {
                    when (val result = useCases.adViewedUseCase(viewData)) {
                        is Result.Success -> {
                            _appState.value =
                                _appState.value.copy(adEvent = AdUpdateEvent.ViewCountUpdated(result.data))
                        }

                        is Result.Error -> {
                            Timber.e(
                                result.exception,
                                "Error updating views counter for ad: ${viewData.key}",
                            )
                        }
                    }
                }

                else -> throw IllegalStateException("UseCases should be of type DataUpdate")
            }
        }

        suspend fun insertAd(ad: Ad): Boolean =
            when (useCases) {
                is UseCases.DataUpdate -> useCases.insertAdUseCase(ad)
                else -> throw IllegalStateException("UseCases should be of type DataUpdate")
            }

        suspend fun deleteAd(adKey: String) {
            when (useCases) {
                is UseCases.DataUpdate -> {
                    useCases.deleteAdUseCase(adKey)
                    _appState.value = _appState.value.copy(adEvent = AdUpdateEvent.AdDeleted)
                }

                else -> throw IllegalStateException("UseCases should be of type DataUpdate")
            }
        }

        suspend fun getMinPrice() {
            val category = appState.value.filter[CATEGORY_FIELD]
            when (useCases) {
                is UseCases.PriceFilters -> {
                    when (val result = useCases.getMinPriceUseCase(category)) {
                        is Result.Success -> {
                            _appState.value = _appState.value.copy(minPrice = result.data)
                        }

                        is Result.Error -> {
                            Timber.e(result.exception, "Error getting min price")
                        }
                    }
                }

                else -> throw IllegalStateException("UseCases should be of type PriceFilters")
            }
        }

        suspend fun getMaxPrice() {
            val category = appState.value.filter[CATEGORY_FIELD]
            when (useCases) {
                is UseCases.PriceFilters -> {
                    when (val result = useCases.getMaxPriceUseCase(category)) {
                        is Result.Success -> {
                            _appState.value = _appState.value.copy(maxPrice = result.data)
                        }

                        is Result.Error -> {
                            Timber.e(result.exception, "Error getting max price")
                        }
                    }
                }

                else -> throw IllegalStateException("UseCases should be of type PriceFilters")
            }
        }

        suspend fun fetchSearchResults(inputSearchQuery: String) {
            when (useCases) {
                is UseCases.Search -> {
                    when (val result = useCases.getSearchResultsUseCase(inputSearchQuery)) {
                        is Result.Success -> {
                            _appState.value = _appState.value.copy(searchResults = result.data)
                        }

                        is Result.Error -> {
                            Timber.e(result.exception, "Error fetching search results")
                            _appState.value = _appState.value.copy(searchResults = emptyList())
                        }
                    }
                }

                else -> throw IllegalStateException("UseCases should be of type Search")
            }
        }

        suspend fun saveTokenFCM(token: String) {
            when (useCases) {
                is UseCases.TokenManagement -> useCases.saveTokenUseCase(token)
                else -> throw IllegalStateException("UseCases should be of type TokenManagement")
            }
        }

        fun formatSearchResults(
            results: List<String>,
            inputSearchQuery: String,
        ): List<Pair<String, String>> =
            when (useCases) {
                is UseCases.Search -> useCases.formatSearchResultsUseCase(results, inputSearchQuery)
                else -> throw IllegalStateException("UseCases should be of type Search")
            }

        fun addToFilter(
            key: String,
            value: String,
        ) {
            when (useCases) {
                is UseCases.Filters -> {
                    _appState.value =
                        _appState.value.copy(
                            filter = useCases.addToFilterUseCase(appState.value.filter, key, value),
                        )
                }

                else -> throw IllegalStateException("UseCases should be of type Filters")
            }
        }

        fun getFilterValue(key: String): String? =
            when (useCases) {
                is UseCases.Filters -> useCases.getFilterValueUseCase(appState.value.filter, key)
                else -> throw IllegalStateException("UseCases should be of type Filters")
            }

        fun updateFilters(newFilters: Map<String, String>) {
            Timber.d("Filter updated VIEWMODEL  DO: ${_appState.value.filter}")
            when (useCases) {
                is UseCases.Filters -> {
                    _appState.update { currentState ->
                        currentState.copy(
                            filter = useCases.updateFiltersUseCase(currentState.filter, newFilters),
                        )
                    }
                }

                else -> throw IllegalStateException("UseCases should be of type Filters")
            }
            Timber.d("Filter updated VIEWMODEL  AFTER: ${_appState.value.filter}")
        }

        fun removeFromFilter(key: String) {
            when (useCases) {
                is UseCases.Filters -> {
                    _appState.value =
                        _appState.value.copy(
                            filter = useCases.removeFromFilterUseCase(appState.value.filter, key),
                        )
                }

                else -> throw IllegalStateException("UseCases should be of type Filters")
            }
        }

        fun clearFilters() {
            when (useCases) {
                is UseCases.Filters -> {
                    _appState.value = _appState.value.copy(filter = useCases.clearFiltersUseCase())
                }

                else -> throw IllegalStateException("UseCases should be of type Filters")
            }
        }
    }
