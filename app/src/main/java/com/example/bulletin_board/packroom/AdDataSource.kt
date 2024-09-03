package com.example.bulletin_board.packroom

import com.example.bulletin_board.model.Ad

interface AdDataSource {
    suspend fun insertAd(ad: Ad)

    suspend fun deleteAd(ad: Ad): Result<Boolean>
}
