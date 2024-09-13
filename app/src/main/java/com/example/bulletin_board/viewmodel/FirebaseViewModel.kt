package com.example.bulletin_board.viewmodel

import android.content.Context
import androidx.activity.result.launch
import androidx.fragment.app.add
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
import com.example.bulletin_board.packroom.RemoteAdDataSource
import com.example.bulletin_board.packroom.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.remove
import kotlin.collections.toMutableSet

@HiltViewModel
class FirebaseViewModel
    @Inject
    constructor(
        val remoteAdDataSource: RemoteAdDataSource,
    ) : ViewModel() {
        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> = _isLoading

        private val _favoriteAds = MutableStateFlow<Set<String>>(emptySet())
        val favoriteAds: StateFlow<Set<String>> = _favoriteAds.asStateFlow()

        fun isAdFavorite(adKey: String): Boolean = favoriteAds.value.contains(adKey)

        fun getHomeAdsData(
            filter: MutableMap<String, String>,
            context: Context,
        ): Flow<PagingData<Ad>> =
            Pager(getPagingConfig()) {
                AdsPagingSource(remoteAdDataSource, filter, context)
            }.flow.cachedIn(viewModelScope)

        fun getPagingConfig(): PagingConfig = PagingConfig(pageSize = 2)

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
            viewModelScope.launch {
                val result = remoteAdDataSource.onFavClick(ad)
                if (result is Result.Success) {
                    val updatedFavorites = _favoriteAds.value.toMutableSet()
                    if (ad.isFav) {
                        updatedFavorites.remove(ad.key)
                    } else {
                        updatedFavorites.add(ad.key)
                    }
                    _favoriteAds.value = updatedFavorites
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
    }
