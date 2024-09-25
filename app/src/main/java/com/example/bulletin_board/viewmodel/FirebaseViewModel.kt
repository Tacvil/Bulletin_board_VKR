package com.example.bulletin_board.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.bulletin_board.adapterFirestore.AdsPagingSource
import com.example.bulletin_board.adapterFirestore.FavoriteAdsPagingSource
import com.example.bulletin_board.adapterFirestore.MyAdsPagingSource
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.AdUpdateEvent
import com.example.bulletin_board.model.AppState
import com.example.bulletin_board.model.FavData
import com.example.bulletin_board.model.ViewData
import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel
    @Inject
    constructor(
        private val adRepository: AdRepository,
    ) : ViewModel() {
        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> = _isLoading

        private val _appState = MutableStateFlow(AppState())
        val appState: StateFlow<AppState> = _appState.asStateFlow()

        fun getHomeAdsData(): Flow<PagingData<Ad>> =
            Pager(config = getPagingConfig()) {
                AdsPagingSource(adRepository, this)
            }.flow.cachedIn(viewModelScope)

        fun getFavoriteAdsData(): Flow<PagingData<Ad>> =
            Pager(config = getPagingConfig()) {
                FavoriteAdsPagingSource(adRepository, this)
            }.flow.cachedIn(viewModelScope)

        fun getMyAdsData(): Flow<PagingData<Ad>> =
            Pager(config = getPagingConfig()) {
                MyAdsPagingSource(adRepository, this)
            }.flow.cachedIn(viewModelScope)

        private fun getPagingConfig(): PagingConfig = PagingConfig(pageSize = 2)

        suspend fun onFavClick(favData: FavData) {
            when (val result = adRepository.onFavClick(favData)) {
                is Result.Success -> {
                    result.data.let { updatedAd ->
                        _appState.value =
                            _appState.value.copy(
                                adEvent = AdUpdateEvent.FavUpdated(updatedAd),
                            )
                    }
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Error updating favorites: ${favData.key}")
                }
            }
        }

        suspend fun adViewed(viewData: ViewData) {
            when (val result = adRepository.adViewed(viewData)) {
                is Result.Success -> {
                    result.data.let { updatedAd ->
                        _appState.value =
                            _appState.value.copy(
                                adEvent = AdUpdateEvent.ViewCountUpdated(updatedAd),
                            )
                    }
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Error updating views counter for ad: ${viewData.key}")
                }
            }
        }

        suspend fun insertAd(ad: Ad): Boolean {
            when (val result = adRepository.insertAd(ad)) {
                is Result.Success -> {
                    return true
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Error insert announcement: $ad")
                    return false
                }
            }
        }

        suspend fun deleteAd(adKey: String) {
            when (val result = adRepository.deleteAd(adKey)) {
                is Result.Success -> {
                    _appState.value =
                        _appState.value.copy(
                            adEvent = AdUpdateEvent.AdDeleted,
                        )
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Error deleting ad: $adKey")
                }
            }
        }

        suspend fun saveTokenDB(token: String) {
            adRepository.saveToken(token)
        }

        fun addToFilter(
            key: String,
            value: String,
        ) {
            _appState.value =
                _appState.value.copy(
                    filter =
                        _appState.value.filter
                            .toMutableMap()
                            .also { it[key] = value },
                )
        }

        fun getFilterValue(key: String): String? = appState.value.filter[key]

        fun updateFilters(newFilters: Map<String, String>) {
            _appState.value =
                _appState.value.copy(
                    filter =
                        _appState.value.filter
                            .toMutableMap()
                            .apply { putAll(newFilters) },
                )
        }

        fun removeFromFilter(key: String) {
            _appState.value =
                _appState.value.copy(
                    filter =
                        _appState.value.filter
                            .toMutableMap()
                            .also { it.remove(key) },
                )
        }

        fun clearFilters() {
            _appState.value = _appState.value.copy(filter = mutableMapOf())
        }
    }
