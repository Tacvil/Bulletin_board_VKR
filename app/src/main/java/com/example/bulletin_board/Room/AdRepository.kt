package com.example.bulletin_board.Room

import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.DbManager
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

        override suspend fun deleteAd(
            ad: Ad,
            param: DbManager.FinishWorkListener,
        ): Result<Boolean> {
            localDataSource.deleteAd(
                ad,
                object : com.example.bulletin_board.model.DbManager {
                    override fun onFinish(isDone: Boolean) {
                        // Здесь можно обновить LiveData или использовать другой способ
                        // уведомления адаптера об удалении объявления
                    }
                },
            )
            remoteDataSource.deleteAd(
                ad,
                object : com.example.bulletin_board.model.DbManager {
                    override fun onFinish(isDone: Boolean) {
                        // Здесь можно обновить LiveData или использовать другой способ
                        // уведомления адаптера об удалении объявления
                    }
                },
            )
            return TODO("Provide the return value")
        }
    }
