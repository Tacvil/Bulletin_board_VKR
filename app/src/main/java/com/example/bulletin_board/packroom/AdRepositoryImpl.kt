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

        override suspend fun onFavClick(favData: FavData): Result<FavData> = remoteAdDataSource.onFavClick(favData)

        override suspend fun adViewed(viewData: ViewData): Result<ViewData> = remoteAdDataSource.adViewed(viewData)

        override suspend fun deleteAd(adKey: String): Result<Boolean> = remoteAdDataSource.deleteAd(adKey)

        override suspend fun insertAd(ad: Ad): Result<Boolean> = remoteAdDataSource.insertAd(ad)

        override suspend fun saveToken(token: String) {
            remoteAdDataSource.saveToken(token)
        }

        override suspend fun getMinPrice(category: String?): Result<Int> = remoteAdDataSource.getMinPrice(category)

        override suspend fun getMaxPrice(category: String?): Result<Int> = remoteAdDataSource.getMaxPrice(category)
    }
