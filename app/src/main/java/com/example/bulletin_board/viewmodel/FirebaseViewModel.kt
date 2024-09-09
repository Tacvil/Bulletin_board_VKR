package com.example.bulletin_board.viewmodel

import android.content.Context
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
import com.example.bulletin_board.model.DbManager
import com.example.bulletin_board.packroom.RemoteAdDataSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel
    @Inject
    constructor(
        val remoteAdDataSource: RemoteAdDataSource,
    ) : ViewModel() {
        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> = _isLoading

        val myAdsData = MutableLiveData<List<Ad>?>(emptyList())
        val favsData = MutableLiveData<List<Ad>?>(emptyList())

        fun getHomeAdsData(
            filter: MutableMap<String, String>,
            context: Context,
        ): Flow<PagingData<DocumentSnapshot>> =
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

        fun getMyAdsData(
            filter: MutableMap<String, String>,
            context: Context,
        ): Flow<PagingData<DocumentSnapshot>> =
            Pager(getPagingConfig()) {
                MyAdsPagingSource(remoteAdDataSource, filter, context) // Используем MyAdsPagingSource
            }.flow.cachedIn(viewModelScope)

        // тут фигня с FinishWorkListener
        suspend fun onFavClick(ad: Ad) {
            remoteAdDataSource.onFavClick(
                ad,
                object : DbManager.FinishWorkListener {
                    override fun onFinish(isDone: Boolean) {
                        // Здесь можно обновить LiveData или использовать другой способ
                        // уведомления адаптера об изменении состояния объявления
                        // Например, можно использовать refresh() у Pager или PagingDataAdapter
                    }
                },
            )
        }

        suspend fun adViewed(ad: Ad) {
            remoteAdDataSource.adViewed(ad)
        }

        fun loadMyAnnouncement() {
            viewModelScope.launch {
                _isLoading.postValue(true)
                try {
                    remoteAdDataSource.getMyAds(
                        object : DbManager.ReadDataCallback {
                            override fun readData(
                                list: ArrayList<Ad>,
                                lastDocument: QueryDocumentSnapshot?,
                            ) {
                                myAdsData.postValue(list)
                            }

                            override fun onComplete() {
                                _isLoading.postValue(false)
                            }

                            override fun onError(e: Exception) {
                                myAdsData.postValue(null)
                                _isLoading.postValue(false)
                            }
                        },
                    )
                } catch (e: Exception) {
                    myAdsData.postValue(null)
                    _isLoading.postValue(false)
                }
            }
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
