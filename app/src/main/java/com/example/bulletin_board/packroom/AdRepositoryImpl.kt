package com.example.bulletin_board.packroom

import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.FavData
import com.example.bulletin_board.model.ViewData
import com.google.firebase.firestore.DocumentSnapshot
import jakarta.inject.Inject
import timber.log.Timber

class AdRepositoryImpl
    @Inject
    constructor(
        private val remoteAdDataSource: RemoteAdDataSource,
    ) : AdRepository {
        override suspend fun getAllAds(
            filter: MutableMap<String, String>,
            key: DocumentSnapshot?,
            limit: Long,
        ): Pair<List<Ad>, DocumentSnapshot?> {
            Timber.d("AdRepositoryImpl.getAllAds: key = $key") // Логируем key
            val (ads, nextKey) = remoteAdDataSource.getAllAds(filter, key, limit)
            Timber.d("AdRepositoryImpl.getAllAds: nextKey = $nextKey") // Логируем nextKey
            return ads to nextKey
        }

        override suspend fun getMyFavs(
            limit: Long,
            startAfter: DocumentSnapshot?,
        ): Pair<List<Ad>, DocumentSnapshot?> {
            val (ads, nextKey) = remoteAdDataSource.getMyFavs(limit, startAfter)
            return ads to nextKey
        }

        override suspend fun getMyAds(
            key: DocumentSnapshot?,
            limit: Long,
        ): Pair<List<Ad>, DocumentSnapshot?> {
            val(ads, nextKey) = remoteAdDataSource.getMyAds(key, limit)
            return ads to nextKey
        }

        override suspend fun onFavClick(favData: FavData): Result<FavData> {
            val result = remoteAdDataSource.onFavClick(favData)
            return result
        }

        override suspend fun adViewed(viewData: ViewData): Result<ViewData> {
            val result = remoteAdDataSource.adViewed(viewData)
            return result
        }

        override suspend fun deleteAd(adKey: String): Result<Boolean> {
            val result = remoteAdDataSource.deleteAd(adKey)
            return result
        }

        override suspend fun insertAd(ad: Ad): Result<Boolean> {
            val result = remoteAdDataSource.insertAd(ad)
            return result
        }

        override suspend fun saveToken(token: String) {
            remoteAdDataSource.saveToken(token)
        }
    }
