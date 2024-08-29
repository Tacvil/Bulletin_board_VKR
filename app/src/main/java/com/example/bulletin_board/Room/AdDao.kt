package com.example.bulletin_board.Room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AdDao {
    @Insert
    suspend fun insert(ad: Ad)

    @Update
    suspend fun update(ad: Ad)

    @Delete
    suspend fun delete(ad: Ad)

    @Query("SELECT * FROM Ad") //  Имя таблицы должно соответствовать имени класса Ad
    fun getAll(): Flow<List<Ad>>

    @Query("SELECT * FROM Ad WHERE key = :key") //  Имя таблицы должно соответствовать имени класса Ad
    fun getById(key: String): Flow<Ad>

    @Query("DELETE FROM Ad")
    suspend fun deleteAllAds()
}
