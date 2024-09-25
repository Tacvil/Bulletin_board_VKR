package com.example.bulletin_board.packroom

import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.FavData
import com.example.bulletin_board.model.ViewData
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.ADS_LIMIT
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.text.toLong

interface AdRepository {
    suspend fun getAllAds(
        filter: MutableMap<String, String>,
        key: DocumentSnapshot? = null,
        limit: Long = ADS_LIMIT.toLong(),
    ): Pair<List<Ad>, DocumentSnapshot?>

    suspend fun getMyFavs(
        limit: Long = ADS_LIMIT.toLong(),
        startAfter: DocumentSnapshot? = null,
    ): Pair<List<Ad>, DocumentSnapshot?>

    suspend fun getMyAds(
        key: DocumentSnapshot? = null,
        limit: Long = ADS_LIMIT.toLong(),
    ): Pair<List<Ad>, DocumentSnapshot?>

    suspend fun onFavClick(favData: FavData): Result<FavData>

    suspend fun adViewed(viewData: ViewData): Result<ViewData>

    suspend fun deleteAd(adKey: String): Result<Boolean>

    suspend fun insertAd(ad: Ad): Result<Boolean>

    suspend fun saveToken(token: String)
}
