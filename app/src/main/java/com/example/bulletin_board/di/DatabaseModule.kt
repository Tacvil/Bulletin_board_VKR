package com.example.bulletin_board.di

import android.content.Context
import androidx.room.Room
import com.example.bulletin_board.packroom.AdDao
import com.example.bulletin_board.packroom.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "app_database",
            ).build()

    @Provides
    fun provideAdDao(database: AppDatabase): AdDao = database.AdDao()
}
