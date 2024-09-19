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

        private val _favoriteAds = MutableStateFlow<Set<String>>(emptySet())
        val favoriteAds: StateFlow<Set<String>> = _favoriteAds.asStateFlow()

        private val _favoriteAdChanged = MutableSharedFlow<Ad>()
        val favoriteAdChanged = _favoriteAdChanged.asSharedFlow()

        private val _adUpdated = MutableSharedFlow<Ad>()
        val adUpdated = _adUpdated.asSharedFlow()

        private val _filter = MutableStateFlow<MutableMap<String, String>>(mutableMapOf())
        val filter: StateFlow<MutableMap<String, String>> = _filter.asStateFlow()

        private val currentFilter = mutableMapOf<String, String>()

        fun getHomeAdsData(): Flow<PagingData<Ad>> =
            Pager(config = getPagingConfig()) {
                AdsPagingSource(adRepository, this)
            }.flow.cachedIn(viewModelScope)

        private fun getPagingConfig(): PagingConfig = PagingConfig(pageSize = 2)

        fun getFavoriteAdsData(): Flow<PagingData<Ad>> =
            Pager(getPagingConfig()) {
                FavoriteAdsPagingSource(
                    remoteAdDataSource,
                ) // Используем FavoriteAdsPagingSource
            }.flow.cachedIn(viewModelScope)

        fun getMyAdsData(): Flow<PagingData<Ad>> =
            Pager(getPagingConfig()) {
                MyAdsPagingSource(remoteAdDataSource) // Используем MyAdsPagingSource
            }.flow.cachedIn(viewModelScope)

        suspend fun onFavClick(ad: Ad) {
            val result = remoteAdDataSource.onFavClick(ad)
            if (result is Result.Success) {
                result.data?.let { updatedAd ->
                    _adUpdated.emit(updatedAd)
                }
            }
        }

        suspend fun adViewed(ad: Ad) {
            remoteAdDataSource.adViewed(ad)
        }

        suspend fun deleteItem(ad: Ad) {
            remoteAdDataSource.deleteAd(
                ad,
            )
        }

        fun saveTokenDB(token: String) {
            remoteAdDataSource.saveToken(token)
        }

        fun addToFilter(
            key: String,
            value: String,
        ) {
            currentFilter[key] = value
        }

        fun getFilterValue(key: String): String? = currentFilter[key]

        fun updateFilter() {
            _filter.value =
                currentFilter.toMutableMap() // Создаем копию, чтобы избежать изменений currentFilter извне
        }
    }
