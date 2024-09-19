package com.example.bulletin_board.di

import com.example.bulletin_board.packroom.AdRepository
import com.example.bulletin_board.packroom.AdRepositoryImpl
import com.example.bulletin_board.packroom.RemoteAdDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun provideAdRepository(remoteAdDataSource: RemoteAdDataSource): AdRepository = AdRepositoryImpl(remoteAdDataSource)
}
