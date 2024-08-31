package com.example.bulletin_board.Room

import com.example.bulletin_board.model.Ad
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber

@Singleton
class AdRepository
    @Inject
    constructor(
        private val localDataSource: LocalAdDataSource,
        private val remoteDataSource: RemoteAdDataSource,
    ) : AdDataSource {
        override fun getAllAds(): Flow<List<Ad>> =
            flow {
                emit(localDataSource.getAllAds().first())

                try {
                    val remoteAds = remoteDataSource.getAllAds().first()
                    localDataSource.deleteAllAds() // Очищаем старые данные перед вставкой новых
                    remoteAds.forEach { localDataSource.insertAd(it) }
                    emit(localDataSource.getAllAds().first())
                } catch (e: Exception) {
                    // Обработка ошибок, например, логирование
                    Timber.e(e, "Error fetching ads from Firestore")
                }
            }

        override fun getAdById(id: String): Flow<Ad> = localDataSource.getAdById(id)

        override suspend fun insertAd(ad: Ad) {
            localDataSource.insertAd(ad)
            remoteDataSource.insertAd(ad)
        }

        override suspend fun updateAd(ad: Ad) {
            localDataSource.updateAd(ad)
            remoteDataSource.updateAd(ad)
        }

        override suspend fun deleteAd(ad: Ad) {
            localDataSource.deleteAd(ad)
            remoteDataSource.deleteAd(ad)
        }
    }
