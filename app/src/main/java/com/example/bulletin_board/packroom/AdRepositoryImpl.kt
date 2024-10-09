package com.example.bulletin_board.packroom

import android.net.Uri
import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.FavData
import com.example.bulletin_board.model.ViewData
import com.google.firebase.firestore.DocumentSnapshot
import jakarta.inject.Inject

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
            val (ads, nextKey) = remoteAdDataSource.getAllAds(filter, key, limit)
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
            val (ads, nextKey) = remoteAdDataSource.getMyAds(key, limit)
            return ads to nextKey
        }

        override suspend fun onFavClick(favData: FavData): Result<FavData> = remoteAdDataSource.onFavClick(favData)

        override suspend fun adViewed(viewData: ViewData): Result<ViewData> = remoteAdDataSource.adViewed(viewData)

        override suspend fun deleteAd(adKey: String): Result<Boolean> = remoteAdDataSource.deleteAd(adKey)

        override suspend fun insertAd(ad: Ad): Result<Boolean> = remoteAdDataSource.insertAd(ad)

        override suspend fun saveToken(token: String) {
            remoteAdDataSource.saveToken(token)
        }

        override suspend fun getMinMaxPrice(category: String?): Result<Pair<Int?, Int?>> = remoteAdDataSource.getMinMaxPrice(category)

        override suspend fun fetchSearchResults(inputSearchQuery: String): Result<List<String>> =
            remoteAdDataSource.fetchSearchResults(inputSearchQuery)

        override suspend fun uploadImage(byteArray: ByteArray): Result<Uri> = remoteAdDataSource.uploadImage(byteArray)

        override suspend fun updateImage(
            byteArray: ByteArray,
            url: String,
        ): Result<Uri> = remoteAdDataSource.updateImage(byteArray, url)

        override suspend fun deleteImageByUrl(oldUrl: String): Result<Boolean> = remoteAdDataSource.deleteImageByUrl(oldUrl)
    }
