package com.example.bulletin_board.packroom

import com.example.bulletin_board.model.Ad
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class LocalAdDataSource
    @Inject
    constructor(
        private val adDao: AdDao,
    ) : AdDataSource {
        override suspend fun insertAd(ad: Ad) {
            TODO("Not yet implemented")
        }

        override suspend fun deleteAd(ad: Ad): Result<Boolean> {
            TODO("Not yet implemented")
        }

        suspend fun deleteAllAds() = adDao.deleteAllAds()
    }
