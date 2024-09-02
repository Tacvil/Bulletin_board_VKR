package com.example.bulletin_board.Room

import com.example.bulletin_board.model.Ad
import com.example.bulletin_board.model.DbManager

interface AdDataSource {
    suspend fun insertAd(ad: Ad)

    suspend fun deleteAd(
        ad: Ad,
        param: DbManager.FinishWorkListener,
    ): Result<Boolean>
}
