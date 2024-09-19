package com.example.bulletin_board.packroom

import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.packroom.RemoteAdDataSource.Companion.ADS_LIMIT
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.text.toLong

interface AdRepository {
    suspend fun getAllAds(
        filter: MutableMap<String, String>,
        key: DocumentSnapshot? = null,
        limit: Long = ADS_LIMIT.toLong(),
    ): Pair<List<Ad>, DocumentSnapshot?>
}
