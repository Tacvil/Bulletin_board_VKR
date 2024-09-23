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
import com.example.bulletin_board.model.FavData
import com.example.bulletin_board.model.ViewData
import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.RemoteAdDataSource
import com.example.bulletin_board.packroom.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel
    @Inject
    constructor(
        private val remoteAdDataSource: RemoteAdDataSource,
        private val adRepository: AdRepository,
    ) : ViewModel() {
        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> = _isLoading

        private val _adUpdated = MutableSharedFlow<AdUpdateEvent>()
        val adUpdated = _adUpdated.asSharedFlow()

        private val _filter = MutableStateFlow<MutableMap<String, String>>(mutableMapOf())
        val filter: StateFlow<MutableMap<String, String>> = _filter.asStateFlow()

        fun getHomeAdsData(): Flow<PagingData<Ad>> =
            Pager(config = getPagingConfig()) {
                AdsPagingSource(adRepository, this)
            }.flow.cachedIn(viewModelScope)

        private fun getPagingConfig(): PagingConfig = PagingConfig(pageSize = 2)

        fun getFavoriteAdsData(): Flow<PagingData<Ad>> =
            Pager(getPagingConfig()) {
                FavoriteAdsPagingSource(remoteAdDataSource, this)
            }.flow.cachedIn(viewModelScope)

        fun getMyAdsData(): Flow<PagingData<Ad>> =
            Pager(getPagingConfig()) {
                MyAdsPagingSource(remoteAdDataSource)
            }.flow.cachedIn(viewModelScope)

        suspend fun onFavClick(favData: FavData) {
            when (val result = remoteAdDataSource.onFavClick(favData)) {
                is Result.Success -> {
                    result.data.let { updatedAd ->
                        _adUpdated.emit(AdUpdateEvent.FavUpdated(updatedAd))
                    }
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Error updating favorites: ${favData.key}")
                }
            }
        }

        suspend fun adViewed(viewData: ViewData) {
            when (val result = remoteAdDataSource.adViewed(viewData)) {
                is Result.Success -> {
                    result.data.let { updatedAd ->
                        _adUpdated.emit(AdUpdateEvent.ViewCountUpdated(updatedAd))
                    }
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Error updating views counter for ad: ${viewData.key}")
                }
            }
        }

        suspend fun deleteAd(adKey: String) {
            when (val result = remoteAdDataSource.deleteAd(adKey)) {
                is Result.Success -> {
                    _adUpdated.emit(AdUpdateEvent.AdDeleted)
                }

                is Result.Error -> {
                    Timber.e(result.exception, "Error deleting ad: $adKey")
                }
            }
        }

        fun saveTokenDB(token: String) {
            remoteAdDataSource.saveToken(token)
        }

        fun addToFilter(
            key: String,
            value: String,
        ) {
            _filter.value = _filter.value.toMutableMap().also { it[key] = value }
        }

        fun getFilterValue(key: String): String? = filter.value[key]

        fun updateFilters(newFilters: Map<String, String>) {
            _filter.value =
                _filter.value.toMutableMap().also { currentFilters ->
                    currentFilters.putAll(newFilters)
                }
        }

        fun removeFromFilter(key: String) {
            _filter.value = _filter.value.toMutableMap().also { it.remove(key) }
        }

        fun clearFilters() {
            _filter.value = mutableMapOf()
        }
    }
