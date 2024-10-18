package com.example.bulletin_board.domain.repository

import android.net.Uri
import com.example.bulletin_board.data.datasource.RemoteAdDataSource.Companion.ADS_LIMIT
import com.example.bulletin_board.data.utils.Result
import com.example.bulletin_board.domain.model.Ad
import com.example.bulletin_board.domain.model.FavData
import com.example.bulletin_board.domain.model.ViewData
import com.google.firebase.firestore.DocumentSnapshot

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

    suspend fun saveToken(token: String): Result<Boolean>

    suspend fun getMinMaxPrice(category: String?): Result<Pair<Int?, Int?>>

    suspend fun fetchSearchResults(inputSearchQuery: String): Result<List<String>>

    suspend fun uploadImage(byteArray: ByteArray): Result<Uri>

    suspend fun updateImage(
        byteArray: ByteArray,
        url: String,
    ): Result<Uri>

    suspend fun deleteImageByUrl(oldUrl: String): Result<Boolean>
}
