package com.example.bulletin_board.packroom

import com.example.bulletin_board.model.Ad
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
    }
