package com.example.bulletin_board.Room

import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.DbManager
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class LocalAdDataSource
    @Inject
    constructor(
        private val adDao: AdDao,
    ) : AdDataSource {
        override fun getAllAds(): Flow<List<Ad>> = adDao.getAll()

        override fun getAdById(id: String): Flow<Ad> = adDao.getById(id)

        override suspend fun insertAd(ad: Ad) = adDao.insert(ad)

        override suspend fun updateAd(ad: Ad) = adDao.update(ad)

        override suspend fun deleteAd(
            ad: Ad,
            param: DbManager.FinishWorkListener,
        ) = adDao.delete(ad)

        suspend fun deleteAllAds() = adDao.deleteAllAds()
    }
