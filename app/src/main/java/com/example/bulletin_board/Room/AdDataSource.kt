package com.example.bulletin_board.Room

import kotlinx.coroutines.flow.Flow

interface AdDataSource {
    fun getAllAds(): Flow<List<Ad>>

    fun getAdById(id: String): Flow<Ad>

    suspend fun insertAd(ad: Ad)

    suspend fun updateAd(ad: Ad)

    suspend fun deleteAd(ad: Ad)
}
