package com.example.bulletin_board.packroom

import com.example.bulletin_board.model.Ad
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AdRepository
    @Inject
    constructor(
        private val localDataSource: LocalAdDataSource,
        private val remoteDataSource: RemoteAdDataSource,
    ) : AdDataSource {
        override suspend fun insertAd(ad: Ad) {
            localDataSource.insertAd(ad)
            remoteDataSource.insertAd(ad)
        }

        override suspend fun deleteAd(ad: Ad): Result<Boolean> {
            TODO("Not yet implemented")
        }
    }
