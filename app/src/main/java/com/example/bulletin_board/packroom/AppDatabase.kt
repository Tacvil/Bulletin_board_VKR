package com.example.bulletin_board.packroom

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bulletin_board.model.Ad

@Database(entities = [Ad::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun AdDao(): AdDao
}
