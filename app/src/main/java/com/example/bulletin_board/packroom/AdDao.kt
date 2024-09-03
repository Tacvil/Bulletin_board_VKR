package com.example.bulletin_board.packroom

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bulletin_board.model.Ad
import kotlinx.coroutines.flow.Flow

@Dao
interface AdDao {
    @Insert
    suspend fun insert(ad: Ad)

    @Update
    suspend fun update(ad: Ad)

    @Delete
    suspend fun delete(ad: Ad)

    @Query("SELECT * FROM ads")
    fun getAll(): Flow<List<Ad>>

    @Query("SELECT * FROM ads WHERE ads.`key` = :key")
    fun getById(key: String): Flow<Ad>

    @Query("DELETE FROM ads")
    suspend fun deleteAllAds()
}
